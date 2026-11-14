package com.hollowvigil;

import net.minecraftforge.common.ForgeConfigSpec;

public class HollowConfig {
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final ForgeConfigSpec CLIENT_SPEC;

    public static final ForgeConfigSpec.BooleanValue ENABLE_HAUNTINGS, ONLY_WHEN_DARK, FAKE_CHAT, WORLD_TAMPERING,
            JUMPSCARES, ENABLE_REAPER, SINNER_ONE_HIT, SANITY_ENABLED, ENABLE_POSSESSION, ENABLE_GIFTS,
            STORM_ON_REAPER, LIGHTNING_ON_REAPER, WELCOME_MESSAGE, INTRO_EVENT;
    public static final ForgeConfigSpec.IntValue AVG_SECONDS, OMEN_SECONDS, COOLDOWN_SECONDS, REAPER_LIFETIME_SECONDS;
    public static final ForgeConfigSpec.DoubleValue JUMPSCARE_CHANCE, REAPER_MAX_CHANCE, SINNER_REAPER_CHANCE, SANITY_DRAIN_MULT;

    public static final ForgeConfigSpec.BooleanValue SCREEN_SHAKE, FLASHES, FOG, SHOW_HUD, VIGNETTE;

    static {
        ForgeConfigSpec.Builder c = new ForgeConfigSpec.Builder();
        c.push("hauntings");
        ENABLE_HAUNTINGS = c.comment("Liga/desliga as aparições automáticas.").define("enabled", true);
        ONLY_WHEN_DARK = c.comment("Aparições só em locais escuros (noite ao ar livre, cavernas).").define("onlyWhenDark", true);
        AVG_SECONDS = c.comment("Tempo médio (s) entre aparições. Pecadores recebem 3x mais.")
                .defineInRange("averageSecondsBetweenHauntings", 90, 20, 100000);
        OMEN_SECONDS = c.defineInRange("omenSeconds", 20, 5, 300);
        COOLDOWN_SECONDS = c.defineInRange("cooldownSeconds", 75, 0, 100000);
        FAKE_CHAT = c.comment("Mensagens falsas no chat.").define("fakeChat", true);
        WORLD_TAMPERING = c.comment("Presságios abrem portas de madeira e apagam tochas perto do jogador.").define("worldTampering", true);
        JUMPSCARES = c.define("jumpscares", true);
        JUMPSCARE_CHANCE = c.defineInRange("jumpscareChance", 0.40D, 0.0D, 1.0D);
        INTRO_EVENT = c.comment("Primeiro evento garantido ~100 s depois de o jogador entrar pela primeira vez.").define("introEvent", true);
        WELCOME_MESSAGE = c.comment("Mensagem única na primeira vez que o jogador entra, confirmando que o mod carregou.").define("welcomeMessage", true);
        c.pop();

        c.push("reaper");
        ENABLE_REAPER = c.comment("Permite a versão agressiva (Ceifador Oco).").define("enabled", true);
        REAPER_MAX_CHANCE = c.comment("Chance máxima de ser o Ceifador para jogadores neutros (sobe 6% por aparição anterior).")
                .defineInRange("maxChance", 0.35D, 0.0D, 1.0D);
        SINNER_REAPER_CHANCE = c.comment("Chance para jogadores com honra baixa (pecadores).").defineInRange("sinnerChance", 0.65D, 0.0D, 1.0D);
        SINNER_ONE_HIT = c.comment("O Ceifador mata pecadores com UM golpe.").define("sinnerOneHitKill", true);
        REAPER_LIFETIME_SECONDS = c.defineInRange("lifetimeSeconds", 150, 20, 3600);
        STORM_ON_REAPER = c.comment("Tempestade quando o Ceifador chega.").define("stormOnArrival", true);
        LIGHTNING_ON_REAPER = c.comment("Raio (visual) quando o Ceifador chega.").define("lightningOnArrival", true);
        c.pop();

        c.push("sanity");
        SANITY_ENABLED = c.define("enabled", true);
        SANITY_DRAIN_MULT = c.comment("Multiplicador da perda de sanidade.").defineInRange("drainMultiplier", 1.0D, 0.0D, 10.0D);
        ENABLE_POSSESSION = c.comment("O Oco pode possuir animais para atormentar o jogador.").define("possession", true);
        c.pop();

        c.push("honor");
        ENABLE_GIFTS = c.comment("Jogadores honrados recebem presentes.").define("gifts", true);
        c.pop();
        COMMON_SPEC = c.build();

        ForgeConfigSpec.Builder k = new ForgeConfigSpec.Builder();
        k.push("accessibility");
        SCREEN_SHAKE = k.define("screenShake", true);
        FLASHES = k.comment("Flashes de susto. Desligue se tiver sensibilidade a luz.").define("flashes", true);
        FOG = k.define("fog", true);
        VIGNETTE = k.comment("Escurecimento nas bordas da tela com sanidade baixa.").define("vignette", true);
        SHOW_HUD = k.comment("Mostrar as barras de Sanidade e Honra.").define("showHud", true);
        k.pop();
        CLIENT_SPEC = k.build();
    }
}
