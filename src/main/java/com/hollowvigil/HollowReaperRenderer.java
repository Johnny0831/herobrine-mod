package com.hollowvigil;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class HollowReaperRenderer extends MobRenderer<HollowReaperEntity, HollowReaperModel<HollowReaperEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HollowVigilMod.MODID, "textures/entity/hollow_reaper.png");
    private static final ResourceLocation EYES = new ResourceLocation(HollowVigilMod.MODID, "textures/entity/hollow_reaper_eyes.png");

    public HollowReaperRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new HollowReaperModel<>(ctx.bakeLayer(HollowReaperModel.LAYER)), 0.7F);
        this.addLayer(new GlowEyesLayer<>(this, EYES));
    }

    @Override
    public ResourceLocation getTextureLocation(HollowReaperEntity entity) {
        return TEXTURE;
    }
}
