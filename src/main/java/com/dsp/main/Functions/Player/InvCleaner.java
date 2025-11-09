package com.dsp.main.Functions.Player;

import com.dsp.main.Core.Event.OnUpdate;
import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.ItemListSetting;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.Slider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;

import static com.dsp.main.Api.mc;

public class InvCleaner extends Module {
    private final ItemListSetting items;
    private final Slider delay;
    private long lastThrowTime = 0;

    public InvCleaner() {
        super("InvCleaner", 0, Category.PLAYER, "Auto throw unwanted items");

        items = new ItemListSetting("Items");
        delay = new Slider("Delay", 0, 1000, 100, 50);

        addSettings(items, delay);
    }

    @SubscribeEvent
    public void onUpdate(OnUpdate event) {
        if (mc.player == null || mc.gameMode == null) return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastThrowTime < delay.getValue()) return;

        if (items.getSelectedItems().isEmpty()) return;

        Inventory inventory = mc.player.getInventory();

        for (int slot = 0; slot < 36; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.isEmpty()) continue;

            Item item = stack.getItem();
            if (items.isItemSelected(item)) {
                int containerSlot = slot < 9 ? slot + 36 : slot;

                try {
                    mc.gameMode.handleInventoryMouseClick(
                            mc.player.containerMenu.containerId,
                            containerSlot,
                            1,
                            ClickType.THROW,
                            mc.player
                    );

                    lastThrowTime = currentTime;
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onEnable() {
        super.onEnable();
        lastThrowTime = 0;
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}