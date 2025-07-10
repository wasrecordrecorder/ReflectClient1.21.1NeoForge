package com.dsp.main.Functions.Render;

import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Settings.CheckBox;
import com.dsp.main.UI.ClickGui.Settings.MultiCheckBox;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.awt.*;
import java.util.Arrays;

public class HudElement extends Module {
    public static Color IconColor = new Color(124, 145, 181);
    public static MultiCheckBox HudElements = new MultiCheckBox("Hud Elements", Arrays.asList(
            new CheckBox("Watermark", true),
            new CheckBox("Player Info", false),
            new CheckBox("Target Hud", false),
            new CheckBox("Potions", false),
            new CheckBox("Stafflist", false),
            new CheckBox("Keybinds", false),
            new CheckBox("Cooldowns", false)
    ));
    public HudElement() {
        super("Hud", 0, Category.RENDER, "Отображение элементов статистики и игры.");
        addSettings(HudElements);
    }
    @SubscribeEvent
    public void onTick(ClientTickEvent.Pre e) {

    }
}
