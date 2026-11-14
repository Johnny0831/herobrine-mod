package com.herobrinemod;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HerobrineMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers e) {
        e.registerEntityRenderer(ModEntities.HEROBRINE.get(), HerobrineRenderer::new);
        e.registerEntityRenderer(ModEntities.HEROBRINE_CORRUPTED.get(), CorruptedHerobrineRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions e) {
        e.registerLayerDefinition(CorruptedHerobrineModel.LAYER, CorruptedHerobrineModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void onRegisterOverlays(RegisterGuiOverlaysEvent e) {
        e.registerAboveAll("horror", ClientHorror::renderOverlay);
    }
}
