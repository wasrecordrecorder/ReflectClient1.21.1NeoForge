package com.dsp.main.UI.ClickGui.Dropdown.Theme;

import com.dsp.main.UI.Themes.Theme;
import com.dsp.main.UI.Themes.ThemesUtil;
import com.dsp.main.Utils.Font.builders.Builder;
import com.dsp.main.Utils.Font.renderers.impl.BuiltText;
import com.dsp.main.Utils.Render.Blur.DrawShader;
import com.dsp.main.Utils.Render.DrawHelper;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix4f;

import java.awt.Color;

import static com.dsp.main.Main.BIKO_FONT;
import static com.dsp.main.Main.ICONS;

public class ThemeButton {
    private final Theme theme;
    private int x;
    private int y;
    private int width;
    private int height;
    private final float scaleFactor;
    private final ThemesFrame parent;
    private float hoverAnimation = 0f;
    private int alpha = 255;

    public ThemeButton(Theme theme, int x, int y, int width, int height, ThemesFrame parent, float scaleFactor) {
        this.theme = theme;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.scaleFactor = scaleFactor;
        this.parent = parent;
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        boolean isHovered = isHovered(mouseX, mouseY);
        boolean isSelected = ThemesUtil.getCurrentStyle().getName().equals(theme.getName());

        hoverAnimation += (isHovered ? 0.1f : -0.1f);
        hoverAnimation = Math.max(0f, Math.min(1f, hoverAnimation));

        Color baseBg = new Color(20, 35, 65, Math.min(alpha, 220));
        Color hoverBg = new Color(30, 45, 80, Math.min(alpha, 235));
        Color selectedBg = new Color(35, 55, 95, Math.min(alpha, 250));

        Color bgColor = isSelected ? selectedBg : interpolateColor(baseBg, hoverBg, hoverAnimation);

        DrawShader.drawRoundBlur(graphics.pose(), x, y, width, height,
                5 * scaleFactor, bgColor.getRGB(), 90, 0.8f);

        if (isSelected) {
            DrawHelper.rectangle(graphics.pose(), x, y,
                    (int) (3 * scaleFactor), height,
                    5 * scaleFactor,
                    ThemesUtil.getCurrentStyle().getColor(50));
        }

        String label = theme.getName();
        int textX = x + (int) (8 * scaleFactor);
        int textY = y + (int) ((height - BIKO_FONT.get().getMetrics().lineHeight() * 9f * scaleFactor) / 2);

        BuiltText text = Builder.text()
                .font(BIKO_FONT.get())
                .text(label)
                .color(new Color(255, 255, 255, alpha).getRGB())
                .size(9f * scaleFactor)
                .thickness(isSelected ? 0.07f : 0.05f)
                .build();
        text.render(new Matrix4f(), textX, textY);

        if (isSelected) {
            BuiltText checkIcon = Builder.text()
                    .font(ICONS.get())
                    .text("R")
                    .color(new Color(100, 255, 100, alpha).getRGB())
                    .size(10f * scaleFactor)
                    .thickness(0.05f)
                    .build();
            checkIcon.render(new Matrix4f(),
                    x + width - (int) (15 * scaleFactor),
                    textY - (int) (1 * scaleFactor));
        }

        int colorRectHeight = (int) (3 * scaleFactor);
        int colorRectY = y + height - colorRectHeight - (int) (2 * scaleFactor);

        DrawHelper.rectRGB(graphics.pose(),
                x + (int) (4 * scaleFactor),
                colorRectY,
                width - (int) (8 * scaleFactor),
                colorRectHeight,
                2 * scaleFactor,
                applyAlpha(theme.getColor(0), alpha),
                applyAlpha(theme.getColor(25), alpha),
                applyAlpha(theme.getColor(50), alpha),
                applyAlpha(theme.getColor(75), alpha));
    }

    public boolean mouseClicked(double mx, double my, int button) {
        if (isHovered(mx, my) && button == 0) {
            ThemesUtil themesUtil = new ThemesUtil();
            themesUtil.init();
            themesUtil.setCurrentThemeByName(theme.getName());
            return true;
        }
        return false;
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
    }

    public boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setWidth(int w) {
        this.width = w;
    }

    public void setAlpha(int alpha) {
        this.alpha = Math.max(0, Math.min(255, alpha));
    }

    public int getHeight() {
        return height;
    }

    private Color interpolateColor(Color c1, Color c2, float factor) {
        int r = (int) (c1.getRed() + (c2.getRed() - c1.getRed()) * factor);
        int g = (int) (c1.getGreen() + (c2.getGreen() - c1.getGreen()) * factor);
        int b = (int) (c1.getBlue() + (c2.getBlue() - c1.getBlue()) * factor);
        int a = (int) (c1.getAlpha() + (c2.getAlpha() - c1.getAlpha()) * factor);
        return new Color(r, g, b, a);
    }

    private int applyAlpha(int color, int alpha) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        return (alpha << 24) | (r << 16) | (g << 8) | b;
    }
}