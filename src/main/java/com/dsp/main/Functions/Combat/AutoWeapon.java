package com.dsp.main.Functions.Combat;

import com.dsp.main.Module;
import com.dsp.main.Utils.TimerUtil;
import com.mojang.datafixers.util.Pair;
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
import static net.minecraft.world.item.Tiers.*;

public class AutoWeapon extends Module {
    public AutoWeapon() {
        super("AutoWeapon", 0, Category.COMBAT, "Automatically switches to the best weapon when attacking");
    }

    @SubscribeEvent
    public void onAttack(AttackEntityEvent event) {
        if (!(event.getTarget() instanceof LivingEntity target) || mc.player == null) return;

        int bestSlot = findBestWeaponSlot(target);
        if (bestSlot >= 0) {
            if (bestSlot <= 8 ) {
                mc.player.getInventory().selected = bestSlot;
            } else {
                if (slowBypass.isEnabled()) isSlowBypass = true;
                mc.gameMode.handlePickItem(bestSlot);
                TimerUtil.sleepVoid(() -> {
                    int xui = findBestWeaponSlot(target);
                    if (xui <= 8 ) {
                        mc.player.getInventory().selected = xui;
                    }
                } , 30);
                {
                int xui = findBestWeaponSlot(target);
                if (xui <= 8 ) {
                    mc.player.getInventory().selected = xui;
                }
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

        // 1) Считаем базовый уровень по тиру
        if (item instanceof TieredItem tieredItem) {
            Tier tier = tieredItem.getTier();
            score += switch (tier) {
                case NETHERITE -> 5;
                case DIAMOND    -> 4;
                case IRON       -> 3;
                case STONE      -> 2;
                case WOOD       -> 1;
                default         -> 0;
            };
        }
        RegistryAccess ra = mc.level.registryAccess();
        Registry<Enchantment> enchantRegistry = ra.registryOrThrow(Registries.ENCHANTMENT);
        Holder<Enchantment> sharpnessHolder = enchantRegistry.getHolderOrThrow(Enchantments.SHARPNESS);
        score += EnchantmentHelper.getItemEnchantmentLevel(sharpnessHolder, stack) * 1.6f;

        Holder<Enchantment> smiteHolder = enchantRegistry.getHolderOrThrow(Enchantments.SMITE);
        score += EnchantmentHelper.getItemEnchantmentLevel(smiteHolder, stack) * 0.7f;

        Holder<Enchantment> baneHolder = enchantRegistry.getHolderOrThrow(Enchantments.BANE_OF_ARTHROPODS);
        score += EnchantmentHelper.getItemEnchantmentLevel(baneHolder, stack) * 0.7f;

        Holder<Enchantment> fireHolder = enchantRegistry.getHolderOrThrow(Enchantments.FIRE_ASPECT);
        score += EnchantmentHelper.getItemEnchantmentLevel(fireHolder, stack) * 0.2f;

        Holder<Enchantment> kbHolder = enchantRegistry.getHolderOrThrow(Enchantments.KNOCKBACK);
        score += EnchantmentHelper.getItemEnchantmentLevel(kbHolder, stack) * 0.5f;

        Holder<Enchantment> lootHolder = enchantRegistry.getHolderOrThrow(Enchantments.LOOTING);
        score += EnchantmentHelper.getItemEnchantmentLevel(lootHolder, stack) * 0.7f;

        Holder<Enchantment> impaleHolder = enchantRegistry.getHolderOrThrow(Enchantments.IMPALING);
        score += EnchantmentHelper.getItemEnchantmentLevel(impaleHolder, stack) * 1.2f;

        Holder<Enchantment> loyaltyHolder = enchantRegistry.getHolderOrThrow(Enchantments.LOYALTY);
        score += EnchantmentHelper.getItemEnchantmentLevel(loyaltyHolder, stack) * 0.5f;

        Holder<Enchantment> channelHolder = enchantRegistry.getHolderOrThrow(Enchantments.CHANNELING);
        score += EnchantmentHelper.getItemEnchantmentLevel(channelHolder, stack) * 0.3f;
        if (stack.isDamaged()) {
            float durabilityRatio = 1f - (stack.getDamageValue() / (float) stack.getMaxDamage());
            score *= durabilityRatio;
        }

        return score;
    }

}
