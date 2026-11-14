package com.herobrinemod;

import net.minecraftforge.common.ForgeConfigSpec;

public class HerobrineConfig {
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final ForgeConfigSpec CLIENT_SPEC;

    // ---- common (servidor) ----
    public static final ForgeConfigSpec.BooleanValue ENABLE_HAUNTINGS;
    public static final ForgeConfigSpec.BooleanValue ONLY_WHEN_DARK;
    public static final ForgeConfigSpec.IntValue AVG_SECONDS;
    public static final ForgeConfigSpec.IntValue OMEN_SECONDS;
    public static final ForgeConfigSpec.IntValue COOLDOWN_SECONDS;
    public static final ForgeConfigSpec.BooleanValue FAKE_CHAT;
    public static final ForgeConfigSpec.BooleanValue WORLD_TAMPERING;
    public static final ForgeConfigSpec.BooleanValue JUMPSCARES;
    public static final ForgeConfigSpec.DoubleValue JUMPSCARE_CHANCE;
    public static final ForgeConfigSpec.BooleanValue ENABLE_CORRUPTED;
    public static final ForgeConfigSpec.DoubleValue CORRUPTED_MAX_CHANCE;
    public static final ForgeConfigSpec.IntValue CORRUPTED_LIFETIME_SECONDS;

    // ---- client (acessibilidade) ----
    public static final ForgeConfigSpec.BooleanValue SCREEN_SHAKE;
    public static final ForgeConfigSpec.BooleanValue FLASHES;
    public static final ForgeConfigSpec.BooleanValue FOG;

    static {
        ForgeConfigSpec.Builder c = new ForgeConfigSpec.Builder();
        c.push("hauntings");
        ENABLE_HAUNTINGS = c.comment("Liga/desliga os eventos de terror automáticos.").define("enabled", true);
        ONLY_WHEN_DARK = c.comment("Só acontece em locais escuros (noite ao ar livre, cavernas).").define("onlyWhenDark", true);
        AVG_SECONDS = c.comment("Tempo médio (segundos) entre eventos, enquanto as condições valem.")
                .defineInRange("averageSecondsBetweenHauntings", 420, 30, 100000);
        OMEN_SECONDS = c.comment("Duração mínima (segundos) dos presságios antes dele aparecer.")
                .defineInRange("omenSeconds", 25, 5, 300);
        COOLDOWN_SECONDS = c.comment("Descanso (segundos) depois de cada aparição.")
                .defineInRange("cooldownSeconds", 240, 0, 100000);
        FAKE_CHAT = c.comment("Mensagens falsas no chat (entrou no jogo / saiu do jogo / falas).").define("fakeChat", true);
        WORLD_TAMPERING = c.comment("Presságios podem abrir portas de madeira e apagar tochas perto do jogador.")
                .define("worldTampering", true);
        JUMPSCARES = c.comment("Quando encarado, o Espreitador pode aparecer colado na sua cara.").define("jumpscares", true);
        JUMPSCARE_CHANCE = c.defineInRange("jumpscareChance", 0.35D, 0.0D, 1.0D);
        c.pop();
        c.push("corrupted");
        ENABLE_CORRUPTED = c.comment("Permite a versão agressiva (Herobrine Corrompido).").define("enabled", true);
        CORRUPTED_MAX_CHANCE = c.comment("Chance máxima de a aparição ser o Corrompido. Sobe 6% a cada aparição anterior (nível de pavor).")
                .defineInRange("maxChance", 0.35D, 0.0D, 1.0D);
        CORRUPTED_LIFETIME_SECONDS = c.comment("Quanto tempo ele caça o jogador antes de sumir.")
                .defineInRange("lifetimeSeconds", 150, 20, 3600);
        c.pop();
        COMMON_SPEC = c.build();

        ForgeConfigSpec.Builder k = new ForgeConfigSpec.Builder();
        k.push("accessibility");
        SCREEN_SHAKE = k.comment("Tremor de câmera.").define("screenShake", true);
        FLASHES = k.comment("Flashes de susto na tela. Desligue se tiver sensibilidade a luz.").define("flashes", true);
        FOG = k.comment("Neblina fechada durante os presságios.").define("fog", true);
        k.pop();
        CLIENT_SPEC = k.build();
    }
}
