package com.hollowvigil;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Sanidade (0-100) e honra (0-100) do jogador, salvas no NBT persistente. Honra começa em 50. */
public final class PlayerStats {
    private PlayerStats() {}

    public static final float SINNER_MAX = 25.0F;
    public static final float HONORED_MIN = 70.0F;

    private static final String K_SANITY = "hv_sanity", K_HONOR = "hv_honor", K_ANIMALS = "hv_animals",
            K_VILLAGERS = "hv_villagers", K_DREAD = "hv_dread", K_INTRO = "hv_intro";

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

    public static void addHonor(ServerPlayer sp, float delta) {
        float before = getHonor(sp);
        float after = clamp(before + delta);
        setHonorRaw(sp, after);
        if (before > SINNER_MAX && after <= SINNER_MAX) {
            sp.sendSystemMessage(Component.translatable("hollow.sinner.warn").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
            Sfx.atPlayer(sp, "entity.elder_guardian.curse", 1.0F, 0.6F);
            HorrorNet.shake(sp, 30, 2.0F);
        } else if (before < HONORED_MIN && after >= HONORED_MIN) {
            sp.sendSystemMessage(Component.translatable("hollow.honored.notice").withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
            Sfx.atPlayer(sp, "block.amethyst_block.chime", 1.0F, 0.8F);
        }
        sync(sp);
    }

    public static boolean isSinner(float honor) { return honor <= SINNER_MAX; }
    public static boolean isHonored(float honor) { return honor >= HONORED_MIN; }
    public static boolean isSinner(Player p) { return isSinner(getHonor(p)); }
    public static boolean isHonored(Player p) { return isHonored(getHonor(p)); }

    // ---- contadores
    public static int getAnimalsKilled(Player p) { return data(p).getInt(K_ANIMALS); }
    public static void addAnimalsKilled(Player p) { data(p).putInt(K_ANIMALS, getAnimalsKilled(p) + 1); }
    public static int getVillagersKilled(Player p) { return data(p).getInt(K_VILLAGERS); }
    public static void addVillagersKilled(Player p) { data(p).putInt(K_VILLAGERS, getVillagersKilled(p) + 1); }
    public static int getDread(Player p) { return data(p).getInt(K_DREAD); }
    public static void setDread(Player p, int v) { data(p).putInt(K_DREAD, Math.max(0, Math.min(30, v))); }
    public static boolean introDone(Player p) { return data(p).getBoolean(K_INTRO); }
    public static void markIntroDone(Player p) { data(p).putBoolean(K_INTRO, true); }

    public static boolean hasCharm(Player p) {
        for (ItemStack s : p.getInventory().items) if (s.is(ModItems.SANITY_CHARM.get())) return true;
        for (ItemStack s : p.getInventory().offhand) if (s.is(ModItems.SANITY_CHARM.get())) return true;
        return false;
    }

    public static void sync(ServerPlayer sp) {
        HorrorNet.sendStats(sp, Math.round(getSanity(sp)), Math.round(getHonor(sp)));
    }
}
