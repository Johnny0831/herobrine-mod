package com.hollowvigil;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

/** Animais possuídos brilham em vermelho pulsante (full-bright) em qualquer mob que tenha o efeito Possuído. */
public class PossessedLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    public PossessedLayer(RenderLayerParent<T, M> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack ps, MultiBufferSource buf, int light, T entity, float limbSwing, float limbSwingAmount,
                       float partialTicks, float age, float netHeadYaw, float headPitch) {
        if (!entity.hasEffect(ModEffects.POSSESSED.get())) return;
        VertexConsumer vc = buf.getBuffer(RenderType.eyes(this.getTextureLocation(entity)));
        float pulse = 0.55F + 0.2F * Mth.sin(age * 0.3F);
        this.getParentModel().renderToBuffer(ps, vc, LightTexture.FULL_BRIGHT,
                LivingEntityRenderer.getOverlayCoords(entity, 0.0F), 1.0F, 0.1F, 0.1F, pulse);
    }
}
