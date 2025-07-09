package com.dsp.main.Mixin;

import com.dsp.main.Api;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.dsp.main.Functions.Render.NoRender.NoRenderElements;
import static com.dsp.main.Main.isDetect;

@OnlyIn(Dist.CLIENT)
@Mixin(BossHealthOverlay.class)
public class BossHealthOverlayMixin {
    @Inject(
            method = "render",
            at = @At("HEAD"),
            cancellable = true
    )
    public void render(GuiGraphics guiGraphics, CallbackInfo ci) {
        if (Api.isEnabled("NoRender") && NoRenderElements.isOptionEnabled("Bossbar") && !isDetect) {
            ci.cancel();
        }
    }
}
