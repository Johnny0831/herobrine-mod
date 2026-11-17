package com.hollowvigil;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.*;
import java.util.function.Predicate;

/**
 * Cérebro do terror. Por jogador:
 *  - SANIDADE: só cai ao OLHAR para uma entidade ou ao ficar em lugar SEM LUZ; sobe com tocha na mão,
 *    ambiente iluminado, dormindo e itens especiais. Modos Assassino/Anjo ignoram o medo.
 *  - APARIÇÕES: IDLE -> OMEN (presságios) -> APPEAR -> COOLDOWN.
 * Honra baixa = mais aparições e Ceifador; honra alta = presentes; honra 100 = nunca aparece.
 */
public class HorrorDirector {
    public static final HorrorDirector INSTANCE = new HorrorDirector();

    public enum Kind { AUTO, STALKER, REAPER, GIFT }
    private enum Phase { IDLE, OMEN, APPEAR, COOLDOWN }

    private static class Haunt {
        Phase phase = Phase.IDLE;
        int ticks, total;
        UUID entity;
        boolean reaper, gift;
        boolean joinSent, chatSent, fogSent;
    }

    private static class Task {
        int delay;
        final Runnable action;
        Task(int delay, Runnable action) { this.delay = delay; this.action = action; }
    }

    private final Map<UUID, Haunt> haunts = new HashMap<>();
    private final Map<UUID, Long> nextGift = new HashMap<>();
    private final Map<UUID, Integer> madness = new HashMap<>();
    private final Map<UUID, Integer> hunt = new HashMap<>();
    private final Map<UUID, Long> lastForcedLook = new HashMap<>();
    private final List<Task> tasks = new ArrayList<>();
    private final List<Task> pending = new ArrayList<>();

    public void schedule(int delayTicks, Runnable action) {
        pending.add(new Task(delayTicks, action));
    }

    // ============================================================ eventos

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
                    HollowVigilMod.LOGGER.error("Erro em tarefa agendada", ex);
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || e.side.isClient()) return;
        if (!(e.player instanceof ServerPlayer sp)) return;
        if (sp.tickCount % 5 == 0) gaze(sp);
        if (sp.tickCount % 20 != 0) return;

        Haunt h = haunts.computeIfAbsent(sp.getUUID(), k -> new Haunt());
        Modes.tick(sp);
        tickSanity(sp, h);
        if (PlayerStats.isAngel(sp)) {   // anjos: as entidades nunca aparecem
            h.phase = Phase.IDLE;
            return;
        }
        switch (h.phase) {
            case IDLE -> tickIdle(sp, h);
            case OMEN -> tickOmen(sp, h);
            case APPEAR -> tickAppear(sp, h);
            case COOLDOWN -> { if (--h.ticks <= 0) h.phase = Phase.IDLE; }
        }
    }

    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent e) {
        UUID id = e.getEntity().getUUID();
        haunts.remove(id);
        madness.remove(id);
        hunt.remove(id);
    }

    // ============================================================ API pública

    /** @return false se o jogador é um anjo (as entidades nunca aparecem para ele). */
    public boolean startHaunt(ServerPlayer sp, Kind kind, boolean immediate) {
        if (PlayerStats.isAngel(sp)) return false;
        Haunt h = haunts.computeIfAbsent(sp.getUUID(), k -> new Haunt());
        h.reaper = false;
        h.gift = false;
        float honor = PlayerStats.getHonor(sp);
        switch (kind) {
            case STALKER -> { }
            case REAPER -> h.reaper = true;
            case GIFT -> h.gift = true;
            default -> {
                long now = sp.level().getGameTime();
                if (PlayerStats.isKiller(sp)) {
                    h.reaper = HollowConfig.ENABLE_REAPER.get() && sp.getRandom().nextDouble() < 0.45D;
                } else if (HollowConfig.ENABLE_GIFTS.get() && PlayerStats.isHonored(honor)
                        && nextGift.getOrDefault(sp.getUUID(), 0L) <= now && sp.getRandom().nextDouble() < 0.45D) {
                    h.gift = true;
                } else {
                    h.reaper = rollReaper(sp, honor);
                }
            }
        }
        h.joinSent = false;
        h.chatSent = false;
        h.fogSent = false;
        HollowVigilMod.LOGGER.info("Hollow Vigil: evento para {} (reaper={}, gift={}, imediato={})",
                sp.getName().getString(), h.reaper, h.gift, immediate);
        if (immediate) {
            spawnEntity(sp, h);
            return true;
        }
        h.phase = Phase.OMEN;
        h.total = h.gift ? 8 : HollowConfig.OMEN_SECONDS.get() + sp.getRandom().nextInt(10);
        h.ticks = h.total;
        return true;
    }

    public void scheduleIntro(ServerPlayer sp) {
        if (!HollowConfig.INTRO_EVENT.get() || PlayerStats.introDone(sp)) return;
        schedule(20 * 100, () -> {
            if (sp.getServer() == null || sp.getServer().getPlayerList().getPlayer(sp.getUUID()) != sp) return;
            PlayerStats.markIntroDone(sp);
            startHaunt(sp, Kind.STALKER, false);
        });
    }

    private boolean rollReaper(ServerPlayer sp, float honor) {
        if (!HollowConfig.ENABLE_REAPER.get() || PlayerStats.isHonored(honor)) return false;
        double chance = Math.min(HollowConfig.REAPER_MAX_CHANCE.get(), PlayerStats.getDread(sp) * 0.06D);
        if (PlayerStats.getSanity(sp) < 30.0F) chance += 0.15D;
        if (PlayerStats.isSinner(honor)) chance = Math.max(chance, HollowConfig.SINNER_REAPER_CHANCE.get());
        return sp.getRandom().nextDouble() < chance;
    }

    // ============================================================ SANIDADE

    private static boolean isLight(ItemStack s) {
        return s.is(Items.TORCH) || s.is(Items.SOUL_TORCH) || s.is(Items.LANTERN) || s.is(Items.SOUL_LANTERN)
                || s.is(ModItems.HOLLOW_LANTERN.get());
    }

    private boolean holdingLight(ServerPlayer sp) {
        return isLight(sp.getMainHandItem()) || isLight(sp.getOffhandItem());
    }

    private boolean holdingHollowLantern(ServerPlayer sp) {
        return sp.getMainHandItem().is(ModItems.HOLLOW_LANTERN.get()) || sp.getOffhandItem().is(ModItems.HOLLOW_LANTERN.get());
    }

    private float drainMult(ServerPlayer sp) {
        float m = HollowConfig.SANITY_DRAIN_MULT.get().floatValue();
        float honor = PlayerStats.getHonor(sp);
        if (PlayerStats.isSinner(honor)) m *= 1.5F;
        else if (PlayerStats.isHonored(honor)) m *= 0.7F;
        if (PlayerStats.hasCharm(sp)) m *= 0.5F;
        return m;
    }

    /** Olhar para uma entidade corrói a sanidade (amostra a cada 0,25 s). */
    private void gaze(ServerPlayer sp) {
        if (!HollowConfig.SANITY_ENABLED.get() || sp.isSpectator() || PlayerStats.isKiller(sp) || PlayerStats.isAngel(sp)) return;
        ServerLevel level = (ServerLevel) sp.level();
        List<Mob> list = level.getEntitiesOfClass(Mob.class, sp.getBoundingBox().inflate(64.0D),
                m -> m.isAlive() && (m instanceof HollowEntity || m instanceof HollowReaperEntity || m instanceof HallucinationEntity));
        for (Mob m : list) {
            if (m instanceof HollowEntity he && he.isBenevolent()) continue;   // o presente é seguro de olhar
            if (LookUtil.isLookingAt(sp, m)) {
                float drain = m instanceof HollowReaperEntity ? 0.70F : 0.45F;
                PlayerStats.addSanity(sp, -drain * drainMult(sp));
                return;
            }
        }
    }

    private void tickSanity(ServerPlayer sp, Haunt h) {
        if (!HollowConfig.SANITY_ENABLED.get() || sp.isSpectator()) return;
        ServerLevel level = (ServerLevel) sp.level();
        float honor = PlayerStats.getHonor(sp);

        if (PlayerStats.isKiller(sp) || PlayerStats.isAngel(sp)) {   // sem medo
            PlayerStats.setSanity(sp, 100.0F);
            PlayerStats.sync(sp);
            if (PlayerStats.isKiller(sp)) killerHunt(sp, level);
            return;
        }

        int light = level.getMaxLocalRawBrightness(sp.blockPosition());
        boolean torch = holdingLight(sp);
        float delta;
        if (light <= 4) delta = torch ? 0.35F : -0.22F;            // sem fonte de luz: perde; com tocha na mão: ganha
        else if (light <= 8) delta = torch ? 0.30F : 0.0F;
        else delta = 0.30F + (torch ? 0.15F : 0.0F);              // ambiente iluminado: ganha
        if (holdingHollowLantern(sp)) delta += 0.60F;
        if (sp.isSleeping()) delta += 4.0F;

        if (delta < 0.0F) delta *= drainMult(sp);
        else {
            if (PlayerStats.hasCharm(sp)) delta += 0.10F;
            if (PlayerStats.isHonored(honor)) delta *= 1.3F;
        }
        float s = Math.max(0.0F, Math.min(100.0F, PlayerStats.getSanity(sp) + delta));
        PlayerStats.setSanity(sp, s);
        PlayerStats.sync(sp);
        applySanityEffects(sp, level, s, honor);
    }

    private void applySanityEffects(ServerPlayer sp, ServerLevel level, float s, float honor) {
        RandomSource r = sp.getRandom();
        boolean possession = HollowConfig.ENABLE_POSSESSION.get();

        if (s <= 70.0F && r.nextDouble() < 0.025D + (70.0F - s) * 0.0015D) ambientHallucination(sp, level);
        if (s <= 50.0F) {
            if (HollowConfig.FAKE_CHAT.get() && r.nextInt(75) == 0) ChatLines.say(sp);
            if (r.nextInt(20) == 0) particles(sp, level, ParticleTypes.ASH, 25);
        }
        if (s <= 35.0F && sp.tickCount % 40 == 0) {
            Sfx.atPlayer(sp, s <= 20.0F ? ModSounds.HEARTBEAT_FAST.get() : ModSounds.HEARTBEAT.get(),
                    0.6F + (35.0F - s) / 35.0F, 1.0F);
        }
        if (s <= 30.0F) {
            if (r.nextInt(60) == 0) HorrorNet.shake(sp, 20, 0.8F);
            if (r.nextInt(70) == 0) HorrorNet.fog(sp, 240);
            if (r.nextInt(45) == 0) spawnSilhouette(sp, level);
            if (r.nextInt(40) == 0) Sfx.around(sp, ModSounds.WHISPER.get(), 0.9F, 0.9F, 3.0D);
            if (r.nextInt(30) == 0) particles(sp, level, ParticleTypes.SOUL, 8);
        }
        if (s <= 20.0F && possession && r.nextInt(40) == 0) possess(level, sp, 2, 0, 40);
        if (s <= 12.0F && HollowConfig.FORCED_LOOK.get() && r.nextInt(90) == 0) forceLookAtNearest(sp, level);
        if (s <= 10.0F) {
            if (possession && !PlayerStats.isHonored(honor) && r.nextInt(30) == 0) possess(level, sp, 2, 1, 30);
            if (r.nextInt(25) == 0) PlayerStats.fear(sp, MobEffects.DARKNESS, 100);
            if (r.nextInt(50) == 0) PlayerStats.fear(sp, MobEffects.CONFUSION, 120);
        }
        if (s <= 0.0F) madness(sp, level, honor);
        else madness.remove(sp.getUUID());
    }

    private void particles(ServerPlayer sp, ServerLevel level, net.minecraft.core.particles.SimpleParticleType type, int count) {
        if (!HollowConfig.PLAYER_PARTICLES.get()) return;
        level.sendParticles(sp, type, true, sp.getX(), sp.getY() + 1.0D, sp.getZ(), count, 4.0D, 2.0D, 4.0D, 0.0D);
    }

    private void forceLookAtNearest(ServerPlayer sp, ServerLevel level) {
        long now = level.getGameTime();
        if (now - lastForcedLook.getOrDefault(sp.getUUID(), -9999L) < 1800L) return;
        List<Mob> list = level.getEntitiesOfClass(Mob.class, sp.getBoundingBox().inflate(40.0D),
                m -> m.isAlive() && (m instanceof HollowEntity || m instanceof HollowReaperEntity || m instanceof HallucinationEntity));
        if (list.isEmpty()) return;
        lastForcedLook.put(sp.getUUID(), now);
        LookUtil.forceLook(sp, list.get(0));
        Sfx.atPlayer(sp, ModSounds.STINGER.get(), 0.9F, 1.1F);
    }

    /** Sanidade zero: alucinações REAIS surgem e atacam. */
    private void madness(ServerPlayer sp, ServerLevel level, float honor) {
        int t = madness.merge(sp.getUUID(), 1, Integer::sum);
        if (t == 1) {
            sp.sendSystemMessage(Component.translatable("hollow.madness.start").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
            HorrorNet.flash(sp, 14);
            HorrorNet.shake(sp, 50, 3.0F);
            Sfx.atPlayer(sp, ModSounds.SCREAM_FAR.get(), 1.0F, 0.7F);
        }
        PlayerStats.fear(sp, MobEffects.DARKNESS, 120);
        if (t % 18 == 0) {
            HorrorNet.flash(sp, 10);
            HorrorNet.shake(sp, 30, 2.5F);
            Sfx.behind(sp, ModSounds.JUMPSCARE.get(), 1.0F, 0.9F, 4.0D);
        }
        if (t % 3 == 0) {
            int max = 3 + (PlayerStats.isSinner(honor) ? 2 : 0);
            List<HallucinationEntity> existing = level.getEntitiesOfClass(HallucinationEntity.class,
                    sp.getBoundingBox().inflate(48.0D), HallucinationEntity::isHostile);
            if (existing.size() < max) {
                spawnHallucination(sp, level, true, 1200, 7.0D, 13.0D);
                Sfx.behind(sp, ModSounds.BREATH.get(), 1.2F, 0.7F, 5.0D);
            }
        }
        if (t % 30 == 0 && !PlayerStats.isHonored(honor)) {
            Haunt h = haunts.computeIfAbsent(sp.getUUID(), k -> new Haunt());
            if ((h.phase == Phase.IDLE || h.phase == Phase.COOLDOWN) && sp.getRandom().nextDouble() < 0.5D) {
                startHaunt(sp, Kind.REAPER, false);
            }
        }
    }

    /** Assassino: a caça nunca acaba - alucinações hostis surgem para serem mortas. */
    private void killerHunt(ServerPlayer sp, ServerLevel level) {
        int t = hunt.merge(sp.getUUID(), 1, Integer::sum);
        if (t % 20 != 0) return;
        List<HallucinationEntity> existing = level.getEntitiesOfClass(HallucinationEntity.class,
                sp.getBoundingBox().inflate(48.0D), HallucinationEntity::isHostile);
        if (existing.size() < 3) spawnHallucination(sp, level, true, 1200, 9.0D, 16.0D);
    }

    private void spawnHallucination(ServerPlayer sp, ServerLevel level, boolean hostile, int lifetime, double minD, double maxD) {
        BlockPos spot = Spots.around(level, sp, minD, maxD, sp.getRandom().nextDouble() * Math.PI * 2.0D, Math.PI);
        if (spot == null) return;
        HallucinationEntity ent = ModEntities.HALLUCINATION.get().create(level);
        if (ent == null) return;
        ent.moveTo(spot.getX() + 0.5D, spot.getY(), spot.getZ() + 0.5D, 0.0F, 0.0F);
        ent.lookAt(EntityAnchorArgument.Anchor.EYES, sp.getEyePosition());
        ent.yHeadRot = ent.getYRot();
        ent.yBodyRot = ent.getYRot();
        ent.setup(sp.getUUID(), hostile, lifetime);
        level.addFreshEntity(ent);
        if (hostile) ent.setTarget(sp);
    }

    private void spawnSilhouette(ServerPlayer sp, ServerLevel level) {
        List<HallucinationEntity> existing = level.getEntitiesOfClass(HallucinationEntity.class, sp.getBoundingBox().inflate(60.0D));
        if (!existing.isEmpty()) return;
        BlockPos spot = Spots.around(level, sp, 14.0D, 26.0D, Spots.lookAngle(sp), 0.8D);
        if (spot == null) return;
        HallucinationEntity ent = ModEntities.HALLUCINATION.get().create(level);
        if (ent == null) return;
        ent.moveTo(spot.getX() + 0.5D, spot.getY(), spot.getZ() + 0.5D, 0.0F, 0.0F);
        ent.lookAt(EntityAnchorArgument.Anchor.EYES, sp.getEyePosition());
        ent.yHeadRot = ent.getYRot();
        ent.yBodyRot = ent.getYRot();
        ent.setup(sp.getUUID(), false, 600);
        level.addFreshEntity(ent);
    }

    // ============================================================ POSSESSÃO

    /** Possui até {@code count} animais próximos. mode 0 = observar/aproximar, 1 = investida. */
    public int possess(ServerLevel level, ServerPlayer sp, int count, int mode, int seconds) {
        if (!HollowConfig.ENABLE_POSSESSION.get() || PlayerStats.isAngel(sp)) return 0;
        List<Animal> animals = new ArrayList<>(level.getEntitiesOfClass(Animal.class, sp.getBoundingBox().inflate(28.0D),
                a -> !(a instanceof TamableAnimal t && t.isTame()) && !a.hasEffect(ModEffects.POSSESSED.get())));
        Collections.shuffle(animals, new Random());
        int n = 0;
        for (Animal a : animals) {
            if (n >= count) break;
            PossessedGoal.attach(a);
            a.getPersistentData().putInt("hv_mode", mode);
            a.addEffect(new MobEffectInstance(ModEffects.POSSESSED.get(), seconds * 20, 0, false, false));
            n++;
        }
        return n;
    }

    // ============================================================ APARIÇÕES

    private void tickIdle(ServerPlayer sp, Haunt h) {
        if (!HollowConfig.ENABLE_HAUNTINGS.get() || sp.isSpectator()) return;
        ServerLevel level = (ServerLevel) sp.level();
        boolean killer = PlayerStats.isKiller(sp);
        if (!killer) {
            if (level.dimension() != Level.OVERWORLD) return;
            if (HollowConfig.ONLY_WHEN_DARK.get() && level.getMaxLocalRawBrightness(sp.blockPosition()) > 6) return;
        }
        float honor = PlayerStats.getHonor(sp);
        double avg = HollowConfig.AVG_SECONDS.get();
        if (killer) avg /= 4.0D;
        else if (PlayerStats.isSinner(honor)) avg /= 3.0D;
        else if (PlayerStats.isHonored(honor)) avg *= 1.3D;
        if (PlayerStats.getSanity(sp) < 40.0F) avg *= 0.6D;
        if (level.random.nextInt(Math.max(1, (int) avg)) != 0) return;
        startHaunt(sp, Kind.AUTO, false);
    }

    private void tickOmen(ServerPlayer sp, Haunt h) {
        ServerLevel level = (ServerLevel) sp.level();
        int remaining = h.ticks;
        double progress = 1.0D - (double) remaining / Math.max(1, h.total);
        boolean sinner = PlayerStats.isSinner(sp);

        if (!h.joinSent) {
            h.joinSent = true;
            if (HollowConfig.FAKE_CHAT.get()) {
                sp.sendSystemMessage(Component.translatable("multiplayer.player.joined",
                        Component.translatable("entity.hollowvigil.hollow")).withStyle(ChatFormatting.YELLOW));
            }
        }

        if (h.gift) {
            if (remaining % 2 == 0) {
                Sfx.behind(sp, ModSounds.CHIME.get(), 0.7F, 0.8F + sp.getRandom().nextFloat() * 0.3F, 5.0D);
            }
            if (--h.ticks <= 0) spawnEntity(sp, h);
            return;
        }

        if (progress > 0.25D && !h.fogSent) {
            h.fogSent = true;
            HorrorNet.fog(sp, remaining * 20 + 200);
            Sfx.atPlayer(sp, ModSounds.DRONE.get(), 0.8F, 1.0F);
        }
        double chance = 0.32D + progress * 0.35D + (sinner ? 0.15D : 0.0D);
        if (sp.getRandom().nextDouble() < chance) randomOmen(sp, level, progress, h, sinner);
        if (remaining <= 12) {
            float p = 1.0F - remaining / 12.0F;
            Sfx.atPlayer(sp, remaining <= 5 ? ModSounds.HEARTBEAT_FAST.get() : ModSounds.HEARTBEAT.get(), 0.5F + 0.9F * p, 1.0F);
        }
        if (remaining <= 3) HorrorNet.shake(sp, 25, 0.6F);
        if (--h.ticks <= 0) spawnEntity(sp, h);
    }

    private void randomOmen(ServerPlayer sp, ServerLevel level, double progress, Haunt h, boolean sinner) {
        if (sinner && sp.getRandom().nextDouble() < 0.45D) {
            penance(sp, level);
            return;
        }
        switch (sp.getRandom().nextInt(16)) {
            case 0 -> footsteps(sp, level, 4);
            case 1 -> { Sfx.atPlayer(sp, "minecraft:ambient.cave", 1.0F, 0.6F); Sfx.atPlayer(sp, ModSounds.DRONE.get(), 0.7F, 0.9F); }
            case 2 -> doorOmen(sp, level);
            case 3 -> torchOmen(sp, level);
            case 4 -> Sfx.behind(sp, "minecraft:entity.creeper.primed", 0.7F, 0.7F, 4.0D);
            case 5 -> Sfx.behind(sp, ModSounds.BREATH.get(), 1.0F, 0.9F, 2.5D);
            case 6 -> {
                if (progress > 0.5D && !h.chatSent && HollowConfig.FAKE_CHAT.get()) {
                    h.chatSent = true;
                    ChatLines.say(sp);
                } else {
                    Sfx.behind(sp, "minecraft:entity.enderman.stare", 0.5F, 0.6F, 10.0D);
                }
            }
            case 7 -> Sfx.around(sp, ModSounds.WHISPER.get(), 1.0F, 0.9F + sp.getRandom().nextFloat() * 0.2F, 2.5D);
            case 8 -> knocks(sp);
            case 9 -> mimic(sp);
            case 10 -> possess(level, sp, 2, 0, 35);
            case 11 -> spawnSilhouette(sp, level);
            case 12 -> Sfx.around(sp, ModSounds.SCRAPE.get(), 0.9F, 1.0F, 9.0D);
            case 13 -> Sfx.around(sp, ModSounds.STATIC_BURST.get(), 0.5F, 1.0F, 3.0D);
            case 14 -> { particles(sp, level, ParticleTypes.ASH, 40); Sfx.atPlayer(sp, ModSounds.DRONE.get(), 0.6F, 0.8F); }
            default -> {
                if (HollowConfig.FAKE_CHAT.get()) ChatLines.say(sp);
                else Sfx.around(sp, ModSounds.WHISPER.get(), 1.0F, 1.0F, 3.0D);
            }
        }
    }

    /** Eventos exclusivos de pecadores: as vítimas cobram. */
    private void penance(ServerPlayer sp, ServerLevel level) {
        int animals = PlayerStats.getAnimalsKilled(sp);
        int villagers = PlayerStats.getVillagersKilled(sp);
        switch (sp.getRandom().nextInt(5)) {
            case 0 -> {
                String[] cries = {"minecraft:entity.cow.death", "minecraft:entity.pig.death", "minecraft:entity.sheep.death",
                        "minecraft:entity.chicken.death", "minecraft:entity.wolf.death"};
                for (int i = 0; i < 3; i++) {
                    final String id = cries[sp.getRandom().nextInt(cries.length)];
                    final double dist = 9.0D - i * 2.0D;
                    schedule(i * 12, () -> { if (sp.isAlive()) Sfx.behind(sp, id, 1.3F, 0.5F, dist); });
                }
            }
            case 1 -> {
                Sfx.behind(sp, "minecraft:entity.villager.death", 1.3F, 0.5F, 7.0D);
                schedule(14, () -> { if (sp.isAlive()) Sfx.behind(sp, "minecraft:entity.villager.no", 1.3F, 0.5F, 4.0D); });
            }
            case 2 -> {
                if (!HollowConfig.FAKE_CHAT.get()) return;
                if (villagers > 0 && sp.getRandom().nextBoolean()) ChatLines.hollowSays(sp, "sin_villagers", villagers);
                else if (animals > 0) ChatLines.hollowSays(sp, "sin_animals", animals);
                else ChatLines.hollowSays(sp, "sin_generic");
            }
            case 3 -> Sfx.around(sp, ModSounds.SCREAM_FAR.get(), 0.9F, 0.8F, 18.0D);
            default -> {
                LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
                if (bolt != null) {
                    Vec3Helper.place(bolt, sp, 40.0D);
                    bolt.setVisualOnly(true);
                    level.addFreshEntity(bolt);
                }
            }
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

    private void knocks(ServerPlayer sp) {
        for (int i = 0; i < 3; i++) {
            schedule(i * 14, () -> { if (sp.isAlive()) Sfx.behind(sp, ModSounds.KNOCK.get(), 1.2F, 0.9F, 3.0D); });
        }
    }

    private void mimic(ServerPlayer sp) {
        String[] ids = {"minecraft:entity.zombie.ambient", "minecraft:entity.skeleton.ambient", "minecraft:entity.spider.ambient",
                "minecraft:entity.zombie_villager.ambient", "minecraft:entity.creeper.hurt", "minecraft:entity.enderman.ambient"};
        Sfx.behind(sp, ids[sp.getRandom().nextInt(ids.length)], 1.0F, 0.6F, 6.0D + sp.getRandom().nextInt(6));
    }

    private void ambientHallucination(ServerPlayer sp, ServerLevel level) {
        switch (sp.getRandom().nextInt(10)) {
            case 0 -> footsteps(sp, level, 3);
            case 1 -> Sfx.behind(sp, "minecraft:entity.creeper.primed", 0.8F, 0.8F, 3.0D);
            case 2 -> knocks(sp);
            case 3 -> mimic(sp);
            case 4 -> Sfx.behind(sp, "minecraft:block.chest.open", 0.9F, 0.7F, 4.0D);
            case 5 -> Sfx.atPlayer(sp, "minecraft:entity.player.hurt", 0.7F, 0.9F);
            case 6 -> Sfx.around(sp, ModSounds.WHISPER.get(), 0.9F, 1.0F, 3.0D);
            case 7 -> Sfx.behind(sp, ModSounds.BREATH.get(), 0.9F, 1.0F, 2.0D);
            case 8 -> Sfx.around(sp, ModSounds.CREAK.get(), 0.9F, 1.0F, 6.0D);
            default -> Sfx.atPlayer(sp, ModSounds.DRONE.get(), 0.7F, 1.0F);
        }
    }

    private void doorOmen(ServerPlayer sp, ServerLevel level) {
        List<BlockPos> doors = HollowConfig.WORLD_TAMPERING.get()
                ? scan(level, sp.blockPosition(), 10, s -> s.is(BlockTags.WOODEN_DOORS)
                        && s.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER)
                : List.of();
        if (doors.isEmpty()) {
            Sfx.around(sp, ModSounds.CREAK.get(), 0.9F, 1.0F, 5.0D);
            return;
        }
        BlockPos pos = doors.get(sp.getRandom().nextInt(Math.min(3, doors.size())));
        BlockState state = level.getBlockState(pos);
        ((DoorBlock) state.getBlock()).setOpen(null, level, state, pos, !state.getValue(DoorBlock.OPEN));
        Sfx.play(sp, ModSounds.CREAK.get(), 0.8F, 1.0F, pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
    }

    private void torchOmen(ServerPlayer sp, ServerLevel level) {
        List<BlockPos> torches = HollowConfig.WORLD_TAMPERING.get()
                ? scan(level, sp.blockPosition(), 10, s -> s.is(Blocks.TORCH) || s.is(Blocks.WALL_TORCH)
                        || s.is(Blocks.SOUL_TORCH) || s.is(Blocks.SOUL_WALL_TORCH))
                : List.of();
        if (torches.isEmpty()) {
            Sfx.atPlayer(sp, ModSounds.DRONE.get(), 0.7F, 0.9F);
            return;
        }
        for (int i = 0; i < Math.min(2, torches.size()); i++) {
            BlockPos pos = torches.get(i);
            level.removeBlock(pos, false);
            level.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5D, pos.getY() + 0.7D, pos.getZ() + 0.5D,
                    8, 0.1D, 0.1D, 0.1D, 0.01D);
            Sfx.play(sp, "minecraft:block.fire.extinguish", 0.9F, 0.8F, pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
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

    /** Demonstração imediata dos efeitos sensoriais (para testes). */
    public void demoScare(ServerPlayer sp) {
        ServerLevel level = (ServerLevel) sp.level();
        sp.sendSystemMessage(Component.translatable("multiplayer.player.joined",
                Component.translatable("entity.hollowvigil.hollow")).withStyle(ChatFormatting.YELLOW));
        Sfx.atPlayer(sp, ModSounds.DRONE.get(), 1.0F, 1.0F);
        HorrorNet.fog(sp, 400);
        footsteps(sp, level, 5);
        particles(sp, level, ParticleTypes.ASH, 80);
        schedule(50, () -> Sfx.around(sp, ModSounds.WHISPER.get(), 1.0F, 1.0F, 2.5D));
        schedule(70, () -> { Sfx.atPlayer(sp, ModSounds.HEARTBEAT.get(), 1.2F, 1.0F); HorrorNet.shake(sp, 30, 1.0F); });
        schedule(100, () -> { ChatLines.say(sp); Sfx.behind(sp, ModSounds.BREATH.get(), 1.0F, 0.9F, 2.0D); });
        schedule(125, () -> ChatLines.advancement(sp));
        schedule(150, () -> ChatLines.ghost(sp));
        schedule(170, () -> {
            Sfx.atPlayer(sp, ModSounds.HEARTBEAT_FAST.get(), 1.4F, 1.0F);
            Sfx.behind(sp, ModSounds.JUMPSCARE.get(), 1.5F, 1.0F, 2.0D);
            HorrorNet.flash(sp, 12);
            HorrorNet.shake(sp, 25, 4.0F);
        });
        schedule(230, () -> sp.sendSystemMessage(Component.translatable("multiplayer.player.left",
                Component.translatable("entity.hollowvigil.hollow")).withStyle(ChatFormatting.YELLOW)));
    }

    private void spawnEntity(ServerPlayer sp, Haunt h) {
        ServerLevel level = (ServerLevel) sp.level();
        double look = Spots.lookAngle(sp);
        Mob mob;
        BlockPos spot;
        if (h.reaper) {
            mob = ModEntities.HOLLOW_REAPER.get().create(level);
            spot = Spots.around(level, sp, 16.0D, 24.0D, look + Math.PI, 0.5D);
        } else if (h.gift) {
            mob = ModEntities.HOLLOW.get().create(level);
            spot = Spots.around(level, sp, 8.0D, 12.0D, look, 0.35D);
        } else {
            mob = ModEntities.HOLLOW.get().create(level);
            double sign = sp.getRandom().nextBoolean() ? 1.0D : -1.0D;
            spot = Spots.around(level, sp, 20.0D, 30.0D, look + sign * 0.55D, 0.25D);
        }
        if (mob == null || spot == null) {
            if (mob != null) mob.discard();
            h.phase = Phase.COOLDOWN;
            h.ticks = 30;
            return;
        }

        mob.moveTo(spot.getX() + 0.5D, spot.getY(), spot.getZ() + 0.5D, 0.0F, 0.0F);
        mob.lookAt(EntityAnchorArgument.Anchor.EYES, sp.getEyePosition());
        mob.yHeadRot = mob.getYRot();
        mob.yBodyRot = mob.getYRot();
        if (mob instanceof HollowEntity he && h.gift) he.setBenevolent(true);
        level.addFreshEntity(mob);
        HollowVigilMod.LOGGER.info("Hollow Vigil: apareceu em {} (reaper={}, gift={})", spot, h.reaper, h.gift);

        boolean sinner = PlayerStats.isSinner(sp);
        boolean killer = PlayerStats.isKiller(sp);
        if (h.reaper) {
            mob.setTarget(sp);
            if (HollowConfig.LIGHTNING_ON_REAPER.get()) {
                LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
                if (bolt != null) {
                    bolt.moveTo(mob.getX(), mob.getY(), mob.getZ());
                    bolt.setVisualOnly(true);
                    level.addFreshEntity(bolt);
                }
            }
            if (HollowConfig.STORM_ON_REAPER.get()) level.setWeatherParameters(0, 6000, true, true);
            Sfx.play(sp, ModSounds.REAPER_ROAR.get(), 1.6F, 0.9F, mob.getX(), mob.getY() + 2.0D, mob.getZ());
            Sfx.atPlayer(sp, ModSounds.STINGER.get(), 1.2F, 0.9F);
            Sfx.atPlayer(sp, ModSounds.TOLL.get(), 1.0F, 0.8F);
            HorrorNet.shake(sp, 50, 2.5F);
            HorrorNet.flash(sp, 6);
            if (HollowConfig.FORCED_LOOK.get() && !killer) LookUtil.forceLook(sp, mob);
            if (HollowConfig.FAKE_CHAT.get()) ChatLines.hollowSays(sp, "reaper");
            if (!killer) possess(level, sp, sinner ? 5 : 2, 1, 45);
        } else if (h.gift) {
            Sfx.atPlayer(sp, ModSounds.CHIME.get(), 1.0F, 0.7F);
            Sfx.atPlayer(sp, ModSounds.ANGEL_CHOIR.get(), 0.6F, 1.0F);
            if (HollowConfig.FAKE_CHAT.get()) ChatLines.hollowSays(sp, "honored");
        } else {
            Sfx.play(sp, ModSounds.STINGER.get(), 1.0F, 1.0F, mob.getX(), mob.getY() + 1.6D, mob.getZ());
            Sfx.atPlayer(sp, ModSounds.DRONE.get(), 0.8F, 0.9F);
            HorrorNet.shake(sp, 15, 0.8F);
            if (!killer && sp.getRandom().nextDouble() < 0.5D) possess(level, sp, 2, 0, 40);
        }
        h.entity = mob.getUUID();
        h.phase = Phase.APPEAR;
        h.ticks = 0;
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
        if (gone) endHaunt(sp, h, level);
    }

    private void endHaunt(ServerPlayer sp, Haunt h, ServerLevel level) {
        if (HollowConfig.FAKE_CHAT.get() && !h.gift) {
            sp.sendSystemMessage(Component.translatable("multiplayer.player.left",
                    Component.translatable("entity.hollowvigil.hollow")).withStyle(ChatFormatting.YELLOW));
        }
        if (h.gift) {
            nextGift.put(sp.getUUID(), level.getGameTime() + 20L * 60L * 10L);
        } else {
            PlayerStats.setDread(sp, PlayerStats.getDread(sp) + 1);
        }
        h.entity = null;
        h.phase = Phase.COOLDOWN;
        h.ticks = HollowConfig.COOLDOWN_SECONDS.get();
    }
}
