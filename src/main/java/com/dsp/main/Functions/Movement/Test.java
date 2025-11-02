package com.dsp.main.Functions.Movement;

import com.dsp.main.Core.Event.OnUpdate;
import com.dsp.main.Core.Other.FreeLook;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.*;
import com.dsp.main.Module;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

import static com.dsp.main.Api.mc;
import static com.dsp.main.Main.isDetect;

public class Test extends Module {

    public Test() {
        super("Test", 0, Category.MOVEMENT, "description");
    }
    @SubscribeEvent
    public void onClientTick(OnUpdate event) {
        if (mc.player != null && !isDetect) {
        }
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
