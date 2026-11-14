package com.hollowvigil;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * O Oco: fica parado te encarando e só anda quando você não olha. Encará-lo tira sanidade.
 * Se for encarado por tempo demais, some - ou aparece colado na sua cara.
 * Modo benevolente (jogadores honrados): entrega um presente e vai embora.
 */
public class HollowEntity extends Monster {
    private static final int STARE_TICKS_TO_VANISH = 50;
    private static final int MAX_LIFETIME = 2400;

    private int stareTicks = 0;
    private int scareTicks = 0;
    private boolean benevolent = false;

    public HollowEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FOLLOW_RANGE, 64.0D);
    }

    public void setBenevolent(boolean value) {
        this.benevolent = value;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 64.0F));
        this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide) return;

        if (scareTicks > 0) {
            if (--scareTicks == 0) vanish();
            return;
        }
        if (this.tickCount > MAX_LIFETIME) {
            vanish();
            return;
        }

        Player player = this.level().getNearestPlayer(this, 64.0D);
        if (player == null || player.isSpectator()) return;

        if (benevolent) {
            this.getNavigation().stop();
            if (this.tickCount > 100 && player instanceof ServerPlayer sp) {
                Gifts.give(sp);
                gentleVanish();
            }
            return;
        }

        if (LookUtil.isLookingAt(player, this)) {
            this.getNavigation().stop();
            PlayerStats.addSanity(player, -0.06F);
            if (++stareTicks >= STARE_TICKS_TO_VANISH) onStaredAt(player);
        } else {
            if (stareTicks > 0) stareTicks--;
            creep(player);
        }
    }

    private void creep(Player player) {
        double d = this.distanceTo(player);
        if (d > 8.0D) {
            if (this.tickCount % 40 == 0 && this.random.nextInt(3) == 0) {
                this.getNavigation().moveTo(player, 0.9D);
            }
        } else {
            this.getNavigation().stop();
        }
    }

    private void onStaredAt(Player player) {
        if (player instanceof ServerPlayer sp && HollowConfig.JUMPSCARES.get()
                && this.random.nextDouble() < HollowConfig.JUMPSCARE_CHANCE.get()) {
            jumpscare(sp);
        } else {
            vanish();
        }
    }

    private void jumpscare(ServerPlayer sp) {
        Vec3 look = sp.getLookAngle();
        Vec3 flat = new Vec3(look.x, 0, look.z);
        flat = flat.lengthSqr() < 1.0E-4 ? new Vec3(0, 0, 1) : flat.normalize();
        Vec3 dest = sp.position().add(flat.scale(1.7D));

        this.getNavigation().stop();
        this.moveTo(dest.x, sp.getY(), dest.z, 0.0F, 0.0F);
        this.lookAt(EntityAnchorArgument.Anchor.EYES, sp.getEyePosition());
        this.yHeadRot = this.getYRot();
        this.yBodyRot = this.getYRot();
        this.scareTicks = 14;
        this.stareTicks = 0;

        PlayerStats.addSanity(sp, -8.0F);
        Sfx.play(sp, "entity.enderman.scream", 1.7F, 0.5F, getX(), getY() + 1.6D, getZ());
        Sfx.play(sp, "entity.ghast.scream", 1.2F, 0.4F, getX(), getY() + 1.6D, getZ());
        HorrorNet.flash(sp, 12);
        HorrorNet.shake(sp, 25, 4.0F);
        sp.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 80, 0, false, false));
    }

    public void vanish() {
        if (this.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 1.0D, getZ(), 25, 0.3D, 0.8D, 0.3D, 0.02D);
            server.playSound(null, blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0F, 0.5F);
        }
        this.discard();
    }

    private void gentleVanish() {
        if (this.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 1.0D, getZ(), 30, 0.3D, 0.9D, 0.3D, 0.02D);
        }
        this.discard();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return super.hurt(source, amount);
        }
        if (benevolent) gentleVanish(); else vanish();
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }
}
