package com.dsp.main.Functions.Combat.Aura.impl.Snap;

import com.dsp.main.Functions.Combat.Aura.Aura;
import com.dsp.main.Functions.Combat.Aura.impl.AttackHandler;
import com.dsp.main.Functions.Combat.Aura.impl.RotationAngle;
import com.dsp.main.Core.Other.FreeLook;
import com.dsp.main.Utils.Minecraft.Client.ClientFallDistance;
import com.dsp.main.Utils.TimerUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import static com.dsp.main.Api.mc;

/*
 * @was_record
 * Да это мои старые снапы, но с проверкой на Fov
 * Created: 21.07.2025 : 22:56
 */


public class SnapRotationFov extends RotationAngle {
    @Override
    public void update(Aura aura, Entity target) {
        SnapRotation(aura.snapSpeed.getValueInt(),aura.snapSpeed.getValueInt(),20, aura);
    }

    private enum SnapState { IDLE, GOING, RETURNING }
    private static SnapState snapState = SnapState.IDLE;
    private static float snapStartYaw;
    private static float snapStartPitch;
    private static float snapTargetYaw;
    private static float snapTargetPitch;
    private static final TimerUtil snapTimer = new TimerUtil();
    private static float lastYawDelta = 0, lastPitchDelta = 0;
    private static final TimerUtil hitTimer = new TimerUtil();
    private static boolean hitTimerStarted = false;


    public static float[] SnapAngleCalculator(Entity target) {
        Vec3 eye = mc.player.getEyePosition(1F);
        Vec3 aim = target.getBoundingBox().getCenter();
        double dx = aim.x - eye.x;
        double dy = aim.y - eye.y;
        double dz = aim.z - eye.z;
        double dist = Math.sqrt(dx * dx + dz * dz);
        float yaw   = (float) (Mth.atan2(dz, dx) * 180F / Math.PI) - 90F;
        float pitch = (float) (-Mth.atan2(dy, dist) * 180F / Math.PI);
        return new float[]{ yaw, pitch };
    }

    public static void SnapRotation(int SNAP_DURATION, int RETURN_DURATION, int ATTACK_TIME_PASS, Aura aura) {
        if (snapState == SnapState.IDLE) {
            mc.player.setYRot(FreeLook.getCameraYaw());
            mc.player.setXRot(FreeLook.getCameraPitch());
        }
        if (mc.player.getAttackStrengthScale(1.0F) >= 0.85F
                && ClientFallDistance.get() > 0
                && snapState == SnapState.IDLE
                && Aura.Target != null
                && mc.player.distanceTo(Aura.Target) <= aura.attackRange.getValue()
                && isInFov((LivingEntity) Aura.Target, aura.fov.getValueInt())) {
            snapStartYaw   = mc.player.getYRot();
            snapStartPitch = mc.player.getXRot();
            snapState      = SnapState.GOING;
            snapTimer.reset();
        }
        if (snapState == SnapState.GOING) {
            float[] rot = SnapAngleCalculator(Aura.Target);
            snapTargetYaw   = rot[0];
            snapTargetPitch = rot[1];

            float progress = Mth.clamp(snapTimer.getTimePassed() / (float) SNAP_DURATION, 0, 1);
            float rawYawDelta   = Mth.wrapDegrees(snapTargetYaw   - snapStartYaw)   * progress;
            float rawPitchDelta =                 (snapTargetPitch - snapStartPitch) * progress;
            float maxYawStep   = 180 * progress;
            float maxPitchStep = 180 * progress;
            float yawDelta   = Mth.clamp(rawYawDelta,   -maxYawStep,   maxYawStep);
            float pitchDelta = Mth.clamp(rawPitchDelta, -maxPitchStep, maxPitchStep);
            if (Math.abs(yawDelta - lastYawDelta) <= 0.1F) {
                yawDelta = lastYawDelta + Math.signum(yawDelta) * 0.1F;
            }
            if (Math.abs(pitchDelta - lastPitchDelta) <= 0.1F) {
                pitchDelta = lastPitchDelta + Math.signum(pitchDelta) * 0.1F;
            }
            if (mc.player.onGround() || (mc.player.isUsingItem() && aura.checks.isOptionEnabled("Don't hit when you eat"))) snapState = SnapState.RETURNING;
            lastYawDelta   = yawDelta;
            lastPitchDelta = pitchDelta;
            float yaw   = snapStartYaw   + yawDelta;
            float pitch = snapStartPitch + pitchDelta;
            yaw   = snapToMouseSensitivity(yaw);
            pitch = Mth.clamp(snapToMouseSensitivity(pitch), -90, 90);
            mc.player.setYRot(yaw);
            mc.player.setXRot(pitch);
            mc.player.yHeadRot = yaw;
            double attackRange = Math.max(aura.attackRange.getValue() + 0.12,
                    3F);
            Vec3 start = mc.player.getEyePosition(1.0F);
            Vec3 end   = start.add(mc.player.getLookAngle().scale(attackRange));
            AABB box = Aura.Target.getBoundingBox();
            Vec3 hitVec = box.clip(start, end).orElse(null);
            if (hitVec != null) {
                if (mc.player.distanceTo(Aura.Target) <= aura.attackRange.getValue()) {
                    if (!hitTimerStarted) {
                        hitTimerStarted = true;
                        hitTimer.reset();
                    } else if (hitTimer.getTimePassed() >= ATTACK_TIME_PASS) {
                        AttackHandler.update(aura, false, false);
                        snapTimer.reset();
                        hitTimerStarted = false;
                        snapState = SnapState.RETURNING;
                    }
                } else {
                    hitTimerStarted = false;
                }
            } else {
                hitTimerStarted = false;
            }
        } else if (snapState == SnapState.RETURNING) {
            if (mc.player.getAttackStrengthScale(1.0F) >= 0.85F
                    && ClientFallDistance.get() > 0
                    && Aura.Target != null
                    && mc.player.distanceTo(Aura.Target) <= aura.attackRange.getValue()
                    && isInFov((LivingEntity) Aura.Target, aura.fov.getValueInt())) {
                snapStartYaw   = mc.player.getYRot();
                snapStartPitch = mc.player.getXRot();
                snapState      = SnapState.GOING;
                snapTimer.reset();
                return;
            }
            float progress = Mth.clamp(snapTimer.getTimePassed() / (float) RETURN_DURATION, 0, 1);
            float rawYawDelta   = Mth.wrapDegrees(snapStartYaw   - snapTargetYaw)   * progress;
            float rawPitchDelta =                 (snapStartPitch - snapTargetPitch) * progress;
            float maxYawStep   = 180 * progress;
            float maxPitchStep =  90 * progress;
            float yawDelta   = Mth.clamp(rawYawDelta,   -maxYawStep,   maxYawStep);
            float pitchDelta = Mth.clamp(rawPitchDelta, -maxPitchStep, maxPitchStep);
            if (Math.abs(yawDelta - lastYawDelta) <= 0.1F) {
                yawDelta = lastYawDelta + Math.signum(yawDelta) * 0.1F;
            }
            if (Math.abs(pitchDelta - lastPitchDelta) <= 0.1F) {
                pitchDelta = lastPitchDelta + Math.signum(pitchDelta) * 0.1F;
            }
            lastYawDelta   = yawDelta;
            lastPitchDelta = pitchDelta;
            float yaw   = snapTargetYaw + yawDelta;
            float pitch = snapTargetPitch + pitchDelta;
            yaw   = snapToMouseSensitivity(yaw);
            pitch = Mth.clamp(snapToMouseSensitivity(pitch), -90, 90);
            mc.player.yHeadRot = yaw;
            mc.player.setYRot(yaw);
            mc.player.setXRot(pitch);
            if (progress >= 1F) {
                snapState = SnapState.IDLE;
            }
        }
    }
}
