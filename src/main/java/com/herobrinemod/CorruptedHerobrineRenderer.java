package com.herobrinemod;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class CorruptedHerobrineRenderer
        extends MobRenderer<HerobrineCorruptedEntity, CorruptedHerobrineModel<HerobrineCorruptedEntity>> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HerobrineMod.MODID, "textures/entity/herobrine_corrupted.png");
    private static final ResourceLocation EYES =
            new ResourceLocation(HerobrineMod.MODID, "textures/entity/herobrine_corrupted_eyes.png");

    public CorruptedHerobrineRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new CorruptedHerobrineModel<>(ctx.bakeLayer(CorruptedHerobrineModel.LAYER)), 0.7F);
        this.addLayer(new GlowEyesLayer<>(this, EYES));
    }

    @Override
    public ResourceLocation getTextureLocation(HerobrineCorruptedEntity entity) {
        return TEXTURE;
    }
}
