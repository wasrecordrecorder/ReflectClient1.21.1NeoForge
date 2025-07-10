package com.dsp.main.Mixin;

import com.dsp.main.Api;
import com.dsp.main.Functions.Player.NoDelay;
import com.dsp.main.Functions.Render.NoRender;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.dsp.main.Main.isDetect;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Shadow
    private int noJumpDelay;
    @Inject(method = "aiStep", at = @At("TAIL"))
    private void resetJumpDelay(CallbackInfo ci) {
        if (!isDetect && Api.isEnabled("NoDelay") && NoDelay.Options.isOptionEnabled("Jumping")) {
            this.noJumpDelay = 0;
        }
    }
    @Inject(
            method = "hasEffect",
            at = @At("HEAD"),
            cancellable = true
    )
    private void disableBlindnessAndDarkness(Holder<MobEffect> effect, CallbackInfoReturnable<Boolean> cir) {
        if ((effect == MobEffects.BLINDNESS || effect == MobEffects.DARKNESS) && Api.isEnabled("NoRender") && NoRender.NoRenderElements.isOptionEnabled("Bad Effects")) {
            cir.setReturnValue(false);
        }
    }
}
