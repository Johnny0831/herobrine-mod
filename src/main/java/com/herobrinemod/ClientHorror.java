package com.herobrinemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

/** Estado e efeitos de terror do lado do cliente: tremor, flash de susto e neblina. */
@Mod.EventBusSubscriber(modid = HerobrineMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientHorror {
    private static final Random RNG = new Random();
    private static int shakeTicks, shakeMax, flashTicks, flashMax, fogTicks, fogMax;
    private static float shakeIntensity;

    public static void receive(HorrorEffectPacket p) {
        switch (p.type()) {
            case SHAKE -> {
                if (HerobrineConfig.SCREEN_SHAKE.get()) {
                    shakeTicks = shakeMax = Math.max(1, p.ticks());
                    shakeIntensity = p.intensity();
                }
            }
            case FLASH -> {
                if (HerobrineConfig.FLASHES.get()) flashTicks = flashMax = Math.max(1, p.ticks());
            }
            case FOG -> {
                if (HerobrineConfig.FOG.get()) fogTicks = fogMax = Math.max(1, p.ticks());
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        if (shakeTicks > 0) shakeTicks--;
        if (flashTicks > 0) flashTicks--;
        if (fogTicks > 0) fogTicks--;
    }

    @SubscribeEvent
    public static void onCamera(ViewportEvent.ComputeCameraAngles e) {
        if (shakeTicks <= 0) return;
        float f = shakeIntensity * ((float) shakeTicks / shakeMax);
        e.setPitch(e.getPitch() + (RNG.nextFloat() - 0.5F) * f);
        e.setYaw(e.getYaw() + (RNG.nextFloat() - 0.5F) * f);
        e.setRoll(e.getRoll() + (RNG.nextFloat() - 0.5F) * f * 0.6F);
    }

    private static float fogStrength() {
        if (fogTicks <= 0) return 0.0F;
        return Math.min(1.0F, Math.min(fogTicks, fogMax - fogTicks) / 40.0F);
    }

    @SubscribeEvent
    public static void onFog(ViewportEvent.RenderFog e) {
        float t = fogStrength();
        if (t <= 0.0F) return;
        e.setNearPlaneDistance(Mth.lerp(t, e.getNearPlaneDistance(), 0.0F));
        e.setFarPlaneDistance(Mth.lerp(t, e.getFarPlaneDistance(), 16.0F));
        e.setCanceled(true);
    }

    @SubscribeEvent
    public static void onFogColor(ViewportEvent.ComputeFogColor e) {
        float t = fogStrength();
        if (t <= 0.0F) return;
        e.setRed(Mth.lerp(t, e.getRed(), 0.03F));
        e.setGreen(Mth.lerp(t, e.getGreen(), 0.03F));
        e.setBlue(Mth.lerp(t, e.getBlue(), 0.04F));
    }

    /** Overlay do susto: tela preta com olhos e dentes brancos. */
    public static void renderOverlay(ForgeGui gui, GuiGraphics g, float partialTick, int w, int h) {
        if (flashTicks <= 0) return;
        float a = Mth.clamp(flashTicks / (float) flashMax * 1.3F, 0.0F, 1.0F);
        int alpha = (int) (a * 255.0F);
        g.fill(0, 0, w, h, alpha << 24);
        int white = (alpha << 24) | 0xFFFFFF;
        int ew = w / 14;
        int eh = h / 22;
        int ey = h * 38 / 100;
        g.fill(w * 3 / 10, ey, w * 3 / 10 + ew, ey + eh, white);
        g.fill(w * 7 / 10 - ew, ey, w * 7 / 10, ey + eh, white);
        int teeth = 14;
        int tw = (w * 40 / 100) / teeth;
        int my = h * 62 / 100;
        for (int i = 0; i < teeth; i++) {
            int x = w * 30 / 100 + i * tw;
            int th = (i % 2 == 0) ? h / 12 : h / 20;
            g.fill(x, my, x + tw - 1, my + th, white);
        }
    }
}
