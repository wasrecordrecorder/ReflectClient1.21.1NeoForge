package com.dsp.main.Functions.Combat.Aura.impl.Rotation;

import com.dsp.main.Functions.Combat.Aura.Aura;
import com.dsp.main.Functions.Combat.Aura.impl.RotationAngle;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import static com.dsp.main.Api.mc;

public class AdaptiveRotation extends RotationAngle {
    private static final float BASE_YAW_SPEED = 18.0F;
    private static final float BASE_PITCH_SPEED = 14.0F;

    @Override
    public void update(Aura aura, Entity target) {
        if (mc.player == null || target == null) return;

        Vec3 playerPos = mc.player.getEyePosition();
        Vec3 targetPos = new Vec3(
                target.getX(),
                target.getY() + target.getBbHeight() * 0.5,
                target.getZ()
        );

        double deltaX = targetPos.x - playerPos.x;
        double deltaY = (targetPos.y + target.getEyeHeight() - target.getBbHeight() * 0.5) - playerPos.y;
        double deltaZ = targetPos.z - playerPos.z;

        double distanceXZ = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        float targetYaw = (float) Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90F;
        float targetPitch = (float) -Math.toDegrees(Math.atan2(deltaY, distanceXZ));

        targetYaw = normalizeAngle(targetYaw);
        float currentYaw = normalizeAngle(mc.player.getYRot());
        float yawDelta = normalizeAngle(targetYaw - currentYaw);
        float pitchDelta = targetPitch - mc.player.getXRot();

        double currentTime = System.currentTimeMillis();
        boolean shouldBoost = Math.sin(currentTime / 400.0) > 0.85;
        float speedMultiplier = shouldBoost ? 1.6F : 1.0F;
        float smoothBoost = shouldBoost ?
                (float) (Math.sin((currentTime % 400) / 400.0 * Math.PI) * 0.6 + 1.0) : 1.0F;

        float rotationDifference = (float) Math.hypot(yawDelta, pitchDelta);
        boolean isTargetBehind = Math.abs(yawDelta) > 90.0F;
        float backTargetMultiplier = isTargetBehind ? 1.8F : 1.0F;

        if (isTargetBehind) {
            float smoothBackTurn = (float) (Math.sin(currentTime / 200.0) * 0.1 + 0.9);
            backTargetMultiplier *= smoothBackTurn;
        }

        float finalYawSpeed = BASE_YAW_SPEED * speedMultiplier * smoothBoost * backTargetMultiplier;
        float finalPitchSpeed = BASE_PITCH_SPEED * speedMultiplier * smoothBoost;

        float microAdjustment = (float) (
                Math.sin(currentTime / 100.0) * 0.15 +
                        Math.cos(currentTime / 150.0) * 0.1
        );

        float moveYaw = Mth.clamp(yawDelta, -finalYawSpeed, finalYawSpeed);
        float movePitch = Mth.clamp(pitchDelta, -finalPitchSpeed, finalPitchSpeed);

        if (rotationDifference < 10.0F) {
            moveYaw += microAdjustment * 0.3F;
            movePitch += microAdjustment * 0.2F;
        }

        float newYaw = normalizeAngle(currentYaw + moveYaw);
        float newPitch = Mth.clamp(mc.player.getXRot() + movePitch, -90.0F, 90.0F);

        mc.player.setYRot(newYaw);
        mc.player.setXRot(newPitch);
    }

    private float normalizeAngle(float angle) {
        while (angle > 180F) angle -= 360F;
        while (angle <= -180F) angle += 360F;
        return angle;
    }
}