package com.herobrinemod;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Modelo original: 3,2 blocos, torso esguio curvado, pescoço longo, 4 braços e mandíbula articulada. */
public class CorruptedHerobrineModel<T extends HerobrineCorruptedEntity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(new ResourceLocation(HerobrineMod.MODID, "herobrine_corrupted"), "main");

    private final ModelPart root, body, neck, head, jaw, armR, armL, extraR, extraL, legR, legL;

    public CorruptedHerobrineModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.neck = body.getChild("neck");
        this.head = neck.getChild("head");
        this.jaw = head.getChild("jaw");
        this.armR = body.getChild("arm_r");
        this.armL = body.getChild("arm_l");
        this.extraR = body.getChild("extra_r");
        this.extraL = body.getChild("extra_l");
        this.legR = root.getChild("leg_r");
        this.legL = root.getChild("leg_l");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition r = mesh.getRoot();

        PartDefinition body = r.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(40, 0).addBox(-4F, -16F, -2F, 8F, 16F, 4F),
                PartPose.offset(0F, 2F, 0F));
        PartDefinition neck = body.addOrReplaceChild("neck",
                CubeListBuilder.create().texOffs(0, 28).addBox(-1.5F, -6F, -1.5F, 3F, 6F, 3F),
                PartPose.offset(0F, -16F, 0F));
        PartDefinition head = neck.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4F, -8F, -4F, 8F, 8F, 8F),
                PartPose.offset(0F, -6F, 0F));
        head.addOrReplaceChild("jaw",
                CubeListBuilder.create().texOffs(0, 16).addBox(-3.5F, 0F, -7F, 7F, 3F, 7F),
                PartPose.offset(0F, -1.5F, 3.5F));

        body.addOrReplaceChild("arm_r",
                CubeListBuilder.create().texOffs(0, 40).addBox(-1F, 0F, -1F, 2F, 34F, 2F),
                PartPose.offset(-5F, -15F, 0F));
        body.addOrReplaceChild("arm_l",
                CubeListBuilder.create().texOffs(10, 40).addBox(-1F, 0F, -1F, 2F, 34F, 2F),
                PartPose.offset(5F, -15F, 0F));
        body.addOrReplaceChild("extra_r",
                CubeListBuilder.create().texOffs(20, 40).addBox(-0.5F, 0F, -0.5F, 1F, 20F, 1F),
                PartPose.offset(-4.5F, -8F, 0F));
        body.addOrReplaceChild("extra_l",
                CubeListBuilder.create().texOffs(26, 40).addBox(-0.5F, 0F, -0.5F, 1F, 20F, 1F),
                PartPose.offset(4.5F, -8F, 0F));

        r.addOrReplaceChild("leg_r",
                CubeListBuilder.create().texOffs(40, 40).addBox(-1.5F, 0F, -1.5F, 3F, 22F, 3F),
                PartPose.offset(-2.5F, 2F, 0F));
        r.addOrReplaceChild("leg_l",
                CubeListBuilder.create().texOffs(56, 40).addBox(-1.5F, 0F, -1.5F, 3F, 22F, 3F),
                PartPose.offset(2.5F, 2F, 0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(T e, float limbSwing, float limbSwingAmount, float age, float netHeadYaw, float headPitch) {
        boolean aggro = e.isAggressive();
        float rad = Mth.PI / 180F;

        body.xRot = 0.25F + Mth.sin(age * 0.05F) * 0.03F;
        neck.xRot = 0.35F;
        head.yRot = netHeadYaw * rad * 0.8F;
        head.xRot = headPitch * rad - 0.3F;
        head.zRot = Mth.sin(age * 1.3F) * 0.05F + (Mth.sin(age * 0.17F) > 0.93F ? 0.35F : 0F); // tique
        jaw.xRot = aggro ? 0.9F + Mth.sin(age * 0.8F) * 0.25F : 0.2F + Mth.sin(age * 0.1F) * 0.05F;

        float swing = Mth.cos(limbSwing * 0.6662F) * 1.2F * limbSwingAmount;
        if (aggro) {
            armR.xRot = -1.35F + Mth.sin(age * 0.6F) * 0.12F;
            armL.xRot = -1.35F - Mth.sin(age * 0.6F) * 0.12F;
        } else {
            armR.xRot = -swing;
            armL.xRot = swing;
        }
        armR.zRot = 0.08F;
        armL.zRot = -0.08F;

        extraR.xRot = Mth.sin(age * 0.7F) * 0.5F - 0.3F;
        extraL.xRot = Mth.sin(age * 0.7F + 2F) * 0.5F - 0.3F;
        extraR.zRot = 0.5F + Mth.sin(age * 0.9F) * 0.2F;
        extraL.zRot = -0.5F - Mth.sin(age * 0.9F + 1F) * 0.2F;

        legR.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        legL.xRot = Mth.cos(limbSwing * 0.6662F + Mth.PI) * 1.4F * limbSwingAmount;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer buf, int light, int overlay,
                               float r, float g, float b, float a) {
        root.render(ps, buf, light, overlay, r, g, b, a);
    }
}
