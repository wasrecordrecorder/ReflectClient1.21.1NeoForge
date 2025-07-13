package com.dsp.main.UI.ClickGui.Components;

import com.dsp.main.UI.ClickGui.Button;
import com.dsp.main.UI.ClickGui.Settings.CheckBox;
import com.dsp.main.Utils.Font.builders.Builder;
import com.dsp.main.Utils.Font.renderers.impl.BuiltText;
import com.dsp.main.Utils.Render.Blur.DrawShader;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix4f;

import java.awt.Color;

import static com.dsp.main.Api.mc;
import static com.dsp.main.Main.BIKO_FONT;
import static com.dsp.main.Main.ICONS;

public class CheckBoxComponent extends Component {
    private static final float ANIMATION_SPEED = 0.2f;
    private static final float ROUNDING = 3.0f;
    private float animationProgress = 0.0f;
    private final CheckBox checkBoxSetting;

    public CheckBoxComponent(CheckBox setting, Button parent, float scaleFactor) {
        super(setting, parent, scaleFactor);
        this.checkBoxSetting = setting;
        this.animationProgress = setting.isEnabled() ? 1.0f : 0.0f;
    }

    @Override
    public float getHeight() {
        float textHeight = BIKO_FONT.get().getMetrics().lineHeight() * 8.5f * scaleFactor + 2 * scaleFactor;
        float iconHeight = BIKO_FONT.get().getMetrics().lineHeight() * 10.5f * scaleFactor + 2 * scaleFactor;
        return Math.max(textHeight, iconHeight) + 4 * scaleFactor;
    }

    @Override
    public void draw(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!isVisible()) return;

        PoseStack poseStack = graphics.pose();
        float targetProgress = checkBoxSetting.isEnabled() ? 1.0f : 0.0f;
        animationProgress = lerp(animationProgress, targetProgress, ANIMATION_SPEED);
        float boxWidth = BIKO_FONT.get().getWidth(checkBoxSetting.getName(), 8.5f * scaleFactor) + 5 * scaleFactor;
        float boxHeight = mc.font.lineHeight * scaleFactor + 1 * scaleFactor;
        DrawShader.drawRoundBlur(poseStack, (float) (x + 4 * scaleFactor), (float) (y + 3 * scaleFactor), boxWidth, boxHeight, ROUNDING * scaleFactor, new Color(29, 29, 31).hashCode(), 90, 0.7f);
        BuiltText nameText = Builder.text()
                .font(BIKO_FONT.get())
                .text(checkBoxSetting.getName())
                .color(new Color(160, 163, 175))
                .size(8.5f * scaleFactor)
                .thickness(0.05f)
                .build();
        nameText.render(new Matrix4f(), (int) (x + 5 * scaleFactor), (int) (y + 4 * scaleFactor));

        BuiltText KnobRender = Builder.text()
                .font(ICONS.get())
                .text(checkBoxSetting.isEnabled() ? "R" : "S")
                .color(new Color(160, 163, 175))
                .size(10.5f * scaleFactor)
                .thickness(0.05f)
                .build();
        KnobRender.render(new Matrix4f(), (float) (parent.getX() + parent.getWidth() - 19 * scaleFactor), (int) (y + 4 * scaleFactor));
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (!isVisible() || button != 0) return;

        float boxWidth = 15 * scaleFactor;
        float boxHeight = 7 * scaleFactor;
        float boxX = (float) (x + parent.getWidth() - boxWidth - 7 * scaleFactor);
        float boxY = (float) (y + parent.getHeight() / 2.0 - boxHeight / 2.0);

        if (mouseX >= boxX && mouseX <= boxX + boxWidth && mouseY >= boxY && mouseY <= boxY + boxHeight) {
            checkBoxSetting.toggle();
        }
    }

    @Override
    public boolean isHovered(double mouseX, double mouseY) {
        if (!isVisible()) return false;

        float boxWidth = 15 * scaleFactor;
        float boxHeight = 7 * scaleFactor;
        float boxX = (float) (x + parent.getWidth() - boxWidth - 7 * scaleFactor);
        float boxY = (float) (y + parent.getHeight() / 2.0 - boxHeight / 2.0);

        return mouseX >= boxX && mouseX <= boxX + boxWidth && mouseY >= boxY && mouseY <= boxY + boxHeight;
    }

    private float lerp(float start, float end, float t) {
        return start + t * (end - start);
    }

    private int interpolateColor(Color start, Color end, float fraction) {
        int r = (int) (start.getRed() + (end.getRed() - start.getRed()) * fraction);
        int g = (int) (start.getGreen() + (end.getGreen() - start.getGreen()) * fraction);
        int b = (int) (start.getBlue() + (end.getBlue() - start.getBlue()) * fraction);
        int a = (int) (start.getAlpha() + (end.getAlpha() - start.getAlpha()) * fraction);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}