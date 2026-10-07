package com.bleachmod.client.model;

import com.bleachmod.Reference;
import com.bleachmod.common.data.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

/** Native ice material on independent plates; skin and equipment stay visible between plates. */
public final class IceArmorLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Reference.id("ice_armor"), "main");
    private static final ResourceLocation TEXTURE = new ResourceLocation("minecraft", "textures/block/packed_ice.png");
    private final ModelPart root;
    public IceArmorLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent, ModelPart root) { super(parent); this.root = root; }
    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition(); PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0,0)
                .addBox(-4.6F, 1, -2.7F, 3, 5, 1.2F).addBox(1.5F, 7, -2.8F, 3, 4, 1.3F)
                .addBox(-4, 0, -2.8F, 8, 1.2F, 5.6F), PartPose.ZERO);
        root.addOrReplaceChild("arm", CubeListBuilder.create().texOffs(0,0)
                .addBox(-2.6F, -2.5F, -2.6F, 5.2F, 4, 5.2F).addBox(-2.5F, 5, -2.5F, 5, 3, 5), PartPose.ZERO);
        root.addOrReplaceChild("leg", CubeListBuilder.create().texOffs(0,0).addBox(-2.4F, 5, -2.7F, 4.8F, 6, 1.2F), PartPose.ZERO);
        root.addOrReplaceChild("wing", CubeListBuilder.create().texOffs(0,0)
                .addBox(2, -6, 3.5F, 18, 2, 1).addBox(5, -4, 3.5F, 14, 12, 0.8F)
                .addBox(17, -7, 3, 3, 18, 1.5F).addBox(11, 6, 3, 2, 6, 1.2F).addBox(6, 5, 3, 2, 5, 1.2F), PartPose.ZERO);
        root.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(0,0)
                .addBox(-1.5F, 9, 3, 3, 5, 4).addBox(-1, 13, 6, 2, 5, 4).addBox(-0.6F, 17, 9, 1.2F, 5, 3), PartPose.ZERO);
        return LayerDefinition.create(mesh, 16, 16);
    }
    @Override public void render(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player,
                                 float limbSwing, float limbAmount, float partialTick, float age, float yaw, float pitch) {
        if (player.isInvisible() || player.isSpectator()) return;
        PlayerCapability.get(player).ifPresent(d -> {
            if (!CharacterData.HYORINMARU.equals(d.getCharacter().getZanpakutoIdentity())) return;
            String form = d.getCharacter().getActiveForm();
            int stage = "bankai".equals(form) ? 3 : "shikai".equals(form) ? 2 : d.getCharacter().isIceArmorVisual() ? 1 : 0;
            if (stage == 0) return;
            draw(pose, buffers, light, getParentModel().body, root.getChild("body"), 1);
            draw(pose, buffers, light, getParentModel().leftArm, root.getChild("arm"), 1);
            if (stage >= 2) {
                draw(pose, buffers, light, getParentModel().rightArm, root.getChild("arm"), 1);
                draw(pose, buffers, light, getParentModel().leftLeg, root.getChild("leg"), 1);
                draw(pose, buffers, light, getParentModel().rightLeg, root.getChild("leg"), 1);
            }
            if (stage == 3) {
                draw(pose, buffers, light, getParentModel().body, root.getChild("tail"), 1);
                ModelPart wing = root.getChild("wing"); wing.zRot = -0.15F + (float) Math.sin(age * 0.08) * 0.05F;
                draw(pose, buffers, light, getParentModel().body, wing, 1);
                draw(pose, buffers, light, getParentModel().body, wing, -1);
            }
        });
    }
    private static void draw(PoseStack pose, MultiBufferSource buffers, int light, ModelPart anchor, ModelPart part, float mirror) {
        pose.pushPose(); anchor.translateAndRotate(pose); pose.scale(mirror, 1, 1);
        part.render(pose, buffers.getBuffer(RenderType.entityTranslucent(TEXTURE)), light, OverlayTexture.NO_OVERLAY, 0.65F, 0.9F, 1, 0.85F);
        pose.popPose();
    }
}
