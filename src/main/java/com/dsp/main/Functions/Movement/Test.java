package com.dsp.main.Functions.Movement;

import com.dsp.main.UI.ClickGui.Settings.CheckBox;
import com.dsp.main.UI.ClickGui.Settings.Input;
import com.dsp.main.UI.ClickGui.Settings.Mode;
import com.dsp.main.UI.ClickGui.Settings.Slider;
import com.dsp.main.Module;

public class Test extends Module {
    private static CheckBox dwa = new CheckBox("Test",false);
    private static Slider we = new Slider("Test Sld", 0, 10, 1, 1);
    private static Mode ld = new Mode("Mode Test", "dwa");
    private static Input ldw = new Input("dwal", "213");

    public Test() {
        super("Test", 0, Category.MOVEMENT, "description");
        addSettings(dwa,we,ld,ldw);
    }
}
