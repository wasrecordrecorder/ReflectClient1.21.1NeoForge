package com.dsp.main.UI.Draggable.DragElements;

import com.dsp.main.Api;
import com.dsp.main.UI.Draggable.DraggableElement;
import com.dsp.main.Utils.Render.Blur.DrawShader;
import com.dsp.main.Utils.Render.DrawHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

import java.awt.*;

import static com.dsp.main.Api.mc;
import static com.dsp.main.Functions.Render.HudElement.HudElements;

public class InventoryHud extends DraggableElement {

    private static final int SLOT_SIZE   = 18;
    private static final int SLOT_GAP    = 1;
    private static final int PADDING     = 4;
    private static final int ROWS        = 3;
    private static final int COLS        = 9;
    private static final int ROUND_RADIUS = 5;

    private long lastToggle = 0;
    private boolean wasVisible = false;
    private float animProgress = 1f;

    public InventoryHud(String name, float initialX, float initialY, boolean canBeDragged) {
        super(name, initialX, initialY, canBeDragged);
    }

    @Override
    public float getWidth() {
        return COLS * SLOT_SIZE + (COLS - 1) * SLOT_GAP + 2 * PADDING;
    }

    @Override
    public float getHeight() {
        return ROWS * SLOT_SIZE + (ROWS - 1) * SLOT_GAP + 2 * PADDING;
    }

    @Override
    public void render(GuiGraphics guiGraphics) {
        boolean visible = HudElements.isOptionEnabled("Inventory Hud") && Api.isEnabled("Hud") && mc.player != null && (hasAnyItemInInv(mc.player) || isChatOpen());
        long now = System.currentTimeMillis();
        if (visible != wasVisible) {
            lastToggle = now;
            wasVisible = visible;
        }
        float duration = 200f;
        float elapsed = Math.min(now - lastToggle, duration);
        animProgress = visible ? elapsed / duration : 1f - elapsed / duration;
        if (animProgress <= 0) return;

        float animWidth = getWidth() * animProgress;
        float animHeight = getHeight() * animProgress;

        DrawShader.drawRoundBlur(guiGraphics.pose(),
                xPos, yPos,
                animWidth, animHeight,
                ROUND_RADIUS,
                new Color(23, 29, 35, 200).getRGB(),
                120, 0.4f);
        guiGraphics.enableScissor((int) xPos, (int) yPos,
                (int) (xPos + animWidth), (int) (yPos + animHeight));

        float startX = xPos + PADDING;
        float startY = yPos + PADDING;
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                int slotIdx = 9 + r * COLS + c;
                ItemStack stack = mc.player.getInventory().getItem(slotIdx);
                float slotX = startX + c * (SLOT_SIZE + SLOT_GAP);
                float slotY = startY + r * (SLOT_SIZE + SLOT_GAP);
                if (slotX + SLOT_SIZE > xPos + animWidth || slotY + SLOT_SIZE > yPos + animHeight) continue;
                DrawHelper.rectangle(guiGraphics.pose(),
                        slotX, slotY,
                        SLOT_SIZE, SLOT_SIZE,
                        1f,
                        new Color(0, 0, 0, 90).getRGB());

                if (!stack.isEmpty()) {
                    guiGraphics.renderItem(stack,
                            (int) slotX + 1,
                            (int) slotY + 1);
                    guiGraphics.renderItemDecorations(
                            mc.font,
                            stack,
                            (int) slotX + 1,
                            (int) slotY + 1);
                }
            }
        }
        guiGraphics.disableScissor();
    }

    public static boolean hasAnyItemInInv(Player player) {
        if (player == null) return false;
        for (int i = 9; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof ArmorItem) continue;
            if (!stack.isEmpty()) {
                return true;
            }
        }
        return false;
    }
}