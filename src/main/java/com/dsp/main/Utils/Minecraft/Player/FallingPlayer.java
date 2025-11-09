package com.dsp.main.Utils.Minecraft.Player;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class FallingPlayer {
    private final LocalPlayer player;
    private double x, y, z;
    private double motionX, motionY, motionZ;
    private final float yaw;
    private int simulatedTicks;

    public FallingPlayer(LocalPlayer player, double x, double y, double z, double motionX, double motionY, double motionZ, float yaw) {
        this.player = player;
        this.x = x;
        this.y = y;
        this.z = z;
        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;
        this.yaw = yaw;
        this.simulatedTicks = 0;
    }

    public static FallingPlayer fromPlayer(LocalPlayer player) {
        return new FallingPlayer(
                player,
                player.position().x,
                player.position().y,
                player.position().z,
                player.getDeltaMovement().x,
                player.getDeltaMovement().y,
                player.getDeltaMovement().z,
                player.getYRot()
        );
    }

    public boolean findFall(float fallDist) {
        Vec3 rotationVec = player.getViewVector(1.0F);
        double tempMotionX = motionX;
        double tempMotionY = motionY;
        double tempMotionZ = motionZ;

        double d = 0.08;
        float n = Mth.cos(player.getXRot() * 0.017453292f);
        n = (float) (n * n * Math.min(rotationVec.length() / 0.4, 1.0));

        Vec3 vec3d = new Vec3(tempMotionX, tempMotionY, tempMotionZ).add(0.0, d * (-1.0 + n * 0.75), 0.0);
        tempMotionY = vec3d.y * 0.9800000190734863;

        return tempMotionY < fallDist;
    }

    public boolean findFall(float fallDist, int ticks) {
        Vec3 rotationVec = player.getViewVector(1.0F);
        double tempMotionX = motionX;
        double tempMotionY = motionY;
        double tempMotionZ = motionZ;

        double d = 0.08;
        float n = Mth.cos(player.getXRot() * 0.017453292f);
        n = (float) (n * n * Math.min(rotationVec.length() / 0.4, 1.0));

        for (int i = 0; i < ticks; i++) {
            Vec3 vec3d = new Vec3(tempMotionX, tempMotionY, tempMotionZ).add(0.0, d * (-1.0 + n * 0.75), 0.0);
            tempMotionY = vec3d.y * 0.9800000190734863;

            if (tempMotionY >= fallDist) {
                return false;
            }
        }

        return true;
    }
}