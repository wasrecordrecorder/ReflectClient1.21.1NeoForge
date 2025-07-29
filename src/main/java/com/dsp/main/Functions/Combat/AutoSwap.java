package com.dsp.main.Functions.Combat;

import com.dsp.main.Core.Event.OnUpdate;
import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.BindCheckBox;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.Mode;
import com.dsp.main.Utils.Minecraft.Client.InvUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.InteractionHand;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

import static com.dsp.main.Api.*;
import static com.dsp.main.Functions.Misc.ClientSetting.slowBypass;

public class AutoSwap extends Module {
    public static Mode firstItem = new Mode("Item 1", "GApple", "Shield", "Totem", "Sphere");
    public static Mode twoItem = new Mode("Item 2", "Sphere", "Totem", "Shield", "GApple");
    public static BindCheckBox swap = new BindCheckBox("Swap", 0, AutoSwap::performSwap);
    private static final InvUtil invUtil = new InvUtil();
    private static final Map<String, Item> ITEM_MAP = new HashMap<>();

    static {
        ITEM_MAP.put("GApple", Items.GOLDEN_APPLE);
        ITEM_MAP.put("Shield", Items.SHIELD);
        ITEM_MAP.put("Totem", Items.TOTEM_OF_UNDYING);
        ITEM_MAP.put("Sphere", Items.PLAYER_HEAD);
    }

    public AutoSwap() {
        super("AutoSwap", 0, Category.COMBAT, "Swaps offhand item on keybind");
        addSettings(firstItem, twoItem, swap);
    }

    private static void performSwap() {
        if (mc.player == null) return;

        Item currentOffhandItem = mc.player.getOffhandItem().getItem();
        Item firstItemType = ITEM_MAP.get(firstItem.getMode());
        Item twoItemType = ITEM_MAP.get(twoItem.getMode());

        if (firstItemType == twoItemType) return;
        int targetSlot = -1;

        if (currentOffhandItem == firstItemType) {
            targetSlot = (twoItemType == Items.TOTEM_OF_UNDYING)
                    ? getTotemSlotPrioritized()
                    : invUtil.getSlotInAllInventory(twoItemType);
        } else if (currentOffhandItem == twoItemType) {
            targetSlot = (firstItemType == Items.TOTEM_OF_UNDYING)
                    ? getTotemSlotPrioritized()
                    : invUtil.getSlotInAllInventory(firstItemType);
        } else {
            targetSlot = (firstItemType == Items.TOTEM_OF_UNDYING)
                    ? getTotemSlotPrioritized()
                    : invUtil.getSlotInAllInventory(firstItemType);

            if (targetSlot == -1) {
                targetSlot = (twoItemType == Items.TOTEM_OF_UNDYING)
                        ? getTotemSlotPrioritized()
                        : invUtil.getSlotInAllInventory(twoItemType);
            }
        }

        if (targetSlot != -1) {
            if (slowBypass.isEnabled()) isSlowBypass = true;
            invUtil.swapHand(targetSlot, InteractionHand.OFF_HAND);
        }
    }
    static int getTotemSlotPrioritized() {
        int enchantedTotem = -1;
        int normalTotem = -1;
        for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
            var stack = mc.player.getInventory().getItem(i);
            if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
                if (stack.isEnchanted()) {
                    enchantedTotem = i <= 8 ? i + 36 : i;
                } else if (normalTotem == -1) {
                    normalTotem = i <= 8 ? i + 36 : i;
                }
            }
        }
        return enchantedTotem != -1 ? enchantedTotem : normalTotem;
    }


    @SubscribeEvent
    public void onUpdate(OnUpdate event) {
    }
}