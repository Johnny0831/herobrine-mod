package com.hollowvigil;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Servidor -> cliente: sanidade, honra, modo (0 normal, 1 assassino, 2 anjo), caçada, fúria (0-100) e nível da faca. */
public record StatsSyncPacket(int sanity, int honor, int mode, int kills, int target, int rage, int knife) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(sanity);
        buf.writeVarInt(honor);
        buf.writeVarInt(mode);
        buf.writeVarInt(kills);
        buf.writeVarInt(target);
        buf.writeVarInt(rage);
        buf.writeVarInt(knife);
    }

    public static StatsSyncPacket decode(FriendlyByteBuf buf) {
        return new StatsSyncPacket(buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                buf.readVarInt(), buf.readVarInt(), buf.readVarInt());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHorror.setStats(this)));
        ctx.get().setPacketHandled(true);
    }
}
