package com.dsp.main.Mixin;

import com.dsp.main.Api;
import com.dsp.main.Functions.Player.NoDelay;
import com.dsp.main.Main;
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

import static com.dsp.main.Api.mc;
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
        if (mc.player.isSprinting()) System.out.println("Sprint Check 1");
        if (isMoving()) System.out.println("isMoving Check 2");
        if (isFt()) System.out.println("isFt single check");
        if ((isFt() || isRw() || isAm() || isSp())) System.out.println("Multi Server Check");

        if (mc.player.isSprinting() && isMoving() && (isFt() || isRw() || isAm() || isSp())) {
            System.out.println("CtidkawkdawkdWKAdkakwakdwakdkadkwkadkwakdakdkwkadkawkdwkakd");
            mc.player.setSprinting(false);
            mc.getConnection().send(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.STOP_SPRINTING));
            mc.player.setDeltaMovement(mc.player.getDeltaMovement().multiply(0.6, 1, 0.6));
        }
    }
    @Inject(
            method = "attack",
            at = @At("TAIL")
    )
    private void resetSprintAfterAttack(Player player, Entity targetEntity, CallbackInfo ci) {
        if (!mc.player.isSprinting() && (isFt() || isRw() || isAm() || isSp())) {
            mc.player.setSprinting(true);
            if (hasEnoughImpulseToStartSprinting() && mc.player.getFoodData().getFoodLevel() >= 3.5F && !mc.player.isUsingItem()) {
                mc.getConnection().send(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.START_SPRINTING));
            }
        }
    }
    @Inject(method = "continueDestroyBlock", at = @At("HEAD"), cancellable = true)
    private void onContinueDestroyBlock(BlockPos posBlock, Direction directionFacing, CallbackInfoReturnable<Boolean> cir) {
        if (!isDetect && Api.isEnabled("NoDelay") && NoDelay.Options.isOptionEnabled("Break Block")) {
            destroyDelay = 0;
        }
    }
    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void onUseItemOn(LocalPlayer player, InteractionHand hand, BlockHitResult result, CallbackInfoReturnable<InteractionResult> cir) {
        if (!isDetect && Api.isEnabled("NoDelay") && NoDelay.Options.isOptionEnabled("Place Block")) {
            destroyDelay = 0;
        }
    }
}