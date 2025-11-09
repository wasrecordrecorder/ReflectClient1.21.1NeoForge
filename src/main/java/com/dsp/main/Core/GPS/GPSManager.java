package com.dsp.main.Core.GPS;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class GPSManager {
    private static boolean enabled = false;
    private static BlockPos targetPos = null;
    private static String targetName = "";

    public static void enableGps(int x, int y, int z) {
        enableGps(x, y, z, "Target");
    }

    public static void enableGps(int x, int y, int z, String name) {
        targetPos = new BlockPos(x, y, z);
        targetName = name;
        enabled = true;
    }

    public static void disableGps() {
        enabled = false;
        targetPos = null;
        targetName = "";
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static BlockPos getTargetPos() {
        return targetPos;
    }

    public static String getTargetName() {
        return targetName;
    }

    public static double getDistance(Vec3 from) {
        if (targetPos == null) return 0;
        return from.distanceTo(Vec3.atCenterOf(targetPos));
    }

    public static float getAngleToTarget(Vec3 from, float playerYaw) {
        if (targetPos == null) return 0;

        Vec3 target = Vec3.atCenterOf(targetPos);
        double deltaX = target.x - from.x;
        double deltaZ = target.z - from.z;

        float angleToTarget = (float) Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90f;
        float relativeAngle = angleToTarget - playerYaw;

        while (relativeAngle > 180) relativeAngle -= 360;
        while (relativeAngle < -180) relativeAngle += 360;

        return relativeAngle;
    }
}