package com.hollowvigil;

import net.minecraftforge.common.ForgeConfigSpec;

public class HollowConfig {
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final ForgeConfigSpec CLIENT_SPEC;

    public static final ForgeConfigSpec.BooleanValue ENABLE_HAUNTINGS, ONLY_WHEN_DARK, FAKE_CHAT, WORLD_TAMPERING,
            JUMPSCARES, ENABLE_REAPER, SINNER_ONE_HIT, SANITY_ENABLED, ENABLE_POSSESSION, ENABLE_GIFTS,
            STORM_ON_REAPER, LIGHTNING_ON_REAPER, WELCOME_MESSAGE, INTRO_EVENT, FORCED_LOOK, PLAYER_PARTICLES,
            ANGEL_FLIGHT;
    public static final ForgeConfigSpec.IntValue AVG_SECONDS, OMEN_SECONDS, COOLDOWN_SECONDS, REAPER_LIFETIME_SECONDS,
            KILLER_TARGET, KILLER_IDLE_SECONDS;
    public static final ForgeConfigSpec.DoubleValue JUMPSCARE_CHANCE, REAPER_MAX_CHANCE, SINNER_REAPER_CHANCE,
            SANITY_DRAIN_MULT, FEED_HONOR;

    public static final ForgeConfigSpec.BooleanValue SCREEN_SHAKE, FLASHES, FOG, SHOW_HUD, VIGNETTE, WHISPER_TEXT, GLITCH;

    static {
        ForgeConfigSpec.Builder c = new ForgeConfigSpec.Builder();
        c.push("hauntings");
        ENABLE_HAUNTINGS = c.comment("Liga/desliga as aparições automáticas.").define("enabled", true);
        ONLY_WHEN_DARK = c.comment("Aparições só em locais escuros (noite ao ar livre, cavernas).").define("onlyWhenDark", true);
        AVG_SECONDS = c.comment("Tempo médio (s) entre aparições. Pecadores 3x mais, assassinos 4x mais.")
                .defineInRange("averageSecondsBetweenHauntings", 90, 20, 100000);
        OMEN_SECONDS = c.defineInRange("omenSeconds", 20, 5, 300);
        COOLDOWN_SECONDS = c.defineInRange("cooldownSeconds", 75, 0, 100000);
        FAKE_CHAT = c.comment("Falas no chat (O Oco, jogadores fantasmas e conquistas falsas).").define("fakeChat", true);
        WORLD_TAMPERING = c.comment("Presságios abrem portas de madeira e apagam tochas perto do jogador.").define("worldTampering", true);
        JUMPSCARES = c.define("jumpscares", true);
        JUMPSCARE_CHANCE = c.defineInRange("jumpscareChance", 0.40D, 0.0D, 1.0D);
        FORCED_LOOK = c.comment("Com sanidade muito baixa a câmera pode ser puxada na direção da entidade.").define("forcedLook", true);
        PLAYER_PARTICLES = c.comment("Partículas (cinzas/almas) visíveis só para o jogador assombrado.").define("playerOnlyParticles", true);
        INTRO_EVENT = c.comment("Primeiro evento garantido ~100 s depois de o jogador entrar pela primeira vez.").define("introEvent", true);
        WELCOME_MESSAGE = c.comment("Mensagem única na primeira vez que o jogador entra.").define("welcomeMessage", true);
        c.pop();

        c.push("reaper");
        ENABLE_REAPER = c.comment("Permite a versão agressiva (Ceifador Oco).").define("enabled", true);
        REAPER_MAX_CHANCE = c.defineInRange("maxChance", 0.35D, 0.0D, 1.0D);
        SINNER_REAPER_CHANCE = c.defineInRange("sinnerChance", 0.65D, 0.0D, 1.0D);
        SINNER_ONE_HIT = c.comment("O Ceifador mata pecadores (honra <= 25) com UM golpe. Assassinos são exceção.").define("sinnerOneHitKill", true);
        REAPER_LIFETIME_SECONDS = c.defineInRange("lifetimeSeconds", 150, 20, 3600);
        STORM_ON_REAPER = c.define("stormOnArrival", true);
        LIGHTNING_ON_REAPER = c.define("lightningOnArrival", true);
        c.pop();

        c.push("sanity");
        SANITY_ENABLED = c.comment("A sanidade só cai ao olhar para uma entidade ou ficar em lugar sem luz.").define("enabled", true);
        SANITY_DRAIN_MULT = c.defineInRange("drainMultiplier", 1.0D, 0.0D, 10.0D);
        ENABLE_POSSESSION = c.comment("O Oco pode possuir animais para atormentar o jogador.").define("possession", true);
        c.pop();

        c.push("honor");
        ENABLE_GIFTS = c.comment("Jogadores honrados recebem presentes.").define("gifts", true);
        FEED_HONOR = c.comment("Honra ganha ao alimentar um animal (use valor negativo para inverter).").defineInRange("feedAnimalHonor", 1.0D, -10.0D, 10.0D);
        c.pop();

        c.push("killer");
        KILLER_TARGET = c.comment("Pontos de caça para absolver o assassino (Ceifador=4, O Oco=2, Alucinação=1).").defineInRange("missionTarget", 15, 1, 500);
        KILLER_IDLE_SECONDS = c.comment("Segundos sem matar até a fúria acabar (a honra sobe e o modo termina com lentidão e exaustão).")
                .defineInRange("idleSecondsBeforeFuryEnds", 180, 30, 3600);
        c.pop();

        c.push("angel");
        ANGEL_FLIGHT = c.comment("O anjo (honra 100) pode voar.").define("flight", true);
        c.pop();
        COMMON_SPEC = c.build();

        ForgeConfigSpec.Builder k = new ForgeConfigSpec.Builder();
        k.push("accessibility");
        SCREEN_SHAKE = k.define("screenShake", true);
        FLASHES = k.comment("Flashes de susto. Desligue se tiver sensibilidade a luz.").define("flashes", true);
        FOG = k.define("fog", true);
        VIGNETTE = k.define("vignette", true);
        GLITCH = k.comment("Falhas visuais e olhos na escuridão com sanidade baixa.").define("glitch", true);
        WHISPER_TEXT = k.comment("Palavras sussurradas na tela com sanidade baixa.").define("whisperText", true);
        SHOW_HUD = k.define("showHud", true);
        k.pop();
        CLIENT_SPEC = k.build();
    }
}
