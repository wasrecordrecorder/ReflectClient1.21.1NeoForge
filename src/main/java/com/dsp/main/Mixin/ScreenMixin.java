package com.dsp.main.Mixin;

import com.dsp.main.UI.MainMenu.MainMenuScreen;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.dsp.main.Api.mc;

@Mixin(Screen.class)
public class ScreenMixin {
    @Inject(
            method="renderBlurredBackground",
            at = @At("HEAD"),
            cancellable = true
    )
    public void renderBlurBackground(CallbackInfo ci) {
        if (mc.screen instanceof MainMenuScreen) {
            ci.cancel();
        }
    }
}
