package com.herobrinemod;

import com.mojang.blaze3d.vertex.PoseStack;
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

    /** Um pouco mais alto e esguio que um jogador: proporção "errada" que incomoda. */
    @Override
    protected void scale(HerobrineEntity entity, PoseStack stack, float partialTick) {
        stack.scale(0.96F, 1.10F, 0.96F);
    }

    @Override
    public ResourceLocation getTextureLocation(HerobrineEntity entity) {
        return TEXTURE;
    }
}
