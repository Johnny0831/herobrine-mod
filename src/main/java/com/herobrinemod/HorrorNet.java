package com.herobrinemod;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class HorrorNet {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(HerobrineMod.MODID, "main"), () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    public static void register() {
        CHANNEL.registerMessage(0, HorrorEffectPacket.class,
                HorrorEffectPacket::encode, HorrorEffectPacket::decode, HorrorEffectPacket::handle);
    }

    public static void sendTo(ServerPlayer player, HorrorEffectPacket packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void shake(ServerPlayer p, int ticks, float intensity) {
        sendTo(p, new HorrorEffectPacket(HorrorEffectPacket.Type.SHAKE, ticks, intensity));
    }

    public static void flash(ServerPlayer p, int ticks) {
        sendTo(p, new HorrorEffectPacket(HorrorEffectPacket.Type.FLASH, ticks, 1.0F));
    }

    public static void fog(ServerPlayer p, int ticks) {
        sendTo(p, new HorrorEffectPacket(HorrorEffectPacket.Type.FOG, ticks, 1.0F));
    }
}
