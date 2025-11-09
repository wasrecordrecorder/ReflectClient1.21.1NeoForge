package com.dsp.main.Core.Discord;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRichPresence;
import com.dsp.main.UI.MainMenu.MainMenuScreen;
import com.dsp.main.Utils.Minecraft.Server.isPvP;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;

import static com.dsp.main.Api.mc;
import static com.dsp.main.Main.isDetect;

public class DiscordRPC {
    public static DiscordRichPresence discordRichPresence = new DiscordRichPresence();
    public static club.minnced.discord.rpc.DiscordRPC discordRPC = club.minnced.discord.rpc.DiscordRPC.INSTANCE;
    public static String state = "Nothing..";
    private static Thread discordThread;
    private static volatile boolean running = false;
    private static boolean isEnabled = false;
    private static boolean isMobile = false;

    public static void startDiscordRPC() {
        if (isMobileDevice()) {
            isMobile = true;
            System.out.println("Discord RPC: Disabled on mobile device");
            return;
        }

        try {
            DiscordEventHandlers eventHandlers = new DiscordEventHandlers();
            discordRPC.Discord_Initialize("1397175637235732530", eventHandlers, true, null);

            discordRichPresence.startTimestamp = System.currentTimeMillis() / 1000L;
            discordRichPresence.largeImageText = "Version: 3.3 | Build: Free";
            discordRichPresence.smallImageText = "Developed by was_record";
            discordRichPresence.largeImageKey = "fon";
            discordRichPresence.smallImageKey = "min";

            isEnabled = true;
            running = true;

            discordThread = new Thread(() -> {
                while (running && !Thread.currentThread().isInterrupted()) {
                    try {
                        updateDiscordPresence();
                        Thread.sleep(2000);
                        if (isDetect) {
                            safeShutdown();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (UnsatisfiedLinkError e) {
                        System.err.println("Discord RPC: Native library error - disabling");
                        isEnabled = false;
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, "Discord-RPC-Updater");

            discordThread.setDaemon(true);
            discordThread.start();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                running = false;
                if (discordThread != null) {
                    discordThread.interrupt();
                }
                safeShutdown();
            }));

            System.out.println("Discord RPC: Successfully initialized");
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Discord RPC: Failed to load native library (mobile device or unsupported platform)");
            isEnabled = false;
        } catch (Exception e) {
            System.err.println("Discord RPC: Failed to initialize - " + e.getMessage());
            e.printStackTrace();
            isEnabled = false;
        }
    }

    private static void updateDiscordPresence() {
        if (!isEnabled || isMobile || mc == null) return;

        try {
            String newState;
            if (mc.screen instanceof MainMenuScreen || mc.screen instanceof TitleScreen) {
                newState = "In main menu";
            } else if (mc.screen instanceof JoinMultiplayerScreen) {
                newState = "Choosing Server";
            } else if (mc.isSingleplayer()) {
                newState = "In single player";
            } else if (mc.getCurrentServer() != null) {
                newState = "Playing on " + mc.getCurrentServer().ip
                        .replace("mc.", "")
                        .replace("play.", "")
                        .replace("gg.", "")
                        .replace("go.", "")
                        .replace("join.", "")
                        .replace("creative.", "")
                        .replace(".top", "")
                        .replace(".ru", "")
                        .replace(".cc", "")
                        .replace(".space", "")
                        .replace(".eu", "")
                        .replace(".com", "")
                        .replace(".net", "")
                        .replace(".xyz", "")
                        .replace(".gg", "")
                        .replace(".me", "")
                        .replace(".su", "")
                        .replace(".fun", "")
                        .replace(".org", "")
                        .replace(".host", "")
                        .replace("localhost", "LocalServer")
                        .replace(":25565", "");
            } else if (mc.screen instanceof OptionsScreen) {
                newState = "On settings";
            } else if (mc.screen instanceof SelectWorldScreen) {
                newState = "Choosing world";
            } else {
                newState = "Loading...";
            }

            if (!newState.equals(state)) {
                state = newState;
                System.out.println("Discord RPC: " + state);
            }

            discordRichPresence.details = state;
            discordRichPresence.state = isPvP.isPvPMode() ? "In PvP" : "Get good, get Reflect..";
            discordRPC.Discord_UpdatePresence(discordRichPresence);
            discordRPC.Discord_RunCallbacks();

        } catch (UnsatisfiedLinkError e) {
            System.err.println("Discord RPC: Native library error - disabling");
            isEnabled = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void safeShutdown() {
        if (!isEnabled || isMobile) return;

        try {
            discordRPC.Discord_ClearPresence();
            discordRPC.Discord_Shutdown();
            isEnabled = false;
            System.out.println("Discord RPC: Shutdown complete");
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Discord RPC: Error during shutdown (already unloaded)");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean isMobileDevice() {
        try {
            String osName = System.getProperty("os.name", "").toLowerCase();
            String osArch = System.getProperty("os.arch", "").toLowerCase();

            if (osArch.contains("arm") || osArch.contains("aarch")) {
                System.out.println("Discord RPC: Detected ARM architecture (" + osArch + ")");
                return true;
            }

            if (System.getProperty("pojav.launcher") != null) {
                System.out.println("Discord RPC: Detected PojavLauncher");
                return true;
            }

            if (System.getProperty("pojavlaunch.version") != null) {
                System.out.println("Discord RPC: Detected PojavLauncher (version property)");
                return true;
            }

            if (System.getenv("ANDROID_ROOT") != null || System.getenv("ANDROID_DATA") != null) {
                System.out.println("Discord RPC: Detected Android environment");
                return true;
            }

            String javaVendor = System.getProperty("java.vendor", "").toLowerCase();
            if (javaVendor.contains("android")) {
                System.out.println("Discord RPC: Detected Android Java vendor");
                return true;
            }

            String javaHome = System.getProperty("java.home", "").toLowerCase();
            if (javaHome.contains("android") || javaHome.contains("termux")) {
                System.out.println("Discord RPC: Detected Android/Termux in java.home");
                return true;
            }

            if (osName.contains("linux") && System.getProperty("user.dir", "").contains("/data/")) {
                System.out.println("Discord RPC: Detected Android data directory structure");
                return true;
            }

        } catch (Exception e) {
            System.err.println("Discord RPC: Error checking mobile device: " + e.getMessage());
            return false;
        }

        return false;
    }

    public static boolean isEnabled() {
        return isEnabled && !isMobile;
    }

    public static boolean isMobile() {
        return isMobile;
    }

    public static void shutdown() {
        running = false;
        if (discordThread != null) {
            discordThread.interrupt();
        }
        safeShutdown();
    }
}