package com.dsp.main.Functions.Combat.Aura.impl.Rotation;

import com.dsp.main.Functions.Combat.Aura.Aura;
import com.dsp.main.Functions.Combat.Aura.impl.RotationAngle;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

import static com.dsp.main.Api.mc;

public class GrimRotation extends RotationAngle {
    private static float yawVelocity = 0.0F;
    private static float pitchVelocity = 0.0F;
    private static long lastPointSwitchTime = 0;
    private static Vec3 currentAimOffset = Vec3.ZERO;
    private static final long POINT_SWITCH_INTERVAL = 500;
    private static final Random random = new Random();
    private static final float MAX_YAW_STEP = 35.0F;
    private static final float MAX_PITCH_STEP = 25.0F;
    private static final float AIM_MODULO_THRESHOLD = 320.0F;

    @Override
    public void update(Aura aura, Entity target) {
        if (mc.player == null || target == null) return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastPointSwitchTime >= POINT_SWITCH_INTERVAL) {
            // Generate new random offset within target bounds
            float offsetX = (random.nextFloat() - 0.5f) * 0.7f;
            float offsetY = (random.nextFloat() - 0.5f) * 0.1f;
            float offsetZ = (random.nextFloat() - 0.5f) * 0.7f;
            currentAimOffset = new Vec3(offsetX, offsetY, offsetZ);
            lastPointSwitchTime = currentTime;
        }

        // Calculate target position with offset
        double deltaX = target.getX() + currentAimOffset.x - mc.player.getX();
        double deltaY = target.getY() + currentAimOffset.y - (mc.player.getY() + mc.player.getEyeHeight()) + 0.7D;
        double deltaZ = target.getZ() + currentAimOffset.z - mc.player.getZ();
        double distance = Mth.sqrt((float) (deltaX * deltaX + deltaZ * deltaZ));

        // Calculate raw yaw and pitch
        float rawYaw = (float) Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0F;
        float rawPitch = (float) -Math.toDegrees(Math.atan2(deltaY, distance));

        float currentYaw = mc.player.getYRot();
        float currentPitch = mc.player.getXRot();

        // Calculate differences and apply smoothing
        float deltaYaw = Mth.wrapDegrees(rawYaw - currentYaw);
        float deltaPitch = normalizeDeltaPitch(Mth.wrapDegrees(rawPitch - currentPitch));

        // Apply step limits and random offset
        float stepYaw = Mth.clamp(deltaYaw, -MAX_YAW_STEP, MAX_YAW_STEP);
        float stepPitch = Mth.clamp(deltaPitch, -MAX_PITCH_STEP, MAX_PITCH_STEP);

        stepYaw += randomOffset(1.0f);
        stepPitch += randomOffset(1.0f);

        // Apply velocity with friction
        float accelerationFactor = 0.3F * 0.3f;
        float frictionFactor = 0.7f;
        yawVelocity = (yawVelocity + stepYaw * accelerationFactor) * frictionFactor;
        pitchVelocity = (pitchVelocity + stepPitch * accelerationFactor) * frictionFactor;

        if (Math.abs(deltaYaw) < 0.1f) {
            yawVelocity = 0;
        }
        if (Math.abs(deltaPitch) < 0.1f) {
            pitchVelocity = 0;
        }

        float newYaw = currentYaw + yawVelocity;
        float newPitch = currentPitch + pitchVelocity;

        float verifyDeltaPitch = Mth.wrapDegrees(newPitch - currentPitch);
        if (Math.abs(verifyDeltaPitch) > AIM_MODULO_THRESHOLD) {
            newPitch = currentPitch + Math.signum(verifyDeltaPitch) * MAX_PITCH_STEP;
        }
        mc.player.setYRot(snapToMouseSensitivity(newYaw));
        if (!isRayIntersectingAABB(mc.player, target)) {
            newPitch = Mth.clamp(newPitch + 0.4F, -90.0F, 90.0F);
            mc.player.setXRot(snapToMouseSensitivity(newPitch));
        }
        mc.player.yHeadRot = mc.player.getYRot();
    }

    private float normalizeDeltaPitch(float deltaPitch) {
        if (Math.abs(deltaPitch) > AIM_MODULO_THRESHOLD) {
            return deltaPitch > 0 ? deltaPitch - 360.0F : deltaPitch + 360.0F;
        }
        return deltaPitch;
    }

    private float randomOffset(float maxOffset) {
        return (float)(Math.random() * 2 * maxOffset - maxOffset);
    }
}