package com.dsp.main.Functions.Misc;

import com.dsp.main.Core.Event.OnUpdate;
import com.dsp.main.Core.Sound.SoundRegister;
import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.CheckBox;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.Mode;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.MultiCheckBox;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;

import java.util.Arrays;

import static com.dsp.main.Api.mc;

public class ClientSetting extends Module {
    public static Mode obhod = new Mode("Inventory Mode", "Packet", "Legit");
    public static CheckBox slowBypass = new CheckBox("Slow Bypass", false);
    public static CheckBox cfgASave = new CheckBox("Auto Save Cfg", true);

    public static CheckBox sound = new CheckBox("Client Sounds", false);
    public static CheckBox TogglSound = new CheckBox("Module Toggle Sound", false).setVisible(() -> sound.isEnabled());
    public static Mode hitSound = new Mode("Hit Sound", "None", "Bell", "Bonk", "Crime", "Metallic", "Rust").setVisible(() -> sound.isEnabled());
    public ClientSetting() {
        super("Client Settings", 0, Category.MISC, "You can setup this client");
        addSettings(obhod, slowBypass, cfgASave,
        sound, TogglSound, hitSound);
    }
    @SubscribeEvent
    public void onUpdate(OnUpdate event) {
    }
}
