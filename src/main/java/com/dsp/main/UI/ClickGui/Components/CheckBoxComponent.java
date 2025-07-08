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

    public CheckBoxComponent(CheckBox setting, Button parent) {
        super(setting, parent);
        this.checkBoxSetting = setting;
        this.animationProgress = setting.isEnabled() ? 1.0f : 0.0f;
    }
    @Override
    public float getHeight() {
        float textHeight = BIKO_FONT.get().getMetrics().lineHeight() * 8.5f + 2;
        float iconHeight = BIKO_FONT.get().getMetrics().lineHeight() * 10.5f + 2;
        return Math.max(textHeight, iconHeight) + 4;
    }

    @Override
    public void draw(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!isVisible()) return;

        PoseStack poseStack = graphics.pose();
        float targetProgress = checkBoxSetting.isEnabled() ? 1.0f : 0.0f;
        animationProgress = lerp(animationProgress, targetProgress, ANIMATION_SPEED);
        float boxWidth = BIKO_FONT.get().getWidth(checkBoxSetting.getName(), 8.5f) + 5;
        float boxHeight = mc.font.lineHeight + 1;
        DrawShader.drawRoundBlur(poseStack, (float) (x + 4), (float) (y + 3), boxWidth, boxHeight, ROUNDING, new Color(29, 29, 31).hashCode(), 90, 0.7f);
        BuiltText nameText = Builder.text()
                .font(BIKO_FONT.get())
                .text(checkBoxSetting.getName())
                .color(new Color(160, 163, 175))
                .size(8.5f)
                .thickness(0.05f)
                .build();
        nameText.render(new Matrix4f(), (int) x + 5, (int) y + 4);


        BuiltText KnobRender = Builder.text()
                .font(ICONS.get())
                .text(checkBoxSetting.isEnabled() ? "R" : "S")
                .color(new Color(160, 163, 175))
                .size(10.5f)
                .thickness(0.05f)
                .build();
        KnobRender.render(new Matrix4f(), (float) ((parent.getX() + parent.getWidth()) - 19), (int) y + 4);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (!isVisible() || button != 0) return;

        float boxWidth = 15;
        float boxHeight = 7;
        float boxX = (float) (x + parent.getWidth() - boxWidth - 7);
        float boxY = (float) (y + parent.getHeight() / 2.0 - boxHeight / 2.0);

        if (mouseX >= boxX && mouseX <= boxX + boxWidth && mouseY >= boxY && mouseY <= boxY + boxHeight) {
            checkBoxSetting.toggle();
        }
    }

    @Override
    public boolean isHovered(double mouseX, double mouseY) {
        if (!isVisible()) return false;

        float boxWidth = 15;
        float boxHeight = 7;
        float boxX = (float) (x + parent.getWidth() - boxWidth - 7);
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