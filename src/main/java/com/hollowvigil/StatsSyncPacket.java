package com.hollowvigil;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Servidor -> cliente: sanidade e honra (0-100) para a HUD. */
public record StatsSyncPacket(int sanity, int honor) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(sanity);
        buf.writeVarInt(honor);
    }

    public static StatsSyncPacket decode(FriendlyByteBuf buf) {
        return new StatsSyncPacket(buf.readVarInt(), buf.readVarInt());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHorror.setStats(sanity, honor)));
        ctx.get().setPacketHandled(true);
    }
}
