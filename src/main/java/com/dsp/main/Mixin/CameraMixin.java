package com.dsp.main.Mixin;

import com.dsp.main.Core.Other.FreeLook;
import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.dsp.main.Api.*;
import static com.dsp.main.Main.isDetect;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow private boolean initialized;
    @Shadow private BlockGetter level;
    @Shadow private Entity entity;
    @Shadow private Vec3 position;
    @Shadow private final BlockPos.MutableBlockPos blockPosition = new BlockPos.MutableBlockPos();
    @Shadow private float xRot;
    @Shadow private float yRot;
    @Shadow private float roll;
    private float prev = 0;
    @Shadow private boolean detached;
    @Shadow private float eyeHeight;
    @Shadow private float eyeHeightOld;
    @Shadow private float partialTickTime;

    @Shadow protected abstract void setRotation(float yRot, float xRot, float roll);
    @Shadow protected abstract void setPosition(double x, double y, double z);
    @Shadow protected abstract void setPosition(Vec3 pos);
    @Shadow protected abstract void move(float zoom, float dy, float dx);
    @Shadow protected abstract float getMaxZoom(float maxZoom);

    @Inject(
            method = "setup(Lnet/minecraft/world/level/BlockGetter;" +
                    "Lnet/minecraft/world/entity/Entity;" +
                    "ZZF)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onSetup(BlockGetter level,
                         Entity entity,
                         boolean detached,
                         boolean thirdPersonReverse,
                         float partialTick,
                         CallbackInfo ci) {
        if (!isDetect) {
            this.initialized = true;
            this.level = level;
            this.entity = entity;
            this.detached = detached;
            this.partialTickTime = partialTick;
            double x = Mth.lerp(partialTick, entity.xo, entity.getX());
            double y = Mth.lerp(partialTick, entity.yo, entity.getY())
                    + Mth.lerp(partialTick, this.eyeHeightOld, this.eyeHeight);
            double z = Mth.lerp(partialTick, entity.zo, entity.getZ());
            this.setPosition(x, y, z);

            float yaw = FreeLook.isFreeLookEnabled ? FreeLook.getCameraYaw() : entity.getYRot();
            float pitch = FreeLook.isFreeLookEnabled ? FreeLook.getCameraPitch() : entity.getXRot();
            this.setRotation(yaw, pitch, 0.0F);
            if (detached) {
                if (thirdPersonReverse) {
                    prev = fast(prev, 180, 10);
                    this.setRotation(this.yRot + prev, -this.xRot, 0.0F);
                } else {
                    prev = fast(prev, 0, 10);
                }
                double temp;
                temp = getDistance(4);
                this.move((float) -temp, 0.0F, 0.0F);
            } else if (entity instanceof LivingEntity living && living.isSleeping()) {
                Direction bedDir = living.getBedOrientation();
                float bedYaw = (bedDir != null) ? bedDir.toYRot() - 180.0F : 0.0F;
                this.setRotation(bedYaw, 0.0F, roll);
                this.move(0.0F, 0.3F, 0.0F);
            }
        }
        if (!isDetect) ci.cancel();
    }
}