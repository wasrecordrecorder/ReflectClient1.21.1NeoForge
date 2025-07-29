package com.dsp.main.Mixin;

import com.dsp.main.Functions.Misc.ClientSetting;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin {

    @Inject(
            method = "playSound",
            at = @At("HEAD"),
            cancellable = true
    )
    private void dsp$cancelVanillaAttackSound(
            SoundEvent sound, float volume, float pitch, CallbackInfo ci
    ) {
        if (sound == net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_NODAMAGE
                || sound == net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_KNOCKBACK
                || sound == net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_SWEEP
                || sound == net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_CRIT
                || sound == net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_STRONG
                || sound == net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_WEAK) {
            boolean suppress = ClientSetting.sound.isEnabled()
                    && !ClientSetting.hitSound.isMode("None");
            if (suppress) ci.cancel();
        }
    }
}