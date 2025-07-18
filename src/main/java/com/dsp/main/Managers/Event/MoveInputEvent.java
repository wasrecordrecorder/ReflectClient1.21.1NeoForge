package com.dsp.main.Managers.Event;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public class MoveInputEvent extends Event implements ICancellableEvent {
    public float forward;
    public float strafe;
    public boolean jump;
    public boolean sneaking;
    public double sneakSlow;
    public float getForward() {
        return forward;
    }
    public void setForward(float forward) {
        this.forward = forward;
    }
    public float getStrafe() {
        return strafe;
    }
    public void setStrafe(float strafe) {
        this.strafe = strafe;
    }
    public boolean isMoving() {
        return getForward() != 0 || getStrafe() != 0;
    }
}