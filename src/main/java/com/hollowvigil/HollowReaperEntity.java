package com.hollowvigil;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Ceifador Oco: a forma agressiva. Teleporta para as suas costas e escurece a visão.
 * Contra pecadores (honra <= 25) mata com UM golpe (exceto assassinos).
 * Só um ASSASSINO consegue feri-lo; os demais apenas o veem se teleportar atrás deles.
 */
public class HollowReaperEntity extends Monster {
    private int blinkCooldown = 100;
    private boolean enraged = false;

    public HollowReaperEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        this.xpReward = 50;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 120.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.34D)
                .add(Attributes.ATTACK_DAMAGE, 9.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 1.0D)
                .add(Attributes.ARMOR, 6.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6D)
                .add(Attributes.FOLLOW_RANGE, 48.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.15D, false));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 48.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide) return;

        LivingEntity tNow = this.getTarget();
        boolean hunted = tNow instanceof Player kp && PlayerStats.isKiller(kp);   // alvo é um assassino
        if (this.tickCount > (hunted ? 12000 : HollowConfig.REAPER_LIFETIME_SECONDS.get() * 20)) {
            vanish();
            return;
        }

        LivingEntity target = this.getTarget();
        if (target instanceof Player tp && PlayerStats.isAngel(tp)) {   // anjos nunca são caçados
            vanish();
            return;
        }

        if (!enraged && this.getHealth() < this.getMaxHealth() * 0.5F) {
            enraged = true;
            this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.44D);
            for (ServerPlayer sp : nearbyPlayers(32.0D)) {
                Sfx.play(sp, ModSounds.REAPER_ROAR.get(), 1.6F, 0.8F, getX(), getY() + 2.0D, getZ());
                HorrorNet.shake(sp, 30, 2.5F);
            }
        }

        if (target instanceof ServerPlayer sp) {
            double dist = this.distanceTo(sp);
            if (this.tickCount % 20 == 0) {
                float vol = (float) Math.max(0.3D, 1.4D - dist / 30.0D);
                boolean close = dist < 14.0D;
                Sfx.atPlayer(sp, close ? ModSounds.HEARTBEAT_FAST.get() : ModSounds.HEARTBEAT.get(), vol, 1.0F);
                if (dist < 24.0D && sp.hasLineOfSight(this)) PlayerStats.fear(sp, MobEffects.DARKNESS, 60);
            }
            if (this.random.nextInt(200) == 0) Sfx.behind(sp, ModSounds.WHISPER.get(), 1.0F, 0.8F, 3.0D);
            if (--blinkCooldown <= 0 && dist > (hunted ? 14.0D : 7.0D)) {
                blinkCooldown = (enraged ? 45 : 85) + this.random.nextInt(70);
                if (blinkBehind(sp)) {
                    Sfx.play(sp, "minecraft:entity.enderman.teleport", 1.0F, 0.4F, getX(), getY() + 1.5D, getZ());
                    Sfx.play(sp, ModSounds.STINGER.get(), 0.9F, 1.1F, getX(), getY() + 1.5D, getZ());
                    HorrorNet.shake(sp, 12, 1.5F);
                }
            }
        }
    }

    private List<ServerPlayer> nearbyPlayers(double r) {
        return this.level().getEntitiesOfClass(ServerPlayer.class, this.getBoundingBox().inflate(r));
    }

    private boolean blinkBehind(LivingEntity t) {
        Vec3 look = t.getLookAngle();
        Vec3 flat = new Vec3(look.x, 0, look.z);
        flat = flat.lengthSqr() < 1.0E-4 ? new Vec3(0, 0, 1) : flat.normalize();
        for (int i = 0; i < 6; i++) {
            Vec3 d = t.position().subtract(flat.scale(2.5D + i * 0.4D));
            AABB box = this.getBoundingBox().move(d.x - getX(), t.getY() - getY(), d.z - getZ());
            if (this.level().noCollision(this, box)) {
                poof();
                this.moveTo(d.x, t.getY(), d.z, t.getYRot() + 180.0F, 0.0F);
                this.getNavigation().stop();
                poof();
                return true;
            }
        }
        return false;
    }

    private void poof() {
        if (this.level() instanceof ServerLevel s) {
            s.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 1.5D, getZ(), 30, 0.4D, 1.2D, 0.4D, 0.02D);
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (target instanceof Player pl && PlayerStats.isSinner(pl) && !PlayerStats.isKiller(pl)
                && HollowConfig.SINNER_ONE_HIT.get()) {
            if (pl instanceof ServerPlayer sp) {
                ChatLines.hollowSays(sp, "verdict");
                Sfx.atPlayer(sp, ModSounds.TOLL.get(), 1.4F, 0.6F);
                HorrorNet.flash(sp, 8);
            }
            this.swing(InteractionHand.MAIN_HAND);
            return target.hurt(this.level().damageSources().mobAttack(this), 1000.0F);
        }
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof ServerPlayer sp) {
            PlayerStats.fear(sp, MobEffects.DARKNESS, 100);
            PlayerStats.fear(sp, MobEffects.CONFUSION, 120);
            PlayerStats.fear(sp, MobEffects.MOVEMENT_SLOWDOWN, 40);
            Sfx.play(sp, ModSounds.JUMPSCARE.get(), 0.9F, 0.8F, getX(), getY() + 2.0D, getZ());
            HorrorNet.shake(sp, 15, 3.0F);
            HorrorNet.flash(sp, 6);
        }
        return hit;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        Entity attacker = source.getEntity();
        boolean allowed = source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)
                || (attacker instanceof Player p && PlayerStats.isKiller(p));
        if (!allowed) {
            if (!this.level().isClientSide && attacker instanceof LivingEntity le && this.random.nextFloat() < 0.35F) {
                blinkBehind(le);
            }
            return false;
        }
        boolean result = super.hurt(source, amount);
        boolean byKiller = attacker instanceof Player pk && PlayerStats.isKiller(pk);
        if (result && !byKiller && !this.level().isClientSide && attacker instanceof LivingEntity le && this.random.nextFloat() < 0.2F) {
            blinkBehind(le);   // contra o assassino ele NUNCA foge: o poder do modo é poder bater nele
        }
        return result;
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (!this.level().isClientSide) {
            for (ServerPlayer sp : nearbyPlayers(40.0D)) {
                Sfx.play(sp, ModSounds.SCREAM_FAR.get(), 1.5F, 0.6F, getX(), getY() + 2.0D, getZ());
                HorrorNet.shake(sp, 40, 3.0F);
                HorrorNet.flash(sp, 10);
            }
        }
    }

    public void vanish() {
        poof();
        this.discard();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        Player near = this.level().getNearestPlayer(this, 48.0D);
        return (near != null && PlayerStats.isKiller(near)) ? null : ModSounds.REAPER_ROAR.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 220;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource src) {
        return ModSounds.STATIC_BURST.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.SCREAM_FAR.get();
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }
}
