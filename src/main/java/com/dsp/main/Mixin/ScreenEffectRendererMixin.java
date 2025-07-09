package com.dsp.main.Mixin;

import com.dsp.main.Api;
import com.dsp.main.Main;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.dsp.main.Functions.Render.NoRender.NoRenderElements;

@OnlyIn(Dist.CLIENT)
@Mixin(ScreenEffectRenderer.class)
public class ScreenEffectRendererMixin {

    @Inject(
            method = "renderScreenEffect",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/neoforged/neoforge/client/ClientHooks;renderBlockOverlay(Lnet/minecraft/world/entity/player/Player;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/neoforged/neoforge/client/event/RenderBlockScreenEffectEvent$OverlayType;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)Z"
            ),
            cancellable = true
    )
    private static void cancelBlockOverlay(Minecraft minecraft, PoseStack poseStack, CallbackInfo ci) {
        if (Api.isEnabled("NoRender") && NoRenderElements.isOptionEnabled("Block") && !Main.isDetect) {
            ci.cancel();
        }
    }

    @Inject(
            method = "renderScreenEffect",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/neoforged/neoforge/client/ClientHooks;renderWaterOverlay(Lnet/minecraft/world/entity/player/Player;Lcom/mojang/blaze3d/vertex/PoseStack;)Z"
            ),
            cancellable = true
    )
    private static void cancelWaterOverlay(Minecraft minecraft, PoseStack poseStack, CallbackInfo ci) {
        if (Api.isEnabled("NoRender") && NoRenderElements.isOptionEnabled("Water") && !Main.isDetect) {
            ci.cancel();
        }
    }

    @Inject(
            method = "renderScreenEffect",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/neoforged/neoforge/client/ClientHooks;renderFireOverlay(Lnet/minecraft/world/entity/player/Player;Lcom/mojang/blaze3d/vertex/PoseStack;)Z"
            ),
            cancellable = true
    )
    private static void cancelFireOverlay(Minecraft minecraft, PoseStack poseStack, CallbackInfo ci) {
        if (Api.isEnabled("NoRender") && NoRenderElements.isOptionEnabled("Fire") && !Main.isDetect) {
            ci.cancel();
        }
    }

    @Inject(
            method = "renderFire",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void cancelFireRender(Minecraft minecraft, PoseStack poseStack, CallbackInfo ci) {
        if (Api.isEnabled("NoRender") && NoRenderElements.isOptionEnabled("Fire") && !Main.isDetect) {
            ci.cancel();
        }
    }

    @Inject(
            method = "renderWater",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void cancelWaterRender(Minecraft minecraft, PoseStack poseStack, CallbackInfo ci) {
        if (Api.isEnabled("NoRender") && NoRenderElements.isOptionEnabled("Water") && !Main.isDetect) {
            ci.cancel();
        }
    }

    @Inject(
            method = "renderTex",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void cancelBlockRender(TextureAtlasSprite texture, PoseStack poseStack, CallbackInfo ci) {
        if (Api.isEnabled("NoRender") && NoRenderElements.isOptionEnabled("Block") && !Main.isDetect) {
            ci.cancel();
        }
    }
}