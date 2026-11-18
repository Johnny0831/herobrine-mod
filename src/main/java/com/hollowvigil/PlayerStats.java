package com.hollowvigil;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Sanidade (0-100), honra (0-100, começa em 50) e modos:
 * honra 0 = ASSASSINO (modo 1), honra 100 = ANJO (modo 2).
 */
public final class PlayerStats {
    private PlayerStats() {}

    public static final float SINNER_MAX = 25.0F;
    public static final float HONORED_MIN = 70.0F;
    public static final float ANGEL_MIN = 99.5F;

    private static final String K_SANITY = "hv_sanity", K_HONOR = "hv_honor", K_ANIMALS = "hv_animals",
            K_VILLAGERS = "hv_villagers", K_DREAD = "hv_dread", K_INTRO = "hv_intro",
            K_KILLER = "hv_killer", K_KILLS = "hv_kills", K_ANGEL = "hv_angel", K_ANGEL_FLY = "hv_angel_fly",
            K_LAST_KILL = "hv_last_kill", K_KNIFE_LVL = "hv_knife_lvl";

    public static CompoundTag data(Player p) {
        CompoundTag root = p.getPersistentData();
        if (!root.contains(Player.PERSISTED_NBT_TAG, Tag.TAG_COMPOUND)) root.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        return root.getCompound(Player.PERSISTED_NBT_TAG);
    }

    private static float clamp(float v) { return Math.max(0.0F, Math.min(100.0F, v)); }

    // ---- sanidade
    public static float getSanity(Player p) {
        CompoundTag d = data(p);
        return d.contains(K_SANITY) ? d.getFloat(K_SANITY) : 100.0F;
    }
    public static void setSanity(Player p, float v) { data(p).putFloat(K_SANITY, clamp(v)); }
    public static void addSanity(Player p, float d) { setSanity(p, getSanity(p) + d); }

    // ---- honra
    public static float getHonor(Player p) {
        CompoundTag d = data(p);
        return d.contains(K_HONOR) ? d.getFloat(K_HONOR) : 50.0F;
    }
    public static void setHonorRaw(Player p, float v) { data(p).putFloat(K_HONOR, clamp(v)); }

    /** Ganho/perda de honra por ações. Assassinos têm honra travada em 0 até concluírem a caçada. */
    public static void addHonor(ServerPlayer sp, float delta) {
        if (isKiller(sp)) return;
        float before = getHonor(sp);
        float after = clamp(before + delta);
        setHonorRaw(sp, after);
        if (after <= 0.01F) {
            Modes.enterKiller(sp);
            return;
        }
        if (before > SINNER_MAX && after <= SINNER_MAX) {
            sp.sendSystemMessage(Component.translatable("hollow.sinner.warn").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
            Sfx.atPlayer(sp, ModSounds.TOLL.get(), 1.0F, 0.8F);
            HorrorNet.shake(sp, 30, 2.0F);
        } else if (before < HONORED_MIN && after >= HONORED_MIN && after < ANGEL_MIN) {
            sp.sendSystemMessage(Component.translatable("hollow.honored.notice").withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
            Sfx.atPlayer(sp, ModSounds.CHIME.get(), 1.0F, 0.8F);
        }
        Modes.refresh(sp);
        sync(sp);
    }

    /** Usado por comandos: define a honra e recalcula os modos. */
    public static void forceHonor(ServerPlayer sp, float v) {
        if (isKiller(sp)) Modes.silentEndKiller(sp);
        setHonorRaw(sp, v);
        Modes.refresh(sp);
        sync(sp);
    }

    public static boolean isSinner(float honor) { return honor <= SINNER_MAX; }
    public static boolean isHonored(float honor) { return honor >= HONORED_MIN; }
    public static boolean isSinner(Player p) { return isSinner(getHonor(p)); }
    public static boolean isHonored(Player p) { return isHonored(getHonor(p)); }

    // ---- modos
    public static boolean isKiller(Player p) { return data(p).getBoolean(K_KILLER); }
    public static void setKiller(Player p, boolean v) { data(p).putBoolean(K_KILLER, v); }
    public static boolean isAngel(Player p) { return !isKiller(p) && getHonor(p) >= ANGEL_MIN; }
    public static boolean angelActive(Player p) { return data(p).getBoolean(K_ANGEL); }
    public static void setAngelActive(Player p, boolean v) { data(p).putBoolean(K_ANGEL, v); }
    public static boolean angelFly(Player p) { return data(p).getBoolean(K_ANGEL_FLY); }
    public static void setAngelFly(Player p, boolean v) { data(p).putBoolean(K_ANGEL_FLY, v); }
    public static int getKills(Player p) { return data(p).getInt(K_KILLS); }
    public static void setKills(Player p, int v) { data(p).putInt(K_KILLS, Math.max(0, v)); }
    public static int mode(Player p) { return isKiller(p) ? 1 : isAngel(p) ? 2 : 0; }

    public static long getLastKill(Player p) { return data(p).getLong(K_LAST_KILL); }
    public static void setLastKill(Player p, long v) { data(p).putLong(K_LAST_KILL, v); }
    public static int getKnifeLevel(Player p) { return data(p).getInt(K_KNIFE_LVL); }
    public static void setKnifeLevel(Player p, int v) { data(p).putInt(K_KNIFE_LVL, Math.max(0, v)); }
    public static void addKnifeLevel(Player p, int v) { setKnifeLevel(p, getKnifeLevel(p) + v); }

    // ---- contadores
    public static int getAnimalsKilled(Player p) { return data(p).getInt(K_ANIMALS); }
    public static void addAnimalsKilled(Player p) { data(p).putInt(K_ANIMALS, getAnimalsKilled(p) + 1); }
    public static int getVillagersKilled(Player p) { return data(p).getInt(K_VILLAGERS); }
    public static void addVillagersKilled(Player p) { data(p).putInt(K_VILLAGERS, getVillagersKilled(p) + 1); }
    public static int getDread(Player p) { return data(p).getInt(K_DREAD); }
    public static void setDread(Player p, int v) { data(p).putInt(K_DREAD, Math.max(0, Math.min(30, v))); }
    public static boolean introDone(Player p) { return data(p).getBoolean(K_INTRO); }
    public static void markIntroDone(Player p) { data(p).putBoolean(K_INTRO, true); }

    // ---- itens
    public static boolean hasItem(Player p, Item item) {
        for (ItemStack s : p.getInventory().items) if (s.is(item)) return true;
        for (ItemStack s : p.getInventory().offhand) if (s.is(item)) return true;
        return false;
    }
    public static boolean hasCharm(Player p) { return hasItem(p, ModItems.SANITY_CHARM.get()); }

    /** Efeito de medo: ignorado por assassinos e anjos. */
    public static void fear(ServerPlayer sp, MobEffect effect, int ticks) {
        if (isKiller(sp) || isAngel(sp)) return;
        sp.addEffect(new MobEffectInstance(effect, ticks, 0, false, false));
    }

    public static void sync(ServerPlayer sp) {
        HorrorNet.sendStats(sp);
    }
}
