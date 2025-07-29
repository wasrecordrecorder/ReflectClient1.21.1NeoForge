package com.dsp.main.Functions.Misc;

import com.dsp.main.Core.Other.Hooks.InventoryScreenHook;
import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.Slider;
import com.dsp.main.Utils.TimerUtil;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.ShulkerBoxScreen;
import net.minecraft.world.inventory.ClickType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.lwjgl.glfw.GLFW;

import static com.dsp.main.Api.mc;

public class ItemScroller extends Module {
    public ItemScroller() {
        super("ItemScroller", 0, Category.MISC, "With this you can scroll items in container screens");
        addSetting(zaderzka);
    }
    private static final Slider zaderzka = new Slider("Delay", 0, 200, 30, 10.0);
    private final TimerUtil timerUtil = new TimerUtil();

    @SubscribeEvent
    public void onTick(ClientTickEvent.Post e) {
        if (!isEnabled()) return;
        if (mc.player != null && mc.gameMode != null) {
            if (mc.screen instanceof ContainerScreen screen) {
                if (screen.getSlotUnderMouse() != null
                        && GLFW.glfwGetKey(mc.getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS
                        && GLFW.glfwGetMouseButton(mc.getWindow().getWindow(), GLFW.GLFW_MOUSE_BUTTON_1) == GLFW.GLFW_PRESS
                        && timerUtil.hasReached((long) zaderzka.getValueInt())) {
                    mc.gameMode.handleInventoryMouseClick(
                            screen.getMenu().containerId,
                            screen.getSlotUnderMouse().index,
                            0,
                            ClickType.QUICK_MOVE,
                            mc.player
                    );
                    timerUtil.reset();
                }
            } else if (mc.screen instanceof InventoryScreenHook screen) {
                if (screen.getSlotUnderMouse() != null
                        && GLFW.glfwGetKey(mc.getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS
                        && GLFW.glfwGetMouseButton(mc.getWindow().getWindow(), GLFW.GLFW_MOUSE_BUTTON_1) == GLFW.GLFW_PRESS
                        && timerUtil.hasReached((long) zaderzka.getValueInt())) {
                    mc.gameMode.handleInventoryMouseClick(
                            screen.getMenu().containerId,
                            screen.getSlotUnderMouse().index,
                            0,
                            ClickType.QUICK_MOVE,
                            mc.player
                    );
                    timerUtil.reset();
                }
            } else if (mc.screen instanceof ShulkerBoxScreen screen) {
                if (screen.getSlotUnderMouse() != null
                        && GLFW.glfwGetKey(mc.getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS
                        && GLFW.glfwGetMouseButton(mc.getWindow().getWindow(), GLFW.GLFW_MOUSE_BUTTON_1) == GLFW.GLFW_PRESS
                        && timerUtil.hasReached((long) zaderzka.getValueInt())) {
                    mc.gameMode.handleInventoryMouseClick(
                            screen.getMenu().containerId,
                            screen.getSlotUnderMouse().index,
                            0,
                            ClickType.QUICK_MOVE,
                            mc.player
                    );
                    timerUtil.reset();
                }
            }
        }
    }
}