package com.dsp.main.Mixin;

import com.dsp.main.Api;
import com.dsp.main.Functions.Player.StreamerMode;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
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
import com.mojang.blaze3d.systems.RenderSystem;

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
    private final long animationDuration = 300;
    @Unique
    private float currentProgress = 0.0f;
    @Unique
    private float targetProgress = 0.0f;
    @Unique
    private boolean wasVisible = false;

    @Shadow
    protected abstract List<PlayerInfo> getPlayerInfos();

    @Inject(method = "setVisible", at = @At("HEAD"))
    private void onSetVisible(boolean visible, CallbackInfo ci) {
        if (this.wasVisible != visible) {
            animationStartTime = System.currentTimeMillis();
            targetProgress = visible ? 1.0f : 0.0f;
            this.wasVisible = visible;
        }
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRenderHead(GuiGraphics guiGraphics, int width, Scoreboard scoreboard, Objective objective, CallbackInfo ci) {
        if (isDetect) return;

        updateAnimation();

        if (currentProgress <= 0.0f && !visible) {
            ci.cancel();
            return;
        }

        if (currentProgress < 1.0f || animationStartTime != -1) {
            PoseStack poseStack = guiGraphics.pose();
            poseStack.pushPose();

            float scale = 0.7f + (currentProgress * 0.3f);
            float offsetY = (1.0f - currentProgress) * -20.0f;

            poseStack.translate(width / 2.0f, offsetY, 0);
            poseStack.scale(scale, scale, 1.0f);
            poseStack.translate(-width / 2.0f, 0, 0);

            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, currentProgress);
        }
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void onRenderTail(GuiGraphics guiGraphics, int width, Scoreboard scoreboard, Objective objective, CallbackInfo ci) {
        if (currentProgress < 1.0f || animationStartTime != -1) {
            guiGraphics.pose().popPose();
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, currentProgress);
        }
    }

    @Unique
    private void updateAnimation() {
        if (animationStartTime == -1 && currentProgress == targetProgress) {
            return;
        }

        if (animationStartTime != -1) {
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - animationStartTime;

            if (elapsedTime >= animationDuration) {
                currentProgress = targetProgress;
                animationStartTime = -1;
            } else {
                float t = (float) elapsedTime / animationDuration;
                float easedT = easeInOutCubic(t);
                currentProgress = Mth.lerp(easedT, targetProgress == 1.0f ? 0.0f : 1.0f, targetProgress);
            }
        } else {
            float delta = (targetProgress - currentProgress) * 0.3f;
            if (Math.abs(delta) < 0.001f) {
                currentProgress = targetProgress;
            } else {
                currentProgress += delta;
            }
        }

        currentProgress = Mth.clamp(currentProgress, 0.0f, 1.0f);
    }

    @Unique
    private float easeInOutCubic(float t) {
        return t < 0.5f ? 4.0f * t * t * t : 1.0f - (float) Math.pow(-2.0f * t + 2.0f, 3.0f) / 2.0f;
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
        if (!Api.isEnabled("StreamerMode")) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        String localPlayerName = mc.player.getName().getString();
        Component originalComponent = cir.getReturnValue();
        String text = originalComponent.getString();

        if (text == null || text.isEmpty()) return;

        String newText = text;
        boolean replaced = false;

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
                newComponent.append(child);
            }
            cir.setReturnValue(newComponent);
        }
    }
}