package com.hollowvigil;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.AnimalTameEvent;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Pecados reduzem a honra; boas ações aumentam (alimentar animais, reproduzir, domar, defender). */
public class HonorEvents {

    @SubscribeEvent
    public void onDeath(LivingDeathEvent e) {
        LivingEntity victim = e.getEntity();
        if (victim.level().isClientSide) return;
        if (!(e.getSource().getEntity() instanceof ServerPlayer sp)) return;

        // caça do assassino
        int points = victim instanceof HollowReaperEntity ? 4 : victim instanceof HollowEntity ? 2 : victim instanceof HallucinationEntity ? 1 : 0;
        if (points > 0) {
            if (PlayerStats.isKiller(sp)) Modes.onEntityKilled(sp, points);
            return;
        }
        if (victim.hasEffect(ModEffects.POSSESSED.get())) {   // libertar um animal possuído não é pecado
            PlayerStats.addHonor(sp, 0.5F);
            PlayerStats.addSanity(sp, 2.0F);
            return;
        }
        if (victim instanceof IronGolem g && !g.isPlayerCreated()) {
            PlayerStats.addHonor(sp, -15.0F);
        } else if (victim instanceof WanderingTrader) {
            PlayerStats.addVillagersKilled(sp);
            PlayerStats.addHonor(sp, -10.0F);
        } else if (victim instanceof AbstractVillager) {
            PlayerStats.addVillagersKilled(sp);
            PlayerStats.addHonor(sp, -20.0F);
        } else if (victim instanceof TamableAnimal t && t.isTame()) {
            PlayerStats.addAnimalsKilled(sp);
            PlayerStats.addHonor(sp, -12.0F);
        } else if (victim instanceof Animal) {
            PlayerStats.addAnimalsKilled(sp);
            PlayerStats.addHonor(sp, victim.isBaby() ? -3.0F : -2.0F);
        } else if (victim instanceof Raider) {
            PlayerStats.addHonor(sp, 3.0F);
        } else if (victim instanceof Enemy) {
            PlayerStats.addHonor(sp, 0.25F);
        }
    }

    @SubscribeEvent
    public void onHurt(LivingHurtEvent e) {
        if (e.getEntity() instanceof AbstractVillager && e.getSource().getEntity() instanceof ServerPlayer sp) {
            PlayerStats.addHonor(sp, -1.5F);
        }
    }

    /** Alimentar um animal (com a comida certa) rende honra. */
    @SubscribeEvent
    public void onFeed(PlayerInteractEvent.EntityInteract e) {
        if (e.getLevel().isClientSide) return;
        if (!(e.getEntity() instanceof ServerPlayer sp)) return;
        if (!(e.getTarget() instanceof Animal a)) return;
        ItemStack stack = e.getItemStack();
        if (stack.isEmpty() || !a.isFood(stack)) return;
        if (a.hasEffect(ModEffects.POSSESSED.get())) return;
        if (!a.isBaby() && !a.canFallInLove()) return;
        long now = a.level().getGameTime();
        if (now - a.getPersistentData().getLong("hv_fed") < 600L) return;
        a.getPersistentData().putLong("hv_fed", now);
        PlayerStats.addHonor(sp, HollowConfig.FEED_HONOR.get().floatValue());
    }

    @SubscribeEvent
    public void onBreed(BabyEntitySpawnEvent e) {
        if (e.getCausedByPlayer() instanceof ServerPlayer sp) PlayerStats.addHonor(sp, 2.0F);
    }

    @SubscribeEvent
    public void onTame(AnimalTameEvent e) {
        if (e.getTamer() instanceof ServerPlayer sp) PlayerStats.addHonor(sp, 5.0F);
    }

    /** Monstros nunca escolhem o anjo (honra 100) como alvo. */
    @SubscribeEvent
    public void onTarget(LivingChangeTargetEvent e) {
        if (e.getNewTarget() instanceof Player p && e.getEntity() instanceof Enemy && PlayerStats.isAngel(p)) {
            e.setCanceled(true);
        }
    }
}
