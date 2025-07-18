package com.dsp.main.Mixin;

import com.dsp.main.Api;
import com.dsp.main.Functions.Render.NoRender;
import com.dsp.main.Managers.FreeLook;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@OnlyIn(Dist.CLIENT)
@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(
            method = "renderConfusionOverlay",
            at = @At("HEAD"),
            cancellable = true
    )
    private void renderConfusionOverlay(GuiGraphics guiGraphics, float scalar, CallbackInfo ci) {
        if (NoRender.NoRenderElements.isOptionEnabled("Bad Effects") && Api.isEnabled("NoRender")) {
            ci.cancel();
        }
    }
    @Inject(method = "bobHurt", at = @At("HEAD"), cancellable = true)
    public void bobHurn(PoseStack poseStack, float partialTicks, CallbackInfo ci) {
        if (Api.isEnabled("NoRender") && NoRender.NoRenderElements.isOptionEnabled("BobHurt")) {
            ci.cancel();
        }
    }
    @Redirect(
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/joml/Quaternionf;conjugate(Lorg/joml/Quaternionf;)Lorg/joml/Quaternionf;"
            )
    )
    private Quaternionf redirectCameraRotationConjugate(Quaternionf quaternionf, Quaternionf dest) {
        if (FreeLook.isFreeLookEnabled) {
            return new Quaternionf().rotationYXZ(
                    (float) Math.PI - FreeLook.getCameraYaw() * (float) (Math.PI / 180.0),
                    -FreeLook.getCameraPitch() * (float) (Math.PI / 180.0),
                    0.0F
            ).conjugate(dest);
        }
        return quaternionf.conjugate(dest);
    }
}
