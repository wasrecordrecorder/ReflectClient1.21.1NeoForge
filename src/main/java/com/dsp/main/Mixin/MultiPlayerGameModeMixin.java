package com.dsp.main.Mixin;

import com.dsp.main.Api;
import com.dsp.main.Functions.Combat.Aura.Aura;
import com.dsp.main.Functions.Combat.BreachSwap;
import com.dsp.main.Functions.Player.NoDelay;
import com.dsp.main.Main;
import com.dsp.main.Utils.Minecraft.Client.InvUtil;
import com.dsp.main.Utils.TimerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.dsp.main.Api.isResetingSprint;
import static com.dsp.main.Api.mc;
import static com.dsp.main.Functions.Combat.Aura.Aura.resetSprintMode;
import static com.dsp.main.Functions.Combat.BreachSwap.hasEverything;
import static com.dsp.main.Functions.Combat.BreachSwap.invUtilForBreach;
import static com.dsp.main.Main.isDetect;
import static com.dsp.main.Utils.Minecraft.Client.ClientPlayerUtil.hasEnoughImpulseToStartSprinting;
import static com.dsp.main.Utils.Minecraft.Client.ClientPlayerUtil.isMoving;
import static com.dsp.main.Utils.Minecraft.Server.WhatServer.*;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {
    @Shadow
    private int destroyDelay;



    @Inject(
            method = "attack",
            at = @At("HEAD")
    )
    private void resetSprintBeforeAttack(Player player, Entity targetEntity, CallbackInfo ci) {
        if (mc.player.isSprinting() && isMoving() && (isAm() || isSp() || isHw() || isFs() || resetSprintMode.isMode("Packet + Slow"))) {
            mc.player.setSprinting(false);
            mc.getConnection().send(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.STOP_SPRINTING));
            mc.player.setDeltaMovement(mc.player.getDeltaMovement().multiply(0.6, 1, 0.6));
        }
        if (mc.player.isSprinting() && isMoving() && isFt()) {
            mc.player.setSprinting(false);
            mc.getConnection().send(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.STOP_SPRINTING));
        }
        if (!Aura.attackType.isMode("1.8") && resetSprintMode.isMode("Legit")) {
            isResetingSprint = true;
        }
        if (resetSprintMode.isMode("Packet")) {
            mc.player.setSprinting(false);
            mc.getConnection().send(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.STOP_SPRINTING));
        }
        if (resetSprintMode.isMode("Slow")) {
            mc.player.setDeltaMovement(mc.player.getDeltaMovement().multiply(0.5, 1, 0.5));
            isResetingSprint = true;
        }
        if (resetSprintMode.isMode("Legit + Packet")) {
            isResetingSprint = true;
            mc.getConnection().send(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.STOP_SPRINTING));
        }
        if (Api.isEnabled("BreachSwap")) {
            if (BreachSwap.hasEverything() && !mc.player.onGround()) {
                int swordId = findSwordSlot(mc.player);
                int maceId = findMaceSlot(mc.player);
                if (swordId <= 8) {
                    swordId = swordId + 36;
                }
                if (maceId <= 8) {
                    maceId = maceId + 36;
                }
                if (mc.player.getInventory().selected == swordId) {
                    invUtilForBreach.swapHand(maceId, InteractionHand.MAIN_HAND);
                }
            }
        }
    }
    @Inject(
            method = "attack",
            at = @At("TAIL")
    )
    private void resetSprintAfterAttack(Player player, Entity targetEntity, CallbackInfo ci) {
        if (!mc.player.isSprinting() && (isFt() || isRw() || isAm() || isSp() || isHw() || isFs() || resetSprintMode.isMode("Packet") || resetSprintMode.isMode("Packet + Slow") || resetSprintMode.isMode("Legit + Packet"))) {
            mc.player.setSprinting(true);
            if (hasEnoughImpulseToStartSprinting() && mc.player.getFoodData().getFoodLevel() >= 3.5F && !mc.player.isUsingItem()) {
                TimerUtil.sleepVoid(() -> mc.getConnection().send(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.START_SPRINTING)), 80);
            }
        }
        if (Api.isEnabled("BreachSwap")) {
            System.out.println(hasEverything());
            if (BreachSwap.hasEverything()) {
                int swordId = findSwordSlot(mc.player);
                int maceId = findMaceSlot(mc.player);
                if (swordId <= 8) {
                    swordId = swordId + 36;
                }
                if (maceId <= 8) {
                    maceId = maceId + 36;
                }
                if (mc.player.getInventory().selected == maceId) {
                    int finalSwordId = swordId;
                    TimerUtil.sleepVoid(() ->invUtilForBreach.swapHand(finalSwordId, InteractionHand.MAIN_HAND), 150);
                }
            }
        }
    }
    @Inject(method = "continueDestroyBlock", at = @At("HEAD"), cancellable = true)
    private void onContinueDestroyBlock(BlockPos posBlock, Direction directionFacing, CallbackInfoReturnable<Boolean> cir) {
        if (!isDetect && Api.isEnabled("NoDelay") && NoDelay.Options.isOptionEnabled("Break Block")) {
            destroyDelay = 0;
        }
    }
    private int findSwordSlot(Player p) {
        for (int i = 0; i < 9; i++) {
            ItemStack s = p.getInventory().getItem(i);
            if (!s.isEmpty() && s.getItem() instanceof SwordItem) return i;
        }
        return -1;
    }
    private int findMaceSlot(Player p) {
        for (int i = 0; i < 9; i++) {
            ItemStack s = p.getInventory().getItem(i);
            if (!s.isEmpty() && s.getItem() instanceof MaceItem) return i;
        }
        return -1;
    }
}