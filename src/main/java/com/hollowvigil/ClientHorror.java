package com.hollowvigil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

/** Efeitos de terror do lado do cliente: tremor, flash, neblina, vinheta de sanidade e HUD (Sanidade/Honra). */
@Mod.EventBusSubscriber(modid = HollowVigilMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientHorror {
    private static final Random RNG = new Random();
    private static int shakeTicks, shakeMax, flashTicks, flashMax, fogTicks, fogMax;
    private static float shakeIntensity;
    private static float clientTime;
    private static int sanity = 100, honor = 50;

    public static void setStats(int s, int h) {
        sanity = s;
        honor = h;
    }

    public static void receive(HorrorEffectPacket p) {
        switch (p.type()) {
            case SHAKE -> {
                if (HollowConfig.SCREEN_SHAKE.get()) {
                    shakeTicks = shakeMax = Math.max(1, p.ticks());
                    shakeIntensity = p.intensity();
                }
            }
            case FLASH -> {
                if (HollowConfig.FLASHES.get()) flashTicks = flashMax = Math.max(1, p.ticks());
            }
            case FOG -> {
                if (HollowConfig.FOG.get()) fogTicks = fogMax = Math.max(1, p.ticks());
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        clientTime += 1.0F;
        if (shakeTicks > 0) shakeTicks--;
        if (flashTicks > 0) flashTicks--;
        if (fogTicks > 0) fogTicks--;
    }

    @SubscribeEvent
    public static void onCamera(ViewportEvent.ComputeCameraAngles e) {
        if (shakeTicks > 0) {
            float f = shakeIntensity * ((float) shakeTicks / shakeMax);
            e.setPitch(e.getPitch() + (RNG.nextFloat() - 0.5F) * f);
            e.setYaw(e.getYaw() + (RNG.nextFloat() - 0.5F) * f);
            e.setRoll(e.getRoll() + (RNG.nextFloat() - 0.5F) * f * 0.6F);
        }
        if (sanity < 50 && HollowConfig.SCREEN_SHAKE.get()) {   // balanço de quem está perdendo a razão
            float k = (50 - sanity) / 50.0F;
            e.setRoll(e.getRoll() + Mth.sin(clientTime * 0.045F) * 2.8F * k);
            e.setPitch(e.getPitch() + Mth.sin(clientTime * 0.031F + 1.0F) * 0.8F * k);
        }
    }

    private static float fogStrength() {
        float t = 0.0F;
        if (fogTicks > 0) t = Math.min(1.0F, Math.min(fogTicks, fogMax - fogTicks) / 40.0F);
        if (sanity < 30 && HollowConfig.FOG.get()) t = Math.max(t, (30 - sanity) / 30.0F * 0.7F);
        return t;
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

    // ------------------------------------------------------------ overlay

    public static void renderOverlay(ForgeGui gui, GuiGraphics g, float partialTick, int w, int h) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (HollowConfig.VIGNETTE.get() && sanity < 40) {
            float strength = (40 - sanity) / 40.0F * (0.75F + 0.25F * Mth.sin(clientTime * 0.2F));
            vignette(g, w, h, strength);
        }

        if (HollowConfig.SHOW_HUD.get() && !mc.options.hideGui) {
            Font font = mc.font;
            float sf = sanity / 100.0F;
            int sColor = sf > 0.6F ? 0xFF4FC3C3 : sf > 0.3F ? 0xFF9B59B6
                    : (Mth.sin(clientTime * 0.3F) > 0 ? 0xFFC0392B : 0xFF8E1B12);
            int ox = sanity < 20 ? RNG.nextInt(3) - 1 : 0;
            int oy = sanity < 20 ? RNG.nextInt(3) - 1 : 0;
            bar(g, font, 8 + ox, 8 + oy, Component.translatable("hud.hollowvigil.sanity"), sf, sColor);

            float hf = honor / 100.0F;
            int hColor = honor <= 25 ? 0xFF8B0000 : honor >= 70 ? 0xFFFFD700 : 0xFFBBBBBB;
            bar(g, font, 8, 31, Component.translatable("hud.hollowvigil.honor"), hf, hColor);
        }

        if (flashTicks > 0) flash(g, w, h);
    }

    private static void bar(GuiGraphics g, Font font, int x, int y, Component label, float frac, int color) {
        int bw = 84, bh = 7;
        g.drawString(font, label, x, y, 0xFFFFFFFF, true);
        int by = y + 10;
        int fill = Math.max(0, Math.min(bw, (int) (bw * frac)));
        g.fill(x - 1, by - 1, x + bw + 1, by + bh + 1, 0xFF000000);
        g.fill(x, by, x + bw, by + bh, 0xFF22222B);
        g.fill(x, by, x + fill, by + bh, color);
        g.fill(x, by, x + fill, by + 2, 0x55FFFFFF);
    }

    private static void vignette(GuiGraphics g, int w, int h, float strength) {
        int steps = 14;
        int sw = Math.max(1, w / 70);
        int sh = Math.max(1, h / 40);
        for (int i = 0; i < steps; i++) {
            float a = Math.max(0.0F, strength * (1.0F - i / (float) steps));
            int col = ((int) (a * 200.0F) << 24) | 0x180000;
            g.fill(i * sw, 0, (i + 1) * sw, h, col);
            g.fill(w - (i + 1) * sw, 0, w - i * sw, h, col);
            g.fill(0, i * sh, w, (i + 1) * sh, col);
            g.fill(0, h - (i + 1) * sh, w, h - i * sh, col);
        }
    }

    private static void flash(GuiGraphics g, int w, int h) {
        float a = Mth.clamp(flashTicks / (float) flashMax * 1.3F, 0.0F, 1.0F);
        int alpha = (int) (a * 255.0F);
        g.fill(0, 0, w, h, alpha << 24);
        int white = (alpha << 24) | 0xFFFFFF;
        int ew = w / 14, eh = h / 22, ey = h * 38 / 100;
        g.fill(w * 3 / 10, ey, w * 3 / 10 + ew, ey + eh, white);
        g.fill(w * 7 / 10 - ew, ey, w * 7 / 10, ey + eh, white);
        int teeth = 14, tw = (w * 40 / 100) / teeth, my = h * 62 / 100;
        for (int i = 0; i < teeth; i++) {
            int x = w * 30 / 100 + i * tw;
            int th = (i % 2 == 0) ? h / 12 : h / 20;
            g.fill(x, my, x + tw - 1, my + th, white);
        }
    }
}
