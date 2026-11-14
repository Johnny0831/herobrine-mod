package com.hollowvigil;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/** Sons tocados apenas para UM jogador, em posições arbitrárias (ninguém mais ouve). */
public final class Sfx {
    private Sfx() {}

    public static void play(ServerPlayer p, SoundEvent ev, float vol, float pitch, double x, double y, double z) {
        if (ev == null) return;
        p.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(ev), SoundSource.AMBIENT,
                x, y, z, vol, pitch, p.getRandom().nextLong()));
    }

    public static void play(ServerPlayer p, String id, float vol, float pitch, double x, double y, double z) {
        ResourceLocation rl = ResourceLocation.tryParse(id);
        if (rl == null) return;
        play(p, BuiltInRegistries.SOUND_EVENT.get(rl), vol, pitch, x, y, z);
    }

    public static void atPlayer(ServerPlayer p, String id, float vol, float pitch) {
        play(p, id, vol, pitch, p.getX(), p.getY(), p.getZ());
    }

    public static Vec3 behindPos(ServerPlayer p, double dist) {
        Vec3 look = p.getLookAngle();
        Vec3 flat = new Vec3(look.x, 0, look.z);
        flat = flat.lengthSqr() < 1.0E-4 ? new Vec3(0, 0, 1) : flat.normalize();
        return p.position().subtract(flat.scale(dist));
    }

    public static void behind(ServerPlayer p, String id, float vol, float pitch, double dist) {
        Vec3 v = behindPos(p, dist);
        play(p, id, vol, pitch, v.x, v.y, v.z);
    }

    public static void behind(ServerPlayer p, SoundEvent ev, float vol, float pitch, double dist) {
        Vec3 v = behindPos(p, dist);
        play(p, ev, vol, pitch, v.x, v.y, v.z);
    }
}
