package com.hollowvigil;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class LookUtil {
    private LookUtil() {}

    /** O jogador está olhando para a entidade (cone estreito + linha de visão)? */
    public static boolean isLookingAt(Player p, Entity e) {
        Vec3 view = p.getViewVector(1.0F).normalize();
        Vec3 to = new Vec3(e.getX() - p.getX(), e.getEyeY() - p.getEyeY(), e.getZ() - p.getZ());
        double dist = to.length();
        if (dist < 1.0E-4D) return true;
        to = to.scale(1.0D / dist);
        return view.dot(to) > 0.96D && p.hasLineOfSight(e);
    }

    /** Puxa a câmera do jogador para a entidade (susto de "cabeça virada à força"). */
    public static void forceLook(ServerPlayer sp, Entity e) {
        double dx = e.getX() - sp.getX();
        double dz = e.getZ() - sp.getZ();
        double dy = e.getEyeY() - sp.getEyeY();
        float yaw = (float) (Math.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F;
        float pitch = (float) (-(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)) * (180.0D / Math.PI)));
        sp.connection.teleport(sp.getX(), sp.getY(), sp.getZ(), yaw, pitch);
    }
}
