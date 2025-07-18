package com.dsp.main.Utils.Minecraft.Client;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.Scoreboard;
import org.joml.Vector4f;

import static com.dsp.main.Api.mc;

public class ClientPlayerUtil {
    public static boolean isPlayerFalling() {
        boolean cancelReason = mc.player.isInLava()
                || mc.player.isPassenger();
        boolean onGround = mc.player.onGround();
        float attackStrength = mc.player.getAttackStrengthScale(1.0f);
        return !cancelReason && attackStrength >= 0.92 && !onGround && mc.player.fallDistance > 0;
    }
    public static boolean isMoving() {
        Vec2 vector2f = mc.player.input.getMoveVector();
        return vector2f.x != 0.0F || vector2f.y != 0.0F;
    }

    public static boolean hasEnoughImpulseToStartSprinting() {
        double d0 = 0.8D;
        return (double)mc.player.input.forwardImpulse >= d0;
    }
    public static float[] getHealthFromScoreboard(LivingEntity target) {
        if (mc.player != null && mc.level != null && mc.gameMode != null) {
            float[] healthInfo = new float[2];
            healthInfo[0] = target.getHealth();
            healthInfo[1] = target.getMaxHealth();

            Scoreboard scoreboard = mc.level.getScoreboard();
            Objective objective = scoreboard.getDisplayObjective(DisplaySlot.BELOW_NAME);
            if (objective != null) {
                ReadOnlyScoreInfo scoreInfo = scoreboard.getPlayerScoreInfo(target, objective);
                if (scoreInfo != null) {
                    healthInfo[0] = scoreInfo.value();
                    healthInfo[1] = 20;
                }
            }

            return healthInfo;
        }

        return new float[0];
    }
    public static Vector4f calculateRotationFromCamera(LivingEntity target) {
        Vec3 targetPos = target.getEyePosition();
        Vec3 playerEyePos = mc.player.getEyePosition();
        Vec3 vec = targetPos.subtract(playerEyePos);
        float rawYaw = (float) Mth.wrapDegrees(Math.toDegrees(Math.atan2(vec.z, vec.x)) - 90F);
        float rawPitch = (float) -Math.toDegrees(Math.atan2(vec.y, Math.sqrt(vec.x * vec.x + vec.z * vec.z)));

        float yawDelta = Mth.wrapDegrees(rawYaw - mc.player.getYRot());
        float pitchDelta = rawPitch - mc.player.getXRot();

        return new Vector4f(rawYaw, rawPitch, yawDelta, pitchDelta);
    }


    public static double calculateFOVFromCamera(LivingEntity target) {
        Vector4f rotation = calculateRotationFromCamera(target);
        float yawDelta = rotation.z;
        float pitchDelta = rotation.w;

        return Math.sqrt(yawDelta * yawDelta + pitchDelta * pitchDelta);
    }
}
