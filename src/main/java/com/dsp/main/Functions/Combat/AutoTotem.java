package com.dsp.main.Functions.Combat;

import com.dsp.main.Managers.Event.OnUpdate;
import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Settings.*;
import net.neoforged.bus.api.SubscribeEvent;

public class AutoTotem extends Module {
    private static Slider health = new Slider("Health", 1, 20, 6, 1);
    private static CheckBox saveEnchantedTotem = new CheckBox("Save Talismans", false);
    private static CheckBox CheckFall = new CheckBox("Fall Distance Check", false);
    private static CheckBox CheckEndCrystal = new CheckBox("End Crystal Check", false);
    private static CheckBox TnTCheck = new CheckBox("TnT Check", false);
    private static CheckBox SwapBack = new CheckBox("Swap Item Back", false);
    public AutoTotem() {
        super("AutoTotem", 0, Category.COMBAT, "Automatically equips totem to avoid your death");
        addSettings(health, saveEnchantedTotem, CheckFall, CheckEndCrystal, TnTCheck, SwapBack);
    }
    @SubscribeEvent
    public void onUpdate(OnUpdate event) {

    }
}
