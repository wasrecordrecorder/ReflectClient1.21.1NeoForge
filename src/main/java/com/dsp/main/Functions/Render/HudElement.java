package com.dsp.main.Functions.Render;

import com.dsp.main.Core.Event.OnUpdate;
import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.CheckBox;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.MultiCheckBox;
import net.neoforged.bus.api.SubscribeEvent;

import java.awt.*;
import java.util.Arrays;

public class HudElement extends Module {
    public static int IconColor = Color.WHITE.getRGB();
    public static MultiCheckBox HudElements = new MultiCheckBox("Hud Elements", Arrays.asList(
            new CheckBox("Watermark", true),
            new CheckBox("Player Info", false),
            new CheckBox("Target Hud", false),
            new CheckBox("Potions", false),
            new CheckBox("StaffList", false),
            new CheckBox("Keybinds", false),
            new CheckBox("Cooldowns", false),
            new CheckBox("Inventory Hud", false)
    ));
    public static CheckBox snapGride = new CheckBox("Draw Snap Gride", false);

    public HudElement() {
        super("Hud", 0, Category.RENDER, "Отображение элементов статистики и игры.");
        addSettings(HudElements,snapGride);
    }
    @SubscribeEvent
    public void ClientTickEvent(OnUpdate e ) {
    }
}