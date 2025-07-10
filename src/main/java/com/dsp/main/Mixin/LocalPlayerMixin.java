package com.dsp.main.Mixin;

import com.dsp.main.Api;
import com.dsp.main.Functions.Player.NoPush;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.dsp.main.Main.isDetect;

@OnlyIn(Dist.CLIENT)
@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
    @Inject(
            method = "moveTowardsClosestSpace",
            at = @At("HEAD"),
            cancellable = true
    )
    private void moveTowardsClosestSpace(double x, double z, CallbackInfo ci) {
        if (!isDetect && Api.isEnabled("NoPush") && NoPush.Options.isOptionEnabled("Blocks")) {
            ci.cancel();
        }
    }
}
