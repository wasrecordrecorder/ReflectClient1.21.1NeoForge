package com.dsp.main.Utils.Render.AnimFromRockstarClient;

/**
 * @author ConeTin
 * @since 4 дек. 2023 г.
 */

public class TimerUtility {

    private long startTime = System.currentTimeMillis();

    public void reset() {
    	startTime = System.currentTimeMillis();
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public boolean passed(float time) {
    	return passed((long) time);
    }

    public boolean passed(long time) {
    	return System.currentTimeMillis() - startTime > time;
    }

    public long getElapsed() {
    	return System.currentTimeMillis() - startTime;
    }
    
}
