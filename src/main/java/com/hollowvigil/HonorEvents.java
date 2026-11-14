package com.hollowvigil;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.raid.Raider;
import net.minecraftforge.event.entity.living.AnimalTameEvent;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Pecados reduzem a honra; boas ações aumentam. */
public class HonorEvents {

    @SubscribeEvent
    public void onDeath(LivingDeathEvent e) {
        LivingEntity victim = e.getEntity();
        if (victim.level().isClientSide) return;
        if (!(e.getSource().getEntity() instanceof ServerPlayer sp)) return;

        if (victim instanceof HollowEntity || victim instanceof HollowReaperEntity) {
            PlayerStats.addSanity(sp, 25.0F);
            return;
        }
        if (victim instanceof HallucinationEntity) {
            PlayerStats.addSanity(sp, 4.0F);
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

    @SubscribeEvent
    public void onBreed(BabyEntitySpawnEvent e) {
        if (e.getCausedByPlayer() instanceof ServerPlayer sp) PlayerStats.addHonor(sp, 2.0F);
    }

    @SubscribeEvent
    public void onTame(AnimalTameEvent e) {
        if (e.getTamer() instanceof ServerPlayer sp) PlayerStats.addHonor(sp, 5.0F);
    }
}
