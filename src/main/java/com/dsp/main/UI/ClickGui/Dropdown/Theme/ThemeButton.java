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

public class ThemeButton {
    private final Theme theme;
    private int x;
    private int y;
    private int width;
    private int height;
    private final float scaleFactor;
    private final ThemesFrame parent;

    public ThemeButton(Theme theme, int x, int y, int width, int height, ThemesFrame parent, float scaleFactor) {
        this.theme = theme;
        this.x = (int) (x * scaleFactor);
        this.y = (int) (y * scaleFactor);
        this.width = (int) (width * scaleFactor);
        this.height = (int) (height * scaleFactor);
        this.scaleFactor = scaleFactor;
        this.parent = parent;
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        DrawShader.drawRoundBlur(graphics.pose(), x, y, width, height, 4 * scaleFactor, new Color(18, 30, 60, 200).hashCode(), 90, 0.7f);
        String label = theme.getName();
        int textX = (int) (x + 5 * scaleFactor);
        int textY = (int) (y + (height - BIKO_FONT.get().getMetrics().lineHeight() * 9f * scaleFactor) / 2);
        BuiltText text = Builder.text()
                .font(BIKO_FONT.get())
                .text(label)
                .color(Color.WHITE.getRGB())
                .size(9f * scaleFactor)
                .thickness(0.05f)
                .build();
        text.render(new Matrix4f(), textX, textY - 1);
        int colorRectHeight = (int) (4 * scaleFactor);
        int colorRectY = (int) (y + height - colorRectHeight - 2 * scaleFactor);
        DrawHelper.rectRGB(graphics.pose(), x + 2, colorRectY, width - 4, colorRectHeight, 2 * scaleFactor,
                theme.getColor(0), theme.getColor(25), theme.getColor(50), theme.getColor(75));
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
        this.x = (int) (x * scaleFactor);
        this.y = (int) (y * scaleFactor);
    }

    public void setWidth(int w) {
        this.width = (int) (w * scaleFactor);
    }

    public int getHeight() {
        return height;
    }
}