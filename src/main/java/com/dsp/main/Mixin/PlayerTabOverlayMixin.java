package com.dsp.main.Mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.stream.Stream;

@Mixin(PlayerTabOverlay.class)
public abstract class PlayerTabOverlayMixin {
    @Shadow
    private boolean visible;

    @Unique
    private long animationStartTime = -1;
    @Unique
    private final long animationDuration = 200; // 200ms
    @Unique
    private float animationProgress = 0.0f;
    @Unique
    private boolean isAppearing = false;

    @Shadow
    protected abstract List<PlayerInfo> getPlayerInfos();

    @Inject(method = "setVisible", at = @At("HEAD"))
    private void onSetVisible(boolean visible, CallbackInfo ci) {
        if (this.visible != visible) {
            animationStartTime = System.currentTimeMillis();
            isAppearing = visible;
            animationProgress = visible ? 0.0f : 1.0f;
        }
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(GuiGraphics guiGraphics, int width, Scoreboard scoreboard, Objective objective, CallbackInfo ci) {
        if (animationStartTime != -1) {
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - animationStartTime;
            if (elapsedTime < animationDuration) {
                float t = (float) elapsedTime / animationDuration;
                animationProgress = isAppearing ? smoothstep(t) : 1.0f - smoothstep(t);
            } else {
                animationProgress = isAppearing ? 1.0f : 0.0f;
                animationStartTime = -1;
            }
        }

        if (animationProgress > 0.0f) {
            guiGraphics.setColor(1.0f, 1.0f, 1.0f, animationProgress);
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRenderTail(GuiGraphics guiGraphics, int width, Scoreboard scoreboard, Objective objective, CallbackInfo ci) {
        if (animationProgress > 0.0f) {
            guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
    }

    @Unique
    private float smoothstep(float t) {
        return t * t * (3 - 2 * t);
    }

    @Redirect(
            method = "getPlayerInfos",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/stream/Stream;limit(J)Ljava/util/stream/Stream;",
                    ordinal = 0
            )
    )
    private Stream<PlayerInfo> redirectLimit(Stream<PlayerInfo> stream, long limit) {
        return stream.limit(140L);
    }
}