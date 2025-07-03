package com.dsp.main.Mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.dsp.main.Main.isDetect;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(method = "createTitle", at = @At("RETURN"), cancellable = true)
    private void onTitleCreating(CallbackInfoReturnable<String> cir) {
        if (!isDetect) {
            cir.setReturnValue("Reflect Client 3.3 - Release");
        }
    }
}