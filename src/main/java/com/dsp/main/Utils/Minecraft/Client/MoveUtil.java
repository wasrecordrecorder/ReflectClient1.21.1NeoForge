package com.dsp.main.Utils.Minecraft.Client;

import com.dsp.main.Core.Event.MoveInputEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

public class MoveUtil {
    private static final Minecraft mc = Minecraft.getInstance();

    public static boolean isMoving() {
        if (mc.player == null) return false;
        return mc.player.zza != 0 || mc.player.xxa != 0;
    }

    public static double getSpeed() {
        if (mc.player == null) return 0;
        double dx = mc.player.getX() - mc.player.xOld;
        double dz = mc.player.getZ() - mc.player.zOld;
        return Math.sqrt(dx * dx + dz * dz);
    }

    public static void setSpeed(double speed, float yaw, double strafe, double forward) {
        if (mc.player == null) return;

        if (forward != 0.0) {
            if (strafe > 0.0) {
                yaw += (forward > 0.0) ? -45 : 45;
            } else if (strafe < 0.0) {
                yaw += (forward > 0.0) ? 45 : -45;
            }
            strafe = 0;
            forward = forward > 0 ? 1 : -1;
        }

        if (strafe > 0.0) strafe = 1;
        if (strafe < 0.0) strafe = -1;

        double rad = Math.toRadians(yaw + 90);
        double sin = Math.sin(rad);
        double cos = Math.cos(rad);

        double motionX = forward * speed * cos + strafe * speed * sin;
        double motionZ = forward * speed * sin - strafe * speed * cos;

        mc.player.setDeltaMovement(motionX, mc.player.getDeltaMovement().y, motionZ);
    }

    public static void setSpeed(double speed) {
        if (mc.player == null) return;
        setSpeed(speed, mc.player.getYRot(), mc.player.xxa, mc.player.zza);
    }

    public static double getMovementYaw() {
        if (mc.player == null) return 0;
        float yaw = mc.player.getYRot();
        float forward = mc.player.zza;
        float strafe = mc.player.xxa;

        if (forward != 0) {
            if (strafe > 0) yaw += (forward > 0) ? -45 : 45;
            else if (strafe < 0) yaw += (forward > 0) ? 45 : -45;
            strafe = 0;
            forward = forward > 0 ? 1 : -1;
        }

        if (strafe > 0) yaw -= 90;
        if (strafe < 0) yaw += 90;

        return yaw;
    }

    public static void strafe(double speed) {
        if (mc.player == null || !isMoving()) return;

        double yaw = getMovementYaw();
        double x = -Math.sin(Math.toRadians(yaw)) * speed;
        double z = Math.cos(Math.toRadians(yaw)) * speed;

        mc.player.setDeltaMovement(x, mc.player.getDeltaMovement().y, z);
    }

    public static void stop() {
        if (mc.player == null) return;
        mc.player.setDeltaMovement(0, mc.player.getDeltaMovement().y, 0);
    }
    public static double[] forward(double speed) {
        if (mc.player == null) return new double[]{0, 0};
        float forward = mc.player.zza;
        float strafe = mc.player.xxa;
        float yaw = mc.player.getYRot();

        if (forward != 0) {
            if (strafe > 0) yaw += (forward > 0) ? -45 : 45;
            else if (strafe < 0) yaw += (forward > 0) ? 45 : -45;
            strafe = 0;
            forward = forward > 0 ? 1 : -1;
        }

        if (strafe > 0) strafe = 1;
        if (strafe < 0) strafe = -1;

        double rad = Math.toRadians(yaw + 90);
        double sin = Math.sin(rad);
        double cos = Math.cos(rad);

        double x = forward * speed * cos + strafe * speed * sin;
        double z = forward * speed * sin - strafe * speed * cos;
        return new double[]{x, z};
    }
    public static double direction(float rotationYaw, float moveForward, float moveStrafing) {
        if (moveForward < 0) rotationYaw += 180;
        float forward = 1F;
        if (moveForward < 0) forward = -0.5F;
        else if (moveForward > 0) forward = 0.5F;

        if (moveStrafing > 0) rotationYaw -= 90 * forward;
        if (moveStrafing < 0) rotationYaw += 90 * forward;

        return Math.toRadians(rotationYaw);
    }

    public static void fixMovement(MoveInputEvent event, float yaw) {
        float forward = event.getForward();
        float strafe = event.getStrafe();
        if (forward == 0 && strafe == 0) return;
        double targetAngle = Mth.wrapDegrees(Math.toDegrees(direction(mc.player.isFallFlying() ? mc.player.getYRot() : yaw, forward, strafe)));
        float closestForward = 0, closestStrafe = 0;
        float closestDifference = Float.MAX_VALUE;
        for (float predictedForward = -1; predictedForward <= 1; predictedForward++) {
            for (float predictedStrafe = -1; predictedStrafe <= 1; predictedStrafe++) {
                if (predictedForward == 0 && predictedStrafe == 0) continue;

                double predictedAngle = Mth.wrapDegrees(Math.toDegrees(direction(mc.player.getYRot(), predictedForward, predictedStrafe)));
                double difference = Math.abs(targetAngle - predictedAngle);

                if (difference < closestDifference) {
                    closestDifference = (float) difference;
                    closestForward = predictedForward;
                    closestStrafe = predictedStrafe;
                }
            }
        }
        event.setForward(closestForward);
        event.setStrafe(closestStrafe);
        event.setCanceled(true);
    }
}
