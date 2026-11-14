package com.hollowvigil;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

final class Vec3Helper {
    private Vec3Helper() {}

    /** Coloca a entidade a 'dist' blocos do jogador, numa direção aleatória (ao nível do chão do jogador). */
    static void place(Entity e, ServerPlayer sp, double dist) {
        double ang = sp.getRandom().nextDouble() * Math.PI * 2.0D;
        e.moveTo(sp.getX() + Math.cos(ang) * dist, sp.getY(), sp.getZ() + Math.sin(ang) * dist);
    }
}
