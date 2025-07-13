package com.dsp.main.Mixin;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

import static com.dsp.main.Api.mc;
import static com.dsp.main.Functions.Render.NoRender.NoRenderElements;
import static com.dsp.main.Main.isDetect;

@Mixin(Gui.class)
public abstract class GuiHotbarMixin {
    @Shadow public abstract void initModdedOverlays();

    private float hotbarOffset = 0.0F;
    private float targetOffset = 0.0F;
    private static final float ANIMATION_SPEED = 10.0F;
    private static final float OFFSET_AMOUNT = -15.0F;

    private void applyAnimation(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (isDetect) return;
        Minecraft minecraft = Minecraft.getInstance();
        boolean isChatOpen = minecraft.screen instanceof ChatScreen;
        targetOffset = isChatOpen ? OFFSET_AMOUNT : 0.0F;
        float delta = deltaTracker.getGameTimeDeltaPartialTick(true);
        float offsetDifference = targetOffset - hotbarOffset;
        if (Math.abs(offsetDifference) > 0.01F) {
            hotbarOffset += offsetDifference * ANIMATION_SPEED * delta / 20.0F;
        } else {
            hotbarOffset = targetOffset;
        }
        if (hotbarOffset != 0.0F) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0.0F, hotbarOffset, 0.0F);
        }
    }

    private void popPoseIfNeeded(GuiGraphics guiGraphics) {
        if (hotbarOffset != 0.0F) {
            guiGraphics.pose().popPose();
        }
    }
    @Inject(method = "renderItemHotbar(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V",
            at = @At("HEAD"))
    private void animateHotbar(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        applyAnimation(guiGraphics, deltaTracker);
    }

    @Inject(method = "renderItemHotbar(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V",
            at = @At("TAIL"))
    private void popHotbarPose(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        popPoseIfNeeded(guiGraphics);
    }

    @Inject(method = "renderHealthLevel(Lnet/minecraft/client/gui/GuiGraphics;)V",
            at = @At("HEAD"))
    private void animateHealthLevel(GuiGraphics guiGraphics, CallbackInfo ci) {
        applyAnimation(guiGraphics, DeltaTracker.ZERO);
    }

    @Inject(method = "renderHealthLevel(Lnet/minecraft/client/gui/GuiGraphics;)V",
            at = @At("TAIL"))
    private void popHealthLevelPose(GuiGraphics guiGraphics, CallbackInfo ci) {
        popPoseIfNeeded(guiGraphics);
    }

    @Inject(method = "renderArmorLevel(Lnet/minecraft/client/gui/GuiGraphics;)V",
            at = @At("HEAD"))
    private void animateArmorLevel(GuiGraphics guiGraphics, CallbackInfo ci) {
        applyAnimation(guiGraphics, DeltaTracker.ZERO);
    }

    @Inject(method = "renderArmorLevel(Lnet/minecraft/client/gui/GuiGraphics;)V",
            at = @At("TAIL"))
    private void popArmorLevelPose(GuiGraphics guiGraphics, CallbackInfo ci) {
        popPoseIfNeeded(guiGraphics);
    }

    @Inject(method = "renderScoreboardSidebar",
            at = @At("HEAD"), cancellable = true)
    private void renderScoreBoard(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!isDetect && NoRenderElements.isOptionEnabled("Scoreboard")) {
            ci.cancel();
        }
    }
    @Inject(method = "renderFoodLevel(Lnet/minecraft/client/gui/GuiGraphics;)V", at = @At("HEAD"))
    private void animateFoodLevel(GuiGraphics guiGraphics, CallbackInfo ci) {
        applyAnimation(guiGraphics, DeltaTracker.ZERO);
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player != null && !isDetect) {
            int screenWidth = minecraft.getWindow().getGuiScaledWidth();
            int itemSize = 16;
            int spacing = 2;
            int armorOffsetY = -35;
            List<ItemStack> armorItems = new ArrayList<>();
            for (ItemStack armor : player.getArmorSlots()) {
                if (!armor.isEmpty()) {
                    armorItems.add(armor);
                }
            }

            int totalWidth = armorItems.size() * itemSize + (armorItems.size() - 1) * spacing;
            int startX = (screenWidth - totalWidth) / 2;

            for (int i = 0; i < armorItems.size(); i++) {
                int itemX = startX + 50 + i * (itemSize + spacing);
                int itemY;
                if (!(mc.screen instanceof ChatScreen)) {
                    itemY = minecraft.getWindow().getGuiScaledHeight() - 22 + armorOffsetY + (int)hotbarOffset;
                } else {
                    itemY = minecraft.getWindow().getGuiScaledHeight() - 22 + (-23) + (int)hotbarOffset;
                }
                guiGraphics.renderItem(armorItems.get(i), itemX, itemY);
                guiGraphics.renderItemDecorations(mc.font ,armorItems.get(i), itemX, itemY);
            }
        }
    }

    @Inject(method = "renderFoodLevel(Lnet/minecraft/client/gui/GuiGraphics;)V", at = @At("TAIL"))
    private void popFoodLevelPose(GuiGraphics guiGraphics, CallbackInfo ci) {
        popPoseIfNeeded(guiGraphics);
    }

    @Inject(method = "renderExperienceBar(Lnet/minecraft/client/gui/GuiGraphics;I)V",
            at = @At("HEAD"))
    private void animateExperienceBar(GuiGraphics guiGraphics, int x, CallbackInfo ci) {
        applyAnimation(guiGraphics, DeltaTracker.ZERO);
    }
    @Inject(method = "renderExperienceBar(Lnet/minecraft/client/gui/GuiGraphics;I)V",
            at = @At("TAIL"))
    private void popExperienceBarPose(GuiGraphics guiGraphics, int x, CallbackInfo ci) {
        popPoseIfNeeded(guiGraphics);
    }

    @Inject(method = "renderExperienceLevel(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V",
            at = @At("HEAD"))
    private void animateExperienceLevel(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        applyAnimation(guiGraphics, deltaTracker);
    }

    @Inject(method = "renderExperienceLevel(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V",
            at = @At("TAIL"))
    private void popExperienceLevelPose(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        popPoseIfNeeded(guiGraphics);
    }
    @Inject(method = "renderSelectedItemName(Lnet/minecraft/client/gui/GuiGraphics;I)V",
            at = @At("HEAD"))
    private void animateSelectedItemName(GuiGraphics guiGraphics, int yShift, CallbackInfo ci) {
        applyAnimation(guiGraphics, DeltaTracker.ZERO);
    }

    @Inject(method = "renderSelectedItemName(Lnet/minecraft/client/gui/GuiGraphics;I)V",
            at = @At("TAIL"))
    private void popSelectedItemNamePose(GuiGraphics guiGraphics, int yShift, CallbackInfo ci) {
        popPoseIfNeeded(guiGraphics);
    }

    @Inject(method = "renderVehicleHealth(Lnet/minecraft/client/gui/GuiGraphics;)V",
            at = @At("HEAD"))
    private void animateVehicleHealth(GuiGraphics guiGraphics, CallbackInfo ci) {
        applyAnimation(guiGraphics, DeltaTracker.ZERO);
    }

    @Inject(method = "renderVehicleHealth(Lnet/minecraft/client/gui/GuiGraphics;)V",
            at = @At("TAIL"))
    private void popVehicleHealthPose(GuiGraphics guiGraphics, CallbackInfo ci) {
        popPoseIfNeeded(guiGraphics);
    }
}