package com.dsp.main.Functions.Player;

import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Settings.CheckBox;
import com.dsp.main.UI.ClickGui.Settings.MultiCheckBox;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.Arrays;

public class NoPush extends Module {
    public static MultiCheckBox Options = new MultiCheckBox("Anti Push Options", Arrays.asList(
            new CheckBox("Blocks", false),
            new CheckBox("Water", false),
            new CheckBox("Entities", false)
    ));
    public NoPush() {
        super("NoPush", 0, Category.PLAYER, "Prevent you from pushing out of block, player, water");
        addSetting(Options);
    }
    @SubscribeEvent
    public void onTick(ClientTickEvent.Pre e) {
    }
}
