package com.hollowvigil;

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

/**
 * GERADO por tools/gen_assets.py (partes, UVs e texturas vêm da mesma especificação).
 * Criatura original: 3,2 blocos, corcunda, pescoço longo, crânio com espinhos, caixa torácica exposta,
 * braços articulados com garras, dois braços finos extras, pernas digitígradas e trapos balançando.
 */
public class HollowReaperModel<T extends HollowReaperEntity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(new ResourceLocation(HollowVigilMod.MODID, "hollow_reaper"), "main");

    private final ModelPart root;
    private final ModelPart body, ribcage, spine0, spine1, spine2, neck, head, jaw, thorn_a_l, thorn_a_r, thorn_b_l, thorn_b_r, arm_u_r, arm_f_r, claw_r0, claw_r1, claw_r2, ex_u_r, ex_f_r, thigh_r, shin_r, foot_r, arm_u_l, arm_f_l, claw_l0, claw_l1, claw_l2, ex_u_l, ex_f_l, thigh_l, shin_l, foot_l, pelvis, cloth_f, cloth_bl, cloth_br;

    public HollowReaperModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.ribcage = this.body.getChild("ribcage");
        this.spine0 = this.body.getChild("spine0");
        this.spine1 = this.body.getChild("spine1");
        this.spine2 = this.body.getChild("spine2");
        this.neck = this.body.getChild("neck");
        this.head = this.neck.getChild("head");
        this.jaw = this.head.getChild("jaw");
        this.thorn_a_l = this.head.getChild("thorn_a_l");
        this.thorn_a_r = this.head.getChild("thorn_a_r");
        this.thorn_b_l = this.head.getChild("thorn_b_l");
        this.thorn_b_r = this.head.getChild("thorn_b_r");
        this.arm_u_r = this.body.getChild("arm_u_r");
        this.arm_f_r = this.arm_u_r.getChild("arm_f_r");
        this.claw_r0 = this.arm_f_r.getChild("claw_r0");
        this.claw_r1 = this.arm_f_r.getChild("claw_r1");
        this.claw_r2 = this.arm_f_r.getChild("claw_r2");
        this.ex_u_r = this.body.getChild("ex_u_r");
        this.ex_f_r = this.ex_u_r.getChild("ex_f_r");
        this.thigh_r = root.getChild("thigh_r");
        this.shin_r = this.thigh_r.getChild("shin_r");
        this.foot_r = this.shin_r.getChild("foot_r");
        this.arm_u_l = this.body.getChild("arm_u_l");
        this.arm_f_l = this.arm_u_l.getChild("arm_f_l");
        this.claw_l0 = this.arm_f_l.getChild("claw_l0");
        this.claw_l1 = this.arm_f_l.getChild("claw_l1");
        this.claw_l2 = this.arm_f_l.getChild("claw_l2");
        this.ex_u_l = this.body.getChild("ex_u_l");
        this.ex_f_l = this.ex_u_l.getChild("ex_f_l");
        this.thigh_l = root.getChild("thigh_l");
        this.shin_l = this.thigh_l.getChild("shin_l");
        this.foot_l = this.shin_l.getChild("foot_l");
        this.pelvis = root.getChild("pelvis");
        this.cloth_f = this.body.getChild("cloth_f");
        this.cloth_bl = this.body.getChild("cloth_bl");
        this.cloth_br = this.body.getChild("cloth_br");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-3F, -14F, -2F, 6F, 14F, 4F), PartPose.offsetAndRotation(0F, 2F, 0F, 0F, 0F, 0F));
        PartDefinition p_ribcage = p_body.addOrReplaceChild("ribcage", CubeListBuilder.create().texOffs(28, 19).addBox(-4F, -12F, -3F, 8F, 9F, 1F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_spine0 = p_body.addOrReplaceChild("spine0", CubeListBuilder.create().texOffs(101, 34).addBox(-0.5F, -1F, 0F, 1F, 3F, 2F), PartPose.offsetAndRotation(0F, -3F, 2F, 0F, 0F, 0F));
        PartDefinition p_spine1 = p_body.addOrReplaceChild("spine1", CubeListBuilder.create().texOffs(108, 34).addBox(-0.5F, -1F, 0F, 1F, 3F, 2F), PartPose.offsetAndRotation(0F, -7F, 2F, 0F, 0F, 0F));
        PartDefinition p_spine2 = p_body.addOrReplaceChild("spine2", CubeListBuilder.create().texOffs(115, 34).addBox(-0.5F, -1F, 0F, 1F, 3F, 2F), PartPose.offsetAndRotation(0F, -11F, 2F, 0F, 0F, 0F));
        PartDefinition p_neck = p_body.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(47, 19).addBox(-1.5F, -7F, -1.5F, 3F, 7F, 3F), PartPose.offsetAndRotation(0F, -14F, 0F, 0F, 0F, 0F));
        PartDefinition p_head = p_neck.addOrReplaceChild("head", CubeListBuilder.create().texOffs(21, 0).addBox(-3.5F, -9F, -4F, 7F, 9F, 8F), PartPose.offsetAndRotation(0F, -7F, 0F, 0F, 0F, 0F));
        PartDefinition p_jaw = p_head.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(60, 19).addBox(-3F, 0F, -7F, 6F, 3F, 7F), PartPose.offsetAndRotation(0F, -1.5F, 3.5F, 0F, 0F, 0F));
        PartDefinition p_thorn_a_l = p_head.addOrReplaceChild("thorn_a_l", CubeListBuilder.create().texOffs(61, 34).addBox(-0.5F, -6F, -0.5F, 1F, 6F, 1F), PartPose.offsetAndRotation(2.2F, -8.5F, 1F, -0.3F, 0F, 0.45F));
        PartDefinition p_thorn_a_r = p_head.addOrReplaceChild("thorn_a_r", CubeListBuilder.create().texOffs(66, 34).addBox(-0.5F, -6F, -0.5F, 1F, 6F, 1F), PartPose.offsetAndRotation(-2.2F, -8.5F, 1F, -0.3F, 0F, -0.45F));
        PartDefinition p_thorn_b_l = p_head.addOrReplaceChild("thorn_b_l", CubeListBuilder.create().texOffs(122, 34).addBox(-0.5F, -4F, -0.5F, 1F, 4F, 1F), PartPose.offsetAndRotation(1.2F, -9F, -1.5F, -0.1F, 0F, 0.2F));
        PartDefinition p_thorn_b_r = p_head.addOrReplaceChild("thorn_b_r", CubeListBuilder.create().texOffs(0, 43).addBox(-0.5F, -4F, -0.5F, 1F, 4F, 1F), PartPose.offsetAndRotation(-1.2F, -9F, -1.5F, -0.1F, 0F, -0.2F));
        PartDefinition p_arm_u_r = p_body.addOrReplaceChild("arm_u_r", CubeListBuilder.create().texOffs(96, 0).addBox(-1F, 0F, -1F, 2F, 12F, 2F), PartPose.offsetAndRotation(-4.5F, -12F, 0F, 0F, 0F, 0F));
        PartDefinition p_arm_f_r = p_arm_u_r.addOrReplaceChild("arm_f_r", CubeListBuilder.create().texOffs(52, 0).addBox(-1F, 0F, -1F, 2F, 15F, 2F), PartPose.offsetAndRotation(0F, 12F, 0F, 0F, 0F, 0F));
        PartDefinition p_claw_r0 = p_arm_f_r.addOrReplaceChild("claw_r0", CubeListBuilder.create().texOffs(71, 34).addBox(-0.5F, 0F, -0.5F, 1F, 5F, 1F), PartPose.offsetAndRotation(-0.8F, 15F, 0F, 0F, 0F, -0.15F));
        PartDefinition p_claw_r1 = p_arm_f_r.addOrReplaceChild("claw_r1", CubeListBuilder.create().texOffs(76, 34).addBox(-0.5F, 0F, -0.5F, 1F, 5F, 1F), PartPose.offsetAndRotation(0.8F, 15F, 0F, 0F, 0F, 0.15F));
        PartDefinition p_claw_r2 = p_arm_f_r.addOrReplaceChild("claw_r2", CubeListBuilder.create().texOffs(81, 34).addBox(-0.5F, 0F, -0.5F, 1F, 5F, 1F), PartPose.offsetAndRotation(0F, 15F, -0.9F, 0.1F, 0F, 0F));
        PartDefinition p_ex_u_r = p_body.addOrReplaceChild("ex_u_r", CubeListBuilder.create().texOffs(87, 19).addBox(-0.5F, 0F, -0.5F, 1F, 9F, 1F), PartPose.offsetAndRotation(-3F, -7F, -1.5F, 0F, 0F, 0F));
        PartDefinition p_ex_f_r = p_ex_u_r.addOrReplaceChild("ex_f_r", CubeListBuilder.create().texOffs(9, 19).addBox(-0.5F, 0F, -0.5F, 1F, 11F, 1F), PartPose.offsetAndRotation(0F, 9F, 0F, 0F, 0F, 0F));
        PartDefinition p_thigh_r = root.addOrReplaceChild("thigh_r", CubeListBuilder.create().texOffs(70, 0).addBox(-1.5F, 0F, -1.5F, 3F, 12F, 3F), PartPose.offsetAndRotation(-2.5F, 2F, 0F, 0F, 0F, 0F));
        PartDefinition p_shin_r = p_thigh_r.addOrReplaceChild("shin_r", CubeListBuilder.create().texOffs(105, 0).addBox(-1F, 0F, -1F, 2F, 12F, 2F), PartPose.offsetAndRotation(0F, 12F, 0F, 0F, 0F, 0F));
        PartDefinition p_foot_r = p_shin_r.addOrReplaceChild("foot_r", CubeListBuilder.create().texOffs(0, 34).addBox(-1.5F, -1F, -5F, 3F, 2F, 6F), PartPose.offsetAndRotation(0F, 12F, 0F, 0F, 0F, 0F));
        PartDefinition p_arm_u_l = p_body.addOrReplaceChild("arm_u_l", CubeListBuilder.create().texOffs(114, 0).addBox(-1F, 0F, -1F, 2F, 12F, 2F), PartPose.offsetAndRotation(4.5F, -12F, 0F, 0F, 0F, 0F));
        PartDefinition p_arm_f_l = p_arm_u_l.addOrReplaceChild("arm_f_l", CubeListBuilder.create().texOffs(61, 0).addBox(-1F, 0F, -1F, 2F, 15F, 2F), PartPose.offsetAndRotation(0F, 12F, 0F, 0F, 0F, 0F));
        PartDefinition p_claw_l0 = p_arm_f_l.addOrReplaceChild("claw_l0", CubeListBuilder.create().texOffs(86, 34).addBox(-0.5F, 0F, -0.5F, 1F, 5F, 1F), PartPose.offsetAndRotation(-0.8F, 15F, 0F, 0F, 0F, -0.15F));
        PartDefinition p_claw_l1 = p_arm_f_l.addOrReplaceChild("claw_l1", CubeListBuilder.create().texOffs(91, 34).addBox(-0.5F, 0F, -0.5F, 1F, 5F, 1F), PartPose.offsetAndRotation(0.8F, 15F, 0F, 0F, 0F, 0.15F));
        PartDefinition p_claw_l2 = p_arm_f_l.addOrReplaceChild("claw_l2", CubeListBuilder.create().texOffs(96, 34).addBox(-0.5F, 0F, -0.5F, 1F, 5F, 1F), PartPose.offsetAndRotation(0F, 15F, -0.9F, 0.1F, 0F, 0F));
        PartDefinition p_ex_u_l = p_body.addOrReplaceChild("ex_u_l", CubeListBuilder.create().texOffs(92, 19).addBox(-0.5F, 0F, -0.5F, 1F, 9F, 1F), PartPose.offsetAndRotation(3F, -7F, -1.5F, 0F, 0F, 0F));
        PartDefinition p_ex_f_l = p_ex_u_l.addOrReplaceChild("ex_f_l", CubeListBuilder.create().texOffs(14, 19).addBox(-0.5F, 0F, -0.5F, 1F, 11F, 1F), PartPose.offsetAndRotation(0F, 9F, 0F, 0F, 0F, 0F));
        PartDefinition p_thigh_l = root.addOrReplaceChild("thigh_l", CubeListBuilder.create().texOffs(83, 0).addBox(-1.5F, 0F, -1.5F, 3F, 12F, 3F), PartPose.offsetAndRotation(2.5F, 2F, 0F, 0F, 0F, 0F));
        PartDefinition p_shin_l = p_thigh_l.addOrReplaceChild("shin_l", CubeListBuilder.create().texOffs(0, 19).addBox(-1F, 0F, -1F, 2F, 12F, 2F), PartPose.offsetAndRotation(0F, 12F, 0F, 0F, 0F, 0F));
        PartDefinition p_foot_l = p_shin_l.addOrReplaceChild("foot_l", CubeListBuilder.create().texOffs(19, 34).addBox(-1.5F, -1F, -5F, 3F, 2F, 6F), PartPose.offsetAndRotation(0F, 12F, 0F, 0F, 0F, 0F));
        PartDefinition p_pelvis = root.addOrReplaceChild("pelvis", CubeListBuilder.create().texOffs(38, 34).addBox(-3.5F, -2F, -2F, 7F, 4F, 4F), PartPose.offsetAndRotation(0F, 2F, 0F, 0F, 0F, 0F));
        PartDefinition p_cloth_f = p_body.addOrReplaceChild("cloth_f", CubeListBuilder.create().texOffs(19, 19).addBox(-1.5F, 0F, -0.5F, 3F, 10F, 1F), PartPose.offsetAndRotation(0F, 0F, -2.5F, 0F, 0F, 0F));
        PartDefinition p_cloth_bl = p_body.addOrReplaceChild("cloth_bl", CubeListBuilder.create().texOffs(97, 19).addBox(-1F, 0F, -0.5F, 2F, 9F, 1F), PartPose.offsetAndRotation(-2F, 0F, 2.5F, 0F, 0F, 0F));
        PartDefinition p_cloth_br = p_body.addOrReplaceChild("cloth_br", CubeListBuilder.create().texOffs(104, 19).addBox(-1F, 0F, -0.5F, 2F, 9F, 1F), PartPose.offsetAndRotation(2F, 0F, 2.5F, 0F, 0F, 0F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(T e, float limbSwing, float limbSwingAmount, float age, float netHeadYaw, float headPitch) {
        boolean aggro = e.isAggressive();
        float rad = Mth.DEG_TO_RAD;
        float walk = limbSwing * 0.6662F;
        float amt = Math.min(1.0F, limbSwingAmount);
        float sway = Mth.sin(age * 0.06F);

        // tronco corcunda e cabeça compensando para continuar olhando para frente
        body.xRot = 0.35F + sway * 0.03F + (aggro ? 0.15F : 0.0F);
        body.yRot = Mth.cos(walk) * 0.18F * amt;
        neck.xRot = 0.25F;
        head.xRot = headPitch * rad - 0.6F - (aggro ? 0.15F : 0.0F);
        head.yRot = netHeadYaw * rad * 0.8F - body.yRot;
        head.zRot = Mth.sin(age * 1.3F) * 0.05F + (Mth.sin(age * 0.17F) > 0.93F ? 0.4F : 0.0F); // tique
        jaw.xRot = aggro ? 0.95F + Mth.sin(age * 0.8F) * 0.25F : 0.2F + Mth.sin(age * 0.1F) * 0.05F;

        // braços principais
        float swingR = Mth.cos(walk + Mth.PI) * 0.9F * amt;
        float swingL = Mth.cos(walk) * 0.9F * amt;
        if (aggro) {
            arm_u_r.xRot = -1.55F + Mth.sin(age * 0.6F) * 0.10F;
            arm_u_l.xRot = -1.55F - Mth.sin(age * 0.6F) * 0.10F;
            arm_f_r.xRot = -0.35F + Mth.sin(age * 0.9F) * 0.15F;
            arm_f_l.xRot = -0.35F - Mth.sin(age * 0.9F) * 0.15F;
        } else {
            arm_u_r.xRot = -0.3F + swingR;
            arm_u_l.xRot = -0.3F + swingL;
            arm_f_r.xRot = -0.25F - Math.abs(swingR) * 0.4F;
            arm_f_l.xRot = -0.25F - Math.abs(swingL) * 0.4F;
        }
        arm_u_r.zRot = 0.12F;
        arm_u_l.zRot = -0.12F;

        // braços extras: aranha nervosa
        ex_u_r.xRot = -0.4F + Mth.sin(age * 0.7F) * 0.4F;
        ex_u_l.xRot = -0.4F + Mth.sin(age * 0.7F + 2.0F) * 0.4F;
        ex_u_r.zRot = 0.7F + Mth.sin(age * 0.9F) * 0.2F;
        ex_u_l.zRot = -0.7F - Mth.sin(age * 0.9F + 1.0F) * 0.2F;
        ex_f_r.xRot = -0.8F + Mth.sin(age * 1.1F) * 0.5F;
        ex_f_l.xRot = -0.8F + Mth.sin(age * 1.1F + 1.5F) * 0.5F;

        // pernas digitígradas (coxa para frente, canela para trás, pé sempre plano)
        float legR = Mth.cos(walk) * 0.9F * amt;
        float legL = Mth.cos(walk + Mth.PI) * 0.9F * amt;
        thigh_r.xRot = -0.55F + legR;
        shin_r.xRot = 1.15F - legR * 0.5F;
        foot_r.xRot = -(thigh_r.xRot + shin_r.xRot);
        thigh_l.xRot = -0.55F + legL;
        shin_l.xRot = 1.15F - legL * 0.5F;
        foot_l.xRot = -(thigh_l.xRot + shin_l.xRot);

        // trapos
        cloth_f.xRot = -0.35F + amt * 0.5F + Mth.sin(age * 0.10F) * 0.06F;
        cloth_bl.xRot = -0.25F + amt * 0.5F + Mth.sin(age * 0.10F + 1.0F) * 0.07F;
        cloth_br.xRot = -0.25F + amt * 0.5F + Mth.sin(age * 0.10F + 2.0F) * 0.07F;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer buf, int light, int overlay,
                               float r, float g, float b, float a) {
        root.render(ps, buf, light, overlay, r, g, b, a);
    }
}
