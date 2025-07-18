package com.dsp.main.Functions.Player;

import com.dsp.main.Managers.Event.OnUpdate;
import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Settings.BindCheckBox;
import com.dsp.main.UI.ClickGui.Settings.CheckBox;
import com.dsp.main.UI.ClickGui.Settings.Slider;
import com.dsp.main.Utils.Minecraft.Chat.ChatUtil;
import com.dsp.main.Utils.Minecraft.Client.InvUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.dsp.main.Api.isSlowBypass;
import static com.dsp.main.Api.mc;
import static com.dsp.main.Functions.Misc.ClientSetting.slowBypass;

public class ElytraHelper extends Module {
    public static BindCheckBox swap = new BindCheckBox("Swap", 0, () -> performSwap());
    public static BindCheckBox useFirework = new BindCheckBox("Use Firework", 0, () -> performFireworkUse());
    private static CheckBox autoStart = new CheckBox("Auto Start", false);
    private static CheckBox autoUseFirework = new CheckBox("Auto Use Firework", false);
    private static Slider autoUseFireworkCdTicks = new Slider("Cd", 1, 10, 3, 1).setVisible(() -> autoUseFirework.isEnabled());
    private static final InvUtil invUtil = new InvUtil();
    private static int currentTick = 0;

    public ElytraHelper() {
        super("ElytraHelper", 0, Category.PLAYER, "Manipulations with elytra and chestplate");
        addSettings(swap, useFirework, autoStart, autoUseFirework, autoUseFireworkCdTicks);
    }

    private static void performSwap() {
        if (mc.player == null) {
            return;
        }

        Item currentChestItem = mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem();

        if (slowBypass.isEnabled()) isSlowBypass = true;

        if (currentChestItem == Items.ELYTRA) {
            int chestplateSlot = getBestChestplate(mc);
            if (chestplateSlot == -1) {
                return;
            }
            invUtil.moveItem(chestplateSlot, EquipmentSlot.CHEST.getIndex(4));
        } else if (currentChestItem instanceof ArmorItem && ((ArmorItem) currentChestItem).getEquipmentSlot() == EquipmentSlot.CHEST) {
            int elytraSlot = invUtil.getSlotInInventory(Items.ELYTRA);
            if (elytraSlot == -1) {
                return;
            }
            invUtil.moveItem(elytraSlot, EquipmentSlot.CHEST.getIndex(4));
        } else {
            int elytraSlot = invUtil.getSlotInInventory(Items.ELYTRA);
            if (elytraSlot != -1) {
                invUtil.moveItem(elytraSlot, EquipmentSlot.CHEST.getIndex(4));
            } else {
                int chestplateSlot = getBestChestplate(mc);
                if (chestplateSlot != -1) {
                    invUtil.moveItem(chestplateSlot, EquipmentSlot.CHEST.getIndex(4));
                }
            }
        }
    }

    private static void performFireworkUse() {
        if (mc.player == null) return;
        Item currentChestItem = mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem();
        if (currentChestItem != Items.ELYTRA) {
            return;
        }
        invUtil.findItemAndThrow(Items.FIREWORK_ROCKET, mc.player.getYRot(), mc.player.getXRot());
    }

    @SubscribeEvent
    public void OnUpdate(OnUpdate event) {
        if (mc.player == null) return;
        Item currentChestItem = mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem();
        if (currentChestItem == Items.ELYTRA && !mc.player.isFallFlying() && !mc.player.onGround() && autoStart.isEnabled()) {
            mc.player.connection.send(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
            mc.player.startFallFlying();
        }
        if (currentChestItem == Items.ELYTRA && mc.player.isFallFlying() && autoUseFirework.isEnabled()) {
            if (currentTick > 0) {
                currentTick--;
            } else {
                currentTick = autoUseFireworkCdTicks.getValueInt() * 20;
                performFireworkUse();
            }
        }
    }

    public static int getBestChestplate(Minecraft client) {
        int bestSlot = -1;
        int highestScore = -1;

        for (int i = 0; i < client.player.getInventory().getContainerSize(); ++i) {
            ItemStack stack = client.player.getInventory().getItem(i);
            if (isChestplate(stack, client) && !stack.is(Items.ELYTRA)) {
                int score = calculateScore(stack);
                if (score > highestScore) {
                    highestScore = score;
                    bestSlot = i;
                }
            }
        }

        return bestSlot;
    }

    public static boolean isChestplate(ItemStack stack, Minecraft client) {
        return client.player.getEquipmentSlotForItem(stack) == EquipmentSlot.CHEST;
    }

    public static int calculateScore(ItemStack stack) {
        AtomicInteger score = new AtomicInteger();
        if (stack.getItem() == Items.NETHERITE_CHESTPLATE) {
            score.addAndGet(6);
        } else if (stack.getItem() == Items.DIAMOND_CHESTPLATE) {
            score.addAndGet(5);
        } else if (stack.getItem() == Items.IRON_CHESTPLATE) {
            score.addAndGet(4);
        } else if (stack.getItem() == Items.CHAINMAIL_CHESTPLATE) {
            score.addAndGet(3);
        } else if (stack.getItem() == Items.GOLDEN_CHESTPLATE) {
            score.addAndGet(2);
        } else if (stack.getItem() == Items.LEATHER_CHESTPLATE) {
            score.addAndGet(1);
        }

        if (stack.isEnchanted()) {
            score.addAndGet(1);
        }

        Map<ResourceKey<Enchantment>, Integer> enchantmentPoints = new HashMap();
        enchantmentPoints.put(Enchantments.PROTECTION, 7);
        enchantmentPoints.put(Enchantments.UNBREAKING, 6);
        enchantmentPoints.put(Enchantments.MENDING, 5);
        enchantmentPoints.put(Enchantments.PROJECTILE_PROTECTION, 2);
        enchantmentPoints.put(Enchantments.BLAST_PROTECTION, 1);
        enchantmentPoints.put(Enchantments.FIRE_PROTECTION, 3);
        enchantmentPoints.put(Enchantments.THORNS, 4);
        stack.getEnchantments().entrySet().forEach((e) -> {
            Enchantment enchantment = (Enchantment) ((Holder) e.getKey()).value();
            int level = e.getValue();
            Integer points = (Integer) enchantmentPoints.get(enchantment);
            if (points != null) {
                score.addAndGet(points * level);
            }

        });
        return score.get();
    }
}