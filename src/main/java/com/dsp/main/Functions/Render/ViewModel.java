package com.dsp.main.Functions.Render;

import com.dsp.main.Core.Event.OnUpdate;
import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.CheckBox;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.Mode;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.Slider;
import net.neoforged.bus.api.SubscribeEvent;

public class ViewModel extends Module {
    public static final Slider rightX = new Slider("Right Hand X", -2, 2, 0, 0.01f);
    public static final Slider rightY = new Slider("Right Hand Y", -2, 2, 0, 0.01f);
    public static final Slider rightZ = new Slider("Right Hand Z", -2, 2, 0, 0.01f);

    public static final Slider leftX = new Slider("Left Hand X", -2, 2, 0, 0.01f);
    public static final Slider leftY = new Slider("Left Hand Y", -2, 2, 0, 0.01f);
    public static final Slider leftZ = new Slider("Left Hand Z", -2, 2, 0, 0.01f);

    public static final CheckBox animationEnabled = new CheckBox("Enable Animation", false);
    public static final Mode animationMode = new Mode("Animation Mode", "Mode",
            "Short", "Simple", "Inward", "Outward", "Block", "Size", "360");
    public static final Slider animationPower = new Slider("Animation Power", 80, 200, 0, 1);
    public static final Slider animationSmoothness = new Slider("Animation Smoothness", 0.1f, 1f, 0.01f, 0.01f);

    public ViewModel() {
        super("ViewModel", 0, Category.RENDER, "Customize first person hand position and animation");

        animationMode.setVisible(() -> animationEnabled.isEnabled());
        animationPower.setVisible(() -> animationEnabled.isEnabled());
        animationSmoothness.setVisible(() -> animationEnabled.isEnabled());

        addSettings(
                rightX, rightY, rightZ,
                leftX, leftY, leftZ,
                animationEnabled,
                animationMode,
                animationPower,
                animationSmoothness
        );
    }
    @SubscribeEvent
    public void onUpdate(OnUpdate e) {

    }
}