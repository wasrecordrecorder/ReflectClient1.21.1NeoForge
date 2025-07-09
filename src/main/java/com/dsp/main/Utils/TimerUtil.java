package com.dsp.main.Utils;

import net.minecraft.client.Minecraft;

import java.util.TimerTask;

public class TimerUtil {
    private long lastMS = -1L;
    static long mc;

    public TimerUtil() {
        this.lastMS = System.currentTimeMillis();
    }

    public boolean hasReached(double delay) {
        return System.currentTimeMillis() - this.lastMS >= delay;
    }

    public boolean hasReached(boolean active, double delay) {
        return active || hasReached(delay);
    }

    public long getLastMS() {
        return lastMS;
    }

    public long getMc() {
        return System.currentTimeMillis() - mc;
    }

    public void reset() {
        this.lastMS = System.currentTimeMillis();
    }

    public long getTimePassed() {
        return System.currentTimeMillis() - lastMS;
    }

    public long getCurrentTime() {
        return System.nanoTime() / 1000000L;
    }

    public void setTime(long time) {
        lastMS = time;
    }

    public static void reset1() {
        mc = System.currentTimeMillis();
    }
    public static boolean hasReached1(final long n) {
        return System.currentTimeMillis() - mc > n;
    }

    public static void sleepVoid(Runnable execute, int cooldown) {
        new java.util.Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Minecraft.getInstance().execute(execute);
            }
        }, cooldown);
    }
}
