package com.hollowvigil;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class HollowRenderer extends HumanoidMobRenderer<HollowEntity, PlayerModel<HollowEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HollowVigilMod.MODID, "textures/entity/hollow.png");
    private static final ResourceLocation EYES = new ResourceLocation(HollowVigilMod.MODID, "textures/entity/hollow_eyes.png");

    public HollowRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new PlayerModel<>(ctx.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
        this.addLayer(new GlowEyesLayer<>(this, EYES));
    }

    /** Mais alto e esguio que um jogador: proporção "errada". */
    @Override
    protected void scale(HollowEntity entity, PoseStack stack, float partialTick) {
        stack.scale(0.96F, 1.10F, 0.96F);
    }

    @Override
    public ResourceLocation getTextureLocation(HollowEntity entity) {
        return TEXTURE;
    }
}
