package com.dsp.main.Functions.Movement;

import com.dsp.main.UI.ClickGui.Settings.*;
import com.dsp.main.Module;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.ArrayList;
import java.util.List;

import static com.dsp.main.Api.mc;
import static com.dsp.main.Main.isDetect;

public class Test extends Module {
    private static CheckBox dwa = new CheckBox("Test Check Box",false);
    private static Slider we = new Slider("Test Slider", 0, 10, 1, 1);
    private static Mode ld = new Mode("TestMode", "Mode1", "dwa", "Matrix", "FunTime", "ReallyWOrld", "holyworld","spookytime");
    private static Input ldw = new Input("PlaceHolder", "DefaultValue");

    public Test() {
        super("Test", 0, Category.MOVEMENT, "description");
        List<CheckBox> options = new ArrayList<>();
        options.add(new CheckBox("Invisible", false));
        options.add(new CheckBox("Friends", true));
        options.add(new CheckBox("Mobs", false));
        options.add(new CheckBox("Players", false));
        options.add(new CheckBox("Moderators", false));
        MultiCheckBox multiCheckBox = new MultiCheckBox("Movement Options", options);
        addSettings(dwa,we,ld,ldw, multiCheckBox);
        addSetting(new BindCheckBox(
                "Disorientation",
                0,
                this::test
        ).setVisible(() -> true));
    }
    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Pre event) {
        if (mc.player != null && !isDetect) {
        }
    }
    public void test(){
        System.out.println("dawada");
    }
}
