package com.hollowvigil;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class HorrorNet {
    private static final String PROTOCOL = "2";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(HollowVigilMod.MODID, "main"), () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    public static void register() {
        CHANNEL.registerMessage(0, HorrorEffectPacket.class,
                HorrorEffectPacket::encode, HorrorEffectPacket::decode, HorrorEffectPacket::handle);
        CHANNEL.registerMessage(1, StatsSyncPacket.class,
                StatsSyncPacket::encode, StatsSyncPacket::decode, StatsSyncPacket::handle);
    }

    public static void sendStats(ServerPlayer p) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> p), new StatsSyncPacket(
                Math.round(PlayerStats.getSanity(p)), Math.round(PlayerStats.getHonor(p)),
                PlayerStats.mode(p), PlayerStats.getKills(p), HollowConfig.KILLER_TARGET.get()));
    }

    /** Assassinos "não temem nada" e anjos estão protegidos: efeitos de susto não são enviados. */
    private static boolean fearless(ServerPlayer p) {
        return PlayerStats.isKiller(p) || PlayerStats.isAngel(p);
    }

    private static void send(ServerPlayer p, HorrorEffectPacket packet) {
        if (fearless(p)) return;
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> p), packet);
    }

    public static void shake(ServerPlayer p, int ticks, float intensity) {
        send(p, new HorrorEffectPacket(HorrorEffectPacket.Type.SHAKE, ticks, intensity));
    }

    public static void flash(ServerPlayer p, int ticks) {
        send(p, new HorrorEffectPacket(HorrorEffectPacket.Type.FLASH, ticks, 1.0F));
    }

    public static void fog(ServerPlayer p, int ticks) {
        send(p, new HorrorEffectPacket(HorrorEffectPacket.Type.FOG, ticks, 1.0F));
    }
}
