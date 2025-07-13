package com.dsp.main.Functions.Render;

import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Settings.CheckBox;
import com.dsp.main.UI.ClickGui.Settings.MultiCheckBox;
import com.dsp.main.UI.Themes.ThemesUtil;
import com.dsp.main.Utils.Render.ColorUtil;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.awt.*;
import java.util.Arrays;

import static com.dsp.main.Api.mc;

public class HudElement extends Module {
    public static int IconColor = Color.WHITE.getRGB();
    public static MultiCheckBox HudElements = new MultiCheckBox("Hud Elements", Arrays.asList(
            new CheckBox("Watermark", true),
            new CheckBox("Player Info", false),
            new CheckBox("Target Hud", false),
            new CheckBox("Potions", false),
            new CheckBox("StaffList", false),
            new CheckBox("Keybinds", false),
            new CheckBox("Cooldowns", false)
    ));
    public static CheckBox snapGride = new CheckBox("Draw Snap Gride", false);

    public HudElement() {
        super("Hud", 0, Category.RENDER, "Отображение элементов статистики и игры.");
        addSettings(HudElements,snapGride);
    }
    @SubscribeEvent
    public void ClientTickEvent(ClientTickEvent.Pre e ) {
    }
}