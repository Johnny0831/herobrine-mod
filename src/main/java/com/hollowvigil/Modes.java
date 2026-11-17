package com.hollowvigil;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.food.FoodData;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ItemStack;

/**
 * ASSASSINO (honra 0): sem medo, mais vida e força, faca, visão vermelha, missão de caçar as entidades
 * (e só ele pode feri-las). ANJO (honra 100): as entidades nunca aparecem, ajuda todas as criaturas, voa.
 */
public final class Modes {
    private Modes() {}

    private static void title(ServerPlayer sp, Component main, Component sub) {
        sp.connection.send(new ClientboundSetTitlesAnimationPacket(10, 70, 25));
        sp.connection.send(new ClientboundSetSubtitleTextPacket(sub));
        sp.connection.send(new ClientboundSetTitleTextPacket(main));
    }

    /** Recalcula os modos a partir da honra atual (chamado a cada segundo e após mudanças). */
    public static void refresh(ServerPlayer sp) {
        if (!PlayerStats.isKiller(sp) && PlayerStats.getHonor(sp) <= 0.01F) {
            enterKiller(sp);
            return;
        }
        boolean angel = PlayerStats.isAngel(sp);
        if (angel && !PlayerStats.angelActive(sp)) enterAngel(sp);
        else if (!angel && PlayerStats.angelActive(sp)) leaveAngel(sp);
    }

    public static void tick(ServerPlayer sp) {
        refresh(sp);
        if (PlayerStats.isKiller(sp)) tickKiller(sp);
        if (PlayerStats.angelActive(sp)) tickAngel(sp);
    }

    // =================================================================== ASSASSINO

    public static void enterKiller(ServerPlayer sp) {
        if (PlayerStats.isKiller(sp)) return;
        HorrorNet.flash(sp, 20);
        HorrorNet.shake(sp, 60, 4.0F);
        if (PlayerStats.angelActive(sp)) leaveAngel(sp);
        PlayerStats.setHonorRaw(sp, 0.0F);
        PlayerStats.setKiller(sp, true);
        PlayerStats.setKills(sp, 0);
        PlayerStats.setSanity(sp, 100.0F);
        tickKiller(sp);
        sp.setHealth(sp.getMaxHealth());
        giveKnife(sp);
        title(sp, Component.translatable("hollow.killer.title").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
                Component.translatable("hollow.killer.subtitle").withStyle(ChatFormatting.RED));
        Sfx.atPlayer(sp, ModSounds.RAGE.get(), 1.4F, 1.0F);
        Sfx.atPlayer(sp, ModSounds.TOLL.get(), 1.2F, 0.7F);
        for (int i = 1; i <= 3; i++) {
            final int n = i;
            HorrorDirector.INSTANCE.schedule(40 * i, () ->
                    sp.sendSystemMessage(Component.translatable("hollow.killer.msg." + n).withStyle(ChatFormatting.RED)));
        }
        PlayerStats.sync(sp);
    }

    private static void tickKiller(ServerPlayer sp) {
        sp.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST, 60, 4, false, false, false));
        sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 60, 0, false, false, false));
        sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 0, false, false, false));
        sp.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0, false, false, false));
        FoodData food = sp.getFoodData();
        food.setExhaustion(food.getExhaustionLevel() * 0.25F);   // fome ~4x mais lenta
        PlayerStats.setSanity(sp, 100.0F);
        if (sp.tickCount % 200 == 0 && !PlayerStats.hasItem(sp, ModItems.KILLER_KNIFE.get())) giveKnife(sp);
    }

    private static void giveKnife(ServerPlayer sp) {
        ItemStack knife = new ItemStack(ModItems.KILLER_KNIFE.get());
        if (!sp.getInventory().add(knife)) Gifts.drop(sp, knife);
    }

    /** Progresso da caçada. */
    public static void onEntityKilled(ServerPlayer sp, int points) {
        int kills = PlayerStats.getKills(sp) + points;
        PlayerStats.setKills(sp, kills);
        int target = HollowConfig.KILLER_TARGET.get();
        sp.displayClientMessage(Component.translatable("hollow.killer.progress", Math.min(kills, target), target)
                .withStyle(ChatFormatting.RED), true);
        Sfx.atPlayer(sp, ModSounds.TOLL.get(), 0.6F, 1.3F);
        if (kills >= target) endKiller(sp);
        else PlayerStats.sync(sp);
    }

    public static void endKiller(ServerPlayer sp) {
        silentEndKiller(sp);
        PlayerStats.setHonorRaw(sp, 40.0F);
        PlayerStats.setSanity(sp, 80.0F);
        title(sp, Component.translatable("hollow.killer.done.title").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
                Component.translatable("hollow.killer.done.subtitle").withStyle(ChatFormatting.YELLOW));
        sp.sendSystemMessage(Component.translatable("hollow.killer.done.msg").withStyle(ChatFormatting.YELLOW));
        Sfx.atPlayer(sp, ModSounds.CHIME.get(), 1.2F, 0.8F);
        Sfx.atPlayer(sp, ModSounds.ANGEL_CHOIR.get(), 0.9F, 1.0F);
        Gifts.drop(sp, new ItemStack(ModItems.HOLLOW_HEART.get()));
        Gifts.drop(sp, new ItemStack(ModItems.SANITY_CHARM.get()));
        PlayerStats.sync(sp);
    }

    /** Sai do modo assassino sem recompensas (comandos / mudança de honra forçada). */
    public static void silentEndKiller(ServerPlayer sp) {
        PlayerStats.setKiller(sp, false);
        PlayerStats.setKills(sp, 0);
        sp.removeEffect(MobEffects.HEALTH_BOOST);
        sp.removeEffect(MobEffects.DAMAGE_BOOST);
        sp.removeEffect(MobEffects.MOVEMENT_SPEED);
        sp.removeEffect(MobEffects.NIGHT_VISION);
    }

    // =================================================================== ANJO

    public static void enterAngel(ServerPlayer sp) {
        if (PlayerStats.angelActive(sp)) return;
        PlayerStats.setAngelActive(sp, true);
        PlayerStats.setSanity(sp, 100.0F);
        title(sp, Component.translatable("hollow.angel.title").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
                Component.translatable("hollow.angel.subtitle").withStyle(ChatFormatting.YELLOW));
        sp.sendSystemMessage(Component.translatable("hollow.angel.msg").withStyle(ChatFormatting.GOLD));
        Sfx.atPlayer(sp, ModSounds.ANGEL_CHOIR.get(), 1.2F, 1.0F);
        Sfx.atPlayer(sp, ModSounds.CHIME.get(), 1.0F, 1.0F);
        if (sp.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.END_ROD, sp.getX(), sp.getY() + 1.0D, sp.getZ(), 60, 0.6D, 1.0D, 0.6D, 0.05D);
        }
        if (HollowConfig.ANGEL_FLIGHT.get() && !sp.isCreative() && !sp.isSpectator() && !sp.getAbilities().mayfly) {
            sp.getAbilities().mayfly = true;
            sp.onUpdateAbilities();
            PlayerStats.setAngelFly(sp, true);
        }
        PlayerStats.sync(sp);
    }

    public static void leaveAngel(ServerPlayer sp) {
        PlayerStats.setAngelActive(sp, false);
        if (PlayerStats.angelFly(sp)) {
            PlayerStats.setAngelFly(sp, false);
            if (!sp.isCreative() && !sp.isSpectator()) {
                sp.getAbilities().mayfly = false;
                sp.getAbilities().flying = false;
                sp.onUpdateAbilities();
            }
        }
        sp.sendSystemMessage(Component.translatable("hollow.angel.fall").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        Sfx.atPlayer(sp, ModSounds.TOLL.get(), 0.8F, 1.2F);
        PlayerStats.sync(sp);
    }

    private static void tickAngel(ServerPlayer sp) {
        ServerLevel level = (ServerLevel) sp.level();
        sp.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, false, false, false));
        sp.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 60, 0, false, false, false));
        sp.addEffect(new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, 400, 0, false, false, false));
        PlayerStats.setSanity(sp, 100.0F);

        if (HollowConfig.ANGEL_FLIGHT.get() && !sp.isCreative() && !sp.isSpectator() && !sp.getAbilities().mayfly) {
            sp.getAbilities().mayfly = true;
            sp.onUpdateAbilities();
            PlayerStats.setAngelFly(sp, true);
        }

        // aura: cura todas as criaturas por perto e acalma quem tentava atacar o anjo
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, sp.getBoundingBox().inflate(16.0D),
                x -> x != sp && x.isAlive() && !(x instanceof HollowEntity) && !(x instanceof HollowReaperEntity)
                        && !(x instanceof HallucinationEntity))) {
            e.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 1, false, false, true));
            if (e instanceof Mob m && m.getTarget() == sp) m.setTarget(null);
        }

        // auréola de partículas
        for (int i = 0; i < 6; i++) {
            double a = i * Math.PI / 3.0D + sp.tickCount * 0.05D;
            level.sendParticles(ParticleTypes.END_ROD, sp.getX() + Math.cos(a) * 0.4D, sp.getY() + 2.15D,
                    sp.getZ() + Math.sin(a) * 0.4D, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        if (sp.getRandom().nextInt(25) == 0) Sfx.atPlayer(sp, ModSounds.CHIME.get(), 0.4F, 0.9F + sp.getRandom().nextFloat() * 0.3F);
        if (sp.getRandom().nextInt(60) == 0) ChatLines.hollowSays(sp, "angel");
    }
}
