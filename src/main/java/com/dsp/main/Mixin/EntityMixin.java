package com.dsp.main.Mixin;

import com.dsp.main.Api;
import com.dsp.main.Functions.Player.NoPush;
import com.dsp.main.Utils.Minecraft.Client.ClientPlayerUtil;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.dsp.main.Main.isDetect;

@OnlyIn(Dist.CLIENT)
@Mixin(Entity.class)
public class EntityMixin {

    @Unique
    private float cachedFallDistance = 0.0f;
    @Inject(
            method = "push(Lnet/minecraft/world/entity/Entity;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    public void push(Entity entity, CallbackInfo ci) {
        if (!isDetect && Api.isEnabled("NoPush") && NoPush.Options.isOptionEnabled("Entities")) {
            ci.cancel();
        }
    }

    @Inject(method = "updateFluidHeightAndDoFluidPushing()V", at = @At("HEAD"), cancellable = true)
    private void cancelFluidPush(CallbackInfo ci) {
        if (!isDetect && Api.isEnabled("NoPush") && NoPush.Options.isOptionEnabled("Water")) {
            ci.cancel();
        }
    }
}
