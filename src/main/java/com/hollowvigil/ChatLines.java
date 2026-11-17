package com.hollowvigil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;

/** GERADO por tools/gen_text.py (contagens). Falas de O Oco, falas fantasmas e conquistas falsas. */
public final class ChatLines {
    private ChatLines() {}

    private static final Map<String, Integer> COUNTS = new HashMap<>();
    static {
        COUNTS.put("general", 30);
        COUNTS.put("dark", 12);
        COUNTS.put("low", 15);
        COUNTS.put("sin_animals", 6);
        COUNTS.put("sin_villagers", 6);
        COUNTS.put("sin_generic", 8);
        COUNTS.put("honored", 10);
        COUNTS.put("gift", 8);
        COUNTS.put("reaper", 10);
        COUNTS.put("killer", 10);
        COUNTS.put("angel", 8);
        COUNTS.put("verdict", 4);
        COUNTS.put("ghost", 8);
    }

    private static Component name() {
        return Component.translatable("entity.hollowvigil.hollow");
    }

    /** Linha aleatória da categoria; %1$s = nome do jogador, %2$s... = argumentos extras. */
    public static Component line(String cat, ServerPlayer sp, Object... extra) {
        int n = COUNTS.getOrDefault(cat, 1);
        Object[] args = new Object[extra.length + 1];
        args[0] = sp.getName();
        System.arraycopy(extra, 0, args, 1, extra.length);
        return Component.translatable("hollow.chat." + cat + "." + (1 + sp.getRandom().nextInt(n)), args);
    }

    public static void hollowSays(ServerPlayer sp, String cat, Object... extra) {
        sp.sendSystemMessage(Component.literal("<").append(name()).append(Component.literal("> ")).append(line(cat, sp, extra)));
    }

    /** Um "jogador" que não existe fala no chat. */
    public static void ghost(ServerPlayer sp) {
        int who = 1 + sp.getRandom().nextInt(4);
        sp.sendSystemMessage(Component.literal("<").append(Component.translatable("hollow.ghost.name." + who))
                .append(Component.literal("> ")).append(line("ghost", sp)));
    }

    /** Conquista falsa, no formato do jogo. */
    public static void advancement(ServerPlayer sp) {
        int n = 1 + sp.getRandom().nextInt(10);
        sp.sendSystemMessage(Component.translatable("chat.type.advancement.task", sp.getDisplayName(),
                Component.translatable("hollow.adv." + n).withStyle(ChatFormatting.GREEN)));
    }

    /** Escolhe sozinho a fala certa para o estado do jogador (honra, sanidade, escuro, modo). */
    public static void say(ServerPlayer sp) {
        double r = sp.getRandom().nextDouble();
        if (r < 0.07D) { advancement(sp); return; }
        if (r < 0.16D) { ghost(sp); return; }
        float sanity = PlayerStats.getSanity(sp);
        int light = ((ServerLevel) sp.level()).getMaxLocalRawBrightness(sp.blockPosition());
        double q = sp.getRandom().nextDouble();
        if (PlayerStats.isKiller(sp)) { hollowSays(sp, "killer"); return; }
        if (PlayerStats.isSinner(sp) && q < 0.6D) {
            int animals = PlayerStats.getAnimalsKilled(sp);
            int villagers = PlayerStats.getVillagersKilled(sp);
            if (villagers > 0 && q < 0.3D) hollowSays(sp, "sin_villagers", villagers);
            else if (animals > 0) hollowSays(sp, "sin_animals", animals);
            else hollowSays(sp, "sin_generic");
            return;
        }
        if (sanity < 40.0F && q < 0.5D) { hollowSays(sp, "low"); return; }
        if (light <= 4 && q < 0.4D) { hollowSays(sp, "dark"); return; }
        if (PlayerStats.isHonored(sp) && q < 0.5D) { hollowSays(sp, "honored"); return; }
        hollowSays(sp, "general");
    }
}
