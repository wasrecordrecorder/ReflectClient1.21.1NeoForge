package com.dsp.main.Functions.Combat.Aura.impl;

import com.dsp.main.Api;
import com.dsp.main.Functions.Combat.Aura.Aura;
import com.dsp.main.Utils.Minecraft.Client.ClientFallDistance;
import com.dsp.main.Utils.Minecraft.Client.InvUtil;
import com.dsp.main.Utils.Minecraft.Player.FallingPlayer;
import com.dsp.main.Utils.Minecraft.Player.IdealHitUtility;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

import static com.dsp.main.Api.mc;
import static com.dsp.main.Utils.Minecraft.Client.ClientPlayerUtil.isPlayerFalling;
import static com.dsp.main.Utils.Minecraft.Server.WhatServer.*;

public class AttackHandler {
    private static int attackDelayTicks = 0;
    private static int shieldBreakTicks = 0;
    private static final InvUtil invUtil = new InvUtil();
    private static boolean isBreakingShield = false;
    private static ItemStack rememberedItem = null;
    private static int skipTicks = 0;

    private static boolean hasClearLineOfSight(Player player, Entity target) {
        if (player == null || target == null || mc.level == null) return false;
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

    @Nullable
    private static Vec3 getHitboxIntersection(Player player, Entity target, double maxDistance) {
        if (player == null || target == null) return null;

        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getViewVector(1.0F);
        Vec3 endPos = eyePos.add(lookVec.scale(maxDistance));

        AABB entityBox = target.getBoundingBox();
        return entityBox.clip(eyePos, endPos).orElse(null);
    }

    private static double getDistanceToHitbox(Player player, Entity target) {
        if (player == null || target == null) return Double.MAX_VALUE;

        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getViewVector(1.0F);
        AABB entityBox = target.getBoundingBox();

        Vec3 hitPos = entityBox.clip(eyePos, eyePos.add(lookVec.scale(10.0))).orElse(null);

        if (hitPos != null) {
            return eyePos.distanceTo(hitPos);
        }

        Vec3 closest = getClosestPointOnAABB(entityBox, eyePos);
        return eyePos.distanceTo(closest);
    }

    private static Vec3 getClosestPointOnAABB(AABB box, Vec3 point) {
        double x = Math.max(box.minX, Math.min(point.x, box.maxX));
        double y = Math.max(box.minY, Math.min(point.y, box.maxY));
        double z = Math.max(box.minZ, Math.min(point.z, box.maxZ));
        return new Vec3(x, y, z);
    }

    public static void update(Aura aura, boolean doFallCheck, boolean isElytraMode) {
        Entity target = Aura.Target;
        if (target == null || mc.player == null || mc.gameMode == null) return;
        if (!target.isAlive() || !target.isAttackable()) return;

        double distanceToHitbox = getDistanceToHitbox(mc.player, target);
        double attackRange = aura.attackRange.getValue();

        if (distanceToHitbox > attackRange) return;

        if (!aura.throughWalls.isEnabled() && !hasClearLineOfSight(mc.player, target)) return;

        Vec3 hitboxIntersection = getHitboxIntersection(mc.player, target, attackRange + 0.5);
        if (hitboxIntersection == null) return;

        if (aura.checks.isOptionEnabled("Hit only weapon")) {
            var item = mc.player.getMainHandItem().getItem();
            if (!(item instanceof SwordItem || item instanceof AxeItem || item instanceof MaceItem || item instanceof TridentItem))
                return;
        }

        handleShieldBreaking(aura, target);

        boolean is18Mode = aura.attackType.getMode().equals("1.8");

        if (!is18Mode && !isElytraMode) {
            if (!handleAdvancedCrits(aura, target, doFallCheck)) return;
        } else if (is18Mode) {
            if (attackDelayTicks < aura.OneAndEitCd.getValueInt()) {
                attackDelayTicks++;
                return;
            }
            attackDelayTicks = 0;
        } else if (isElytraMode) {
            float cooldown = mc.player.getAttackStrengthScale(0.5F);
            if (cooldown < 1.0F) return;
        }

        if (aura.checks.isOptionEnabled("Don't hit when you eat") && mc.player.isUsingItem()) {
            if (aura.checks.isOptionEnabled("Disabling shield") && mc.player.getUseItem().getItem() == Items.SHIELD) {
                mc.player.releaseUsingItem();
            } else return;
        }

        performAttack(target);
    }

    private static void handleShieldBreaking(Aura aura, Entity target) {
        if (!aura.checks.isOptionEnabled("Break Shield")) return;
        if (!(target instanceof Player targetPlayer)) return;

        if (targetPlayer.isBlocking() && targetPlayer.isUsingItem()) {
            int axeSlot = invUtil.getSlotInAllInventory(Items.NETHERITE_AXE);
            if (axeSlot == -1) axeSlot = invUtil.getSlotInAllInventory(Items.DIAMOND_AXE);
            if (axeSlot == -1) axeSlot = invUtil.getSlotInAllInventory(Items.IRON_AXE);
            if (axeSlot == -1) axeSlot = invUtil.getSlotInAllInventory(Items.WOODEN_AXE);
            if (axeSlot == -1) axeSlot = invUtil.getSlotInAllInventory(Items.GOLDEN_AXE);
            if (axeSlot == -1) axeSlot = invUtil.getSlotInAllInventory(Items.STONE_AXE);

            if (!isBreakingShield && axeSlot != -1) {
                rememberedItem = mc.player.getMainHandItem().copy();
                invUtil.swapHand(axeSlot, InteractionHand.MAIN_HAND);
                isBreakingShield = true;
                shieldBreakTicks = 0;
            }

            if (isBreakingShield && shieldBreakTicks >= 2) {
                mc.gameMode.attack(mc.player, target);
                mc.player.swing(InteractionHand.MAIN_HAND);
                shieldBreakTicks = 0;
            } else if (isBreakingShield) {
                shieldBreakTicks++;
            }
        } else if (isBreakingShield) {
            if (rememberedItem != null) {
                int rememberedSlot = invUtil.getSlotWithExactStack(rememberedItem);
                if (rememberedSlot != -1) {
                    invUtil.swapHand(rememberedSlot, InteractionHand.MAIN_HAND);
                }
            }
            isBreakingShield = false;
            shieldBreakTicks = 0;
            rememberedItem = null;
        }
    }

    private static boolean handleAdvancedCrits(Aura aura, Entity target, boolean doFallCheck) {
        float cooldown = mc.player.getAttackStrengthScale(0.5F);

        if (cooldown < IdealHitUtility.getAICooldown()) return false;

        if (aura.checks.isOptionEnabled("Smart Crits")) {
            boolean spacePressed = mc.options.keyJump.isDown();
            boolean canCrit = isPlayerFalling();
            boolean predictCrit = !mc.player.onGround() &&
                    IdealHitUtility.canAIFall() &&
                    FallingPlayer.fromPlayer(mc.player).findFall(IdealHitUtility.getNewFallDistance((net.minecraft.world.entity.LivingEntity) target));

            if (spacePressed && !canCrit && !predictCrit) {
                return false;
            }

            if (predictCrit && skipTicks <= 0) {
                skipTicks = 1;
                return false;
            }
        } else if (aura.onlyCrits.isEnabled()) {
            if (!canCritical() && doFallCheck) {
                return false;
            }
        }

        if (skipTicks > 0) {
            skipTicks--;
        }

        return true;
    }

    private static boolean canCritical() {
        if (mc.player == null) return false;

        double yDiff = (double)((int) mc.player.getY()) - mc.player.getY();
        boolean bl4 = yDiff == -0.01250004768371582;
        boolean bl5 = yDiff == -0.1875;

        return (!mc.player.onGround() && ClientFallDistance.get() > IdealHitUtility.getAIFallDistance() && IdealHitUtility.canAIFall())
                || (bl5 || bl4) && !mc.player.isShiftKeyDown()
                || mc.player.hasEffect(MobEffects.BLINDNESS)
                || mc.player.hasEffect(MobEffects.LEVITATION)
                || mc.player.hasEffect(MobEffects.SLOW_FALLING)
                || mc.player.isInLava()
                || mc.player.isInWater()
                || mc.player.onClimbable()
                || mc.player.isPassenger()
                || mc.player.getAbilities().flying
                || !((Aura) Api.getModule("Aura")).onlyCrits.isEnabled();
    }

    private static void performAttack(Entity target) {
        IdealHitUtility.incrementAttacks();

        mc.gameMode.attack(mc.player, target);
        mc.player.swing(InteractionHand.MAIN_HAND);

        if (ClientFallDistance.get() > 0.1f) {
            IdealHitUtility.setFdCount(0);
        } else {
            IdealHitUtility.setFdCount(IdealHitUtility.getFdCount() + 1);
        }
    }
}