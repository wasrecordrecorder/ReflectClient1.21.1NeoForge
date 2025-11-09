package com.dsp.main.Utils.Minecraft.Client;

import net.minecraft.client.player.LocalPlayer;

public class ClientFallDistance {

    protected static float fallDistance = 0.0F;

    private static boolean wasOnGround = false;
    private static double lastMotionY = 0.0;

    public static void update(LocalPlayer player) {
        if (player == null) {
            reset();
            return;
        }

        boolean onGround = player.onGround();
        double motionY = player.getDeltaMovement().y;

        if (onGround) {
            if (fallDistance > 0.0F && !wasOnGround) {
                onLanded(player);
            }
            fallDistance = 0.0F;
        } else {
            if (motionY < 0.0) {
                fallDistance -= (float)motionY;
            }
        }

        wasOnGround = onGround;
        lastMotionY = motionY;
    }

    private static void onLanded(LocalPlayer player) {
        if (fallDistance > 3.0F) {
            System.out.println("Landed with fall distance: " + fallDistance);
        }
    }

    public static void reset() {
        fallDistance = 0.0F;
        wasOnGround = false;
        lastMotionY = 0.0;
    }

    public static float get() {
        return fallDistance;
    }

    public static boolean isFalling() {
        return fallDistance > 0.0F && !wasOnGround;
    }

    public static boolean willTakeDamage() {
        return fallDistance > 3.0F;
    }

    public static float calculateDamage() {
        if (fallDistance <= 3.0F) return 0.0F;
        return fallDistance - 3.0F;
    }
}