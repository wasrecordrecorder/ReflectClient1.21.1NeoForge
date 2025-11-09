package com.dsp.main.Core.GPS;

import com.dsp.main.Utils.Font.builders.Builder;
import com.dsp.main.Utils.Font.renderers.impl.BuiltText;
import com.dsp.main.Utils.Render.Blur.DrawShader;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.CoreShaders;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.joml.Matrix4f;

import java.awt.*;

import static com.dsp.main.Main.BIKO_FONT;

public class GPSRenderer {

    private static final Minecraft mc = Minecraft.getInstance();
    private static final ResourceLocation ARROW_TEXTURE = ResourceLocation.fromNamespaceAndPath("dsp", "textures/arrow.png");

    private static boolean showDistance = true;
    private static boolean showCoords = true;
    private static boolean blur = false;

    private float arrowAnimProgress = 0f;
    private float chatOffsetAnim = 0f;

    @SubscribeEvent
    public void onRender(RenderGuiEvent.Post e) {
        if (!GPSManager.isEnabled() || mc.player == null) return;

        arrowAnimProgress += 2f;
        if (arrowAnimProgress >= 360f) arrowAnimProgress = 0f;

        boolean chatOpen = mc.screen != null;
        float targetOffset = chatOpen ? 20f : 0f;
        chatOffsetAnim = Mth.lerp(0.15f, chatOffsetAnim, targetOffset);

        GuiGraphics guiGraphics = e.getGuiGraphics();

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        int centerX = screenWidth / 2;
        int centerY = (int) (screenHeight - 70 - chatOffsetAnim);

        renderInfoBox(guiGraphics, centerX, centerY);
        renderArrow(guiGraphics, centerX, centerY);
    }

    private void renderInfoBox(GuiGraphics guiGraphics, int centerX, int centerY) {
        double distance = GPSManager.getDistance(mc.player.position());
        String distText = String.format("%.1f m", distance);

        float textWidth = BIKO_FONT.get().getWidth(distText, 8f);

        int boxWidth = (int) Math.max(35, textWidth + 10);
        int boxHeight = 17;

        int x = centerX - boxWidth / 2;
        int y = centerY - boxHeight / 2;

        if (blur) {
            DrawShader.drawRoundBlur(guiGraphics.pose(), x, y, boxWidth, boxHeight, 2,
                    new Color(5, 15, 25).hashCode(),
                    90,
                    0.6f);
            //DrawShader.drawRoundBlur(guiGraphics.pose(), x, y, boxWidth, boxHeight, 2,
            //        new Color(30, 30, 30, 180).getRGB(), 90, 0.5f);
        } else {
            DrawShader.drawRoundBlur(guiGraphics.pose(), x, y, boxWidth, boxHeight, 2,
                    new Color(10, 15, 25, 200).getRGB(), 0, 0);
        }

        if (showDistance) {
            BuiltText distanceText = Builder.text()
                    .font(BIKO_FONT.get())
                    .text(distText)
                    .color(Color.WHITE)
                    .size(8f)
                    .thickness(0.05f)
                    .build();

            float distWidth = BIKO_FONT.get().getWidth(distText, 8f);
            distanceText.render(new Matrix4f(), centerX - distWidth / 2, y + 5);
        }
    }

    private void renderArrow(GuiGraphics guiGraphics, int centerX, int centerY) {
        float angleToTarget = GPSManager.getAngleToTarget(mc.player.position(), mc.player.getYRot());

        double distance = GPSManager.getDistance(mc.player.position());
        String distText = String.format("%.1f m", distance);
        float textWidth = BIKO_FONT.get().getWidth(distText, 8f);

        int boxWidth = (int) Math.max(35, textWidth + 10);
        int boxHeight = 17;

        int padding = 8;
        int halfWidth = boxWidth / 2 + padding;
        int halfHeight = boxHeight / 2 + padding;

        ArrowPosition arrowPos = calculateArrowPosition(angleToTarget - 90, centerX, centerY, halfWidth, halfHeight);

        int arrowSize = 16;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(arrowPos.x, arrowPos.y, 0);
        guiGraphics.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(arrowPos.rotation));
        guiGraphics.pose().translate(-arrowSize / 2f, -arrowSize / 2f, 0);

        guiGraphics.blit(
                net.minecraft.client.renderer.RenderType::guiTextured,
                ARROW_TEXTURE,
                0,
                0,
                0.0f,
                0.0f,
                arrowSize,
                arrowSize,
                arrowSize,
                arrowSize
        );

        guiGraphics.pose().popPose();
    }

    private ArrowPosition calculateArrowPosition(float angle, int centerX, int centerY, int halfWidth, int halfHeight) {
        float normalizedAngle = angle;
        while (normalizedAngle < 0) normalizedAngle += 360;
        while (normalizedAngle >= 360) normalizedAngle -= 360;

        float x, y;

        if (normalizedAngle >= 315 || normalizedAngle < 45) {
            float t = normalizedAngle >= 315 ? (normalizedAngle - 315) / 90f : (normalizedAngle + 45) / 90f;
            x = centerX + halfWidth;
            y = centerY - halfHeight + (halfHeight * 2 * t);
        }
        else if (normalizedAngle >= 45 && normalizedAngle < 135) {
            float t = (normalizedAngle - 45) / 90f;
            x = centerX + halfWidth - (halfWidth * 2 * t);
            y = centerY + halfHeight;
        }
        else if (normalizedAngle >= 135 && normalizedAngle < 225) {
            float t = (normalizedAngle - 135) / 90f;
            x = centerX - halfWidth;
            y = centerY + halfHeight - (halfHeight * 2 * t);
        }
        else {
            float t = (normalizedAngle - 225) / 90f;
            x = centerX - halfWidth + (halfWidth * 2 * t);
            y = centerY - halfHeight;
        }

        return new ArrowPosition(x, y, angle);
    }

    public static void setShowDistance(boolean show) {
        showDistance = show;
    }

    public static void setShowCoords(boolean show) {
        showCoords = show;
    }

    public static void setBlur(boolean enable) {
        blur = enable;
    }

    private static class ArrowPosition {
        float x, y, rotation;

        ArrowPosition(float x, float y, float rotation) {
            this.x = x;
            this.y = y;
            this.rotation = rotation;
        }
    }
}