package com.hollowvigil;

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
}
