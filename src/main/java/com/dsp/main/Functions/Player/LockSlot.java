package com.dsp.main.Functions.Player;

import com.dsp.main.Core.Event.ClientPacketSendEvent;
import com.dsp.main.Core.Event.OnUpdate;
import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.MultiCheckBox;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.CheckBox;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.world.inventory.ClickType;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.Arrays;

public class LockSlot extends Module {
    public static MultiCheckBox lockSlots = new MultiCheckBox("Locked Slots", Arrays.asList(
            new CheckBox("Slot 1", false),
            new CheckBox("Slot 2", false),
            new CheckBox("Slot 3", false),
            new CheckBox("Slot 4", false),
            new CheckBox("Slot 5", false),
            new CheckBox("Slot 6", false),
            new CheckBox("Slot 7", false),
            new CheckBox("Slot 8", false),
            new CheckBox("Slot 9", false)
    ));

    private boolean wasDropPressed = false;

    public LockSlot() {
        super("Lock Slot", 0, Category.PLAYER, "Preventing dropping items by Q key in selected slots");
        addSettings(lockSlots);
    }

    @SubscribeEvent
    public void onPacketSend(ClientPacketSendEvent e) {
        if (Minecraft.getInstance().player == null) return;
        if (e.getPacket() instanceof ServerboundPlayerActionPacket packet) {
            if (packet.getAction() == ServerboundPlayerActionPacket.Action.DROP_ITEM ||
                    packet.getAction() == ServerboundPlayerActionPacket.Action.DROP_ALL_ITEMS) {
                int currentSlot = Minecraft.getInstance().player.getInventory().selected;
                if (isSlotLocked(currentSlot)) {
                    e.setCanceled(true);
                    Minecraft.getInstance().options.keyDrop.setDown(false);
                }
            }
        }
    }

    @SubscribeEvent
    public void onUpdate(OnUpdate e) {
        if (Minecraft.getInstance().player == null) return;
        if (Minecraft.getInstance().options.keyDrop.isDown()) {
            if (!wasDropPressed) {
                wasDropPressed = true;
                int currentSlot = Minecraft.getInstance().player.getInventory().selected;
                if (isSlotLocked(currentSlot)) {
                    Minecraft.getInstance().options.keyDrop.setDown(false);
                }
            }
        } else {
            wasDropPressed = false;
        }
    }

    public static boolean isSlotLocked(int slot) {
        return lockSlots.isOptionEnabled("Slot " + (slot + 1));
    }
}