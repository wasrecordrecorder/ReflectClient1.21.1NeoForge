package com.dsp.main.Core.Event;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public class SlowWalkingEvent extends Event implements ICancellableEvent {
    private float forwardImpulse;
    private float leftImpulse;

    public SlowWalkingEvent(float forwardImpulse, float leftImpulse) {
        this.forwardImpulse = forwardImpulse;
        this.leftImpulse = leftImpulse;
    }

    public float getForwardImpulse() {
        return forwardImpulse;
    }

    public void setForwardImpulse(float forwardImpulse) {
        this.forwardImpulse = forwardImpulse;
    }

    public float getLeftImpulse() {
        return leftImpulse;
    }

    public void setLeftImpulse(float leftImpulse) {
        this.leftImpulse = leftImpulse;
    }
}