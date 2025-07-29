package com.dsp.main.Utils.Minecraft.Client;

import com.dsp.main.Core.Event.OnUpdate;
import com.dsp.main.Utils.TimerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.InteractionHand;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;

import java.util.List;

import static com.dsp.main.Api.isSlowBypass;
import static com.dsp.main.Functions.Player.ClickActions.shiftBp;

public class AutoEatUtil {
    private static final Minecraft mc = Minecraft.getInstance();

    public static boolean eating = false;
    private static int eatSlot = -1;
    private static int origSlot = -1;
    private static long lastUsedTime = 0L;
    private static int foundSlot = -1;
    private static final long COOLDOWN_MS = 1000L;

    public static void eatItemFromInventory(Item itemToUse) {
        long now = System.currentTimeMillis();
        if (eating || now - lastUsedTime < COOLDOWN_MS || mc.player == null || mc.level == null) {
            return;
        }
        if (mc.player.getCooldowns().isOnCooldown(itemToUse)) {
            return;
        }
        eating = false;
        eatSlot = -1;

        Player player = mc.player;
        origSlot = player.getInventory().selected;

        foundSlot = -1;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty()) {
                if (itemToUse == Items.POTION) {
                    List<MobEffectInstance> effects = (List<MobEffectInstance>) stack.get(DataComponents.POTION_CONTENTS).getAllEffects();
                    boolean hasHeal = effects.stream()
                            .anyMatch(e -> e.getEffect() == MobEffects.HEAL);
                    if (hasHeal) {
                        foundSlot = i;
                        break;
                    }
                } else {
                    if (stack.getItem() == itemToUse) {
                        foundSlot = i;
                        break;
                    }
                }
            }
        }
        if (foundSlot < 0) {
            return;
        }

        lastUsedTime = now;
        if (foundSlot < 9) {
            eatSlot = foundSlot;
            player.getInventory().selected = eatSlot;
        } else {
            eatSlot = foundSlot;
            mc.gameMode.handlePickItem(foundSlot);
        }
        player.swing(InteractionHand.MAIN_HAND);
        mc.options.keyUse.setDown(true);
        if (shiftBp.isEnabled()) isSlowBypass = true;
        TimerUtil.sleepVoid(() -> eating = true, 180);
    }

    @SubscribeEvent
    public void onClientTick(OnUpdate event) {
        if (!eating || mc.player == null) {
            return;
        }
        Player player = mc.player;
        if (player.isUsingItem()) {
            if (shiftBp.isEnabled()) isSlowBypass = true;
            mc.options.keyUse.setDown(true);
        }
    }

    private static void finishEating() {
        mc.options.keyUse.setDown(false);
        if (shiftBp.isEnabled()) isSlowBypass = true;
        if (foundSlot > 8) {
            mc.gameMode.handlePickItem(foundSlot);
        }

        if (mc.player != null) {
            mc.player.stopUsingItem();
            TimerUtil.sleepVoid(() -> mc.player.getInventory().selected = origSlot, 200);
            TimerUtil.sleepVoid(() -> mc.player.getInventory().selected = origSlot, 300);
        }
        eating = false;
        eatSlot = -1;
        foundSlot = -1;
    }

    @SubscribeEvent
    public void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof Player) || !event.getEntity().level().isClientSide()) {
            return;
        }
        if (eating) {
            finishEating();
        }
    }
}