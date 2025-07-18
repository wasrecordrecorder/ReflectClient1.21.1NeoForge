package com.dsp.main.Functions.Combat.Aura.impl.Rotation;

import com.dsp.main.Functions.Combat.Aura.Aura;
import com.dsp.main.Functions.Combat.Aura.impl.RotationAngle;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import static com.dsp.main.Api.mc;

public class FunTime extends RotationAngle {
    private Vec3 currentTargetPos;
    private Vec3 randomTargetPos;
    private InterpolationType currentInterpolation;
    private static final float LERP_SPEED = 0.5F;
    private static final float THRESHOLD = 0.01F;
    private static final float ROTATION_FACTOR = 0.2F;
    private static final float MAX_ROTATION_SPEED = 20.0F;
    private enum InterpolationType {
        LINEAR,
        SMOOTHSTEP,
        SMOOTHERSTEP,
        COSINE,
        CATMULL_ROM
    }

    @Override
    public void update(Aura aura, Entity target) {
        if (mc.player == null || target == null) return;
        if (currentTargetPos == null) {
            currentTargetPos = target.position().add(0, target.getBbHeight() * 0.5, 0);
        }
        if (randomTargetPos == null || currentTargetPos.distanceTo(randomTargetPos) < THRESHOLD) {
            randomTargetPos = getRandomPointInHitbox(target);
            currentInterpolation = InterpolationType.values()[(int) (Math.random() * InterpolationType.values().length)];
        }
        currentTargetPos = interpolate(currentTargetPos, randomTargetPos, LERP_SPEED, currentInterpolation);
        Vec3 playerPos = mc.player.getEyePosition();
        double deltaX = currentTargetPos.x - playerPos.x;
        double deltaY = currentTargetPos.y - playerPos.y;
        double deltaZ = currentTargetPos.z - playerPos.z;
        double distanceXZ = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        float targetYaw = (float) Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90F;
        float targetPitch = (float) -Math.toDegrees(Math.atan2(deltaY, distanceXZ));
        targetYaw = normalizeAngle(targetYaw);
        float currentYaw = normalizeAngle(mc.player.getYRot());
        float yawDiff = normalizeAngle(targetYaw - currentYaw);
        float yawStep = Math.min(Math.abs(yawDiff) * ROTATION_FACTOR, MAX_ROTATION_SPEED) * Math.signum(yawDiff);
        float newYaw = currentYaw + yawStep;
        float currentPitch = mc.player.getXRot();
        float pitchDiff = targetPitch - currentPitch;
        float pitchStep = Math.min(Math.abs(pitchDiff) * ROTATION_FACTOR, MAX_ROTATION_SPEED) * Math.signum(pitchDiff);
        float newPitch = currentPitch + pitchStep;
        mc.player.setYRot(newYaw);
        mc.player.setXRot(newPitch);
    }

    private Vec3 interpolate(Vec3 from, Vec3 to, float t, InterpolationType type) {
        switch (type) {
            case LINEAR:
                return lerp(from, to, t);
            case SMOOTHSTEP:
                return smoothstep(from, to, t);
            case SMOOTHERSTEP:
                return smootherstep(from, to, t);
            case COSINE:
                return cosine(from, to, t);
            case CATMULL_ROM:
                return catmullRom(from, to, t);
            default:
                return lerp(from, to, t);
        }
    }

    private Vec3 lerp(Vec3 from, Vec3 to, float t) {
        double x = from.x + (to.x - from.x) * t;
        double y = from.y + (to.y - from.y) * t;
        double z = from.z + (to.z - from.z) * t;
        return new Vec3(x, y, z);
    }

    private Vec3 smoothstep(Vec3 from, Vec3 to, float t) {
        float tSmooth = t * t * (3 - 2 * t);
        double x = from.x + (to.x - from.x) * tSmooth;
        double y = from.y + (to.y - from.y) * tSmooth;
        double z = from.z + (to.z - from.z) * tSmooth;
        return new Vec3(x, y, z);
    }

    private Vec3 smootherstep(Vec3 from, Vec3 to, float t) {
        float tSmoother = t * t * t * (t * (t * 6 - 15) + 10);
        double x = from.x + (to.x - from.x) * tSmoother;
        double y = from.y + (to.y - from.y) * tSmoother;
        double z = from.z + (to.z - from.z) * tSmoother;
        return new Vec3(x, y, z);
    }

    private Vec3 cosine(Vec3 from, Vec3 to, float t) {
        float tCos = (float) (1.0 - Math.cos(t * Math.PI)) / 2.0f;
        double x = from.x + (to.x - from.x) * tCos;
        double y = from.y + (to.y - from.y) * tCos;
        double z = from.z + (to.z - from.z) * tCos;
        return new Vec3(x, y, z);
    }

    private Vec3 catmullRom(Vec3 from, Vec3 to, float t) {
        float t2 = t * t;
        float t3 = t2 * t;
        float h1 = 2 * t3 - 3 * t2 + 1;
        float h2 = -2 * t3 + 3 * t2;
        double x = from.x * h1 + to.x * h2;
        double y = from.y * h1 + to.y * h2;
        double z = from.z * h1 + to.z * h2;
        return new Vec3(x, y, z);
    }

    private Vec3 getRandomPointInHitbox(Entity target) {
        AABB bb = target.getBoundingBox();
        double minX = bb.minX;
        double minY = bb.minY;
        double minZ = bb.minZ;
        double maxX = bb.maxX;
        double maxY = bb.maxY;
        double maxZ = bb.maxZ;
        double randX = minX + Math.random() * (maxX - minX);
        double randY = minY + Math.random() * (maxY - minY);
        double randZ = minZ + Math.random() * (maxZ - minZ);
        return new Vec3(randX, randY, randZ);
    }

    private float normalizeAngle(float angle) {
        while (angle > 180F) angle -= 360F;
        while (angle <= -180F) angle += 360F;
        return angle;
    }
}