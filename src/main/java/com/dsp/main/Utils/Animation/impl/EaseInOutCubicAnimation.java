package com.dsp.main.Utils.Animation.impl;

import com.dsp.main.Utils.Animation.Animation;
import com.dsp.main.Utils.Animation.Direction;

public class EaseInOutCubicAnimation extends Animation {
    public EaseInOutCubicAnimation(int ms, double endPoint) {
        super(ms, endPoint);
    }

    public EaseInOutCubicAnimation(int ms, double endPoint, Direction direction) {
        super(ms, endPoint, direction);
    }

    @Override
    protected double getEquation(double x) {
        double t = x / duration;
        if (t < 0.5) {
            return 4.0 * t * t * t;
        } else {
            double f = (t - 1.0);
            return 1.0 + 4.0 * f * f * f;
        }
    }
}