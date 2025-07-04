package com.dsp.main.UI.ClickGui.Components;

import com.dsp.main.UI.ClickGui.Button;
import com.dsp.main.UI.ClickGui.Settings.Setting;
import com.dsp.main.UI.ClickGui.Settings.Slider;
import com.dsp.main.Utils.Font.FontRenderers;
import com.dsp.main.Utils.Render.DrawHelper;
import com.mojang.blaze3d.vertex.PoseStack;

import java.awt.*;

import static com.dsp.main.Api.mc;

public class SliderComponent extends Component {
    private boolean sliding;
    private final Slider numberSetting;
    private double renderWidth;

    public SliderComponent(Setting setting, Button parent, double defaultValue) {
        super(setting, parent);
        this.numberSetting = (Slider) setting;
        this.numberSetting.setValue(defaultValue);
        this.renderWidth = 0;
    }

    @Override
    public void draw(PoseStack poseStack, int mouseX, int mouseY) {
        if (!setting.isVisible()) return;

        double min = numberSetting.getMin();
        double max = numberSetting.getMax();
        double value = numberSetting.getValue();

        int compX = (int) this.x;
        int compY = (int) this.y;
        int width = parent.getWidth();
        int height = parent.getHeight();

        // Относительная позиция мыши в пределах компонента
        double relativeMouseX = mouseX - compX;
        double diff = Math.min(width, Math.max(0, relativeMouseX));

        // Целевой размер полосы слайдера
        double targetWidth = width * (value - min) / (max - min);
        // Плавная анимация ширины полосы
        renderWidth += (targetWidth - renderWidth) * 0.1;

        // Фон слайдера (темно-серый полупрозрачный)
        Color backgroundColor = new Color(30, 30, 30, 200);
        DrawHelper.rectangle(poseStack, compX + 2, compY + 16, width - 4, height - 1, 5, backgroundColor.hashCode());

        // Полная линия слайдера (фон)
        int lineY = compY + height / 2 + 4; // чуть ниже центра
        DrawHelper.rectangle(poseStack, compX + 6, lineY, width - 12, 4, 2, new Color(60, 60, 60).hashCode());

        // Активная часть линии (заполненный цвет)
        int fillWidth = (int) Math.max(4, renderWidth - 12);
        DrawHelper.rectangle(poseStack, compX + 6, lineY, fillWidth, 4, 2, new Color(69, 239, 0).hashCode());

        // Ползунок (замена круга на скруглённый прямоугольник)
        int knobX = compX + 6 + fillWidth - 6; // подвинуть левее, чтобы центр совпадал
        int knobY = lineY - 3; // немного выше линии
        int knobSize = 12;
        DrawHelper.rectangle(poseStack, knobX, knobY, knobSize, knobSize, 8, new Color(88, 88, 88).hashCode());
        DrawHelper.rectangle(poseStack, knobX + 3, knobY + 3, knobSize - 6, knobSize - 6, 8, new Color(69, 239, 0).hashCode());

        // Обновление значения при слайдинге мышью
        if (sliding) {
            if (diff == 0) {
                numberSetting.setValue(min);
            } else {
                double newValue = ((diff / width) * (max - min) + min);
                // Округляем до 2 знаков после запятой
                numberSetting.setValue(Math.round(newValue * 100.0) / 100.0);
            }
        }

        FontRenderers.umbrellatext15.drawString(poseStack, numberSetting.getName() + ": ",
                compX + 8, compY + 4, Color.WHITE);

        String valueText = String.format("%.2f", numberSetting.getValue());
        FontRenderers.umbrellatext15.drawString(poseStack, valueText,
                compX + width - mc.font.width(valueText) - 8, compY + 4, Color.WHITE);

        super.draw(poseStack, mouseX, mouseY);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (isHovered(mouseX, mouseY) && button == 0) {
            sliding = true;
        }
        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isHovered(double mouseX, double mouseY) {
        int compX = (int) this.x;
        int compY = (int) this.y;
        int width = parent.getWidth();
        int height = parent.getHeight();

        return mouseX > compX && mouseX < compX + width
                && mouseY > compY && mouseY < compY + height;
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        sliding = false;
        super.mouseReleased(mouseX, mouseY, button);
    }
}
