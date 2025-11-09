package com.dsp.main.Utils.Minecraft.Player;

import com.dsp.main.Api;
import com.dsp.main.Functions.Combat.Aura.Aura;
import com.dsp.main.Utils.Minecraft.Client.ClientFallDistance;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import static com.dsp.main.Api.mc;
import static com.dsp.main.Utils.Minecraft.Server.WhatServer.*;

public class IdealHitUtility {

    private static boolean jumped = false;
    private static int fdCount = 0;
    private static int attacks = 0;

    public static void setJumped(boolean value) {
        jumped = value;
    }

    public static boolean isJumped() {
        return jumped;
    }

    public static void setFdCount(int value) {
        fdCount = value;
    }

    public static int getFdCount() {
        return fdCount;
    }

    public static void incrementAttacks() {
        attacks++;
    }

    public static int getAttacks() {
        return attacks;
    }

    public static void resetAttacks() {
        attacks = 0;
    }

    public static float getAICooldown() {
        if (mc.player == null) return 0.944f;

        if (mc.player.getMainHandItem().getItem() == Items.AIR) {
            return 1;
        }

        if (mc.player.hasEffect(MobEffects.BLINDNESS)
                || mc.player.hasEffect(MobEffects.LEVITATION)
                || mc.player.hasEffect(MobEffects.SLOW_FALLING)
                || mc.player.isInLava()
                || mc.player.isInWater()
                || mc.player.onClimbable()
                || mc.player.isPassenger()
                || isInWeb()
                || mc.player.isFallFlying()
                || mc.player.getAbilities().flying
                || !((Aura) Api.getModule("Aura")).onlyCrits.isEnabled())
            return 0.944f;

        if (mc.player.getMainHandItem().getItem() instanceof AxeItem ||
                mc.player.getMainHandItem().getItem() instanceof ShovelItem)
            return 0.99f;

        if (Api.isEnabled("ElytraTarget"))
            return 1;

        return 0.944f;
    }

    public static float getAIFallDistance() {
        if (mc.player == null) return 0;

        if (isSp()) {
            return attacks % Math.round(randomRange(4, 10)) == 0 ? randomRange(0.15f, 0.23f) : 0;
        }

        return 0;
    }

    public static float getNewFallDistance(LivingEntity target) {
        if (mc.player == null) return 0;

        Aura aura = (Aura) Api.getModule("Aura");
        if (aura == null) return 0;

        if (isSp()) {
            if (collideWith(target) && !(getBlock(0, 2, 0) != Blocks.AIR &&
                    getBlock(0, -1, 0) != Blocks.AIR && mc.options.keyJump.isDown())) {
                return fdCount >= 10 ? 0.3f : 0.15f;
            }

            return fdCount >= 10 ? 0.15f : 0;
        }

        if (Api.isEnabled("Criticals") && isRw()) {
            return 1;
        }

        return 0;
    }

    public static boolean canAIFall() {
        if (mc.player == null) return false;

        if (getBlock(0, 2, 0) != Blocks.AIR && getBlock(0, -1, 0) != Blocks.AIR &&
                isSp() && fdCount > 8) {
            return false;
        }

        return ((getBlock(0, 3, 0) == Blocks.AIR && getBlock(0, 2, 0) == Blocks.AIR &&
                getBlock(0, 1, 0) == Blocks.AIR)
                || ClientFallDistance.get() < (getBlock(0, 2, 0) != Blocks.AIR ? 0.08f : 0.6f)
                || ClientFallDistance.get() > 1.2f);
    }

    private static boolean isBlockBelow() {
        if (mc.player == null) return false;

        Vec3 pos = mc.player.position().add(0, -1, 0);
        AABB hitbox = mc.player.getBoundingBox();

        float off = 0.15f;

        return !isAir(hitbox.minX-off, pos.y, hitbox.minZ-off)
                || !isAir(hitbox.maxX+off, pos.y, hitbox.minZ-off)
                || !isAir(hitbox.minX-off, pos.y, hitbox.maxZ+off)
                || !isAir(hitbox.maxX+off, pos.y, hitbox.maxZ+off);
    }

    private static boolean isAir(double x, double y, double z) {
        if (mc.level == null) return true;
        return mc.level.getBlockState(new BlockPos((int)x, (int)y, (int)z)).getBlock() == Blocks.AIR;
    }

    private static boolean isInWeb() {
        if (mc.player == null || mc.level == null) return false;
        return mc.level.getBlockState(mc.player.blockPosition()).getBlock() == Blocks.COBWEB;
    }

    private static boolean collideWith(LivingEntity target) {
        if (mc.player == null || target == null) return false;
        return mc.player.getBoundingBox().intersects(target.getBoundingBox());
    }

    private static net.minecraft.world.level.block.Block getBlock(int x, int y, int z) {
        if (mc.player == null || mc.level == null) return Blocks.AIR;
        BlockPos pos = mc.player.blockPosition().offset(x, y, z);
        return mc.level.getBlockState(pos).getBlock();
    }

    private static float randomRange(float min, float max) {
        return min + (float)(Math.random() * (max - min));
    }
}