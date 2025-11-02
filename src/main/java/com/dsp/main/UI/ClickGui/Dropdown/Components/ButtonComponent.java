package com.dsp.main.UI.ClickGui.Dropdown.Components;

import com.dsp.main.UI.ClickGui.Dropdown.Button;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.ButtonSetting;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.Setting;
import com.dsp.main.UI.Themes.ThemesUtil;
import com.dsp.main.Utils.Font.builders.Builder;
import com.dsp.main.Utils.Font.renderers.impl.BuiltText;
import com.dsp.main.Utils.Render.Blur.DrawShader;
import com.dsp.main.Utils.Render.DrawHelper;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix4f;

import java.awt.Color;

import static com.dsp.main.Main.BIKO_FONT;

public class ButtonComponent extends Component {
    private final ButtonSetting buttonSetting;
    private float hoverAnimation = 0;
    private float clickAnimation = 0;

    public ButtonComponent(Setting setting, Button parent, float scaleFactor) {
        super(setting, parent, scaleFactor);
        this.buttonSetting = (ButtonSetting) setting;
    }

    @Override
    public float getHeight() {
        return BIKO_FONT.get().getMetrics().lineHeight() * 8f * scaleFactor + 8 * scaleFactor;
    }

    @Override
    public void draw(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!setting.isVisible()) return;

        int compX = (int) this.x;
        int compY = (int) this.y;
        int width = (int) (parent.getWidth());

        boolean isHovered = isHovered(mouseX, mouseY);
        hoverAnimation += isHovered ? (1 - hoverAnimation) * 0.15f : (0 - hoverAnimation) * 0.15f;
        clickAnimation = Math.max(0, clickAnimation - 0.1f);

        int buttonX = (int) (compX + 5 * scaleFactor);
        int buttonY = (int) (compY + 2 * scaleFactor);
        int buttonWidth = (int) (width - 10 * scaleFactor);
        int buttonHeight = (int) getHeight() - (int) (4 * scaleFactor);

        int color1 = ThemesUtil.getCurrentStyle().getColorLowSpeed(1);
        int color2 = ThemesUtil.getCurrentStyle().getColorLowSpeed(2);

        int bgColor = new Color(29, 29, 31, (int) (180 + 75 * hoverAnimation)).getRGB();

        DrawShader.drawRoundBlur(
                graphics.pose(),
                (float) (buttonX - 3.5),
                buttonY,
                buttonWidth,
                buttonHeight,
                4 * scaleFactor,
                bgColor,
                90,
                0.3f + 0.3f * hoverAnimation
        );

        String text = buttonSetting.getButtonText();
        float textWidth = BIKO_FONT.get().getWidth(text, 8f * scaleFactor);
        int textX = (int) (buttonX + (buttonWidth - textWidth) / 2);
        int textY = (int) (buttonY + (buttonHeight - BIKO_FONT.get().getMetrics().lineHeight() * 8f * scaleFactor) / 2);

        BuiltText buttonText = Builder.text()
                .font(BIKO_FONT.get())
                .text(text)
                .color(Color.WHITE)
                .size(8f * scaleFactor)
                .thickness(0.05f)
                .build();
        buttonText.render(new Matrix4f(), textX - 3, textY + clickAnimation * 2);
    }

    @Override
    public void mouseClicked(int mx, int my, int mb) {
        if (!setting.isVisible()) return;

        if (isHovered(mx, my) && mb == 0) {
            buttonSetting.click();
            clickAnimation = 1.0f;
        }
    }

    @Override
    public boolean isHovered(double mx, double my) {
        if (!setting.isVisible()) return false;
        return mx > x + 5 * scaleFactor && mx < x + parent.getWidth() - 5 * scaleFactor
                && my > y + 2 * scaleFactor && my < y + getHeight() - 2 * scaleFactor;
    }

    private int interpolateColor(int color1, int color2, float factor) {
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;
        int a1 = (color1 >> 24) & 0xFF;

        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;
        int a2 = (color2 >> 24) & 0xFF;

        int r = (int) (r1 + (r2 - r1) * factor);
        int g = (int) (g1 + (g2 - g1) * factor);
        int b = (int) (b1 + (b2 - b1) * factor);
        int a = (int) (a1 + (a2 - a1) * factor);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}