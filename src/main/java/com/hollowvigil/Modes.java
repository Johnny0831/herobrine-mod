package com.hollowvigil;

import net.minecraft.ChatFormatting;
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
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

/**
 * ASSASSINO (honra 0): sem medo e sem sons de terror, +20 de vida máxima (sem regeneração: só se cura matando),
 * força, velocidade, fome lenta, visão vermelha, Faca do Assassino (infinita, evolui a cada caça) e uma caçada
 * cada vez mais difícil. Só ele fere as entidades. Se passar muito tempo sem matar, a fúria acaba:
 * a honra sobe, o modo termina e ele fica lento e exausto.
 * ANJO (honra 100): as entidades nunca aparecem, ajuda todas as criaturas, voa.
 */
public final class Modes {
    private Modes() {}

    private static final UUID HP_UUID = UUID.fromString("f6f0a8d2-0c53-4a43-9a1e-5c6d7b0c1d11");

    private static void title(ServerPlayer sp, Component main, Component sub) {
        sp.connection.send(new ClientboundSetTitlesAnimationPacket(10, 70, 25));
        sp.connection.send(new ClientboundSetSubtitleTextPacket(sub));
        sp.connection.send(new ClientboundSetTitleTextPacket(main));
    }

    /** Nível da caçada (0-8): quanto maior, mais e mais fortes são as entidades que vêm atrás do assassino. */
    public static int huntLevel(Player p) {
        return Math.min(8, PlayerStats.getKills(p) / 3);
    }

    /** Fúria restante (0-100) do assassino; cai enquanto ele não mata ninguém. */
    public static int rage(ServerPlayer sp) {
        if (!PlayerStats.isKiller(sp)) return 0;
        long last = PlayerStats.getLastKill(sp);
        if (last <= 0L) return 100;
        double idle = (sp.level().getGameTime() - last) / 20.0D;
        double limit = Math.max(1, HollowConfig.KILLER_IDLE_SECONDS.get());
        return (int) Math.max(0, Math.min(100, 100.0D - idle * 100.0D / limit));
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

    /** Após renascer: o corpo do assassino (vida extra) e a faca voltam na hora. */
    public static void onRespawn(ServerPlayer sp) {
        if (PlayerStats.isKiller(sp)) {
            PlayerStats.setSanity(sp, 100.0F);
            ensureKillerBody(sp);
            ensureKnife(sp);
        }
        PlayerStats.sync(sp);
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
        PlayerStats.setKnifeLevel(sp, 0);
        PlayerStats.setLastKill(sp, sp.level().getGameTime());
        PlayerStats.setSanity(sp, 100.0F);
        ensureKillerBody(sp);
        sp.setHealth(sp.getMaxHealth());
        ensureKnife(sp);
        applyKillerEffects(sp);
        title(sp, Component.translatable("hollow.killer.title").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
                Component.translatable("hollow.killer.subtitle").withStyle(ChatFormatting.RED));
        Sfx.always(sp, ModSounds.RAGE.get(), 1.4F, 1.0F);
        Sfx.always(sp, ModSounds.TOLL.get(), 1.2F, 0.7F);
        for (int i = 1; i <= 3; i++) {
            final int n = i;
            HorrorDirector.INSTANCE.schedule(40 * i, () ->
                    sp.sendSystemMessage(Component.translatable("hollow.killer.msg." + n).withStyle(ChatFormatting.RED)));
        }
        PlayerStats.sync(sp);
    }

    /** +20 de vida máxima por modificador permanente de atributo (o efeito Health Boost era removido a cada renovação). */
    private static void ensureKillerBody(ServerPlayer sp) {
        AttributeInstance hp = sp.getAttribute(Attributes.MAX_HEALTH);
        if (hp != null && hp.getModifier(HP_UUID) == null) {
            hp.addTransientModifier(new AttributeModifier(HP_UUID, "hollowvigil_killer_vitality", 20.0D,
                    AttributeModifier.Operation.ADDITION));
            sp.setHealth(sp.getMaxHealth());
        }
    }

    private static void removeKillerBody(ServerPlayer sp) {
        AttributeInstance hp = sp.getAttribute(Attributes.MAX_HEALTH);
        if (hp != null && hp.getModifier(HP_UUID) != null) hp.removeModifier(HP_UUID);
        if (sp.getHealth() > sp.getMaxHealth()) sp.setHealth(sp.getMaxHealth());
    }

    private static void applyKillerEffects(ServerPlayer sp) {
        sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 60, 0, false, false, false));
        sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 0, false, false, false));
        sp.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0, false, false, false));
    }

    private static void tickKiller(ServerPlayer sp) {
        ensureKillerBody(sp);
        applyKillerEffects(sp);
        FoodData food = sp.getFoodData();
        food.setExhaustion(food.getExhaustionLevel() * 0.25F);   // fome ~4x mais lenta
        PlayerStats.setSanity(sp, 100.0F);
        ensureKnife(sp);
        tickIdle(sp);
    }

    // ---- faca (soulbound: volta sozinha e não pode ser jogada fora)

    public static void ensureKnife(ServerPlayer sp) {
        if (!PlayerStats.hasItem(sp, ModItems.KILLER_KNIFE.get())) giveKnife(sp);
    }

    private static void giveKnife(ServerPlayer sp) {
        ItemStack knife = new ItemStack(ModItems.KILLER_KNIFE.get());
        KillerKnifeItem.setLevel(knife, PlayerStats.getKnifeLevel(sp));
        knife.getOrCreateTag().putBoolean("Unbreakable", true);
        if (!sp.getInventory().add(knife)) Gifts.drop(sp, knife);
    }

    private static void updateKnives(ServerPlayer sp) {
        Inventory inv = sp.getInventory();
        int level = PlayerStats.getKnifeLevel(sp);
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (s.is(ModItems.KILLER_KNIFE.get())) KillerKnifeItem.setLevel(s, level);
        }
    }

    private static void removeKnives(ServerPlayer sp) {
        Inventory inv = sp.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (inv.getItem(i).is(ModItems.KILLER_KNIFE.get())) inv.setItem(i, ItemStack.EMPTY);
        }
    }

    // ---- caça

    /** Qualquer morte causada pelo assassino reinicia o cronômetro da fúria. */
    public static void noteKill(ServerPlayer sp) {
        PlayerStats.setLastKill(sp, sp.level().getGameTime());
        if (PlayerStats.getHonor(sp) > 0.01F) PlayerStats.setHonorRaw(sp, 0.0F);   // a fúria volta
    }

    /** Uma entidade foi caçada: progresso da missão, faca mais forte e cura (a única cura do modo). */
    public static void onEntityKilled(ServerPlayer sp, int points) {
        noteKill(sp);
        int kills = PlayerStats.getKills(sp) + points;
        PlayerStats.setKills(sp, kills);
        PlayerStats.addKnifeLevel(sp, points);
        updateKnives(sp);
        sp.setHealth(Math.min(sp.getMaxHealth(), sp.getHealth() + 2.0F + points * 2.0F));
        int target = HollowConfig.KILLER_TARGET.get();
        sp.displayClientMessage(Component.translatable("hollow.killer.progress", Math.min(kills, target), target)
                .withStyle(ChatFormatting.RED), true);
        Sfx.always(sp, ModSounds.TOLL.get(), 0.6F, 1.3F);
        if (kills >= target) endKiller(sp);
        else PlayerStats.sync(sp);
    }

    /** Sem matar por muito tempo: a fúria esvazia, a honra sobe e o modo termina com lentidão e exaustão. */
    private static void tickIdle(ServerPlayer sp) {
        long now = sp.level().getGameTime();
        long last = PlayerStats.getLastKill(sp);
        if (last <= 0L) {
            PlayerStats.setLastKill(sp, now);
            return;
        }
        int idle = (int) ((now - last) / 20L);
        int limit = HollowConfig.KILLER_IDLE_SECONDS.get();
        if (idle >= limit * 6 / 10 && idle < limit && idle % 10 == 0) {
            sp.displayClientMessage(Component.translatable("hollow.killer.fading").withStyle(ChatFormatting.GOLD), true);
        }
        if (idle >= limit) {
            float honor = PlayerStats.getHonor(sp) + 1.0F;
            PlayerStats.setHonorRaw(sp, honor);
            if (honor >= 20.0F) exitExhausted(sp);
        }
    }

    private static void exitExhausted(ServerPlayer sp) {
        silentEndKiller(sp);
        removeKnives(sp);
        PlayerStats.setKnifeLevel(sp, 0);
        PlayerStats.setHonorRaw(sp, 20.0F);
        PlayerStats.setSanity(sp, 60.0F);
        sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 1200, 2, false, true, true));
        sp.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 1200, 1, false, true, true));
        sp.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 1200, 1, false, true, true));
        sp.addEffect(new MobEffectInstance(MobEffects.HUNGER, 600, 1, false, true, true));
        title(sp, Component.translatable("hollow.killer.exhausted.title").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD),
                Component.translatable("hollow.killer.exhausted.subtitle").withStyle(ChatFormatting.DARK_GRAY));
        sp.sendSystemMessage(Component.translatable("hollow.killer.exhausted.msg").withStyle(ChatFormatting.GRAY));
        Sfx.always(sp, ModSounds.BREATH.get(), 1.0F, 0.7F);
        PlayerStats.sync(sp);
    }

    /** Missão cumprida: honra renasce, recompensas, a faca fica como troféu. */
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
        sp.removeEffect(MobEffects.DAMAGE_BOOST);
        sp.removeEffect(MobEffects.MOVEMENT_SPEED);
        sp.removeEffect(MobEffects.NIGHT_VISION);
        removeKillerBody(sp);
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

        for (int i = 0; i < 6; i++) {
            double a = i * Math.PI / 3.0D + sp.tickCount * 0.05D;
            level.sendParticles(ParticleTypes.END_ROD, sp.getX() + Math.cos(a) * 0.4D, sp.getY() + 2.15D,
                    sp.getZ() + Math.sin(a) * 0.4D, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        if (sp.getRandom().nextInt(25) == 0) Sfx.atPlayer(sp, ModSounds.CHIME.get(), 0.4F, 0.9F + sp.getRandom().nextFloat() * 0.3F);
        if (sp.getRandom().nextInt(60) == 0) ChatLines.hollowSays(sp, "angel");
    }
}
