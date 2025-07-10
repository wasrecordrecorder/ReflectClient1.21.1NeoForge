package com.dsp.main;

import com.dsp.main.Functions.Combat.TriggerBot;
import com.dsp.main.Functions.Misc.AntiAttack;
import com.dsp.main.Functions.Misc.AutoLeave;
import com.dsp.main.Functions.Misc.UnHook;
import com.dsp.main.Functions.Player.ClickActions;
import com.dsp.main.Functions.Player.NoDelay;
import com.dsp.main.Functions.Player.NoPush;
import com.dsp.main.Functions.Render.HudElement;
import com.dsp.main.Functions.Render.NoRender;
import com.dsp.main.Managers.ConfigSystem.CfgManager;
import com.dsp.main.UI.ClickGui.ClickGuiScreen;
import com.dsp.main.Functions.Movement.Test;
import com.dsp.main.Functions.Movement.AutoSprint;
import com.dsp.main.UI.ClickGui.Settings.BindCheckBox;
import com.dsp.main.UI.ClickGui.Settings.Setting;
import com.dsp.main.UI.Draggable.DragManager;
import com.dsp.main.UI.Draggable.DraggableElement;
import com.dsp.main.UI.MainMenu.MainMenuScreen;
import com.dsp.main.Managers.Hooks.InventoryScreenHook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.*;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.dsp.main.Main.isDetect;

public class Api {
    private double mouseX;
    private double mouseY;
    public static Minecraft mc = Minecraft.getInstance();
    public static CopyOnWriteArrayList<Module> Functions = new CopyOnWriteArrayList<>();
    private boolean isCfgLoaded = false;

    private static final Timer autoSaveTimer = new Timer(true);

    static {
        autoSaveTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (isDetect) return;
                CfgManager.saveCfg("autoload");
                DragManager.save();
            }
        }, 10 * 60 * 1000, 10 * 60 * 1000);
    }

    public static void Initialize() {
        Functions.add(new AutoSprint());
        Functions.add(new Test());
        Functions.add(new HudElement());
        Functions.add(new NoRender());
        Functions.add(new AutoLeave());
        Functions.add(new TriggerBot());
        Functions.add(new ClickActions());
        Functions.add(new UnHook());
        Functions.add(new NoDelay());
        Functions.add(new AntiAttack());
        Functions.add(new NoPush());
    }

    public static boolean isEnabled(String name) {
        for (Module m : Functions) {
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
        if (!(mc.level == null) && !(mc.player == null) && !isDetect) {
            if (e.getAction() == 1 && mc.screen == null) {
                for (Module m : Functions) {
                    if (m.getKeyCode() == e.getKey()) {
                        m.toggle();
                    }
                }
            }
            for (Module module : Functions) {
                if (!module.isEnabled()) continue;
                for (Setting setting : module.getSettings()) {
                    if (setting instanceof BindCheckBox) {
                        BindCheckBox bind = (BindCheckBox) setting;
                        if (e.getKey() == bind.getBindKey()
                                && e.getAction() == GLFW.GLFW_PRESS) {
                            bind.execute();
                        }
                    }
                }
            }
            if (e.getKey() == GLFW.GLFW_KEY_RIGHT_SHIFT && mc.screen == null) {
                mc.setScreen(new ClickGuiScreen());
            }
        }
    }
    @SubscribeEvent
    public void onMouseKey(InputEvent.MouseButton.Pre e) {
        if (isDetect) return;
        if (!(mc.level == null) && !(mc.player == null)) {
            for (Module module : Functions) {
                if (!module.isEnabled()) continue;
                for (Setting setting : module.getSettings()) {
                    if (setting instanceof BindCheckBox) {
                        BindCheckBox bind = (BindCheckBox) setting;
                        if (e.getButton() == bind.getBindKey()
                                && e.getAction() == GLFW.GLFW_PRESS
                                && e.getButton() != 0) {
                            bind.execute();
                        }
                    }
                }
            }
        }
    }
    @SubscribeEvent
    public void onRenderGui(RenderGuiEvent.Post event) {
        GuiGraphics guiGraphics = event.getGuiGraphics();
        MouseHandler mouseHandler = Minecraft.getInstance().mouseHandler;
        mouseX = mouseHandler.xpos() * Minecraft.getInstance().getWindow().getGuiScaledWidth() / Minecraft.getInstance().getWindow().getScreenWidth();
        mouseY = mouseHandler.ypos() * Minecraft.getInstance().getWindow().getGuiScaledHeight() / Minecraft.getInstance().getWindow().getScreenHeight();

        for (DraggableElement element : DragManager.draggables.values()) {
            element.onDraw((int) mouseX, (int) mouseY, Minecraft.getInstance().getWindow());
            element.render(guiGraphics);
        }
    }

    @SubscribeEvent
    public void onMouseClick(ScreenEvent.MouseButtonPressed.Pre event) {
        for (DraggableElement element : DragManager.draggables.values()) {
            element.onClick(event.getMouseX(), event.getMouseY(), event.getButton());
        }
    }

    @SubscribeEvent
    public void onMouseRelease(ScreenEvent.MouseButtonReleased.Pre event) {
        for (DraggableElement element : DragManager.draggables.values()) {
            element.onRelease(event.getButton());
        }
    }

    @SubscribeEvent
    public void onInitGui(ScreenEvent.Init.Pre e) {
        if (e.getScreen() instanceof net.minecraft.client.gui.screens.TitleScreen && !isDetect) {
            if (!isCfgLoaded) {
                CfgManager.loadCfg("autoload");
                isCfgLoaded = true;
            }

            mc.setScreen(new MainMenuScreen());
        } if (e.getScreen() instanceof  net.minecraft.client.gui.screens.inventory.InventoryScreen && !isDetect && mc.player != null) {
            mc.setScreen(new InventoryScreenHook(mc.player));
        }
    }
}