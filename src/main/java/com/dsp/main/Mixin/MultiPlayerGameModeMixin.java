package com.dsp.main.Mixin;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.dsp.main.Api.mc;
import static com.dsp.main.Utils.Minecraft.Server.WhatServer.*;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {
    @Inject(
            method = "attack",
            at = @At("HEAD")
    )
    private void resetSprintBeforeAttack(Player player, Entity targetEntity, CallbackInfo ci) {
        if (mc.player.isSprinting() && isMoving() && (isFt() || isRw() || isAm() || isSp())) {
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
    private static boolean isMoving() {
        Vec2 vector2f = mc.player.input.getMoveVector();
        return vector2f.x != 0.0F || vector2f.y != 0.0F;
    }

    private static boolean hasEnoughImpulseToStartSprinting() {
        double d0 = 0.8D;
        return (double)mc.player.input.forwardImpulse >= d0;
    }
}