package com.hollowvigil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

/** Procura lugares válidos para fazer entidades aparecerem ao redor do jogador (céu aberto ou caverna). */
public final class Spots {
    private Spots() {}

    public static double lookAngle(ServerPlayer sp) {
        Vec3 l = sp.getLookAngle();
        return Math.atan2(l.z, l.x);
    }

    public static BlockPos around(ServerLevel level, ServerPlayer sp, double minD, double maxD, double centerAngle, double spread) {
        boolean cave = !level.canSeeSky(sp.blockPosition());
        for (int i = 0; i < 14; i++) {
            double ang = centerAngle + (sp.getRandom().nextDouble() - 0.5D) * 2.0D * spread;
            double dist = minD + sp.getRandom().nextDouble() * (maxD - minD);
            if (cave) dist = Math.min(dist, Math.max(minD, 12.0D));
            BlockPos p = find(level, sp, sp.getX() + Math.cos(ang) * dist, sp.getZ() + Math.sin(ang) * dist);
            if (p != null) return p;
        }
        return null;
    }

    private static BlockPos find(ServerLevel level, ServerPlayer sp, double fx, double fz) {
        int x = (int) Math.floor(fx);
        int z = (int) Math.floor(fz);
        if (!level.hasChunkAt(new BlockPos(x, sp.getBlockY(), z))) return null;
        if (level.canSeeSky(sp.blockPosition())) {
            BlockPos p = new BlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z), z);
            return level.getFluidState(p.below()).isEmpty() ? p : null;
        }
        for (int dy = 3; dy >= -3; dy--) {
            BlockPos p = new BlockPos(x, sp.getBlockY() + dy, z);
            if (level.getBlockState(p).isAir() && level.getBlockState(p.above()).isAir()
                    && level.getBlockState(p.above(2)).isAir()
                    && level.getBlockState(p.below()).isFaceSturdy(level, p.below(), Direction.UP)) {
                return p;
            }
        }
        return null;
    }
}
