package com.dsp.main.Functions.Movement;

import com.dsp.main.Core.Event.OnUpdate;
import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.CheckBox;
import net.neoforged.bus.api.SubscribeEvent;

public class ScreenWalk extends Module {
    public static CheckBox shift = new CheckBox("UnlockShift", false);

    public ScreenWalk() {
        super("ScreenWalk", 0, Category.MOVEMENT, "Allows walking when any screen is opened");
        addSettings(shift);
    }

    @SubscribeEvent
    public void onTick(OnUpdate e) {
    }
}