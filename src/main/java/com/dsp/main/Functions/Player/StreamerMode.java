package com.dsp.main.Functions.Player;

import com.dsp.main.Core.Event.OnUpdate;
import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.CheckBox;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.Input;
import net.neoforged.bus.api.SubscribeEvent;

public class StreamerMode extends Module {
    private static CheckBox isCustomName = new CheckBox("CustomName", false);
    public static CheckBox FuntimePr = new CheckBox("FunTime Media", true);
    private static Input customNameInput = new Input("Custom Name", "Piona").setVisible(() -> isCustomName.isEnabled());

    public static String ProtectedName = "JoonSino";
    public StreamerMode() {
        super("StreamerMode", 0, Category.PLAYER, "Protection for your info");
        addSettings(isCustomName,customNameInput, FuntimePr);
    }
    @SubscribeEvent
    public void onUpdate(OnUpdate event) {
        ProtectedName = isCustomName.isEnabled() ? customNameInput.getValue() : "JoonSino";
    }
}
