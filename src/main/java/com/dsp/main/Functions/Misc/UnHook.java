package com.dsp.main.Functions.Misc;

import com.dsp.main.Module;

import static com.dsp.main.Main.isDetect;

public class UnHook extends Module {
    public UnHook() {
        super("UnHook", 0, Category.MISC, "UnHook software to evade bans from moderators");
    }

    @Override
    public void onEnable() {
        super.onEnable();
        isDetect = true;
        disableAllModules();
    }
}
