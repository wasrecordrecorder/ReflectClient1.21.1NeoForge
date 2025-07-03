package com.dsp.main.Mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends EffectRenderingInventoryScreen<InventoryMenu> {
    @Unique
    private long animationStartTime = -1;
    @Unique
    private final long animationDuration = 200; // 200ms
    @Unique
    private float animationProgress = 0.0f;

    public InventoryScreenMixin(Player player) {
        super(player.inventoryMenu, player.getInventory(), Component.translatable("container.crafting"));
    }

    @Inject(method = "init", at = @At("HEAD"))
    private void onInit(CallbackInfo ci) {
        animationStartTime = System.currentTimeMillis();
        animationProgress = 0.0f;
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - animationStartTime;
        if (elapsedTime < animationDuration) {
            animationProgress = smoothstep((float) elapsedTime / animationDuration);
        } else {
            animationProgress = 1.0f;
            animationStartTime = -1;
        }
        renderCustomBackground(guiGraphics);
        guiGraphics.pose().pushPose();
        applyAnimation(guiGraphics);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRenderTail(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        guiGraphics.pose().popPose();
    }

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/inventory/InventoryScreen;renderBackground(Lnet/minecraft/client/gui/GuiGraphics;IIF)V"
            )
    )
    private void redirectRenderBackground(InventoryScreen instance, GuiGraphics guiGraphics, int i, int i2, float v) {
    }

    @Unique
    private void renderCustomBackground(GuiGraphics guiGraphics) {
        int alpha = (int) (animationProgress * 170);
        int color = (alpha << 24);
        guiGraphics.fill(0, 0, this.width, this.height, color);
    }

    @Unique
    private void applyAnimation(GuiGraphics guiGraphics) {
        float offsetY = (1.0f - animationProgress) * -this.height;
        guiGraphics.pose().translate(0, offsetY, 0);
        guiGraphics.setColor(1.0f, 1.0f, 1.0f, animationProgress);
    }

    @Unique
    private float smoothstep(float t) {
        return t * t * (3 - 2 * t);
    }
}