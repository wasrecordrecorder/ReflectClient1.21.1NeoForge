package com.dsp.main.Utils.Minecraft.Client;

import com.dsp.main.Core.Other.FreeLook;
import com.dsp.main.Utils.Minecraft.Chat.ChatUtil;
import com.dsp.main.Utils.Render.Mine;
import com.dsp.main.Utils.TimerUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;

import static com.dsp.main.Functions.Misc.ClientSetting.legitUse;
import static com.dsp.main.Functions.Misc.ClientSetting.obhod;

public class InvUtil implements Mine {
    public final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final Minecraft mc = Minecraft.getInstance();
    public static boolean requestFreeLook = false;

    public void moveItem(int from, int to) {
        if (from == to || from == -1)
            return;

        from = from < 9 ? from + 36 : from;
        int finalFrom = from;
        clickSlotId(from, 0, ClickType.PICKUP, obhod.isMode("Packet"));
        clickSlotId(to, 0, ClickType.PICKUP, obhod.isMode("Packet"));
        clickSlotId(from, 0, ClickType.PICKUP, obhod.isMode("Packet"));
    }

    public void swapHand(Slot slot, InteractionHand hand) {
        if (slot != null) swapHand(slot.index, hand);
    }
    public int getSlotInInventory(Predicate<Item> predicate) {
        for (int i = 0; i < 45; i++) {
            if (predicate.test(mc.player.getInventory().getItem(i).getItem())) {
                return i;
            }
        }
        return -1;
    }

    public void swapHand(int slotId, InteractionHand hand) {
        if (slotId == -1) return;
        int button = hand == InteractionHand.MAIN_HAND ? mc.player.getInventory().selected : 40;
        clickSlotId(slotId, button, ClickType.SWAP, obhod.isMode("Packet"));
        //TimerUtil.sleepVoid(() -> clickSlotId(slotId, button, ClickType.SWAP, packet), 0);

        mc.player.getInventory().setChanged();
    }

    public Slot getSlot(Item item) {
        return mc.player.containerMenu.slots.stream()
                .filter(s -> s.getItem().getItem() == item)
                .findFirst()
                .orElse(null);
    }

    public Slot getInventorySlot(Item item) {
        return mc.player.containerMenu.slots.stream()
                .filter(s -> s.getItem().getItem() == item && s.index >= mc.player.containerMenu.slots.size() - 37)
                .findFirst()
                .orElse(null);
    }

    public Slot getInventorySlot(List<Item> items) {
        return mc.player.containerMenu.slots.stream()
                .filter(s -> items.contains(s.getItem().getItem()) && s.index >= mc.player.containerMenu.slots.size() - 37)
                .findFirst()
                .orElse(null);
    }

    public int getInventoryCount(Item item) {
        return IntStream.range(0, 45)
                .filter(i -> mc.player.getInventory().getItem(i).getItem() == item)
                .map(i -> mc.player.getInventory().getItem(i).getCount())
                .sum();
    }

    public Slot getFoodMaxSaturationSlot() {
        return mc.player.containerMenu.slots.stream()
                .filter(s -> s.getItem().get(DataComponents.FOOD) != null && !s.getItem().get(DataComponents.FOOD).canAlwaysEat())
                .max(Comparator.comparingDouble(s -> s.getItem().get(DataComponents.FOOD).nutrition()))
                .orElse(null);
    }

    public Slot getSlot(List<Item> items) {
        return mc.player.containerMenu.slots.stream()
                .filter(s -> items.contains(s.getItem().getItem()))
                .findFirst()
                .orElse(null);
    }

    public int getSlotInInventoryOrHotbar(Item item, boolean inHotBar) {
        int firstSlot = inHotBar ? 0 : 9;
        int lastSlot = inHotBar ? 9 : 36;
        int finalSlot = -1;
        for (int i = firstSlot; i < lastSlot; i++) {
            if (mc.player.getInventory().getItem(i).getItem() == item) {
                finalSlot = i;
            }
        }
        return finalSlot;
    }

    public int getCount(Item item) {
        return mc.player.containerMenu.slots.stream()
                .filter(s -> s.getItem().getItem() == item)
                .mapToInt(s -> s.getItem().getCount())
                .sum();
    }

    public int getItemInHotBar(Item item) {
        return IntStream.range(0, 9)
                .filter(i -> mc.player.getInventory().getItem(i).getItem() == item)
                .findFirst()
                .orElse(-1);
    }

    public Slot getAxeSlot() {
        return mc.player.containerMenu.slots.stream()
                .filter(s -> s.getItem().getItem() instanceof AxeItem)
                .findFirst()
                .orElse(null);
    }

    public void findItemAndThrow(Item item, float yaw, float pitch) {
        if (mc.player.getCooldowns().isOnCooldown(new ItemStack(item))) {
            ChatUtil.sendMessage(item.getName().getString() + " - в кд");
            return;
        }

        Slot slot = getSlot(item);
        if (slot == null) {
            ChatUtil.sendMessage(item.getName().getString() + " - нету");
            return;
        }

        findItemAndThrow(slot, yaw, pitch);
    }

    public void findItemAndThrow(Slot slot, float yaw, float pitch) {
        swapHand(slot, InteractionHand.MAIN_HAND);
        useItem(InteractionHand.MAIN_HAND, yaw, pitch);
        if (legitUse.isEnabled()) {
            TimerUtil.sleepVoid(() -> swapHand(slot, InteractionHand.MAIN_HAND), 80);
        } else {
            swapHand(slot, InteractionHand.MAIN_HAND);
        }
    }

    public void clickSlot(Slot slot, int button, ClickType clickType, boolean packet) {
        if (slot != null) clickSlotId(slot.index, button, clickType, packet);
    }

    public void clickSlotId(int slotId, int buttonId, ClickType clickType, boolean packet) {
        clickSlotId(mc.player.containerMenu.containerId, slotId, buttonId, clickType, packet);
    }

    public void clickSlotId(int windowId, int slotId, int buttonId, ClickType clickType, boolean packet) {
        if (packet) {
            Int2ObjectMap<ItemStack> changedSlots = new Int2ObjectOpenHashMap<>();
            mc.player.connection.send(new ServerboundContainerClickPacket(
                    windowId,
                    mc.player.containerMenu.getStateId(),
                    slotId,
                    buttonId,
                    clickType,
                    ItemStack.EMPTY,
                    changedSlots
            ));
        } else {
            mc.gameMode.handleInventoryMouseClick(windowId, slotId, buttonId, clickType, mc.player);
        }
    }
    int secuCount = 0;

    public void useItem(InteractionHand hand, float yaw, float pitch) {

        if (FreeLook.isFreeLookEnabled) {
            requestFreeLook = true;
            mc.player.setYRot(yaw);
            mc.player.setXRot(pitch);
        }
        mc.player.connection.send(new ServerboundUseItemPacket(hand,secuCount++,(int) yaw, pitch));
        if (requestFreeLook) TimerUtil.sleepVoid(() -> requestFreeLook = false, 100);
    }

    public long noEmptyHotBarSlots() {
        return getHotBarSlots().stream().filter(s -> s.getItem().isEmpty()).count();
    }

    public List<Slot> getHotBarSlots() {
        List<Slot> slots = mc.player.containerMenu.slots;
        return new ArrayList<>(slots.stream().filter(i -> i.index > slots.size() - 10).toList());
    }

    public List<Slot> getMainInventorySlots() {
        List<Slot> list = new ArrayList<>();
        for (int i = mc.player.containerMenu.slots.size() - 37; i < mc.player.containerMenu.slots.size(); i++) {
            list.add(mc.player.containerMenu.getSlot(i));
        }
        return list;
    }

    public static int getSlotIDFromItem(Item item) {
        for (ItemStack stack : mc.player.getInventory().armor) {
            if (stack.getItem() == item) {
                return -2;
            }
        }
        int slot = -1;
        for (int i = 0; i < 36; i++) {
            ItemStack s = mc.player.getInventory().getItem(i);
            if (s.getItem() == item) {
                slot = i;
                break;
            }
        }
        if (slot < 9 && slot != -1) {
            slot = slot + 36;
        }
        return slot;
    }

    public static int getFireWorks() {
        for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
            if (mc.player.getInventory().getItem(i).getItem() instanceof FireworkRocketItem) {
                return i;
            }
        }
        return -1;
    }

    public int getSlotInHotbar(Item item) {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getItem(i).getItem() == item) {
                return i;
            }
        }
        return -1;
    }

    public int getSlotInInventory(Item item) {
        for (int i = 9; i < 45; i++) {
            if (mc.player.getInventory().getItem(i).getItem() == item) {
                return i;
            }
        }
        return -1;
    }
    public int getSlotInAllInventory(Item item) {
        for (int i = 0; i < 45; i++) {
            if (mc.player.getInventory().getItem(i).getItem() == item) {
                if (i <= 8) return i + 36;
                return i;
            }
        }
        return -1;
    }
    public int getSlotWithExactStack(ItemStack target) {
        for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (ItemStack.isSameItemSameComponents(stack, target)
                    && stack.getHoverName().equals(target.getHoverName())) {
                if (i <= 8) return i + 36;
                return i;
            }
        }
        return -1;
    }
    public static boolean ValidateItem(ItemStack item) {
        if (item.getItem() == Items.ENDER_EYE) return item.getHoverName().getString().contains("★");
        if (item.getItem() == Items.NETHERITE_SCRAP) return item.getHoverName().getString().contains("★");
        if (item.getItem() == Items.DRIED_KELP) return item.getHoverName().getString().contains("★");
        if (item.getItem() == Items.SUGAR) return item.getHoverName().getString().contains("★");
        if (item.getItem() == Items.PHANTOM_MEMBRANE) return item.getHoverName().getString().contains("★");
        if (item.getItem() == Items.FIRE_CHARGE) return item.getHoverName().getString().contains("★");
        if (item.getItem() == Items.SNOWBALL) return item.getHoverName().getString().contains("★");
        if (item.getItem() == Items.PLAYER_HEAD) return item.getHoverName().getString().contains("★");
        if (item.getItem() == Items.SPLASH_POTION) return item.getHoverName().getString().contains("★");
        if (item.getItem() == Items.WIND_CHARGE) return item.getHoverName().getString().contains("★");
        if (item.getItem() == Items.TRIPWIRE_HOOK) return item.getHoverName().getString().contains("★");
        if (item.getItem() == Items.TIPPED_ARROW) return item.getHoverName().getString().contains("★");
        if (item.getItem() == Items.TNT) return item.getHoverName().getString().contains("★");
        return true;
    }
}