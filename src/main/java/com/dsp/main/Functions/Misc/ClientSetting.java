package com.dsp.main.Functions.Misc;

import com.dsp.main.Managers.Event.OnUpdate;
import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Settings.CheckBox;
import com.dsp.main.UI.ClickGui.Settings.Mode;
import net.neoforged.bus.api.SubscribeEvent;

public class ClientSetting extends Module {
    public static Mode obhod = new Mode("Inventory Mode", "Packet", "Legit");
    public static CheckBox slowBypass = new CheckBox("Slow Bypass", false);
    public static CheckBox cfgASave = new CheckBox("Auto Save Cfg", true);
    public ClientSetting() {
        super("Client Settings", 0, Category.MISC, "You can setup this client");
        addSettings(obhod, slowBypass, cfgASave);
    }
    @SubscribeEvent
    public void onUpdate(OnUpdate event) {
    }
}
