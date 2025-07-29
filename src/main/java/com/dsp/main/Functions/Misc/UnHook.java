package com.dsp.main.Functions.Misc;

import com.dsp.main.Core.Event.OnUpdate;
import com.dsp.main.Module;
import net.neoforged.bus.api.SubscribeEvent;

import static com.dsp.main.Api.mc;
import static com.dsp.main.Main.isDetect;

public class UnHook extends Module {
    public UnHook() {
        super("UnHook", 0, Category.MISC, "UnHook software to evade bans from moderators");
    }

    @Override
    public void onEnable() {
        super.onEnable();
        mc.setScreen(null);
        isDetect = true;
        disableAllModules();
    }
    @SubscribeEvent
    public void ondwad(OnUpdate event) {

    }
}
