package com.dsp.main.Functions.Combat;

import com.dsp.main.Module;
import com.dsp.main.Utils.TimerUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;

import java.util.*;

import static com.dsp.main.Api.isSlowBypass;
import static com.dsp.main.Api.mc;
import static com.dsp.main.Functions.Misc.ClientSetting.slowBypass;

public class AutoWeapon extends Module {
    public AutoWeapon() {
        super("AutoWeapon", 0, Category.COMBAT, "Automatically switches to the best weapon when attacking");
    }

    @SubscribeEvent
    public void onAttack(AttackEntityEvent event) {
        if (!(event.getTarget() instanceof LivingEntity target) || mc.player == null) return;

        int bestSlot = findBestWeaponSlot(target);
        if (bestSlot >= 0) {
            if (bestSlot <= 8) {
                mc.player.getInventory().selected = bestSlot;
            } else {
                if (slowBypass.isEnabled()) isSlowBypass = true;

                mc.gameMode.handleInventoryButtonClick(mc.player.containerMenu.containerId, bestSlot);

                TimerUtil.sleepVoid(() -> {
                    int xui = findBestWeaponSlot(target);
                    if (xui <= 8) {
                        mc.player.getInventory().selected = xui;
                    }
                }, 30);

                int xui = findBestWeaponSlot(target);
                if (xui <= 8) {
                    mc.player.getInventory().selected = xui;
                }
            }
        }
    }

    public static int findBestWeaponSlot(LivingEntity target) {
        Map<Integer, Float> weaponScores = new HashMap<>();
        Player player = mc.player;

        if (player == null) return -1;

        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.isEmpty()) continue;

            float score = calculateWeaponScore(stack, target);
            if (score > 0) {
                weaponScores.put(slot, score);
            }
        }

        return weaponScores.isEmpty() ? -1 :
                Collections.max(weaponScores.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    private static float calculateWeaponScore(ItemStack stack, LivingEntity target) {
        Item item = stack.getItem();
        if (!(item instanceof SwordItem) &&
                !(item instanceof AxeItem) &&
                !(item instanceof TridentItem)) {
            return 0;
        }

        float score = 0f;

        if (mc.level == null) return score;

        RegistryAccess ra = mc.level.registryAccess();
        Registry<Enchantment> enchantRegistry = ra.lookup(Registries.ENCHANTMENT).orElse(null);

        if (enchantRegistry == null) return score;

        score += getEnchantLevel(enchantRegistry, stack, Enchantments.SHARPNESS) * 1.6f;
        score += getEnchantLevel(enchantRegistry, stack, Enchantments.SMITE) * 0.7f;
        score += getEnchantLevel(enchantRegistry, stack, Enchantments.BANE_OF_ARTHROPODS) * 0.7f;
        score += getEnchantLevel(enchantRegistry, stack, Enchantments.FIRE_ASPECT) * 0.2f;
        score += getEnchantLevel(enchantRegistry, stack, Enchantments.KNOCKBACK) * 0.5f;
        score += getEnchantLevel(enchantRegistry, stack, Enchantments.LOOTING) * 0.7f;
        score += getEnchantLevel(enchantRegistry, stack, Enchantments.IMPALING) * 1.2f;
        score += getEnchantLevel(enchantRegistry, stack, Enchantments.LOYALTY) * 0.5f;
        score += getEnchantLevel(enchantRegistry, stack, Enchantments.CHANNELING) * 0.3f;

        if (stack.isDamaged()) {
            float durabilityRatio = 1f - (stack.getDamageValue() / (float) stack.getMaxDamage());
            score *= durabilityRatio;
        }

        return score;
    }

    private static float getTierScore(String tierName) {
        return switch (tierName.toLowerCase()) {
            case "netherite" -> 5f;
            case "diamond" -> 4f;
            case "iron" -> 3f;
            case "stone" -> 2f;
            case "wood", "wooden" -> 1f;
            default -> 0f;
        };
    }

    private static int getEnchantLevel(Registry<Enchantment> registry, ItemStack stack, net.minecraft.resources.ResourceKey<Enchantment> enchantmentKey) {
        try {
            Holder.Reference<Enchantment> holder = registry.get(enchantmentKey).orElse(null);
            if (holder == null) return 0;
            return EnchantmentHelper.getItemEnchantmentLevel(holder, stack);
        } catch (Exception e) {
            return 0;
        }
    }
}