package com.hollowvigil;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

/**
 * Cliente: tremor, flash, neblina, vinheta, falhas visuais, olhos na escuridão, palavras sussurradas,
 * visão vermelha do Assassino, brilho do Anjo e a HUD texturizada de Sanidade/Honra.
 */
@Mod.EventBusSubscriber(modid = HollowVigilMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientHorror {
    private static final ResourceLocation HUD = new ResourceLocation(HollowVigilMod.MODID, "textures/gui/hud.png");
    private static final Random RNG = new Random();

    private static int shakeTicks, shakeMax, flashTicks, flashMax, fogTicks, fogMax;
    private static float shakeIntensity;
    private static float clientTime;

    // estado sincronizado
    private static int sanity = 100, honor = 50, mode = 0, kills = 0, target = 15;
    private static float dispSanity = 100.0F, dispHonor = 50.0F;
    private static int lossFlash = 0, gainFlash = 0;

    // efeitos de sanidade baixa
    private static int glitchTicks = 0, eyeTicks = 0, whisperTicks = 0, whisperIdx = 1;
    private static final float[] glitchY = new float[3];
    private static float eyeX, eyeY, whisperX, whisperY;

    public static void setStats(StatsSyncPacket p) {
        if (p.sanity() < sanity - 2) lossFlash = 14;
        else if (p.sanity() > sanity + 4) gainFlash = 10;
        sanity = p.sanity();
        honor = p.honor();
        mode = p.mode();
        kills = p.kills();
        target = Math.max(1, p.target());
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
        if (lossFlash > 0) lossFlash--;
        if (gainFlash > 0) gainFlash--;
        dispSanity += (sanity - dispSanity) * 0.12F;
        dispHonor += (honor - dispHonor) * 0.12F;

        if (mode != 0) return;
        if (glitchTicks > 0) glitchTicks--;
        else if (HollowConfig.GLITCH.get() && sanity < 25 && RNG.nextFloat() < 0.02F) {
            glitchTicks = 3;
            for (int i = 0; i < glitchY.length; i++) glitchY[i] = RNG.nextFloat();
        }
        if (eyeTicks > 0) eyeTicks--;
        else if (HollowConfig.GLITCH.get() && sanity < 22 && RNG.nextFloat() < 0.01F) {
            eyeTicks = 35;
            eyeX = RNG.nextBoolean() ? 0.05F + RNG.nextFloat() * 0.2F : 0.75F + RNG.nextFloat() * 0.2F;
            eyeY = 0.3F + RNG.nextFloat() * 0.45F;
        }
        if (whisperTicks > 0) whisperTicks--;
        else if (HollowConfig.WHISPER_TEXT.get() && sanity < 30 && RNG.nextFloat() < 0.006F) {
            whisperTicks = 70;
            whisperIdx = 1 + RNG.nextInt(10);
            whisperX = 0.08F + RNG.nextFloat() * 0.6F;
            whisperY = 0.2F + RNG.nextFloat() * 0.6F;
        }
    }

    // ------------------------------------------------------------ câmera / neblina

    @SubscribeEvent
    public static void onCamera(ViewportEvent.ComputeCameraAngles e) {
        if (shakeTicks > 0) {
            float f = shakeIntensity * ((float) shakeTicks / shakeMax);
            e.setPitch(e.getPitch() + (RNG.nextFloat() - 0.5F) * f);
            e.setYaw(e.getYaw() + (RNG.nextFloat() - 0.5F) * f);
            e.setRoll(e.getRoll() + (RNG.nextFloat() - 0.5F) * f * 0.6F);
        }
        if (mode == 0 && sanity < 50 && HollowConfig.SCREEN_SHAKE.get()) {
            float k = (50 - sanity) / 50.0F;
            e.setRoll(e.getRoll() + Mth.sin(clientTime * 0.045F) * 2.8F * k);
            e.setPitch(e.getPitch() + Mth.sin(clientTime * 0.031F + 1.0F) * 0.8F * k);
        }
    }

    private static float fogStrength() {
        if (mode != 0) return 0.0F;
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
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        Font font = mc.font;

        if (mode == 1) {                       // ASSASSINO: visão vermelha
            float pulse = 0.85F + 0.15F * Mth.sin(clientTime * 0.15F);
            g.fill(0, 0, w, h, ((int) (0x34 * pulse) << 24) | 0xB00000);
            if (HollowConfig.VIGNETTE.get()) vignette(g, w, h, 0.9F * pulse, 0x400000);
        } else if (mode == 2) {                // ANJO: brilho dourado suave
            g.fill(0, 0, w, h, 0x12FFE08A);
            if (HollowConfig.VIGNETTE.get()) vignette(g, w, h, 0.35F + 0.05F * Mth.sin(clientTime * 0.08F), 0xFFD27A);
        } else {
            if (HollowConfig.VIGNETTE.get() && sanity < 40) {
                float beat = sanity < 35 ? heartbeatPulse() : 0.0F;
                float strength = (40 - sanity) / 40.0F * (0.75F + 0.25F * Mth.sin(clientTime * 0.2F)) + 0.25F * beat;
                vignette(g, w, h, strength, 0x180000);
            }
            if (glitchTicks > 0) glitch(g, w, h);
            if (eyeTicks > 0) eyes(g, w, h);
            if (whisperTicks > 0) whisper(g, font, w, h);
        }

        if (HollowConfig.SHOW_HUD.get() && !mc.options.hideGui) hud(g, font);
        if (flashTicks > 0) flash(g, w, h);
        RenderSystem.disableBlend();
    }

    private static float heartbeatPulse() {
        float phase = clientTime % 40.0F;
        float a = (float) Math.exp(-Math.pow((phase - 2.0F) / 2.5F, 2));
        float b = 0.7F * (float) Math.exp(-Math.pow((phase - 12.0F) / 2.5F, 2));
        return Math.max(a, b);
    }

    private static void vignette(GuiGraphics g, int w, int h, float strength, int rgb) {
        int steps = 14;
        int sw = Math.max(1, w / 70);
        int sh = Math.max(1, h / 40);
        for (int i = 0; i < steps; i++) {
            float a = Math.max(0.0F, Math.min(1.0F, strength * (1.0F - i / (float) steps)));
            int col = ((int) (a * 200.0F) << 24) | rgb;
            g.fill(i * sw, 0, (i + 1) * sw, h, col);
            g.fill(w - (i + 1) * sw, 0, w - i * sw, h, col);
            g.fill(0, i * sh, w, (i + 1) * sh, col);
            g.fill(0, h - (i + 1) * sh, w, h - i * sh, col);
        }
    }

    private static void glitch(GuiGraphics g, int w, int h) {
        for (int i = 0; i < glitchY.length; i++) {
            int y = (int) (glitchY[i] * h);
            int hh = 2 + (i * 3);
            g.fill(0, y, w, y + hh, (i % 2 == 0) ? 0x40FF2020 : 0x4020E0FF);
            g.fill(RNG.nextInt(Math.max(1, w / 2)), y + hh, w, y + hh + 1, 0x30FFFFFF);
        }
    }

    private static void eyes(GuiGraphics g, int w, int h) {
        float prog = 1.0F - eyeTicks / 35.0F;
        float a = Mth.sin((float) Math.PI * prog);
        int alpha = (int) (a * 220.0F);
        if (alpha < 12) return;
        int x = (int) (eyeX * w), y = (int) (eyeY * h);
        g.fill(x - 5, y - 3, x + 17, y + 5, (alpha / 3 << 24));
        g.fill(x, y, x + 4, y + 2, (alpha << 24) | 0xE8E8E8);
        g.fill(x + 10, y, x + 14, y + 2, (alpha << 24) | 0xE8E8E8);
    }

    private static void whisper(GuiGraphics g, Font font, int w, int h) {
        float prog = 1.0F - whisperTicks / 70.0F;
        int alpha = (int) (Mth.sin((float) Math.PI * prog) * 230.0F);
        if (alpha < 16) return;
        float scale = 1.7F;
        g.pose().pushPose();
        g.pose().scale(scale, scale, 1.0F);
        g.drawString(font, Component.translatable("hud.hollowvigil.whisper." + whisperIdx),
                (int) (whisperX * w / scale), (int) (whisperY * h / scale), (alpha << 24) | 0x8E1A1A, false);
        g.pose().popPose();
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

    // ------------------------------------------------------------ HUD texturizada

    private static void hud(GuiGraphics g, Font font) {
        int x = 6, y = 6;

        // Sanidade
        float sf = Mth.clamp(dispSanity / 100.0F, 0.0F, 1.0F);
        int fillV = sf > 0.6F ? HudAtlas.FILL_TEAL_V : sf > 0.3F ? HudAtlas.FILL_VIOLET_V : HudAtlas.FILL_CRIMSON_V;
        int icon = sf > 0.3F ? HudAtlas.ICON_EYE : HudAtlas.ICON_EYE_LOW;
        boolean low = sf <= 0.3F;
        float alpha = low ? 0.78F + 0.22F * Mth.sin(clientTime * 0.35F) : 1.0F;
        int ox = low && sf < 0.2F ? RNG.nextInt(3) - 1 : 0;
        int valueColor = sf > 0.6F ? 0xFF7FE8E8 : sf > 0.3F ? 0xFFC9A0F0 : 0xFFFF6A5A;
        bar(g, font, x + ox, y, icon, sf, fillV, Math.round(dispSanity), valueColor, alpha, true);
        int sx = x + 22 + 4, sy = y + 2 + 3;
        if (lossFlash > 0) outline(g, sx - 1, sy - 1, 102, 12, ((int) (lossFlash / 14.0F * 200.0F) << 24) | 0xFF2020);
        if (gainFlash > 0) outline(g, sx - 1, sy - 1, 102, 12, ((int) (gainFlash / 10.0F * 160.0F) << 24) | 0x40FFE0);

        // Honra
        float hf = Mth.clamp(dispHonor / 100.0F, 0.0F, 1.0F);
        int hIcon = mode == 1 ? HudAtlas.ICON_KNIFE : mode == 2 ? HudAtlas.ICON_WINGS
                : honor <= 25 ? HudAtlas.ICON_SKULL : honor >= 70 ? HudAtlas.ICON_HALO : HudAtlas.ICON_SCALES;
        int hColor = honor <= 25 ? 0xFFFF6A5A : honor >= 70 ? 0xFFFFE070 : 0xFFE0E0E8;
        bar(g, font, x, y + 22, hIcon, hf, HudAtlas.FILL_HONOR_V, Math.round(dispHonor), hColor, 1.0F, true);
        int mx = x + 22 + 4 + 50;   // marca central (50%)
        g.fill(mx, y + 22 + 5, mx + 1, y + 22 + 15, 0x66FFFFFF);

        if (mode == 1) killerRow(g, font, x, y + 44);
        else if (mode == 2) angelRow(g, font, x, y + 44, hf);
    }

    private static void outline(GuiGraphics g, int x, int y, int w, int h, int argb) {
        g.fill(x, y, x + w, y + 1, argb);
        g.fill(x, y + h - 1, x + w, y + h, argb);
        g.fill(x, y, x + 1, y + h, argb);
        g.fill(x + w - 1, y, x + w, y + h, argb);
    }

    private static void bar(GuiGraphics g, Font font, int x, int y, int iconIdx, float frac, int fillV, int value,
                            int valueColor, float alpha, boolean sheen) {
        final int tw = HudAtlas.TEX_W, th = HudAtlas.TEX_H;
        g.setColor(1.0F, 1.0F, 1.0F, alpha);
        g.blit(HUD, x, y, HudAtlas.SOCKET_U, HudAtlas.SOCKET_V, HudAtlas.SOCKET_SIZE, HudAtlas.SOCKET_SIZE, tw, th);
        g.blit(HUD, x + 2, y + 2, iconIdx * 16, HudAtlas.ICON_V, 16, 16, tw, th);
        int fx = x + 22, fy = y + 2;
        g.blit(HUD, fx, fy, 0, 0, HudAtlas.FRAME_W, HudAtlas.FRAME_H, tw, th);
        int sx = fx + 4, sy = fy + 3;
        int fw = Math.max(0, Math.min(HudAtlas.FILL_W, Math.round(HudAtlas.FILL_W * frac)));
        if (fw > 0) g.blit(HUD, sx, sy, 0, fillV, fw, HudAtlas.FILL_H, tw, th);
        if (sheen && fw > 6) {
            int off = (int) ((clientTime * 0.8F) % 150.0F) - 40;
            int start = Math.max(0, off);
            int end = Math.min(fw, off + HudAtlas.SHEEN_W);
            if (end > start) g.blit(HUD, sx + start, sy, start - off, HudAtlas.SHEEN_V, end - start, HudAtlas.FILL_H, tw, th);
        }
        g.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        g.drawString(font, String.valueOf(value), fx + HudAtlas.FRAME_W + 4, y + 7, valueColor, true);
    }

    private static void killerRow(GuiGraphics g, Font font, int x, int y) {
        final int tw = HudAtlas.TEX_W, th = HudAtlas.TEX_H;
        g.blit(HUD, x, y, 0, HudAtlas.BADGE_V, HudAtlas.BADGE_W, HudAtlas.BADGE_H, tw, th);
        g.drawCenteredString(font, Component.translatable("hud.hollowvigil.killer"), x + 48, y + 3, 0xFFFF4040);
        g.drawString(font, Component.translatable("hollow.killer.progress", Math.min(kills, target), target), x, y + 17, 0xFFFF9A8A, true);
        int by = y + 28;
        g.blit(HUD, x, by, 0, 0, HudAtlas.FRAME_W, HudAtlas.FRAME_H, tw, th);
        int fw = Math.max(0, Math.min(HudAtlas.FILL_W, Math.round(HudAtlas.FILL_W * Math.min(1.0F, kills / (float) target))));
        if (fw > 0) g.blit(HUD, x + 4, by + 3, 0, HudAtlas.FILL_MISSION_V, fw, HudAtlas.FILL_H, tw, th);
    }

    private static void angelRow(GuiGraphics g, Font font, int x, int y, float honorFrac) {
        final int tw = HudAtlas.TEX_W, th = HudAtlas.TEX_H;
        g.blit(HUD, x, y, 0, HudAtlas.BADGE_V, HudAtlas.BADGE_W, HudAtlas.BADGE_H, tw, th);
        g.drawCenteredString(font, Component.translatable("hud.hollowvigil.angel"), x + 48, y + 3, 0xFFFFE070);
        for (int i = 0; i < 7; i++) {   // brilhos dourados sobre a barra de honra
            float t = clientTime * 0.05F + i * 1.7F;
            int px = x + 26 + (int) (100.0F * ((t * 0.37F + i * 0.13F) % 1.0F));
            int py = y - 22 + 4 + (int) (4.0F + 3.0F * Mth.sin(t * 3.0F + i));
            int a = (int) ((Mth.sin(t * 4.0F + i) * 0.5F + 0.5F) * 230.0F);
            if (a > 20) g.fill(px, py, px + 1, py + 1, (a << 24) | 0xFFF3B0);
        }
    }
}
