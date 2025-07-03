package com.dsp.main.Mixin;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.dsp.main.Main.isDetect;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Shadow
    private int noJumpDelay;
    @Inject(method = "aiStep", at = @At("TAIL"))
    private void resetJumpDelay(CallbackInfo ci) {
        if (!isDetect) {
            this.noJumpDelay = 0;
        }
    }
}
