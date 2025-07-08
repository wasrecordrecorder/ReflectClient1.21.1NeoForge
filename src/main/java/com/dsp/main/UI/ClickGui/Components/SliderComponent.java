package com.dsp.main.UI.ClickGui.Components;

import com.dsp.main.UI.ClickGui.Button;
import com.dsp.main.UI.ClickGui.Settings.Slider;
import com.dsp.main.Utils.Font.builders.Builder;
import com.dsp.main.Utils.Font.renderers.impl.BuiltText;
import com.dsp.main.Utils.Render.Blur.DrawShader;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix4f;

import java.awt.Color;

import static com.dsp.main.Main.BIKO_FONT;

public class SliderComponent extends Component {
    private static final float ROUNDING = 0.6f;
    private static final float ANIMATION_SPEED = 0.1f;
    private boolean dragging = false;
    private float animationProgress = 0.0f;
    private final Slider sliderSetting;

    public SliderComponent(Slider setting, Button parent, float defaultValue) {
        super(setting, parent);
        this.sliderSetting = setting;
        this.animationProgress = (float) ((defaultValue - setting.getMin()) / (setting.getMax() - setting.getMin()));
    }
    @Override
    public float getHeight() {
        float textHeight = BIKO_FONT.get().getMetrics().lineHeight() * 7f;
        float sliderHeight = 6;
        return textHeight + sliderHeight;
    }

    @Override
    public void draw(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!isVisible()) return;

        PoseStack poseStack = graphics.pose();

        // Draw setting name
        BuiltText nameText = Builder.text()
                .font(BIKO_FONT.get())
                .text(sliderSetting.getName())
                .color(new Color(160, 163, 175))
                .size(7f)
                .thickness(0.05f)
                .build();
        nameText.render(new Matrix4f(), (int) x + 5 , (int) y + 3);

        // Draw value
        String valueStr = String.format("%.2f", sliderSetting.getValue());
        float valueWidth = BIKO_FONT.get().getWidth(valueStr, 7f);
        BuiltText valueText = Builder.text()
                .font(BIKO_FONT.get())
                .text(valueStr)
                .color(new Color(160, 163, 175))
                .size(7f)
                .thickness(0.05f)
                .build();
        valueText.render(new Matrix4f(), (int) (x + parent.getWidth() - 10 - valueWidth), (int) y + 3);

        // Update animation
        float targetProgress = (float) ((sliderSetting.getValue() - sliderSetting.getMin()) / (sliderSetting.getMax() - sliderSetting.getMin()));
        animationProgress = lerp(animationProgress, targetProgress, ANIMATION_SPEED);

        // Draw slider background
        float sliderWidth = (float) (parent.getWidth() - 15);
        DrawShader.drawRoundBlur(poseStack, (float) x + 5, (float) y + 11, sliderWidth, 2, ROUNDING, new Color(28, 28, 31).hashCode(), 90, 0.7f);

        // Draw filled portion
        float fillWidth = sliderWidth * animationProgress;
        DrawShader.drawRoundBlur(poseStack, (float) x + 5, (float) y + 11, fillWidth, 2, ROUNDING, new Color(128, 132, 150).hashCode(), 90, 0.7f);


        // Handle dragging
        if (dragging) {
            float progress = (float) Math.max(0, Math.min(1, (mouseX - x - 5) / sliderWidth));
            float newValue = (float) (sliderSetting.getMin() + progress * (sliderSetting.getMax() - sliderSetting.getMin()));
            sliderSetting.setValue((float) (Math.round(newValue / sliderSetting.getInc()) * sliderSetting.getInc()));
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (!isVisible() || button != 0) return;

        // Check if mouse is over the slider bar
        if (mouseX >= x + 5 && mouseX <= x + parent.getWidth() - 5 && mouseY >= y + 10 && mouseY <= y + 13) {
            dragging = true;
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
    }

    @Override
    public boolean isHovered(double mouseX, double mouseY) {
        if (!isVisible()) return false;

        return mouseX >= x + 5 && mouseX <= x + parent.getWidth() - 5 && mouseY >= y + 10 && mouseY <= y + 13;
    }

    private float lerp(float start, float end, float t) {
        return start + t * (end - start);
    }
}