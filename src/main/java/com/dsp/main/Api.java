package com.dsp.main;

import com.dsp.main.Functions.Combat.*;
import com.dsp.main.Functions.Combat.Aura.Aura;
import com.dsp.main.Functions.Misc.*;
import com.dsp.main.Functions.Movement.*;
import com.dsp.main.Functions.Player.*;
import com.dsp.main.Functions.Render.*;
import com.dsp.main.Managers.ConfigSystem.CfgManager;
import com.dsp.main.Managers.Event.OnUpdate;
import com.dsp.main.Managers.Event.UpdateInputEvent;
import com.dsp.main.Managers.Other.KeyboardInputHook;
import com.dsp.main.UI.ClickGui.ClickGuiScreen;
import com.dsp.main.Functions.Movement.Test;
import com.dsp.main.Functions.Movement.AutoSprint;
import com.dsp.main.UI.ClickGui.Settings.BindCheckBox;
import com.dsp.main.UI.ClickGui.Settings.Setting;
import com.dsp.main.UI.Draggable.DragElements.StaffList;
import com.dsp.main.UI.Draggable.DragManager;
import com.dsp.main.UI.Draggable.DraggableElement;
import com.dsp.main.UI.MainMenu.MainMenuScreen;
import com.dsp.main.Managers.Other.InventoryScreenHook;
import com.dsp.main.UI.Notifications.NotificationManager;
import com.dsp.main.Utils.TimerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.*;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.dsp.main.Functions.Misc.ClientSetting.cfgASave;
import static com.dsp.main.Functions.Render.HudElement.snapGride;
import static com.dsp.main.Main.isDetect;

public class Api {
    private double mouseX;
    private double mouseY;
    public static Minecraft mc = Minecraft.getInstance();
    public static CopyOnWriteArrayList<Module> Functions = new CopyOnWriteArrayList<>();
    private boolean isCfgLoaded = false;
    public static boolean isResetingSprint = false;
    public static boolean isSlowBypass = false;
    public static NotificationManager notificationManager = new NotificationManager();

    private static final Timer autoSaveTimer = new Timer(true);

    static {
        autoSaveTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (isDetect || !cfgASave.isEnabled()) return;
                CfgManager.saveCfg("autoload");
                DragManager.save();
            }
        }, 10 * 60 * 1000, 10 * 60 * 1000);
    }

    public static void Initialize() {
        Collections.addAll(Functions,
                // Misc
                new AutoLeave(), new AntiAttack(), new AutoAccept(), new UnHook(), new ItemScroller(), new AutoRespawn(),
                new ItemSwapFix(), new AutoAuch(), new AutoJoiner(), new ClientSetting(),
                // Combat
                new Aura(), new TriggerBot(), new AimAssistant(), new AntiBot(), new AutoGApple(), new HitBox(), new AutoWeapon(),
                new AutoFlipFireball(), new AutoSwap(), new AutoTotem(),

                //Movement
                new AutoSprint(), new NoSlow(), new Speed(), new ScreenWalk(),

                // Player
                new ClickActions(), new NoDelay(), new NoPush(), new FastExp(), new FreelookModule(), new ElytraHelper(),

                // Render
                new HudElement(), new NoRender(), new Notifications(), new NameTagsModule()
        );
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
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRenderGui(RenderGuiEvent.Pre event) {
        GuiGraphics guiGraphics = event.getGuiGraphics();
        MouseHandler mouseHandler = Minecraft.getInstance().mouseHandler;
        mouseX = mouseHandler.xpos() * Minecraft.getInstance().getWindow().getGuiScaledWidth() / Minecraft.getInstance().getWindow().getScreenWidth();
        mouseY = mouseHandler.ypos() * Minecraft.getInstance().getWindow().getGuiScaledHeight() / Minecraft.getInstance().getWindow().getScreenHeight();
        if (DragManager.isAnyDragging() && snapGride.isEnabled()) {
            DragManager.renderGrid(guiGraphics, mc.getWindow());
        }
        for (DraggableElement element : DragManager.draggables.values()) {
            element.onDraw((int) mouseX, (int) mouseY, Minecraft.getInstance().getWindow());
            element.render(guiGraphics);
        }
        notificationManager.render(guiGraphics);
    }

    @SubscribeEvent
    public void onMouseClick(ScreenEvent.MouseButtonPressed.Pre event) {
        if (mc.player == null || mc.level == null) return;
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
    public void OnTickEvent(OnUpdate event) {
        if (mc.player == null) return;
        if (!(mc.player.input instanceof KeyboardInputHook)) {
            mc.player.input = new KeyboardInputHook(mc.options);
        }
    }

    @SubscribeEvent
    public void onClientCommand(ClientChatEvent event) {
        String msg = event.getMessage();
        String[] parts = msg.toLowerCase().trim().split("\\s+");
        System.out.println(Arrays.toString(parts));
        for (DraggableElement element : DragManager.draggables.values()) {
            System.out.println(element);
            if (element instanceof StaffList staffList) {
                staffList.handleCommand(msg);
            }
        }
        if (msg.startsWith(".staff")) event.setCanceled(true);
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
    @SubscribeEvent
    public void onUpdateInput(UpdateInputEvent event) {
        if (isResetingSprint) {
            event.setLeftImpulse(0.0f);
            event.setForwardImpulse(0.0f);
            event.setSprintTriggerTime(5);
            mc.player.setSprinting(false);
            TimerUtil.sleepVoid(() -> isResetingSprint = false, 40);
        }
        if (isSlowBypass) {
            event.setLeftImpulse(0.0f);
            event.setForwardImpulse(0.0f);
            event.setSprintTriggerTime(5);
            mc.player.setSprinting(false);
            TimerUtil.sleepVoid(() -> isSlowBypass = false, 150);
        }
    }
}