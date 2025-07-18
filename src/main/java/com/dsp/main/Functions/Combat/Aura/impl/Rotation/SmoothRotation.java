package com.dsp.main.Functions.Combat.Aura.impl.Rotation;

import com.dsp.main.Functions.Combat.Aura.Aura;
import com.dsp.main.Functions.Combat.Aura.impl.RotationAngle;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import static com.dsp.main.Api.mc;

public class SmoothRotation extends RotationAngle {
    private static final float SMOOTH_SPEED = 20.0F;

    @Override
    public void update(Aura aura, Entity target) {
        if (mc.player == null || target == null) return;

        Vec3 playerPos = mc.player.getEyePosition();
        Vec3 targetPos = target.position().add(0, target.getBbHeight() * 0.5, 0);

        double deltaX = targetPos.x - playerPos.x;
        double deltaY = targetPos.y - playerPos.y;
        double deltaZ = targetPos.z - playerPos.z;

        double distanceXZ = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        float targetYaw = (float) Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90F;
        float targetPitch = (float) -Math.toDegrees(Math.atan2(deltaY, distanceXZ));
        targetYaw = normalizeAngle(targetYaw);
        float currentYaw = normalizeAngle(mc.player.getYRot());
        float yawDiff = normalizeAngle(targetYaw - currentYaw);
        float yawStep = Math.min(Math.abs(yawDiff), SMOOTH_SPEED) * Math.signum(yawDiff);
        float newYaw = currentYaw + yawStep;
        float currentPitch = mc.player.getXRot();
        float pitchDiff = targetPitch - currentPitch;
        float pitchStep = Math.min(Math.abs(pitchDiff), SMOOTH_SPEED) * Math.signum(pitchDiff);
        float newPitch = currentPitch + pitchStep;
        mc.player.setYRot(newYaw);
        mc.player.setXRot(newPitch);
    }

    private float normalizeAngle(float angle) {
        while (angle > 180F) angle -= 360F;
        while (angle <= -180F) angle += 360F;
        return angle;
    }
}