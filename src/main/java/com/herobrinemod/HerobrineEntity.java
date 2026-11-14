package com.herobrinemod;

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
 * O Espreitador: fica parado te encarando. Anda devagar SÓ quando você não está olhando.
 * Se você o encara por tempo demais, ele some - ou aparece colado na sua cara.
 */
public class HerobrineEntity extends Monster {
    private static final int STARE_TICKS_TO_VANISH = 50;
    private static final int MAX_LIFETIME = 2400;

    private int stareTicks = 0;
    private int scareTicks = 0;

    public HerobrineEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FOLLOW_RANGE, 64.0D);
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

        if (isPlayerLookingAtMe(player)) {
            this.getNavigation().stop();
            if (++stareTicks >= STARE_TICKS_TO_VANISH) onStaredAt(player);
        } else {
            if (stareTicks > 0) stareTicks--;
            creep(player);
        }
    }

    /** Aproxima-se lentamente enquanto ninguém olha. */
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

    private boolean isPlayerLookingAtMe(Player player) {
        Vec3 view = player.getViewVector(1.0F).normalize();
        Vec3 toMe = new Vec3(this.getX() - player.getX(),
                this.getEyeY() - player.getEyeY(),
                this.getZ() - player.getZ()).normalize();
        return view.dot(toMe) > 0.96D && player.hasLineOfSight(this);
    }

    private void onStaredAt(Player player) {
        if (player instanceof ServerPlayer sp && HerobrineConfig.JUMPSCARES.get()
                && this.random.nextDouble() < HerobrineConfig.JUMPSCARE_CHANCE.get()) {
            jumpscare(sp);
        } else {
            vanish();
        }
    }

    /** Teleporta para ~1,7 bloco na frente do jogador, com grito, flash e tremor. */
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

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return super.hurt(source, amount);
        }
        vanish();
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }
}
