package com.hollowvigil;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

/** Silhueta sombria semitransparente com olhos brancos. */
public class HallucinationRenderer extends HumanoidMobRenderer<HallucinationEntity, PlayerModel<HallucinationEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HollowVigilMod.MODID, "textures/entity/hallucination.png");
    private static final ResourceLocation EYES = new ResourceLocation(HollowVigilMod.MODID, "textures/entity/hallucination_eyes.png");

    public HallucinationRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new PlayerModel<>(ctx.bakeLayer(ModelLayers.PLAYER), false), 0.0F);
        this.addLayer(new GlowEyesLayer<>(this, EYES));
    }

    @Override
    protected RenderType getRenderType(HallucinationEntity entity, boolean bodyVisible, boolean translucent, boolean glowing) {
        return RenderType.entityTranslucent(TEXTURE);
    }

    @Override
    public ResourceLocation getTextureLocation(HallucinationEntity entity) {
        return TEXTURE;
    }
}
