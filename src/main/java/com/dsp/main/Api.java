package com.dsp.main;

import com.dsp.main.ClickGui.ClickGuiScreen;
import com.dsp.main.Functions.Misc.Test;
import com.dsp.main.Functions.Movement.AutoSprint;
import com.dsp.main.Utils.Render.DrawHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.dsp.main.Utils.Minecraft.UserSession.UserSessionUtil.setNameSession;

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
    public void OnTick(RenderGuiEvent.Post event) {
        if (mc.player != null) {
            DrawHelper.rectangle(new PoseStack(), mc.getWindow().getGuiScaledWidth() /2, mc.getWindow().getGuiScaledHeight() /2, 85f, 20, 3, new Color(30,30,30,100).getRGB());
        }
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.Key e) {
        if (!(mc.level == null) && !(mc.player == null))
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
        if (e.getScreen() instanceof net.minecraft.client.gui.screens.TitleScreen) {
            setNameSession("XDawlXOption");
            //mc.setScreen(new MainMenuScreen());
        }
    }
}