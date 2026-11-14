package com.hollowvigil;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.UUID;

/**
 * Alucinação. Modo silhueta (sanidade baixa): figura parada à distância que some quando olhada.
 * Modo hostil (sanidade ZERO): REAL - persegue e fere a vítima. Some quando a sanidade volta a 25.
 */
public class HallucinationEntity extends Monster {
    private UUID victim;
    private boolean hostile;
    private int lifetime = 600;
    private int stareTicks = 0;

    public HallucinationEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        this.xpReward = 0;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 12.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.36D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.FOLLOW_RANGE, 40.0D);
    }

    public void setup(UUID victim, boolean hostile, int lifetimeTicks) {
        this.victim = victim;
        this.hostile = hostile;
        this.lifetime = lifetimeTicks;
    }

    public boolean isHostile() {
        return hostile;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2D, true));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 40.0F));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide) return;

        Player p = victim == null ? null : this.level().getPlayerByUUID(victim);
        if (p == null || --lifetime <= 0) {
            fade();
            return;
        }

        if (hostile) {
            this.setTarget(p);
            if (PlayerStats.getSanity(p) >= 25.0F) fade();
        } else {
            this.getNavigation().stop();
            if (LookUtil.isLookingAt(p, this)) {
                if (++stareTicks >= 25) fade();
            } else if (this.distanceTo(p) < 7.0D) {
                if (p instanceof ServerPlayer sp) {
                    PlayerStats.addSanity(sp, -3.0F);
                    Sfx.behind(sp, "entity.enderman.stare", 0.8F, 0.5F, 3.0D);
                    HorrorNet.shake(sp, 10, 1.0F);
                }
                fade();
            }
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof ServerPlayer sp) {
            PlayerStats.addSanity(sp, -3.0F);
            HorrorNet.shake(sp, 10, 2.0F);
        }
        return hit;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!hostile && !source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            fade();
            return false;
        }
        return super.hurt(source, amount);
    }

    public void fade() {
        if (this.level() instanceof ServerLevel s) {
            s.sendParticles(ParticleTypes.SOUL, getX(), getY() + 1.0D, getZ(), 18, 0.3D, 0.8D, 0.3D, 0.02D);
        }
        this.discard();
    }

    @Override
    protected boolean shouldDropLoot() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }
}
