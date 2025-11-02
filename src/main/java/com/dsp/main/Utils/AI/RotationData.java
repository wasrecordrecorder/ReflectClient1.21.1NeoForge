package com.dsp.main.Utils.AI;

public class RotationData {
    public final float yawDelta;
    public final float pitchDelta;
    public final float targetYaw;
    public final float targetPitch;
    public final float sinceAttack;
    public final float distance;
    public final float onGround;
    public final float miniHitbox;
    public final float yaw;
    public final float pitch;

    public RotationData(float yawDelta, float pitchDelta, float targetYaw, float targetPitch, float sinceAttack, float distance, float onGround, float miniHitbox, float yaw, float pitch) {
        this.yawDelta = yawDelta;
        this.pitchDelta = pitchDelta;
        this.targetYaw = targetYaw;
        this.targetPitch = targetPitch;
        this.sinceAttack = sinceAttack;
        this.distance = distance;
        this.onGround = onGround;
        this.miniHitbox = miniHitbox;
        this.yaw = yaw;
        this.pitch = pitch;
    }
}