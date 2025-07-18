package com.dsp.main.Functions.Combat.Aura.impl;

import com.dsp.main.Functions.Combat.Aura.Aura;
import com.dsp.main.Utils.Minecraft.Client.ClientPlayerUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import static com.dsp.main.Api.mc;

public class AttackHandler {
    private static int attackDelayTicks = 0;

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

    private boolean isRayIntersectingAABB(Player player, Entity entity, double maxDistance) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getViewVector(1.0F).scale(maxDistance);
        Vec3 endPos = eyePos.add(lookVec);
        AABB entityBox = entity.getBoundingBox();
        return entityBox.clip(eyePos, endPos).isPresent();
    }

    public static void update(Aura aura) {
        Entity target = Aura.Target;
        if (target == null || !target.isAlive() || !target.isAttackable()) {
            return;
        }
        if (mc.player.distanceTo(target) > aura.attackRange.getValue() + 0.1f) {
            return;
        }
        if (aura.checks.isOptionEnabled("Hit only weapon")) {
            var item = mc.player.getMainHandItem().getItem();
            if (!(item instanceof SwordItem || item instanceof AxeItem || item instanceof MaceItem)) {
                return;
            }
        }
        if (aura.checks.isOptionEnabled("Don't hit when you eat") && mc.player.isUsingItem()) {
            return;
        }
        boolean is18Mode = aura.attackType.getMode().equals("1.8");
        if (!is18Mode) {
            if (!aura.onlyCrits.isEnabled()) {
                float cooldown = mc.player.getAttackStrengthScale(0.5F);
                if (cooldown < 1.0F) {
                    return;
                }
            } else if (!ClientPlayerUtil.isPlayerFalling()) {
                return;
            }
        } else {
            if (attackDelayTicks < aura.OneAndEitCd.getValueInt()) {
                attackDelayTicks++;
                return;
            }
            attackDelayTicks = 0;
        }

        AttackHandler handler = new AttackHandler();
        if (!aura.throughWalls.isEnabled()) {
            if (!handler.hasClearLineOfSight(mc.player, target)) {
                return;
            }
        }

        if (!handler.isRayIntersectingAABB(mc.player, target, aura.attackRange.getValue() + 0.1f)) {
            return;
        }

        if (mc.gameMode != null && mc.player != null) {
            mc.gameMode.attack(mc.player, target);
            mc.player.swing(InteractionHand.MAIN_HAND);
        }
    }
}