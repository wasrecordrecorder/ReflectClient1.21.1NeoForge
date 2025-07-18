package com.dsp.main.Mixin;

import com.dsp.main.Managers.FreeLook;
import net.minecraft.client.Camera;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow @Final private static Vector3f FORWARDS;
    @Shadow @Final private static Vector3f UP;
    @Shadow @Final private static Vector3f LEFT;
    @Shadow @Final private Vector3f up;
    @Shadow @Final private Vector3f forwards;
    @Shadow @Final private Vector3f left;
    @Shadow private float xRot;
    @Shadow private float yRot;
    @Shadow @Final private Quaternionf rotation;

    @Redirect(
            method = "setup",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Camera;setRotation(FFF)V"
            )
    )
    private void redirectSetRotation(Camera camera, float yRot, float xRot, float roll) {
        if (FreeLook.isFreeLookEnabled) {
            this.xRot = FreeLook.getCameraPitch();
            this.yRot = FreeLook.getCameraYaw();
            this.rotation.rotationYXZ(
                    (float) Math.PI - this.yRot * (float) (Math.PI / 180.0),
                    -this.xRot * (float) (Math.PI / 180.0),
                    0.0F
            );
            FORWARDS.rotate(this.rotation, this.forwards);
            UP.rotate(this.rotation, this.up);
            LEFT.rotate(this.rotation, this.left);
        } else {
            this.xRot = xRot;
            this.yRot = yRot;
            this.rotation.rotationYXZ(
                    (float) Math.PI - yRot * (float) (Math.PI / 180.0),
                    -xRot * (float) (Math.PI / 180.0),
                    -roll * (float) (Math.PI / 180.0)
            );
            FORWARDS.rotate(this.rotation, this.forwards);
            UP.rotate(this.rotation, this.up);
            LEFT.rotate(this.rotation, this.left);
        }
    }
}