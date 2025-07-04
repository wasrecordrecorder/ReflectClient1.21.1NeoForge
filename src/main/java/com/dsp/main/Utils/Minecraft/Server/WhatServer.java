package com.dsp.main.Utils.Minecraft.Server;

import static com.dsp.main.Api.mc;

public class WhatServer {
    public static boolean isFt() {
        if (mc.getCurrentServer() != null) return mc.getCurrentServer().ip.equals("funtime");
        return false;
    }
    public static boolean isRw() {
        if (mc.getCurrentServer() != null) return mc.getCurrentServer().ip.equals("reallyworld");
        return false;
    }
    public static boolean isHw() {
        if (mc.getCurrentServer() != null) return mc.getCurrentServer().ip.equals("holyworld");
        return false;
    }
    public static boolean isAm() {
        if (mc.getCurrentServer() != null) return mc.getCurrentServer().ip.equals("aresmine");
        return false;
    }
    public static boolean isSt() {
        if (mc.getCurrentServer() != null) return mc.getCurrentServer().ip.equals("spookytime");
        return false;
    }
    public static boolean isSp() {
        if (mc.getCurrentServer() != null) return mc.getCurrentServer().ip.equals("saturn-x");
        return false;
    }
    public static boolean isSpt() {
        if (mc.getCurrentServer() != null) return mc.getCurrentServer().ip.equals("space-time");
        return false;
    }
}
