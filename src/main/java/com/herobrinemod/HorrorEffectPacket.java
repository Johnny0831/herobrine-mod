package com.herobrinemod;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Servidor -> cliente: dispara efeitos visuais de terror. */
public record HorrorEffectPacket(Type type, int ticks, float intensity) {
    public enum Type { SHAKE, FLASH, FOG }

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(type);
        buf.writeVarInt(ticks);
        buf.writeFloat(intensity);
    }

    public static HorrorEffectPacket decode(FriendlyByteBuf buf) {
        return new HorrorEffectPacket(buf.readEnum(Type.class), buf.readVarInt(), buf.readFloat());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHorror.receive(this)));
        ctx.get().setPacketHandled(true);
    }
}
