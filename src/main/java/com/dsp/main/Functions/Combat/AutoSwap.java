package com.dsp.main.Functions.Combat;

import com.dsp.main.Managers.Event.OnUpdate;
import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Settings.BindCheckBox;
import com.dsp.main.UI.ClickGui.Settings.CheckBox;
import com.dsp.main.UI.ClickGui.Settings.Mode;
import com.dsp.main.Utils.Minecraft.Chat.ChatUtil;
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

        if (currentOffhandItem == firstItemType) {
            int targetSlot = invUtil.getSlotInAllInventory(twoItemType);
            if (targetSlot != -1) {
                if (slowBypass.isEnabled()) isSlowBypass = true;
                invUtil.swapHand(targetSlot, InteractionHand.OFF_HAND);
            }
        } else if (currentOffhandItem == twoItemType) {
            int targetSlot = invUtil.getSlotInAllInventory(firstItemType);
            if (targetSlot != -1) {
                if (slowBypass.isEnabled()) isSlowBypass = true;
                invUtil.swapHand(targetSlot, InteractionHand.OFF_HAND);
            }
        } else {
            int targetSlot = invUtil.getSlotInAllInventory(firstItemType);
            if (targetSlot != -1) {
                if (slowBypass.isEnabled()) isSlowBypass = true;
                invUtil.swapHand(targetSlot, InteractionHand.OFF_HAND);
            } else {
                targetSlot = invUtil.getSlotInAllInventory(twoItemType);
                if (targetSlot != -1) {
                    if (slowBypass.isEnabled()) isSlowBypass = true;
                    invUtil.swapHand(targetSlot, InteractionHand.OFF_HAND);
                }
            }
        }
    }

    @SubscribeEvent
    public void onUpdate(OnUpdate event) {
    }
}