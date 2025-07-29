package com.dsp.main.Functions.Player;

import com.dsp.main.Mixin.Accesors.MouseHandlerAccessor;
import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.Slider;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import org.lwjgl.glfw.GLFW;

import static com.dsp.main.Api.mc;

public class TapeMouse extends Module {
    public static Slider delay = new Slider("Delay", 0.5, 5, 1.5, 0.5);

    private long lastClick;

    public TapeMouse() {
        super("Tape Mouse", 0, Category.PLAYER, "Automatic mouse clicking");
        addSettings(delay);
    }

    @SubscribeEvent
    public void onRender(RenderFrameEvent.Post e) {
        long interval = (long) (delay.getValue() * 100);
        if (System.currentTimeMillis() - lastClick >= interval && (org.lwjgl.glfw.GLFW.glfwGetMouseButton(mc.getWindow().getWindow(), org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT) == org.lwjgl.glfw.GLFW.GLFW_PRESS) && mc.screen == null) {
            long window = mc.getWindow().getWindow();
            MouseHandlerAccessor mouse = (MouseHandlerAccessor) mc.mouseHandler;
            mouse.invokeOnPress(window, 0, 1, 0);
            mouse.invokeOnPress(window, 0, 0, 0);
            lastClick = System.currentTimeMillis();
        }
    }
}