package com.hollowvigil;

import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
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

    /** Adiciona o brilho vermelho de "possuído" aos animais comuns. */
    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers e) {
        addPossessed(e, EntityType.COW);
        addPossessed(e, EntityType.PIG);
        addPossessed(e, EntityType.SHEEP);
        addPossessed(e, EntityType.CHICKEN);
        addPossessed(e, EntityType.WOLF);
        addPossessed(e, EntityType.CAT);
        addPossessed(e, EntityType.RABBIT);
        addPossessed(e, EntityType.HORSE);
        addPossessed(e, EntityType.FOX);
        addPossessed(e, EntityType.GOAT);
    }

    private static <T extends LivingEntity> void addPossessed(EntityRenderersEvent.AddLayers e, EntityType<T> type) {
        LivingEntityRenderer<T, ?> renderer = e.getRenderer(type);
        if (renderer != null) addLayerRaw(renderer);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void addLayerRaw(LivingEntityRenderer renderer) {
        renderer.addLayer(new PossessedLayer(renderer));
    }
}
