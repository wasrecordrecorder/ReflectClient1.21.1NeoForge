package com.dsp.main.Mixin;

import com.dsp.main.Api;
import com.dsp.main.Main;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.dsp.main.Functions.Render.NoRender.NoRenderElements;

@OnlyIn(Dist.CLIENT)
@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @Inject(
            method = "renderSnowAndRain",
            at = @At("HEAD"),
            cancellable = true
    )
    private void cancelSnowAndRain(LightTexture lightTexture, float partialTick, double camX, double camY, double camZ, CallbackInfo ci) {
        if (Api.isEnabled("NoRender") && NoRenderElements.isOptionEnabled("Rain and Snow") && !Main.isDetect) {
            ci.cancel();
        }
    }
    @Inject(
            method = "tickRain",
            at = @At("HEAD"),
            cancellable = true
    )
    private void cancelSnowAndRain(Camera camera, CallbackInfo ci) {
        if (Api.isEnabled("NoRender") && NoRenderElements.isOptionEnabled("Rain and Snow") && !Main.isDetect) {
            ci.cancel();
        }
    }
}
