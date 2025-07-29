package com.dsp.main.Functions.Combat.Aura.impl;

import com.dsp.main.Functions.Combat.Aura.Aura;
import com.dsp.main.Utils.Minecraft.Client.ClientPlayerUtil;
import com.dsp.main.Utils.Minecraft.Client.InvUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import static com.dsp.main.Api.mc;

public class AttackHandler {
    private static int attackDelayTicks = 0;
    private static int shieldBreakTicks = 0;
    private static final InvUtil invUtil = new InvUtil();
    private static boolean isBreakingShield = false;
    private static ItemStack rememderedItem = null;

    private boolean hasClearLineOfSight(Player player, Entity target) {
        Vec3 playerEyePos = player.getEyePosition();
        Vec3 targetPos = target.position().add(0, target.getBbHeight() / 2, 0);
        ClipContext context = new ClipContext(
                playerEyePos,
                targetPos,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player
        );
        return mc.level.clip(context).getType() != HitResult.Type.BLOCK;
    }

    private boolean isRayIntersectingAABB(Player player, Entity target, double maxDistance) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getViewVector(1.0F).scale(maxDistance);
        Vec3 endPos = eyePos.add(lookVec);
        AABB entityBox = target.getBoundingBox();
        return entityBox.clip(eyePos, endPos).isPresent();
    }

    public static void update(Aura aura, boolean doFallCheck) {
        Entity target = Aura.Target;
        if (target == null || !target.isAlive() || !target.isAttackable()) return;
        if (mc.player.distanceTo(target) > aura.attackRange.getValue() + 0.1f) return;
        AttackHandler handler = new AttackHandler();
        if (!aura.throughWalls.isEnabled() && !handler.hasClearLineOfSight(mc.player, target)) return;
        if (!handler.isRayIntersectingAABB(mc.player, target, aura.attackRange.getValue() + 0.1f)) return;
        if (aura.checks.isOptionEnabled("Hit only weapon")) {
            var item = mc.player.getMainHandItem().getItem();
            if (!(item instanceof SwordItem || item instanceof AxeItem || item instanceof MaceItem)) return;
        }

        if (aura.checks.isOptionEnabled("Break Shield") && target instanceof Player targetPlayer) {
            if (targetPlayer.isBlocking() && targetPlayer.isUsingItem()) {
                int axeSlot = invUtil.getSlotInAllInventory(Items.NETHERITE_AXE);
                if (axeSlot == -1) axeSlot = invUtil.getSlotInAllInventory(Items.DIAMOND_AXE);
                if (axeSlot == -1) axeSlot = invUtil.getSlotInAllInventory(Items.IRON_AXE);
                if (axeSlot == -1) axeSlot = invUtil.getSlotInAllInventory(Items.WOODEN_AXE);
                if (axeSlot == -1) axeSlot = invUtil.getSlotInAllInventory(Items.GOLDEN_AXE);
                if (axeSlot == -1) axeSlot = invUtil.getSlotInAllInventory(Items.STONE_AXE);

                if (!isBreakingShield && axeSlot != -1) {
                    rememderedItem = mc.player.getMainHandItem();
                    invUtil.swapHand(axeSlot, InteractionHand.MAIN_HAND);
                    isBreakingShield = true;
                    shieldBreakTicks = 0;
                }

                if (isBreakingShield) {
                    if (shieldBreakTicks >= 2) {
                        if (mc.gameMode != null && mc.player != null) {
                            mc.gameMode.attack(mc.player, target);
                            mc.player.swing(InteractionHand.MAIN_HAND);
                        }
                        shieldBreakTicks = 0;
                    } else {
                        shieldBreakTicks++;
                    }
                    return;
                }
            } else if (isBreakingShield) {
                int rememberedSlot = invUtil.getSlotWithExactStack(rememderedItem);
                if (rememberedSlot != -1) {
                    invUtil.swapHand(rememberedSlot, InteractionHand.MAIN_HAND);
                }
                isBreakingShield = false;
                shieldBreakTicks = 0;
            }
        }

        boolean is18Mode = aura.attackType.getMode().equals("1.8");
        if (!is18Mode) {
            if (!aura.onlyCrits.isEnabled()) {
                float cooldown = mc.player.getAttackStrengthScale(0.5F);
                if (cooldown < 1.0F) return;
            } else if (!ClientPlayerUtil.isPlayerFalling() && doFallCheck) {
                return;
            }
        } else {
            if (attackDelayTicks < aura.OneAndEitCd.getValueInt()) {
                attackDelayTicks++;
                return;
            }
            attackDelayTicks = 0;
        }

        if (aura.checks.isOptionEnabled("Don't hit when you eat") && mc.player.isUsingItem()) {
            if (aura.checks.isOptionEnabled("Disabling shield") && mc.player.getUseItem().getItem() == Items.SHIELD) {
                mc.player.releaseUsingItem();
            } else return;
        }

        if (mc.gameMode != null && mc.player != null) {
            mc.gameMode.attack(mc.player, target);
            mc.player.swing(InteractionHand.MAIN_HAND);
        }
    }
}
