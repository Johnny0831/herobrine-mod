package com.hollowvigil;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.player.Player;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Comportamento de um animal possuído. Modo 0 (observar): congela quando você olha e se aproxima quando não olha.
 * Modo 1 (investida): corre e ataca, tirando vida e sanidade. Jogadores honrados nunca são feridos.
 */
public class PossessedGoal extends Goal {
    private static Field goalField;
    private static final Set<Mob> ATTACHED = Collections.newSetFromMap(new WeakHashMap<>());

    private final Mob mob;
    private int attackCooldown = 0;
    private int soundCooldown = 40;

    public PossessedGoal(Mob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    /** Anexa o comportamento (uma vez por mob, por sessão) via reflexão no seletor de goals. */
    public static void attach(Mob mob) {
        if (!ATTACHED.add(mob)) return;
        try {
            if (goalField == null) {
                Field fallback = null;
                for (Field f : Mob.class.getDeclaredFields()) {
                    if (f.getType() != GoalSelector.class) continue;
                    if (fallback == null) fallback = f;
                    if (f.getName().equals("goalSelector") || f.getName().equals("f_21345_")) {
                        fallback = f;
                        break;
                    }
                }
                if (fallback != null) {
                    fallback.setAccessible(true);
                    goalField = fallback;
                }
            }
            if (goalField != null) {
                ((GoalSelector) goalField.get(mob)).addGoal(0, new PossessedGoal(mob));
            }
        } catch (Exception ex) {
            HollowVigilMod.LOGGER.error("Falha ao anexar goal de possessão", ex);
        }
    }

    private Player target() {
        return mob.level().getNearestPlayer(mob, 40.0D);
    }

    @Override
    public boolean canUse() {
        return mob.hasEffect(ModEffects.POSSESSED.get()) && target() != null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void stop() {
        mob.getNavigation().stop();
    }

    @Override
    public void tick() {
        Player p = target();
        if (p == null) return;
        int mode = mob.getPersistentData().getInt("hv_mode");
        boolean honored = PlayerStats.isHonored(p) || PlayerStats.isKiller(p);
        mob.getLookControl().setLookAt(p, 30.0F, 30.0F);
        double d2 = mob.distanceToSqr(p);

        if (mode == 1 && !honored) {
            if (mob.tickCount % 8 == 0) mob.getNavigation().moveTo(p, 1.5D);
            if (attackCooldown > 0) attackCooldown--;
            if (d2 < 4.8D && attackCooldown <= 0) {
                attackCooldown = 20;
                float dmg = PlayerStats.isSinner(p) ? 6.0F : 3.0F;
                p.hurt(mob.level().damageSources().mobAttack(mob), dmg);
                if (p instanceof ServerPlayer sp) HorrorNet.shake(sp, 10, 2.0F);
            }
        } else {
            if (LookUtil.isLookingAt(p, mob)) mob.getNavigation().stop();
            else if (d2 > 16.0D && mob.tickCount % 10 == 0) mob.getNavigation().moveTo(p, 1.1D);
        }

        if (--soundCooldown <= 0 && p instanceof ServerPlayer sp) {
            soundCooldown = 70 + mob.getRandom().nextInt(60);
            ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
            Sfx.play(sp, key.getNamespace() + ":entity." + key.getPath() + ".ambient", 1.3F, 0.45F,
                    mob.getX(), mob.getY(), mob.getZ());
        }
    }
}
