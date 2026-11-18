package com.hollowvigil;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
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
 * O Oco: fica parado te encarando e só anda quando você não olha. Olhar para ele corrói a sanidade
 * (o diretor faz a conta). Encarado por tempo demais, some - ou surge colado na sua cara.
 * Benevolente (jogador honrado): entrega um presente e vai embora.
 * Só um ASSASSINO consegue feri-lo - e, contra ele, O Oco nunca some: persegue e ataca até morrer.
 */
public class HollowEntity extends Monster {
    private static final int STARE_TICKS_TO_VANISH = 50;
    private static final int MAX_LIFETIME = 2400;
    private static final int MAX_LIFETIME_HUNTING = 12000;

    private int stareTicks = 0;
    private int scareTicks = 0;
    private int attackCooldown = 0;
    private boolean benevolent = false;

    public HollowEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.27D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.FOLLOW_RANGE, 64.0D);
    }

    public void setBenevolent(boolean value) { this.benevolent = value; }

    public boolean isBenevolent() { return benevolent; }

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

        Player player = this.level().getNearestPlayer(this, 64.0D);
        boolean hunted = player != null && PlayerStats.isKiller(player);   // alvo é um assassino
        if (this.tickCount > (hunted ? MAX_LIFETIME_HUNTING : MAX_LIFETIME)) {
            vanish();
            return;
        }
        if (player == null || player.isSpectator()) return;
        if (PlayerStats.isAngel(player)) {   // anjos nunca são vistos
            vanish();
            return;
        }

        if (benevolent) {
            this.getNavigation().stop();
            if (this.tickCount > 100 && player instanceof ServerPlayer sp) {
                Gifts.give(sp);
                gentleVanish();
            }
            return;
        }

        if (hunted) {   // contra o assassino: persegue e ataca, nunca recua
            if (this.tickCount % 10 == 0) this.getNavigation().moveTo(player, 1.15D);
            if (attackCooldown > 0) attackCooldown--;
            if (attackCooldown <= 0 && this.distanceToSqr(player) < 6.5D) {
                attackCooldown = 25;
                this.swing(InteractionHand.MAIN_HAND);
                this.doHurtTarget(player);
            }
            return;
        }

        if (LookUtil.isLookingAt(player, this)) {
            this.getNavigation().stop();
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

        Sfx.play(sp, ModSounds.JUMPSCARE.get(), 1.8F, 1.0F, getX(), getY() + 1.6D, getZ());
        Sfx.play(sp, ModSounds.STINGER.get(), 1.4F, 0.9F, getX(), getY() + 1.0D, getZ());
        HorrorNet.flash(sp, 12);
        HorrorNet.shake(sp, 25, 4.0F);
        PlayerStats.fear(sp, MobEffects.DARKNESS, 80);
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
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return super.hurt(source, amount);
        if (source.getEntity() instanceof Player p && PlayerStats.isKiller(p)) return super.hurt(source, amount);   // não some ao ser atacado
        if (benevolent) gentleVanish(); else vanish();
        return false;
    }

    private boolean nearKiller() {
        Player near = this.level().getNearestPlayer(this, 48.0D);
        return near != null && PlayerStats.isKiller(near);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return (benevolent || nearKiller()) ? null : ModSounds.HOLLOW_IDLE.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 240;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource src) {
        return nearKiller() ? SoundEvents.ENDERMAN_HURT : ModSounds.STATIC_BURST.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return nearKiller() ? SoundEvents.ENDERMAN_DEATH : ModSounds.SCREAM_FAR.get();
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }
}
