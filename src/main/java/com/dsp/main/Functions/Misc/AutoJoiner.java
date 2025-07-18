package com.dsp.main.Functions.Misc;

import com.dsp.main.Managers.Event.OnUpdate;
import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Settings.Input;
import com.dsp.main.UI.ClickGui.Settings.Mode;
import com.dsp.main.UI.ClickGui.Settings.Slider;
import com.dsp.main.Utils.Minecraft.Chat.ChatUtil;
import com.dsp.main.Utils.Minecraft.Client.InvUtil;
import com.dsp.main.Utils.Minecraft.Server.WhatServer;
import com.dsp.main.Utils.TimerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.event.ServerChatEvent;
import org.apache.logging.log4j.core.jmx.Server;

import static com.dsp.main.Api.mc;
import static com.dsp.main.Utils.Minecraft.Client.InvUtil.getSlotIDFromItem;

public class AutoJoiner extends Module {
    private boolean isJoinedRwHub = false;
    private boolean isJoinedSpookyLobbu = false;
    private static Mode server = new Mode("Server", "Funtime", "SpookyTime Duels", "Rw");
    private static Input anarchy = new Input("Anarchy", "103").setVisible(() -> server.isMode("Funtime"));
    private static Slider grief = new Slider("Grief", 1, 54, 13, 1).setVisible(() -> server.isMode("Rw"));
    private long lastActionTime = 0;
    private boolean waitingForContainer = false;
    private boolean waitingForSecondClick = false;
    private boolean waitingForDuelCLick = false;

    public AutoJoiner() {
        super("Auto Join", 0, Category.MISC, "Automatically joining to selected servers");
        addSettings(server, anarchy, grief);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        isJoinedSpookyLobbu = false;
        waitingForContainer = false;
        waitingForSecondClick = false;
        waitingForDuelCLick = false;
        isJoinedRwHub = false;
    }

    @SubscribeEvent
    public void onUpdate(OnUpdate event) {
        if (mc.player == null || mc.level == null) return;

        if (server.isMode("Rw") && WhatServer.isRw() && isJoinedRwHub) {
            isJoinedRwHub = false;
            if (FindItemSlot(Items.COMPASS, 9) > 0) {
                mc.gameMode.handlePickItem(FindItemSlot(Items.COMPASS, 9));
            }
            mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
            lastActionTime = System.currentTimeMillis();
            waitingForContainer = true;
        }

        if (waitingForContainer && System.currentTimeMillis() - lastActionTime >= 500) {
            if (mc.screen instanceof AbstractContainerScreen<?> containerScreen) {
                for (Slot slot : containerScreen.getMenu().slots) {
                    ItemStack stack = slot.getItem();
                        Component name = stack.getHoverName();
                        if (name.getString().contains("ГРИФЕРСКОЕ ВЫЖИВАНИЕ")) {
                            mc.gameMode.handleInventoryMouseClick(
                                    containerScreen.getMenu().containerId,
                                    slot.index,
                                    0,
                                    net.minecraft.world.inventory.ClickType.PICKUP,
                                    mc.player
                            );
                            lastActionTime = System.currentTimeMillis();
                            waitingForContainer = false;
                            waitingForSecondClick = true;
                            break;
                        }
                }
            } else {
                TimerUtil.sleepVoid(() -> isJoinedRwHub = true, 500);
            }
            waitingForContainer = false;
        }

        if (waitingForSecondClick && System.currentTimeMillis() - lastActionTime >= 500) {
            if (mc.screen instanceof AbstractContainerScreen<?> containerScreen) {
                String targetName = "#" + grief.getValueInt();
                for (Slot slot : containerScreen.getMenu().slots) {
                    ItemStack stack = slot.getItem();
                        Component name = stack.getHoverName();
                        if (name.getString().contains(targetName)) {
                            mc.gameMode.handleInventoryMouseClick(
                                    containerScreen.getMenu().containerId,
                                    slot.index,
                                    0,
                                    net.minecraft.world.inventory.ClickType.PICKUP,
                                    mc.player
                            );
                            waitingForSecondClick = false;
                            break;
                        }
                }
            }
            waitingForSecondClick = false;
        }

        // -------
        if (WhatServer.isSt() && server.isMode("SpookyTime Duels") && mc.screen == null && !isJoinedSpookyLobbu) {
            mc.player.getInventory().selected = 0;
            if (FindItemSlot(Items.COMPASS, 9) > 0) {
                mc.player.getInventory().selected = (FindItemSlot(Items.COMPASS, 9));
            }
            if (FindItemSlot(Items.DIAMOND_SWORD, 9) == 0) {
                isJoinedSpookyLobbu = true;
                this.toggle();
                return;
            }
            mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
            waitingForDuelCLick = true;
            lastActionTime = System.currentTimeMillis();
        }
        if (waitingForDuelCLick && System.currentTimeMillis() - lastActionTime >= 400) {
            if (mc.screen instanceof AbstractContainerScreen<?> containerScreen) {
                for (Slot slot : containerScreen.getMenu().slots) {
                    ItemStack stack = slot.getItem();
                    Component name = stack.getHoverName();
                    if (name.getString().contains("Дуэли")) {
                        mc.gameMode.handleInventoryMouseClick(
                                containerScreen.getMenu().containerId,
                                slot.index,
                                0,
                                net.minecraft.world.inventory.ClickType.PICKUP,
                                mc.player
                        );
                        lastActionTime = System.currentTimeMillis();
                        waitingForDuelCLick = false;
                        break;
                    }
                }
            } else {
                TimerUtil.sleepVoid(() -> isJoinedSpookyLobbu = false, 200);
            }
        }
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        String msg = event.getMessage().getString();
        if (msg.contains("! Внимание !") && msg.contains("Чтобы ваш Аккаунт был в БЕЗОПАСНОСТИ!") && WhatServer.isFt() && !anarchy.getValue().isEmpty()) {
            mc.player.connection.sendCommand("an" + anarchy.getValue());
        } else if ((msg.contains("Добро пожаловать на FunTime.su") || msg.contains("Наши сообщества здесь /links") || msg.contains("Наш официальный сайт FunTime.su")) && WhatServer.isFt() && !anarchy.getValue().isEmpty()) {
            mc.player.connection.sendCommand("an" + anarchy.getValue());
        } else if ((msg.contains("Наш Телеграм t.me/funtime") && WhatServer.isFt() && !anarchy.getValue().isEmpty())) {
            mc.player.connection.sendCommand("an" + anarchy.getValue());
        }
        if (msg.contains("Добро пожаловать на сервер ReallyWorld!")) {
            isJoinedRwHub = true;
        }
        // ------
    }
    @SubscribeEvent
    public void onChat2(ServerChatEvent event) {
        String msg = event.getMessage().getString();
        if (msg.contains("! Внимание !") && msg.contains("Вам НУЖНО ввести команду /vk!") && msg.contains("Чтобы ваш Аккаунт был в БЕЗОПАСНОСТИ!") && WhatServer.isFt() && !anarchy.getValue().isEmpty()) {
            mc.player.connection.sendCommand("an" + anarchy.getValue());
        } else if ((msg.contains("Добро пожаловать на FunTime.su") || msg.contains("Наши сообщества здесь /links") || msg.contains("Наш официальный сайт FunTime.su")) && WhatServer.isFt() && !anarchy.getValue().isEmpty()) {
            mc.player.connection.sendCommand("an" + anarchy.getValue());
        }
    }
    public static int FindItemSlot(Item ItemToFind, int SlotsRange) {
        LocalPlayer player = mc.player;
        int normalTotemSlot = -1;
        int enchantedTotemSlot = -1;

        for (int i = 0; i < SlotsRange; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty() || stack.getItem() != ItemToFind)
                continue;
            if (ItemToFind == Items.TOTEM_OF_UNDYING) {
                if (stack.isEnchanted()) {
                    enchantedTotemSlot = i;
                    break;
                } else {
                    normalTotemSlot = i;
                }
            } else {
                return i;
            }
        }
        return enchantedTotemSlot != -1 ? enchantedTotemSlot : normalTotemSlot;
    }
}