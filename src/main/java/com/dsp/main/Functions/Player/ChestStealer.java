package com.dsp.main.Functions.Player;

import com.dsp.main.Core.Event.OnUpdate;
import com.dsp.main.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.Arrays;
import java.util.List;

public class ChestStealer extends Module {
    private long lastMove;

    public ChestStealer() {
        super("Auto Myst", 0, Category.PLAYER, "Automatically takes items from event chests");
    }

    private boolean shouldSkip(String title) {
        String low = title.toLowerCase();
        return low.contains("аукционы") || low.contains("поиск:") || low.contains("хранилище") || low.contains("эндер-сундук");
    }

    @SubscribeEvent
    public void onUpdate(OnUpdate e) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !(mc.player.containerMenu instanceof ChestMenu chest)) return;

        Screen screen = mc.screen;
        if (!(screen instanceof AbstractContainerScreen)) return;
        String title = screen.getTitle().getString();
        if (shouldSkip(title)) return;

        int size = chest.getContainer().getContainerSize();
        List<Integer> order = Arrays.asList(
                53, 51, 49, 47, 45, 43, 41, 39, 37, 35, 33, 31, 29, 27, 25, 23, 21, 19, 17, 15, 13, 11, 9, 7, 5, 3, 1,
                52, 50, 48, 46, 44, 42, 40, 38, 36, 34, 32, 30, 28, 26, 24, 22, 20, 18, 16, 14, 12, 10, 8, 6, 4, 2, 0
        );

        for (int slot : order) {
            if (slot >= size) continue;
            ItemStack stack = chest.getContainer().getItem(slot);
            if (!stack.isEmpty() && System.currentTimeMillis() - lastMove > 55) {
                mc.gameMode.handleInventoryMouseClick(
                        chest.containerId, slot, 0, ClickType.QUICK_MOVE, mc.player
                );
                lastMove = System.currentTimeMillis();
                break;
            }
        }
    }
}