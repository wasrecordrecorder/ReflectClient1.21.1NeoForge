package com.dsp.main.Mixin;

import com.dsp.main.Api;
import com.dsp.main.Main;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.FogParameters;
import net.minecraft.client.renderer.LevelRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.dsp.main.Functions.Render.NoRender.NoRenderElements;

@OnlyIn(Dist.CLIENT)
@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Inject(
            method = "addWeatherPass(Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;Lnet/minecraft/world/phys/Vec3;FLnet/minecraft/client/renderer/FogParameters;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void cancelWeatherPassDeprecated(
            FrameGraphBuilder frameGraphBuilder,
            Vec3 cameraPosition,
            float partialTick,
            FogParameters fog,
            CallbackInfo ci
    ) {
        if (Api.isEnabled("NoRender") && NoRenderElements.isOptionEnabled("Rain and Snow") && !Main.isDetect) {
            ci.cancel();
        }
    }

    @Inject(
            method = "tickParticles",
            at = @At("HEAD"),
            cancellable = true
    )
    private void cancelTickParticles(Camera camera, CallbackInfo ci) {
        if (Api.isEnabled("NoRender") && NoRenderElements.isOptionEnabled("Rain and Snow") && !Main.isDetect) {
            ci.cancel();
        }
    }
}