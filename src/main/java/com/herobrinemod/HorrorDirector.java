package com.herobrinemod;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.*;
import java.util.function.Predicate;

/**
 * Cérebro do terror psicológico. Para cada jogador roda uma máquina de estados:
 * IDLE -> OMEN (presságios) -> APPEAR (Herobrine em campo) -> COOLDOWN.
 * Cada aparição aumenta o "nível de pavor" (salvo no jogador), que eleva a chance da versão agressiva.
 */
public class HorrorDirector {
    public static final HorrorDirector INSTANCE = new HorrorDirector();
    private static final String DREAD_KEY = "herobrine_dread";

    private enum Phase { IDLE, OMEN, APPEAR, COOLDOWN }

    private static class Haunt {
        Phase phase = Phase.IDLE;
        int ticks;
        int total;
        UUID entity;
        boolean corrupted;
        boolean joinSent, chatSent, fogSent;
    }

    private static class Task {
        int delay;
        final Runnable action;
        Task(int delay, Runnable action) { this.delay = delay; this.action = action; }
    }

    private final Map<UUID, Haunt> haunts = new HashMap<>();
    private final List<Task> tasks = new ArrayList<>();
    private final List<Task> pending = new ArrayList<>();

    public void schedule(int delayTicks, Runnable action) {
        pending.add(new Task(delayTicks, action));
    }

    // ------------------------------------------------------------ eventos

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        if (!pending.isEmpty()) {
            tasks.addAll(pending);
            pending.clear();
        }
        Iterator<Task> it = tasks.iterator();
        while (it.hasNext()) {
            Task t = it.next();
            if (--t.delay <= 0) {
                it.remove();
                try {
                    t.action.run();
                } catch (Exception ex) {
                    HerobrineMod.LOGGER.error("Erro em tarefa agendada do Herobrine", ex);
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || e.side.isClient()) return;
        if (!(e.player instanceof ServerPlayer sp) || sp.tickCount % 20 != 0) return;
        Haunt h = haunts.computeIfAbsent(sp.getUUID(), k -> new Haunt());
        switch (h.phase) {
            case IDLE -> tickIdle(sp, h);
            case OMEN -> tickOmen(sp, h);
            case APPEAR -> tickAppear(sp, h);
            case COOLDOWN -> { if (--h.ticks <= 0) h.phase = Phase.IDLE; }
        }
    }

    @SubscribeEvent
    public void onLogin(PlayerEvent.PlayerLoggedInEvent e) {
        if (HerobrineConfig.WELCOME_MESSAGE.get() && e.getEntity() instanceof ServerPlayer sp) {
            sp.sendSystemMessage(Component.translatable("herobrine.welcome")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }
    }

    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent e) {
        haunts.remove(e.getEntity().getUUID());
    }

    // ------------------------------------------------------------ API pública

    /** @param forceCorrupted null = sorteia; @param immediate pula os presságios. */
    public void startHaunt(ServerPlayer sp, Boolean forceCorrupted, boolean immediate) {
        Haunt h = haunts.computeIfAbsent(sp.getUUID(), k -> new Haunt());
        h.corrupted = forceCorrupted != null ? forceCorrupted : rollCorrupted(sp);
        HerobrineMod.LOGGER.info("Herobrine: evento iniciado para {} (corrompido={}, imediato={})",
                sp.getName().getString(), h.corrupted, immediate);
        h.joinSent = false;
        h.chatSent = false;
        h.fogSent = false;
        if (immediate) {
            spawnEntity(sp, h);
            return;
        }
        h.phase = Phase.OMEN;
        h.total = HerobrineConfig.OMEN_SECONDS.get() + sp.getRandom().nextInt(10);
        h.ticks = h.total;
    }

    public static int getDread(Player p) {
        return p.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getInt(DREAD_KEY);
    }

    public static void setDread(Player p, int value) {
        CompoundTag root = p.getPersistentData();
        CompoundTag persisted = root.getCompound(Player.PERSISTED_NBT_TAG);
        persisted.putInt(DREAD_KEY, value);
        root.put(Player.PERSISTED_NBT_TAG, persisted);
    }

    /** Demonstração imediata de todos os efeitos sensoriais (som, chat, neblina, tremor, flash) - útil para testar. */
    public void demoScare(ServerPlayer sp) {
        ServerLevel level = (ServerLevel) sp.level();
        sp.sendSystemMessage(Component.translatable("multiplayer.player.joined", "Herobrine").withStyle(ChatFormatting.YELLOW));
        Sfx.atPlayer(sp, "ambient.cave", 1.0F, 0.6F);
        HorrorNet.fog(sp, 400);
        footsteps(sp, level, 5);
        schedule(60, () -> {
            Sfx.atPlayer(sp, "entity.warden.heartbeat", 1.2F, 1.0F);
            HorrorNet.shake(sp, 30, 1.0F);
        });
        schedule(80, () -> Sfx.atPlayer(sp, "entity.warden.heartbeat", 1.4F, 1.2F));
        schedule(100, () -> {
            fakeChat(sp);
            Sfx.behind(sp, "entity.enderman.stare", 1.0F, 0.5F, 6.0D);
        });
        schedule(140, () -> {
            Sfx.behind(sp, "entity.enderman.scream", 1.5F, 0.5F, 2.0D);
            HorrorNet.flash(sp, 12);
            HorrorNet.shake(sp, 25, 4.0F);
        });
        schedule(180, () -> sp.sendSystemMessage(Component.translatable("multiplayer.player.left", "Herobrine")
                .withStyle(ChatFormatting.YELLOW)));
    }

    // ------------------------------------------------------------ fases

    private void tickIdle(ServerPlayer sp, Haunt h) {
        if (!HerobrineConfig.ENABLE_HAUNTINGS.get() || sp.isSpectator()) return;
        ServerLevel level = (ServerLevel) sp.level();
        if (level.dimension() != Level.OVERWORLD) return;
        if (HerobrineConfig.ONLY_WHEN_DARK.get() && level.getMaxLocalRawBrightness(sp.blockPosition()) > 6) return;
        if (level.random.nextInt(Math.max(1, HerobrineConfig.AVG_SECONDS.get())) != 0) return;
        startHaunt(sp, null, false);
    }

    private boolean rollCorrupted(ServerPlayer sp) {
        if (!HerobrineConfig.ENABLE_CORRUPTED.get()) return false;
        double chance = Math.min(HerobrineConfig.CORRUPTED_MAX_CHANCE.get(), getDread(sp) * 0.06D);
        return sp.getRandom().nextDouble() < chance;
    }

    private void tickOmen(ServerPlayer sp, Haunt h) {
        ServerLevel level = (ServerLevel) sp.level();
        int remaining = h.ticks;
        double progress = 1.0D - (double) remaining / Math.max(1, h.total);

        if (!h.joinSent) {
            h.joinSent = true;
            if (HerobrineConfig.FAKE_CHAT.get()) {
                sp.sendSystemMessage(Component.translatable("multiplayer.player.joined", "Herobrine")
                        .withStyle(ChatFormatting.YELLOW));
            }
        }
        if (progress > 0.25D && !h.fogSent) {
            h.fogSent = true;
            HorrorNet.fog(sp, remaining * 20 + 200);
        }
        if (sp.getRandom().nextDouble() < 0.30D + progress * 0.35D) {
            randomOmen(sp, level, progress, h);
        }
        if (remaining <= 12) {
            float p = 1.0F - remaining / 12.0F;
            Sfx.atPlayer(sp, "entity.warden.heartbeat", 0.4F + 0.9F * p, 0.9F + 0.4F * p);
        }
        if (remaining <= 3) HorrorNet.shake(sp, 25, 0.6F);

        if (--h.ticks <= 0) spawnEntity(sp, h);
    }

    private void randomOmen(ServerPlayer sp, ServerLevel level, double progress, Haunt h) {
        Random r = new Random();
        switch (sp.getRandom().nextInt(8)) {
            case 0 -> footsteps(sp, level, 4);
            case 1 -> Sfx.atPlayer(sp, "ambient.cave", 1.0F, 0.6F + r.nextFloat() * 0.3F);
            case 2 -> doorOmen(sp, level);
            case 3 -> torchOmen(sp, level);
            case 4 -> Sfx.behind(sp, "entity.creeper.primed", 0.7F, 0.7F, 4.0D);
            case 5 -> Sfx.behind(sp, "entity.warden.nearby_closer", 0.6F, 0.8F, 6.0D);
            case 6 -> {
                if (progress > 0.5D && !h.chatSent && HerobrineConfig.FAKE_CHAT.get()) {
                    h.chatSent = true;
                    fakeChat(sp);
                } else {
                    Sfx.behind(sp, "entity.enderman.stare", 0.5F, 0.6F, 10.0D);
                }
            }
            default -> Sfx.behind(sp, "ambient.soul_sand_valley.mood", 1.0F, 0.7F, 8.0D);
        }
    }

    private void footsteps(ServerPlayer sp, ServerLevel level, int count) {
        for (int i = 0; i < count; i++) {
            final double dist = 7.0D - i * 1.3D;
            schedule(i * 9, () -> {
                if (!sp.isAlive()) return;
                BlockState ground = level.getBlockState(sp.blockPosition().below());
                Sfx.behind(sp, ground.getSoundType().getStepSound(), 0.9F, 0.8F, dist);
            });
        }
    }

    private void doorOmen(ServerPlayer sp, ServerLevel level) {
        List<BlockPos> doors = HerobrineConfig.WORLD_TAMPERING.get()
                ? scan(level, sp.blockPosition(), 10, s -> s.is(BlockTags.WOODEN_DOORS)
                        && s.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER)
                : List.of();
        if (doors.isEmpty()) {
            Sfx.behind(sp, "block.wooden_door.close", 0.9F, 0.8F, 5.0D);
            return;
        }
        BlockPos pos = doors.get(sp.getRandom().nextInt(Math.min(3, doors.size())));
        BlockState state = level.getBlockState(pos);
        ((DoorBlock) state.getBlock()).setOpen(null, level, state, pos, !state.getValue(DoorBlock.OPEN));
    }

    private void torchOmen(ServerPlayer sp, ServerLevel level) {
        List<BlockPos> torches = HerobrineConfig.WORLD_TAMPERING.get()
                ? scan(level, sp.blockPosition(), 10, s -> s.is(Blocks.TORCH) || s.is(Blocks.WALL_TORCH)
                        || s.is(Blocks.SOUL_TORCH) || s.is(Blocks.SOUL_WALL_TORCH))
                : List.of();
        if (torches.isEmpty()) {
            Sfx.atPlayer(sp, "ambient.cave", 1.0F, 0.5F);
            return;
        }
        for (int i = 0; i < Math.min(2, torches.size()); i++) {
            BlockPos pos = torches.get(i);
            level.removeBlock(pos, false);
            level.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5D, pos.getY() + 0.7D, pos.getZ() + 0.5D,
                    8, 0.1D, 0.1D, 0.1D, 0.01D);
            Sfx.play(sp, "block.fire.extinguish", 0.9F, 0.8F, pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
        }
    }

    private static List<BlockPos> scan(ServerLevel level, BlockPos c, int r, Predicate<BlockState> test) {
        List<BlockPos> out = new ArrayList<>();
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        for (int x = -r; x <= r; x++) {
            for (int y = -4; y <= 4; y++) {
                for (int z = -r; z <= r; z++) {
                    m.set(c.getX() + x, c.getY() + y, c.getZ() + z);
                    if (test.test(level.getBlockState(m))) out.add(m.immutable());
                }
            }
        }
        out.sort(Comparator.comparingDouble(p -> c.distSqr(p)));
        return out;
    }

    private void fakeChat(ServerPlayer sp) {
        int n = 1 + sp.getRandom().nextInt(6);
        sp.sendSystemMessage(Component.literal("<Herobrine> ").append(Component.translatable("herobrine.chat." + n)));
    }

    // ------------------------------------------------------------ aparição

    private void spawnEntity(ServerPlayer sp, Haunt h) {
        ServerLevel level = (ServerLevel) sp.level();
        Mob mob = h.corrupted
                ? ModEntities.HEROBRINE_CORRUPTED.get().create(level)
                : ModEntities.HEROBRINE.get().create(level);
        if (mob == null) {
            h.phase = Phase.IDLE;
            return;
        }

        Vec3 look = sp.getLookAngle();
        double base = Math.atan2(look.z, look.x);
        boolean cave = !level.canSeeSky(sp.blockPosition());
        BlockPos spot = null;
        for (int i = 0; i < 14 && spot == null; i++) {
            double rnd = sp.getRandom().nextDouble();
            double ang = h.corrupted
                    ? base + Math.PI + (sp.getRandom().nextDouble() - 0.5D)
                    : base + (rnd < 0.5D ? -1 : 1) * (0.25D + sp.getRandom().nextDouble() * 0.5D);
            double dist = h.corrupted ? 16.0D + rnd * 8.0D : 20.0D + rnd * 10.0D;
            if (cave) dist = Math.min(dist, 12.0D);
            spot = findSpot(level, sp, sp.getX() + Math.cos(ang) * dist, sp.getZ() + Math.sin(ang) * dist);
        }
        if (spot == null) {
            mob.discard();
            h.phase = Phase.COOLDOWN;
            h.ticks = 30;
            return;
        }

        mob.moveTo(spot.getX() + 0.5D, spot.getY(), spot.getZ() + 0.5D, 0.0F, 0.0F);
        mob.lookAt(EntityAnchorArgument.Anchor.EYES, sp.getEyePosition());
        mob.yHeadRot = mob.getYRot();
        mob.yBodyRot = mob.getYRot();
        level.addFreshEntity(mob);

        if (h.corrupted) {
            mob.setTarget(sp);
            Sfx.play(sp, "entity.ender_dragon.growl", 1.5F, 0.4F, mob.getX(), mob.getY() + 2.0D, mob.getZ());
            HorrorNet.shake(sp, 40, 1.5F);
            if (HerobrineConfig.FAKE_CHAT.get()) fakeChat(sp);
        } else {
            Sfx.play(sp, "entity.enderman.stare", 1.0F, 0.5F, mob.getX(), mob.getY() + 1.6D, mob.getZ());
            HorrorNet.shake(sp, 15, 0.8F);
        }
        HerobrineMod.LOGGER.info("Herobrine: apareceu em {} (corrompido={})", spot, h.corrupted);
        h.entity = mob.getUUID();
        h.phase = Phase.APPEAR;
        h.ticks = 0;
    }

    private BlockPos findSpot(ServerLevel level, ServerPlayer sp, double fx, double fz) {
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

    private void tickAppear(ServerPlayer sp, Haunt h) {
        h.ticks++;
        ServerLevel level = (ServerLevel) sp.level();
        Entity ent = h.entity == null ? null : level.getEntity(h.entity);
        boolean gone = ent == null || !ent.isAlive();
        if (!gone && h.ticks > 600) {
            ent.discard();
            gone = true;
        }
        if (gone) endHaunt(sp, h);
    }

    private void endHaunt(ServerPlayer sp, Haunt h) {
        if (HerobrineConfig.FAKE_CHAT.get()) {
            sp.sendSystemMessage(Component.translatable("multiplayer.player.left", "Herobrine")
                    .withStyle(ChatFormatting.YELLOW));
        }
        setDread(sp, Math.min(30, getDread(sp) + 1));
        h.entity = null;
        h.phase = Phase.COOLDOWN;
        h.ticks = HerobrineConfig.COOLDOWN_SECONDS.get();
    }
}
