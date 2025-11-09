package com.dsp.main.Mixin;

import com.dsp.main.Api;
import com.dsp.main.Functions.Render.NoRender;
import com.dsp.main.Core.Other.FreeLook;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@OnlyIn(Dist.CLIENT)
@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @ModifyArg(
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/joml/Vector3f;<init>(FFF)V",
                    ordinal = 0,
                    remap = false
            ),
            index = 0
    )
    private float disableConfusionX(float x) {
        if (NoRender.NoRenderElements.isOptionEnabled("Bad Effects") && Api.isEnabled("NoRender")) {
            return 0.0F;
        }
        return x;
    }

    @ModifyArg(
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/joml/Vector3f;<init>(FFF)V",
                    ordinal = 0,
                    remap = false
            ),
            index = 1
    )
    private float disableConfusionY(float y) {
        if (NoRender.NoRenderElements.isOptionEnabled("Bad Effects") && Api.isEnabled("NoRender")) {
            return 0.0F;
        }
        return y;
    }

    @ModifyArg(
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/joml/Vector3f;<init>(FFF)V",
                    ordinal = 0,
                    remap = false
            ),
            index = 2
    )
    private float disableConfusionZ(float z) {
        if (NoRender.NoRenderElements.isOptionEnabled("Bad Effects") && Api.isEnabled("NoRender")) {
            return 0.0F;
        }
        return z;
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
