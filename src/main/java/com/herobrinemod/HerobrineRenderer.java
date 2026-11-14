package com.herobrinemod;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class HerobrineRenderer extends HumanoidMobRenderer<HerobrineEntity, PlayerModel<HerobrineEntity>> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HerobrineMod.MODID, "textures/entity/herobrine.png");
    private static final ResourceLocation EYES =
            new ResourceLocation(HerobrineMod.MODID, "textures/entity/herobrine_eyes.png");

    public HerobrineRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new PlayerModel<>(ctx.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
        this.addLayer(new GlowEyesLayer<>(this, EYES));
    }

    @Override
    public ResourceLocation getTextureLocation(HerobrineEntity entity) {
        return TEXTURE;
    }
}
