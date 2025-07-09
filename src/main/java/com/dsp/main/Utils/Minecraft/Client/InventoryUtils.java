package com.dsp.main.Utils.Minecraft.Client;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

public class InventoryUtils {
    private static boolean upDowned = false;
    private static int pendingSwapSlot = -1;
    private static int pendingHotbarSlot = -1;
    private static boolean swapScheduled = false;

    public static int useItemFromInventory(Item itemToUse, int slotFromHotbar) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.level == null) return -1;

        int origSlot = player.getInventory().selected;

        // Поиск в хотбаре (слоты 0-8)
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() == itemToUse) {
                player.getInventory().selected = i;
                mc.gameMode.useItem(player, InteractionHand.MAIN_HAND);
                player.getInventory().selected = origSlot;
                return i;
            }
        }

        // Поиск в остальном инвентаре (слоты 9+)
        for (int i = 9; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() == itemToUse) {
                if (stack.getItem() == Items.ENDER_PEARL && player.isFallFlying()) {
                    return -1; // Не используем эндер-жемчуг в полёте
                }

                // Сохраняем данные слота в хотбаре
                ItemStack hotbarItem = player.getInventory().getItem(slotFromHotbar);
                Component hotbarName = hotbarItem.getDisplayName();


                // Меняем предметы местами
                mc.gameMode.handleInventoryMouseClick(
                        player.containerMenu.containerId,
                        i,
                        slotFromHotbar,
                        net.minecraft.world.inventory.ClickType.SWAP,
                        player
                );

                // Проверяем, появился ли нужный предмет в хотбаре
                for (int j = 0; j < 9; j++) {
                    if (player.getInventory().getItem(j).getItem() == itemToUse) {
                        player.getInventory().selected = j;
                        mc.gameMode.useItem(player, InteractionHand.MAIN_HAND);
                        player.getInventory().selected = origSlot;
                        for (int e = 9; e < player.getInventory().getContainerSize(); e++) {
                            ItemStack currentStack = player.getInventory().getItem(e);
                            if (currentStack.getItem() == hotbarItem.getItem() &&
                                    currentStack.getDisplayName().equals(hotbarName)) {
                                pendingSwapSlot = e;
                                pendingHotbarSlot = slotFromHotbar;
                                if (!swapScheduled) {
                                    scheduleSwap();
                                    swapScheduled = true;
                                }
                                player.getInventory().setChanged();
                                return e;
                            }
                        }
                        return j;
                    }
                }
            }
        }
        return -1;
    }

    private static void scheduleSwap() {
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> {
            if (pendingSwapSlot != -1 && pendingHotbarSlot != -1) {
                Minecraft mc = Minecraft.getInstance();
                Player player = mc.player;
                if (player != null) {
                    mc.gameMode.handleInventoryMouseClick(
                            player.containerMenu.containerId,
                            pendingSwapSlot,
                            pendingHotbarSlot,
                            net.minecraft.world.inventory.ClickType.SWAP,
                            player
                    );
                    mc.gameMode.handleInventoryMouseClick(
                            player.containerMenu.containerId,
                            pendingHotbarSlot,
                            pendingHotbarSlot,
                            net.minecraft.world.inventory.ClickType.PICKUP,
                            player
                    );
                    pendingSwapSlot = -1;
                    pendingHotbarSlot = -1;
                    swapScheduled = false;
                    NeoForge.EVENT_BUS.unregister(InventoryUtils.class);
                }
            }
        });
    }
    private static boolean ValidateItem(ItemStack item) {
        if (item.getItem() == Items.ENDER_EYE) return item.getHoverName().getString().contains("★");
        if (item.getItem() == Items.NETHERITE_SCRAP) return item.getHoverName().getString().contains("★");
        if (item.getItem() == Items.DRIED_KELP) return item.getHoverName().getString().contains("★");
        if (item.getItem() == Items.SUGAR) return item.getHoverName().getString().contains("★");
        if (item.getItem() == Items.PHANTOM_MEMBRANE) return item.getHoverName().getString().contains("★");
        if (item.getItem() == Items.FIRE_CHARGE) return item.getHoverName().getString().contains("★");
        if (item.getItem() == Items.SNOWBALL) return item.getHoverName().getString().contains("★");
        return true;
    }
}