package com.dsp.main.Functions.Player;

import com.dsp.main.Core.Event.OnUpdate;
import com.dsp.main.Core.Other.FreeLook;
import com.dsp.main.Mixin.Accesors.MouseHandlerAccessor;
import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Dropdown.ClickGuiScreen;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.Mode;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.Slider;
import com.dsp.main.Utils.TimerUtil;
import net.minecraft.util.Mth;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.Random;

import static com.dsp.main.Api.mc;

public class AntiAfk extends Module {
    private static TimerUtil timer = new TimerUtil();
    private static Mode mode = new Mode("Mode", "Jump", "Command", "Hit", "Rotation");
    private static Slider time = new Slider("Delay", 1, 60, 30, 1);
    public AntiAfk() {
        super("Anti Afk", 0, Category.PLAYER, "Prevents you from kicking by afk");
        addSettings(mode,time);
    }
    @SubscribeEvent
    public void onupdatre(OnUpdate e) {
        if (mc.player == null || mc.screen instanceof ClickGuiScreen) return;
        if (timer.hasReached(time.getValueInt() * 1000L)) {
            switch (mode.getMode()) {
                case "Jump":
                    mc.player.jumpFromGround();
                    break;
                case "Command":
                    mc.player.connection.sendCommand(String.valueOf(new Random().nextFloat()));
                    break;
                case "Hit":
                    long window = mc.getWindow().getWindow();
                    MouseHandlerAccessor mouse = (MouseHandlerAccessor) mc.mouseHandler;
                    mouse.invokeOnPress(window, 0, 1, 0);
                    mouse.invokeOnPress(window, 0, 0, 0);
                    break;
                case "Rotation":
                    FreeLook.requestFreeLook("AntiAfk");
                    mc.player.setYRot(Mth.clamp(new Random().nextFloat(360), 0, 360));
                    FreeLook.releaseFreeLook("AntiAfk");
                    break;
            }
            timer.reset();
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        timer.reset();
    }
}
