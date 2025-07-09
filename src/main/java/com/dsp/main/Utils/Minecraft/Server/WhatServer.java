package com.dsp.main.Utils.Minecraft.Server;

import static com.dsp.main.Api.mc;

public class WhatServer {
    public static boolean isFt() {
        if (mc.getCurrentServer() != null) return mc.getCurrentServer().ip.contains("funtime");
        return false;
    }
    public static boolean isRw() {
        if (mc.getCurrentServer() != null) return mc.getCurrentServer().ip.contains("reallyworld");
        return false;
    }
    public static boolean isHw() {
        if (mc.getCurrentServer() != null) return mc.getCurrentServer().ip.contains("holyworld");
        return false;
    }
    public static boolean isAm() {
        if (mc.getCurrentServer() != null) return mc.getCurrentServer().ip.contains("aresmine");
        return false;
    }
    public static boolean isSt() {
        if (mc.getCurrentServer() != null) return mc.getCurrentServer().ip.contains("spookytime");
        return false;
    }
    public static boolean isSp() {
        if (mc.getCurrentServer() != null) return mc.getCurrentServer().ip.contains("saturn-x");
        return false;
    }
    public static boolean isSpt() {
        if (mc.getCurrentServer() != null) return mc.getCurrentServer().ip.contains("space-time");
        return false;
    }
}
