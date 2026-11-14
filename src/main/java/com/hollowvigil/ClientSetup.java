package com.hollowvigil;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HollowVigilMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers e) {
        e.registerEntityRenderer(ModEntities.HOLLOW.get(), HollowRenderer::new);
        e.registerEntityRenderer(ModEntities.HOLLOW_REAPER.get(), HollowReaperRenderer::new);
        e.registerEntityRenderer(ModEntities.HALLUCINATION.get(), HallucinationRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions e) {
        e.registerLayerDefinition(HollowReaperModel.LAYER, HollowReaperModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void onRegisterOverlays(RegisterGuiOverlaysEvent e) {
        e.registerAboveAll("horror", ClientHorror::renderOverlay);
    }

    /** Anima o brilho vermelho de "possuído" nos animais comuns. */
    @SubscribeEvent
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void onAddLayers(EntityRenderersEvent.AddLayers e) {
        EntityType<?>[] types = {EntityType.COW, EntityType.PIG, EntityType.SHEEP, EntityType.CHICKEN, EntityType.WOLF,
                EntityType.CAT, EntityType.RABBIT, EntityType.HORSE, EntityType.FOX, EntityType.GOAT};
        for (EntityType<?> type : types) {
            EntityRenderer<?> r = e.getRenderer(type);
            if (r instanceof LivingEntityRenderer lr) {
                lr.addLayer(new PossessedLayer(lr));
            }
        }
    }
}
