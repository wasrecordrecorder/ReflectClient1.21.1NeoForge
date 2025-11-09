package com.dsp.main.Mixin;

import com.dsp.main.Functions.Render.ViewModel;
import com.dsp.main.Utils.Render.SwingAnimation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.dsp.main.Api.isEnabled;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {

    @Unique
    private static final SwingAnimation swingAnim = new SwingAnimation(0, 0, 0.1f);

    @Inject(
            method = "renderArmWithItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V",
                    shift = At.Shift.AFTER
            )
    )
    private void applyHandOffset(
            AbstractClientPlayer player, float partialTicks, float pitch, InteractionHand hand, float swingProgress, ItemStack stack, float equippedProgress, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, CallbackInfo ci
    ) {
        if (!isEnabled("ViewModel")) return;

        if (hand == InteractionHand.MAIN_HAND) {
            HumanoidArm mainArm = player.getMainArm();
            if (mainArm == HumanoidArm.RIGHT) {
                poseStack.translate(
                        ViewModel.rightX.getValue(),
                        ViewModel.rightY.getValue(),
                        ViewModel.rightZ.getValue()
                );
            } else {
                poseStack.translate(
                        ViewModel.leftX.getValue(),
                        ViewModel.leftY.getValue(),
                        ViewModel.leftZ.getValue()
                );
            }
        } else {
            HumanoidArm mainArm = player.getMainArm();
            if (mainArm == HumanoidArm.RIGHT) {
                poseStack.translate(
                        ViewModel.leftX.getValue(),
                        ViewModel.leftY.getValue(),
                        ViewModel.leftZ.getValue()
                );
            } else {
                poseStack.translate(
                        ViewModel.rightX.getValue(),
                        ViewModel.rightY.getValue(),
                        ViewModel.rightZ.getValue()
                );
            }
        }
    }

    @Inject(
            method = "applyItemArmAttackTransform",
            at = @At("HEAD"),
            cancellable = true
    )
    private void customSwingAnimation(
            PoseStack poseStack,
            HumanoidArm arm,
            float swingProgress,
            CallbackInfo ci
    ) {
        if (!isEnabled("ViewModel") || !ViewModel.animationEnabled.isEnabled()) {
            return;
        }

        int side = arm == HumanoidArm.RIGHT ? 1 : -1;

        swingAnim.speed = (float) ViewModel.animationSmoothness.getValue();
        swingAnim.setTarget(Mth.sin(swingProgress * swingProgress * (float) Math.PI));
        float animValue = swingAnim.getAnimation();

        float power = (float) ViewModel.animationPower.getValue();
        String mode = ViewModel.animationMode.getMode();

        switch (mode) {
            case "Short" -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(side * (45 + Mth.sin(swingProgress * swingProgress * (float) Math.PI) * -20)));
                float f1 = Mth.sin(Mth.sqrt(swingProgress) * (float) Math.PI);
                poseStack.mulPose(Axis.ZP.rotationDegrees(side * f1 * -20));
                poseStack.mulPose(Axis.XP.rotationDegrees(f1 * -power));
                poseStack.mulPose(Axis.YP.rotationDegrees(side * -45));
                ci.cancel();
            }
            case "Simple" -> {
                poseStack.translate(0, 0.1, 0);
                poseStack.mulPose(Axis.XP.rotationDegrees(animValue * -power));
                poseStack.mulPose(Axis.ZP.rotationDegrees(animValue * 45));
                poseStack.mulPose(Axis.YP.rotationDegrees(animValue * 15));
                poseStack.translate(0, -0.1, 0);
                ci.cancel();
            }
            case "Inward" -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(90));
                poseStack.mulPose(Axis.ZP.rotationDegrees(-70));
                poseStack.mulPose(Axis.XP.rotationDegrees(-90 + -power * animValue));
                ci.cancel();
            }
            case "Outward" -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(90));
                poseStack.mulPose(Axis.ZP.rotationDegrees(-70));
                poseStack.mulPose(Axis.XP.rotationDegrees(-90 + power * animValue));
                ci.cancel();
            }
            case "Block" -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(76));
                poseStack.mulPose(Axis.YP.rotationDegrees(animValue * -5));
                poseStack.mulPose(Axis.XN.rotationDegrees(animValue * power));
                poseStack.mulPose(Axis.XP.rotationDegrees(-90));
                ci.cancel();
            }
            case "Size" -> {
                poseStack.scale(1, 1, animValue * 5 + 1);
                poseStack.mulPose(Axis.YP.rotationDegrees(60));
                poseStack.mulPose(Axis.XP.rotationDegrees(-90));
                poseStack.mulPose(Axis.ZP.rotationDegrees(-45));
                ci.cancel();
            }
            case "360" -> {
                poseStack.mulPose(Axis.XP.rotationDegrees(swingProgress * -360));
                ci.cancel();
            }
            case "Mode" -> {
                poseStack.translate(0.5, -0.1, 0);
                poseStack.mulPose(Axis.XP.rotationDegrees(animValue * -45));
                poseStack.translate(-0.5, 0.1, 0);
                poseStack.translate(0.5, -0.1, 0);
                poseStack.mulPose(Axis.YP.rotationDegrees(animValue * -20));
                poseStack.translate(-0.5, 0.1, 0);
                poseStack.mulPose(Axis.YP.rotationDegrees(50));
                poseStack.mulPose(Axis.XP.rotationDegrees(-90));
                poseStack.mulPose(Axis.YP.rotationDegrees(50));
                ci.cancel();
            }
        }
    }
}