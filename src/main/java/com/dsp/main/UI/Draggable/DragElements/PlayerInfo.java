package com.dsp.main.UI.Draggable.DragElements;

import com.dsp.main.Api;
import com.dsp.main.UI.Draggable.DraggableElement;
import com.dsp.main.Utils.Font.builders.Builder;
import com.dsp.main.Utils.Font.renderers.impl.BuiltText;
import com.dsp.main.Utils.Render.DrawHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix4f;
import java.awt.Color;

import static com.dsp.main.Api.mc;
import static com.dsp.main.Functions.Render.HudElement.HudElements;
import static com.dsp.main.Functions.Render.HudElement.IconColor;
import static com.dsp.main.Main.BIKO_FONT;
import static com.dsp.main.Main.ICONS;

public class PlayerInfo extends DraggableElement {
    private static final int BASE_HEIGHT = 16;
    private static final int TEXT_HEIGHT = 10;
    private static final int PADDING = 5;
    private static final int GAP = 2;

    public PlayerInfo(String name, float initialX, float initialY, boolean canBeDragged) {
        super(name, initialX, initialY, canBeDragged);
    }

    @Override
    public float getWidth() {
        String coords = getCoordinates();
        String speed = getSpeed();
        float coordsWidth = BIKO_FONT.get().getWidth(coords, TEXT_HEIGHT);
        float speedWidth = BIKO_FONT.get().getWidth(speed, TEXT_HEIGHT);
        return Math.max(coordsWidth, speedWidth) + PADDING * 2;
    }

    @Override
    public float getHeight() {
        return BASE_HEIGHT * 2 + GAP;
    }

    @Override
    public void render(GuiGraphics guiGraphics) {
        if (HudElements.isOptionEnabled("Player Info") && Api.isEnabled("Hud")) {
            String coords = getCoordinates();
            String speed = getSpeed();
            float coordsWidth = BIKO_FONT.get().getWidth(coords, TEXT_HEIGHT) + 5;
            float speedWidth = BIKO_FONT.get().getWidth(speed, TEXT_HEIGHT) + 5;
            float screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
            float yPos = screenHeight - getHeight() - PADDING;
            DrawHelper.rectangle(new PoseStack(), xPos, yPos + BASE_HEIGHT + GAP, coordsWidth + 15, BASE_HEIGHT, 6, new Color(23, 29, 35, 200).getRGB());
            DrawHelper.rectangle(new PoseStack(), xPos, yPos, speedWidth + 13, BASE_HEIGHT, 6, new Color(23, 29, 35, 200).getRGB());

            BuiltText CoordsIcon = Builder.text()
                    .font(ICONS.get())
                    .text("M")
                    .color(IconColor)
                    .size(12f)
                    .thickness(0.05f)
                    .build();
            CoordsIcon.render(new Matrix4f(), xPos, yPos + BASE_HEIGHT + GAP + 2.5);
            BuiltText BpsIcon = Builder.text()
                    .font(ICONS.get())
                    .text("O")
                    .color(IconColor)
                    .size(12f)
                    .thickness(0.05f)
                    .build();
            BpsIcon.render(new Matrix4f(), xPos, yPos- 0.5 + (BASE_HEIGHT - TEXT_HEIGHT) / 2);

            BuiltText coordsText = Builder.text()
                    .font(BIKO_FONT.get())
                    .text(coords)
                    .color(Color.WHITE)
                    .size((float) TEXT_HEIGHT)
                    .thickness(0.05f)
                    .build();
            coordsText.render(new Matrix4f(), xPos + 15, yPos + BASE_HEIGHT + 1 + GAP + (BASE_HEIGHT - TEXT_HEIGHT) / 2);

            BuiltText speedText = Builder.text()
                    .font(BIKO_FONT.get())
                    .text(speed)
                    .color(Color.WHITE)
                    .size((float) TEXT_HEIGHT)
                    .thickness(0.05f)
                    .build();
            speedText.render(new Matrix4f(), xPos + 15, yPos + 1 + (BASE_HEIGHT - TEXT_HEIGHT) / 2);
        }
    }

    private String getCoordinates() {
        if (mc.player != null && mc.level != null) {
            return String.format("%.0f  %.0f  %.0f", mc.player.getX(), mc.player.getY(), mc.player.getZ());
        }
        return "0  0  0";
    }

    private String getSpeed() {
        if (mc.player != null && mc.level != null) {
            double dx = mc.player.getX() - mc.player.xo;
            double dz = mc.player.getZ() - mc.player.zo;
            double bps = Math.sqrt(dx * dx + dz * dz) * 20;
            return String.format("%.2f", bps);
        }
        return "0.00";
    }
}