package com.dsp.main;

import com.dsp.main.Core.Sound.SoundRegister;
import com.dsp.main.Functions.Combat.*;
import com.dsp.main.Functions.Combat.Aura.Aura;
import com.dsp.main.Functions.Combat.Aura.ElytraTarget;
import com.dsp.main.Functions.Misc.*;
import com.dsp.main.Functions.Movement.*;
import com.dsp.main.Functions.Player.*;
import com.dsp.main.Functions.Render.*;
import com.dsp.main.Core.ConfigSystem.CfgManager;
import com.dsp.main.Core.Event.OnUpdate;
import com.dsp.main.Core.Event.UpdateInputEvent;
import com.dsp.main.Core.Other.Hooks.KeyboardInputHook;
import com.dsp.main.Functions.Render.XRay.XRay;
import com.dsp.main.UI.ClickGui.CsWindow.MainScreen;
import com.dsp.main.UI.ClickGui.Dropdown.ClickGuiScreen;
import com.dsp.main.Functions.Movement.AutoSprint;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.BindCheckBox;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.Setting;
import com.dsp.main.UI.Draggable.DragElements.StaffList;
import com.dsp.main.UI.Draggable.DragManager;
import com.dsp.main.UI.Draggable.DraggableElement;
import com.dsp.main.UI.MainMenu.MainMenuScreen;
import com.dsp.main.Core.Other.Hooks.InventoryScreenHook;
import com.dsp.main.UI.Notifications.NotificationManager;
import com.dsp.main.Utils.Engine.Particle.SinusoidEngine;
import com.dsp.main.Utils.Minecraft.Client.ClientFallDistance;
import com.dsp.main.Utils.TimerUtil;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.dsp.main.Functions.Misc.ClientSetting.*;
import static com.dsp.main.Main.isDetect;

public class Api {
    private double mouseX;
    private double mouseY;
    public static Minecraft mc = Minecraft.getInstance();
    public static CopyOnWriteArrayList<Module> Functions = new CopyOnWriteArrayList<>();
    private boolean isCfgLoaded = false;
    private int ticksSinceStart = 0;
    public static boolean isResetingSprint = false;
    public static boolean isSlowBypass = false;
    public static NotificationManager notificationManager = new NotificationManager();
    public static float partialTickAp;
    private static Path customConfigDirCache;
    private static Path dragDataCache;

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
                //misc
                new AutoLeave(), new AntiAttack(), new AutoAccept(), new UnHook(), new ItemScroller(), new AutoRespawn(),
                new ItemSwapFix(), new AutoAuch(), new AutoJoiner(), new ClientSetting(), new FuntimeHelper(), new HolyworldHelper(),
                new NoServerRot(), new SRPSpoof(), new SlowPacket(), new AuctionHelper(), new AutoEZ(), new DeathCoordinates(),
                new AutoFish(),
                //combat
                new Aura(), new ElytraTarget(), new TriggerBot(), new AimAssistant(), new AntiBot(), new AutoGApple(), new HitBox(), new AutoWeapon(),
                new AutoFlipFireball(), new AutoSwap(), new AutoTotem(), new BreachSwap(), new AimTrainer(), new SuperBow(),
                //movement
                new AutoSprint(), new NoSlow(), new Speed(), new ScreenWalk(), new Scaffhold(), new ElytraRecast(),
                //player
                new ClickActions(), new NoDelay(), new NoPush(), new FastExp(), new FreelookModule(), new ElytraHelper(),
                new AutoSoup(), new SafeWalking(), new LockSlot(), new TapeMouse(), new ClanInvest(),
                new ChestStealer(), new AntiAfk(), new AutoDodge(), new InvCleaner(), new Nuker(),
                //render
                new HudElement(), new NoRender(), new Notifications(), new NameTagsModule(), new Predictions(), new Fullbright(),
                new Snow(), new BoxEsp(), new JumpCircles(), new TNTTimer(), new XRay(), new ViewModel()
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

    public static Module getModule(String name) {
        for (Module m : Functions) {
            if (Objects.equals(m.name, name)) {
                return m;
            }
        }
        return null;
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
                if (ClickGuiType.isMode("Dropdown")) {
                    mc.setScreen(new ClickGuiScreen());
                } else {
                    mc.setScreen(new MainScreen());
                }
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
        partialTickAp = event.getPartialTick().getGameTimeDeltaTicks();
        MouseHandler mouseHandler = Minecraft.getInstance().mouseHandler;
        mouseX = mouseHandler.xpos() * Minecraft.getInstance().getWindow().getGuiScaledWidth() / Minecraft.getInstance().getWindow().getScreenWidth();
        mouseY = mouseHandler.ypos() * Minecraft.getInstance().getWindow().getGuiScaledHeight() / Minecraft.getInstance().getWindow().getScreenHeight();
        DragManager.renderGrid(guiGraphics, mc.getWindow());
        for (DraggableElement element : DragManager.draggables.values()) {
            element.updateAnimation((float) deltaTime());
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
    public void onAttack(AttackEntityEvent e) {
        if (isDetect) return;
        if (e.getTarget() == mc.player || !sound.isEnabled() || hitSound.isMode("None")) return;
        SoundEvent selectedSound = switch (hitSound.getMode().toUpperCase()) {
            case "BELL"     -> SoundRegister.BELL.get();
            case "BONK"     -> SoundRegister.BONK.get();
            case "CRIME"    -> SoundRegister.CRIME.get();
            case "METALLIC" -> SoundRegister.METALLIC.get();
            case "RUST"     -> SoundRegister.RUST.get();
            default         -> null;
        };
        if (selectedSound != null) {
            mc.player.playSound(selectedSound, 1.0F, 1.0F);
        }
    }

    @SubscribeEvent
    public void OnTickEvent(OnUpdate event) {
        if (mc.player == null || mc.level == null) {
            ClientFallDistance.reset();
            return;
        }

        if (!isCfgLoaded) {
            ticksSinceStart++;
            if (ticksSinceStart >= 40) {
                CfgManager.loadCfg("autoload");
                isCfgLoaded = true;
            }
        }
        ClientFallDistance.update(mc.player);
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
            mc.setScreen(new MainMenuScreen());
        }
        if (e.getScreen() instanceof net.minecraft.client.gui.screens.inventory.InventoryScreen && !isDetect && mc.player != null) {
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

    public static float fast(float end, float start, float multiple) {
        return (1 - Mth.clamp((float) (deltaTime() * multiple), 0, 1)) * end + Mth.clamp((float) (deltaTime() * multiple), 0, 1) * start;
    }

    private static double deltaTime() {
        float fps = 60;
        try {
            fps = Integer.parseInt(Minecraft.getInstance().fpsString.split(" ")[0]);
        }catch (Exception ignore){}
        return fps > 0 ? (1.0000 / fps) : 1;
    }

    public static Path getCrossPlatformAppDataFolder() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return Paths.get(System.getenv("APPDATA"));
        } else if (os.contains("mac")) {
            return Paths.get(System.getProperty("user.home"), "Library", "Application Support");
        } else {
            return Paths.get(System.getProperty("user.home"));
        }
    }

    public static Path getCustomConfigDir() {
        if (customConfigDirCache == null) {
            Path baseDir = getCrossPlatformAppDataFolder();
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                customConfigDirCache = baseDir.resolve("Some");
            } else {
                customConfigDirCache = baseDir.resolve(".Some");
            }
        }
        return customConfigDirCache;
    }

    private static float animation = 0;

    @SubscribeEvent
    public void onEventContinuous(RenderFrameEvent.Pre e) {
        if (mc.player == null) return;
        if (mc.options.getCameraType() == CameraType.FIRST_PERSON) animation = fast(animation, 0f, 10);
        else animation = fast(animation, 1f, 10);
        SinusoidEngine.tickAll(mc.level);
    }

    public static double getDistance(double dis) {
        return 1f + ((dis - 1f) * animation);
    }

    @SubscribeEvent
    public void onPacket(net.neoforged.neoforge.client.event.ClientChatReceivedEvent event) {
        try {
            if (Api.isEnabled("AutoEZ")) {
                if (event.getMessage() != null) {
                    ClientboundSystemChatPacket packet = new ClientboundSystemChatPacket(event.getMessage(), false);
                    AutoEZ.onChatPacket(packet);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}