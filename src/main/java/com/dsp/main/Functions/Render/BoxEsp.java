package com.dsp.main.Functions.Render;

import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.Mode;
import com.dsp.main.UI.Themes.ThemesUtil;
import com.dsp.main.Utils.Render.DrawHelper;
import com.dsp.main.Utils.Render.Other.ESPUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.joml.Vector3d;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.dsp.main.Api.mc;

public class BoxEsp extends Module {

    public Mode boxStyle;
    public Mode healthBarPosition;

    private final Map<String, Float> healthAnimations = new HashMap<>();
    private final Map<String, Long> lastUpdateTimes = new HashMap<>();

    public BoxEsp() {
        super("Esp", 0, Category.RENDER, "ESP with health bars");

        boxStyle = new Mode("BoxStyle", "Full", "Corners");
        healthBarPosition = new Mode("HP Position", "Left", "Right", "Top", "Bottom");

        addSettings(boxStyle, healthBarPosition);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        healthAnimations.clear();
        lastUpdateTimes.clear();
    }

    @SubscribeEvent
    public void onRenderGui(RenderGuiEvent.Post event) {
        if (mc == null || mc.level == null || mc.player == null) return;

        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);

        try {
            for (Player player : mc.level.players()) {
                if (player == null || player == mc.player || player.isInvisible()) continue;

                try {
                    renderPlayerBox(event, player, partialTick);
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
    }

    private void renderPlayerBox(RenderGuiEvent.Post event, Player player, float partialTick) {
        if (player == null || event == null) return;

        try {
            AABB box = getInterpolatedBoundingBox(player, partialTick);
            if (box == null) return;

            List<Vec3> corners = getBoxCorners(box);
            List<Vector3d> screenCorners = new ArrayList<>();

            boolean allVisible = true;
            for (Vec3 corner : corners) {
                if (corner == null) {
                    allVisible = false;
                    break;
                }
                Vector3d screen = ESPUtils.toScreen(corner);
                if (screen == null || screen.z == 0) {
                    allVisible = false;
                    break;
                }
                screenCorners.add(screen);
            }

            if (!allVisible || screenCorners.isEmpty()) return;

            double minX = screenCorners.stream().mapToDouble(v -> v.x).min().orElse(0);
            double minY = screenCorners.stream().mapToDouble(v -> v.y).min().orElse(0);
            double maxX = screenCorners.stream().mapToDouble(v -> v.x).max().orElse(0);
            double maxY = screenCorners.stream().mapToDouble(v -> v.y).max().orElse(0);

            if (minX >= maxX || minY >= maxY) return;

            float width = (float) (maxX - minX);
            float height = (float) (maxY - minY);
            float distance = mc.player.distanceTo(player);
            float scale = calculateScale(distance);

            int color1 = ThemesUtil.getCurrentStyle().getColorLowSpeed(1);
            int color2 = ThemesUtil.getCurrentStyle().getColorLowSpeed(2);
            int color3 = ThemesUtil.getCurrentStyle().getColorLowSpeed(3);
            int color4 = ThemesUtil.getCurrentStyle().getColorLowSpeed(4);

            switch (boxStyle.getMode()) {
                case "Full" -> {
                    drawFullBox(event, (float) minX, (float) minY, width, height, color1, color2, color3, color4, scale);
                }
                case "Corners" -> {

                    drawCornersBox(event, (float) minX, (float) minY, width, height, color1, color2, color3, color4, scale);
                }
            }

            drawHealthBar(event, player, (float) minX, (float) minY, width, height, color1, color4, scale);
        } catch (Exception ignored) {}
    }

    private AABB getInterpolatedBoundingBox(Player player, float partialTick) {
        if (player == null) return null;

        try {
            double x = player.xOld + (player.getX() - player.xOld) * partialTick;
            double y = player.yOld + (player.getY() - player.yOld) * partialTick;
            double z = player.zOld + (player.getZ() - player.zOld) * partialTick;

            AABB box = player.getBoundingBox();
            double halfWidth = box.getXsize() / 2.0;
            double height = box.getYsize();

            return new AABB(
                    x - halfWidth, y, z - halfWidth,
                    x + halfWidth, y + height, z + halfWidth
            );
        } catch (Exception e) {
            return player.getBoundingBox();
        }
    }

    private float calculateScale(float distance) {
        float minScale = 0.3f;
        float maxScale = 1.0f;
        float minDistance = 3.0f;
        float maxDistance = 30.0f;

        if (distance <= minDistance) return maxScale;
        if (distance >= maxDistance) return minScale;

        float normalizedDistance = (distance - minDistance) / (maxDistance - minDistance);
        return maxScale - (normalizedDistance * (maxScale - minScale));
    }

    private void drawFullBox(RenderGuiEvent.Post event, float x, float y, float width, float height, int color1, int color2, int color3, int color4, float scale) {
        if (event == null) return;

        try {
            float lineWidth = Math.max(0.5f, 1.0f * scale);

            DrawHelper.drawMCHorizontalBuilding(event.getGuiGraphics().pose(), x, y, x + width, y + lineWidth, color1, color2);
            DrawHelper.drawMCHorizontalBuilding(event.getGuiGraphics().pose(), x, y + height - lineWidth, x + width, y + height, color3, color4);
            DrawHelper.drawMCVerticalBuilding(event.getGuiGraphics().pose(), x, y, x + lineWidth, y + height, color1, color3);
            DrawHelper.drawMCVerticalBuilding(event.getGuiGraphics().pose(), x + width - lineWidth, y, x + width, y + height, color2, color4);
        } catch (Exception ignored) {}
    }

    private void drawCornersBox(RenderGuiEvent.Post event, float x, float y, float width, float height, int color1, int color2, int color3, int color4, float scale) {
        if (event == null) return;

        try {
            float baseCornerSize = 20.0f;
            float cornerSize = baseCornerSize * scale;
            float maxCornerSize = Math.min(width, height) / 2f;
            float actualCornerSize = Math.min(cornerSize, maxCornerSize);
            float lineWidth = Math.max(0.5f, 1.0f * scale);

            DrawHelper.drawMCHorizontalBuilding(event.getGuiGraphics().pose(), x, y, x + actualCornerSize, y + lineWidth, color1, color2);
            DrawHelper.drawMCVerticalBuilding(event.getGuiGraphics().pose(), x, y, x + lineWidth, y + actualCornerSize, color1, color3);
            DrawHelper.drawMCHorizontalBuilding(event.getGuiGraphics().pose(), x + width - actualCornerSize, y, x + width, y + lineWidth, color2, color1);
            DrawHelper.drawMCVerticalBuilding(event.getGuiGraphics().pose(), x + width - lineWidth, y, x + width, y + actualCornerSize, color2, color4);
            DrawHelper.drawMCHorizontalBuilding(event.getGuiGraphics().pose(), x, y + height - lineWidth, x + actualCornerSize, y + height, color3, color4);
            DrawHelper.drawMCVerticalBuilding(event.getGuiGraphics().pose(), x, y + height - actualCornerSize, x + lineWidth, y + height, color3, color1);
            DrawHelper.drawMCHorizontalBuilding(event.getGuiGraphics().pose(), x + width - actualCornerSize, y + height - lineWidth, x + width, y + height, color4, color3);
            DrawHelper.drawMCVerticalBuilding(event.getGuiGraphics().pose(), x + width - lineWidth, y + height - actualCornerSize, x + width, y + height, color4, color2);
        } catch (Exception ignored) {}
    }

    private void drawHealthBar(RenderGuiEvent.Post event, Player player, float boxX, float boxY, float boxWidth, float boxHeight, int colorTop, int colorBottom, float scale) {
        if (event == null || player == null) return;

        try {
            float health = player.getHealth();
            float maxHealth = player.getMaxHealth();

            String playerId = player.getUUID().toString();
            float targetPercent = Mth.clamp(health / maxHealth, 0f, 1f);
            float currentPercent = animateSmooth(playerId, targetPercent);

            float basePadding = 2.0f;
            float baseWidth = 2.0f;
            float baseOffset = 3.0f;

            float padding = basePadding * scale;
            float hpWidth = Math.max(1.0f, baseWidth * scale);
            float offset = baseOffset * scale;

            float barX, barY, barWidth, barHeight;
            boolean isVertical = true;

            switch (healthBarPosition.getMode()) {
                case "Left" -> {
                    barX = boxX - hpWidth - padding - offset;
                    barY = boxY;
                    barWidth = hpWidth;
                    barHeight = boxHeight;
                }
                case "Right" -> {
                    barX = boxX + boxWidth + padding + offset;
                    barY = boxY;
                    barWidth = hpWidth;
                    barHeight = boxHeight;
                }
                case "Top" -> {
                    barX = boxX;
                    barY = boxY - hpWidth - padding - offset;
                    barWidth = boxWidth;
                    barHeight = hpWidth;
                    isVertical = false;
                }
                case "Bottom" -> {
                    barX = boxX;
                    barY = boxY + boxHeight + padding + offset;
                    barWidth = boxWidth;
                    barHeight = hpWidth;
                    isVertical = false;
                }
                default -> {
                    barX = boxX - hpWidth - padding - offset;
                    barY = boxY;
                    barWidth = hpWidth;
                    barHeight = boxHeight;
                }
            }

            int bgColor = new Color(0, 0, 0, 180).getRGB();

            DrawHelper.drawRectBuilding(event.getGuiGraphics().pose(),
                    barX, barY,
                    barX + barWidth, barY + barHeight,
                    bgColor);

            if (isVertical) {
                float healthHeight = barHeight * currentPercent;
                float healthStartY = barY + barHeight - healthHeight;

                DrawHelper.drawMCVerticalBuilding(event.getGuiGraphics().pose(),
                        barX, healthStartY,
                        barX + barWidth, barY + barHeight,
                        colorBottom, colorTop);
            } else {
                float healthWidth = barWidth * currentPercent;

                DrawHelper.drawMCHorizontalBuilding(event.getGuiGraphics().pose(),
                        barX, barY,
                        barX + healthWidth, barY + barHeight,
                        colorTop, colorBottom);
            }
        } catch (Exception ignored) {}
    }

    private float animateSmooth(String key, float target) {
        try {
            long currentTime = System.currentTimeMillis();
            long lastTime = lastUpdateTimes.getOrDefault(key, currentTime);
            float deltaTime = Math.min((currentTime - lastTime) / 1000.0f, 0.1f);
            lastUpdateTimes.put(key, currentTime);

            float current = healthAnimations.getOrDefault(key, target);
            float diff = target - current;

            if (Math.abs(diff) < 0.001f) {
                healthAnimations.put(key, target);
                return target;
            }

            float smoothness = 0.15f;
            float change = diff * smoothness * deltaTime * 60f;
            current += change;
            healthAnimations.put(key, current);
            return Mth.clamp(current, 0f, 1f);
        } catch (Exception e) {
            return target;
        }
    }

    private List<Vec3> getBoxCorners(AABB box) {
        List<Vec3> corners = new ArrayList<>();
        if (box == null) return corners;

        try {
            corners.add(new Vec3(box.minX, box.minY, box.minZ));
            corners.add(new Vec3(box.minX, box.minY, box.maxZ));
            corners.add(new Vec3(box.maxX, box.minY, box.minZ));
            corners.add(new Vec3(box.maxX, box.minY, box.maxZ));
            corners.add(new Vec3(box.minX, box.maxY, box.minZ));
            corners.add(new Vec3(box.minX, box.maxY, box.maxZ));
            corners.add(new Vec3(box.maxX, box.maxY, box.minZ));
            corners.add(new Vec3(box.maxX, box.maxY, box.maxZ));
        } catch (Exception ignored) {}

        return corners;
    }
}