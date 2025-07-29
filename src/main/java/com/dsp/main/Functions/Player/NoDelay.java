package com.dsp.main.Functions.Player;

import com.dsp.main.Core.Event.OnUpdate;
import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.CheckBox;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.MultiCheckBox;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.Arrays;

public class NoDelay extends Module {
    public static MultiCheckBox Options = new MultiCheckBox("Options", Arrays.asList(
            new CheckBox("Jumping", false),
            new CheckBox("Break Block", false),
            new CheckBox("Place Blocks", false)
    ));

    public NoDelay() {
        super("NoDelay", 0, Category.PLAYER, "removing delays from some things");
        addSetting(Options);
    }
    @SubscribeEvent
    public void doawoda(OnUpdate event) {
    }
}
