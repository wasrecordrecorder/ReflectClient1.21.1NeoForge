package com.dsp.main.Mixin;

import com.dsp.main.Api;
import com.dsp.main.Functions.Player.StreamerMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.stream.Stream;

import static com.dsp.main.Main.isDetect;

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
        if (isDetect) return;
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

    @Inject(method = "getNameForDisplay", at = @At("RETURN"), cancellable = true)
    private void onGetNameForDisplay(PlayerInfo playerInfo, CallbackInfoReturnable<Component> cir) {
        if (Api.isEnabled("StreamerMode")) {
            Minecraft mc = Minecraft.getInstance();
            String localPlayerName = mc.player != null ? mc.player.getName().getString() : "";
            Component originalComponent = cir.getReturnValue();
            String text = originalComponent.getString();
            boolean replaced = false;
            String newText = text;
            if (!localPlayerName.isEmpty() && text.contains(localPlayerName)) {
                newText = newText.replace(localPlayerName, StreamerMode.ProtectedName);
                replaced = true;
            }
            if (StreamerMode.FuntimePr.isEnabled() && text.toLowerCase().contains("funtime")) {
                newText = newText.replaceAll("(?i)funtime", "xuitime");
                replaced = true;
            }

            if (replaced) {
                MutableComponent newComponent = Component.literal(newText).withStyle(originalComponent.getStyle());
                for (Component child : originalComponent.getSiblings()) {
                    String childText = child.getString();
                    String newChildText = childText;

                    if (!localPlayerName.isEmpty() && childText.contains(localPlayerName)) {
                        newChildText = newChildText.replace(localPlayerName, StreamerMode.ProtectedName);
                    }
                    if (StreamerMode.FuntimePr.isEnabled() && childText.toLowerCase().contains("funtime")) {
                        newChildText = newChildText.replaceAll("(?i)funtime", "xuitime");
                    }
                }
                cir.setReturnValue(newComponent);
            }
        }
    }
}