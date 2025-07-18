package com.dsp.main.Functions.Render;

import com.dsp.main.Managers.Event.OnUpdate;
import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Settings.CheckBox;
import com.dsp.main.UI.ClickGui.Settings.MultiCheckBox;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderBlockScreenEffectEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import java.util.Arrays;

public class NoRender extends Module {
    public static MultiCheckBox NoRenderElements = new MultiCheckBox("No Render Elements", Arrays.asList(
            new CheckBox("Fire", true),
            new CheckBox("Block", true),
            new CheckBox("Water", false),
            new CheckBox("Rain and Snow", false),
            new CheckBox("Scoreboard", false),
            new CheckBox("Bossbar", false),
            new CheckBox("Bad Effects", true),
            new CheckBox("BobHurt", true)
    ));
    public NoRender() {
        super("NoRender", 0, Category.RENDER, "Removing bad effects from your screen");
        addSettings(NoRenderElements);
    }
    @SubscribeEvent
    public void onOverlay(OnUpdate event) {

    }
}
