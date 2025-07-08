package com.dsp.main;

import com.dsp.main.UI.ClickGui.ClickGuiScreen;
import com.dsp.main.Functions.Movement.Test;
import com.dsp.main.Functions.Movement.AutoSprint;
import com.dsp.main.UI.MainMenu.MainMenuScreen;
import com.dsp.main.Managers.Hooks.InventoryScreenHook;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.*;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.dsp.main.Main.isDetect;

public class Api {
    public static Minecraft mc = Minecraft.getInstance();
    public static CopyOnWriteArrayList<Module> modules = new CopyOnWriteArrayList<>();

    public static void Initialize() {
        modules.add(new AutoSprint());
        modules.add(new Test());
    }

    public static boolean isEnabled(String name) {
        for (Module m : modules) {
            if (m.isEnabled()) {
                if (Objects.equals(m.name, name)) {
                    return true;
                }
            }
        }
        return false;
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.Key e) {
        if (!(mc.level == null) && !(mc.player == null) && !isDetect)
            if (e.getAction() == 1 && mc.screen == null) {
                for (Module m : modules) {
                    if (m.getKeyCode() == e.getKey()) {
                        m.toggle();
                    }
                }
            }
        if (e.getKey() == GLFW.GLFW_KEY_RIGHT_SHIFT && mc.screen == null) {
            mc.setScreen(new ClickGuiScreen());
        }
    }

    @SubscribeEvent
    public void onInitGui(ScreenEvent.Init.Pre e) {
        if (e.getScreen() instanceof net.minecraft.client.gui.screens.TitleScreen && !isDetect) {
            mc.setScreen(new MainMenuScreen());
        } if (e.getScreen() instanceof  net.minecraft.client.gui.screens.inventory.InventoryScreen && !isDetect && mc.player != null) {
            mc.setScreen(new InventoryScreenHook(mc.player));
        }
    }
}