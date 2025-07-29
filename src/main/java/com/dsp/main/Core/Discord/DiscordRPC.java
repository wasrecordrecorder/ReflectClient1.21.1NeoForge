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
    public static void startDiscordRPC() {
        DiscordEventHandlers eventHandlers = new DiscordEventHandlers();
        discordRPC.Discord_Initialize("1397175637235732530", eventHandlers, true, null);

        discordRichPresence.startTimestamp = System.currentTimeMillis() / 1000L;
        discordRichPresence.largeImageText = "Version: 3.3 | Build: Free";
        discordRichPresence.smallImageText = "Developed by was_record";
        discordRichPresence.largeImageKey = "fon";
        discordRichPresence.smallImageKey = "min";

        running = true;
        discordThread = new Thread(() -> {
            while (running && !Thread.currentThread().isInterrupted()) {
                try {
                    updateDiscordPresence();
                    Thread.sleep(2000);
                    if (isDetect) {
                        discordRPC.Discord_Shutdown();
                        discordRPC.Discord_ClearPresence();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
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
            discordRPC.Discord_Shutdown();
        }));
    }

    private static void updateDiscordPresence() {
        if (mc == null) return;

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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
