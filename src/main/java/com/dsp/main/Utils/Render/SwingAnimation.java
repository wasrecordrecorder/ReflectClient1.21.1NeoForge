package com.dsp.main.Utils.Render;

public class SwingAnimation {
    private float current;
    private float target;
    public float speed;

    public SwingAnimation(float initial, float target, float speed) {
        this.current = initial;
        this.target = target;
        this.speed = speed;
    }

    public void setTarget(float target) {
        this.target = target;
    }

    public float getAnimation() {
        if (Math.abs(current - target) < 0.001f) {
            current = target;
        } else {
            current += (target - current) * speed;
        }
        return current;
    }

    public void reset() {
        this.current = 0;
        this.target = 0;
    }
}