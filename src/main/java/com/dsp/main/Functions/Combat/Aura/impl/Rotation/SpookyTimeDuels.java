package com.dsp.main.Functions.Combat.Aura.impl.Rotation;

import com.dsp.main.Functions.Combat.Aura.Aura;
import com.dsp.main.Functions.Combat.Aura.impl.RotationAngle;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

import static com.dsp.main.Api.mc;

public class SpookyTimeDuels extends RotationAngle {
    public static float yawDifference = 0;
    private static float yawVelocity = 0.0F;
    private static float pitchVelocity = 0.0F;
    private static long lastPointSwitchTime = 0;
    private static Vec3 currentAimOffset = Vec3.ZERO;
    private static final long POINT_SWITCH_INTERVAL = 500; // 500ms between point switches
    private static final Random random = new Random();

    @Override
    public void update(Aura aura, Entity target) {
        if (mc.player == null || target == null) return;

        // Update aim point periodically
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastPointSwitchTime >= POINT_SWITCH_INTERVAL) {
            // Generate new random offset within target bounds
            float offsetX = (random.nextFloat() - 0.5f) * 0.4f; // Within ±0.4 blocks
            float offsetY = (random.nextFloat() - 0.5f) * 0.6f; // Within ±0.5 blocks
            float offsetZ = (random.nextFloat() - 0.5f) * 0.4f; // Within ±0.4 blocks
            currentAimOffset = new Vec3(offsetX, offsetY, offsetZ);
            lastPointSwitchTime = currentTime;
        }

        // Calculate target position with offset
        double deltaX = target.getX() + currentAimOffset.x - mc.player.getX();
        double deltaY = target.getY() + currentAimOffset.y - (mc.player.getY() + mc.player.getEyeHeight()) + 0.7D;
        double deltaZ = target.getZ() + currentAimOffset.z - mc.player.getZ();
        double distance = Mth.sqrt((float) (deltaX * deltaX + deltaZ * deltaZ));
        float FSetCalculate = (float) (Mth.atan2(deltaZ, deltaX) * (180D / Math.PI)) - 90;
        yawDifference = Mth.wrapDegrees(FSetCalculate - mc.player.getYRot());

        int[] fset1Cycle = {94, 95, 96};
        int[] fset2Cycle = {85, 84, 83};
        int cycleIndex = (int) ((currentTime / 800) % 3);
        int Fset1 = fset1Cycle[cycleIndex];
        int Fset2 = fset2Cycle[cycleIndex];
        float Fsett = yawDifference > 0 ? Fset1 : Fset2;
        float yaw = (float) (Mth.atan2(deltaZ, deltaX) * (180D / Math.PI)) - Fsett;

        float pitch = (float) (-(Mth.atan2(deltaY, distance) * (180D / Math.PI)));
        // Add oscillation for more dynamic movement
        float time = (currentTime % 1000) / 1000F;
        float oscillation = (2 + random.nextInt(8)) * (float) Math.sin(2 * Math.PI * 0.7 * time);
        pitch += oscillation;
        float radius = (6) * (1 - time);
        yaw += (float) (radius * Math.cos(time * 10));

        float currentYaw = mc.player.getYRot();
        float currentPitch = mc.player.getXRot();
        float yawDiff = Mth.wrapDegrees(yaw - currentYaw);
        float pitchDiff = Mth.wrapDegrees(pitch - currentPitch);
        float accelerationFactor = 0.3F * 0.3f;
        float frictionFactor = 0.7f;
        yawVelocity = (yawVelocity + yawDiff * accelerationFactor) * frictionFactor;
        if (Math.abs(yawDiff) < 0.1f) {
            yawVelocity = 0;
        }
        float newYaw = currentYaw + yawVelocity;
        mc.player.setYRot(snapToMouseSensitivity(newYaw));
        if (!isRayIntersectingAABB(mc.player, target)) {
            pitchVelocity = (pitchVelocity + pitchDiff * accelerationFactor) * frictionFactor;
            if (Math.abs(pitchDiff) < 0.1F) {
                pitchVelocity = 0;
            }
            float newPitch = Mth.clamp(currentPitch + pitchVelocity + 0.2F, -90.0F, 90.0F);
            mc.player.setXRot(snapToMouseSensitivity(newPitch));
        }
        mc.player.yHeadRot = mc.player.getYRot();
    }
}