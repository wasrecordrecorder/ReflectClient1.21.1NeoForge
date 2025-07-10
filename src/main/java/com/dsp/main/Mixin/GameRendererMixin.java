package com.dsp.main.Mixin;

import com.dsp.main.Api;
import com.dsp.main.Functions.Render.NoRender;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@OnlyIn(Dist.CLIENT)
@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(
            method = "renderConfusionOverlay",
            at = @At("HEAD"),
            cancellable = true
    )
    private void renderConfusionOverlay(GuiGraphics guiGraphics, float scalar, CallbackInfo ci) {
        if (NoRender.NoRenderElements.isOptionEnabled("Bad Effects") && Api.isEnabled("NoRender")) {
            ci.cancel();
        }
    }
}
