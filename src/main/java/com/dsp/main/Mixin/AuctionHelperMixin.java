package com.dsp.main.Mixin;

import com.dsp.main.Functions.Misc.AuctionHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class AuctionHelperMixin<T extends AbstractContainerMenu> {

    @Shadow public T menu;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderLabels(Lnet/minecraft/client/gui/GuiGraphics;II)V", shift = At.Shift.AFTER))
    private void renderAuctionHighlight(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        AuctionHelper helper = AuctionHelper.getInstance();
        if (helper == null || !helper.isEnabled()) return;

        for (Slot slot : menu.slots) {
            Integer slotType = helper.getSlotHighlight(slot);
            if (slotType != null) {
                int x = slot.x;
                int y = slot.y;

                float pulse = (float) (Math.sin(System.currentTimeMillis() / 1500.0) * 0.25 + 0.75);

                int color;
                if (slotType == 0) {
                    int r = (int) (0 * pulse);
                    int g = (int) (255 * pulse);
                    int b = (int) (0 * pulse);
                    color = 0xFF000000 | (r << 16) | (g << 8) | b;
                } else {
                    int r = (int) (255 * pulse);
                    int g = (int) (255 * pulse);
                    int b = (int) (0 * pulse);
                    color = 0xFF000000 | (r << 16) | (g << 8) | b;
                }

                guiGraphics.fill(x, y, x + 16, y + 16, color);
            }
        }
    }
}