package com.dsp.main.Utils.Minecraft.Player;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class PredictUtility {

    public static Vec3 predictElytraPos(LivingEntity player, int ticks) {
        return predictElytraPos(player, player.position(), ticks);
    }

    public static Vec3 predictElytraPos(LivingEntity player, Vec3 pos, int ticks) {
        Vec3 motion = player.getDeltaMovement();

        for (int i = 0; i < ticks; i++) {
            Vec3 lookVec = player.getViewVector(1.0F);
            float pitchRad = (float) Math.toRadians(player.getXRot());
            double horizontalSpeed = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
            double motionMag = motion.length();
            float f1 = Mth.cos(pitchRad);
            f1 = (float) (f1 * f1 * Math.min(1.0D, lookVec.length() / 0.4D));

            motion = motion.add(0.0D, -0.08D * (-1.0D + (double) f1 * 0.75D), 0.0D);

            if (motion.y < 0.0D && horizontalSpeed > 0.0D) {
                double d5 = motion.y * -0.1D * f1;
                motion = motion.add(lookVec.x * d5 / horizontalSpeed, d5, lookVec.z * d5 / horizontalSpeed);
            }

            if (pitchRad < 0.0F && horizontalSpeed > 0.0D) {
                double lift = motionMag * (-Mth.sin(pitchRad)) * 0.04D;
                motion = motion.add(-lookVec.x * lift / horizontalSpeed, lift * 3.2D, -lookVec.z * lift / horizontalSpeed);
            }

            if (horizontalSpeed > 0.0D) {
                motion = motion.add(
                        (lookVec.x / horizontalSpeed * motionMag - motion.x) * 0.1D,
                        0.0D,
                        (lookVec.z / horizontalSpeed * motionMag - motion.z) * 0.1D
                );
            }

            motion = motion.multiply(0.99D, 0.98D, 0.99D);
            pos = pos.add(motion);
        }

        return pos;
    }
}