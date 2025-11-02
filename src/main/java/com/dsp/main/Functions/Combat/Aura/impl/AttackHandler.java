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

import javax.annotation.Nullable;

import static com.dsp.main.Api.mc;
import static com.dsp.main.Utils.Minecraft.Client.ClientPlayerUtil.isPlayerFalling;

public class AttackHandler {
    private static int attackDelayTicks = 0;
    private static int shieldBreakTicks = 0;
    private static final InvUtil invUtil = new InvUtil();
    private static boolean isBreakingShield = false;
    private static ItemStack rememderedItem = null;

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

    public static void update(Aura aura, boolean doFallCheck) {
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
            if (!(item instanceof SwordItem || item instanceof AxeItem || item instanceof MaceItem || item instanceof TridentItem)) return;
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
                    rememderedItem = mc.player.getMainHandItem().copy();
                    invUtil.swapHand(axeSlot, InteractionHand.MAIN_HAND);
                    isBreakingShield = true;
                    shieldBreakTicks = 0;
                }

                if (isBreakingShield) {
                    if (shieldBreakTicks >= 2) {
                        mc.gameMode.attack(mc.player, target);
                        mc.player.swing(InteractionHand.MAIN_HAND);
                        shieldBreakTicks = 0;
                    } else {
                        shieldBreakTicks++;
                    }
                    return;
                }
            } else if (isBreakingShield) {
                if (rememderedItem != null) {
                    int rememberedSlot = invUtil.getSlotWithExactStack(rememderedItem);
                    if (rememberedSlot != -1) {
                        invUtil.swapHand(rememberedSlot, InteractionHand.MAIN_HAND);
                    }
                }
                isBreakingShield = false;
                shieldBreakTicks = 0;
                rememderedItem = null;
            }
        }

        boolean is18Mode = aura.attackType.getMode().equals("1.8");

        if (!is18Mode) {
            float cooldown = mc.player.getAttackStrengthScale(0.5F);
            if (cooldown < 1.0F) return;

            if (aura.checks.isOptionEnabled("Smart Crits")) {
                boolean spacePressed = mc.options.keyJump.isDown();
                boolean canCrit = isPlayerFalling();

                if (spacePressed && !canCrit) {
                    return;
                }
            } else if (aura.onlyCrits.isEnabled()) {
                if (!isPlayerFalling() && doFallCheck) {
                    return;
                }
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

        mc.gameMode.attack(mc.player, target);
        mc.player.swing(InteractionHand.MAIN_HAND);
    }
}