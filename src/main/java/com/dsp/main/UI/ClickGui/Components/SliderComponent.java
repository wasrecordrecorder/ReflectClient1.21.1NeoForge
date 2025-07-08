package com.dsp.main.UI.ClickGui.Components;

import com.dsp.main.UI.ClickGui.Button;
import com.dsp.main.UI.ClickGui.Settings.Setting;
import com.dsp.main.UI.ClickGui.Settings.Slider;
import com.dsp.main.Utils.Render.DrawHelper;
import net.minecraft.client.gui.GuiGraphics;

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
    public void draw(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!setting.isVisible()) return;

        double min = numberSetting.getMin();
        double max = numberSetting.getMax();
        double value = numberSetting.getValue();

        int compX = (int) this.x;
        int compY = (int) this.y;
        int width = parent.getWidth();
        int height = parent.getHeight();

        double relativeMouseX = mouseX - compX;
        double diff = Math.min(width, Math.max(0, relativeMouseX));

        double targetWidth = width * (value - min) / (max - min);
        renderWidth += (targetWidth - renderWidth) * 0.1;

        // Draw background
        Color backgroundColor = new Color(20, 30, 50);
        DrawHelper.rectangle(graphics.pose(), compX, compY, width, height, 4, backgroundColor.hashCode());

        // Draw slider elements
        int lineY = compY + 12;
        DrawHelper.rectangle(graphics.pose(), compX + 6, lineY, width - 12, 4, 2, new Color(60, 60, 60).hashCode());
        int fillWidth = (int) Math.max(4, renderWidth - 12);
        DrawHelper.rectangle(graphics.pose(), compX + 6, lineY, fillWidth, 4, 2, new Color(69, 239, 0).hashCode());

        int knobX = compX + 6 + fillWidth - 6;
        int knobY = lineY - 3;
        int knobSize = 12;
        DrawHelper.rectangle(graphics.pose(), knobX, knobY, knobSize, knobSize, 8, new Color(88, 88, 88).hashCode());
        DrawHelper.rectangle(graphics.pose(), knobX + 3, knobY + 3, knobSize - 6, knobSize - 6, 8, new Color(69, 239, 0).hashCode());
        if (sliding) {
            if (diff == 0) {
                numberSetting.setValue(min);
            } else {
                double newValue = ((diff / width) * (max - min) + min);
                numberSetting.setValue(Math.round(newValue * 100.0) / 100.0);
            }
        }

        // Draw text with default Minecraft font
        graphics.drawString(mc.font, numberSetting.getName() + ": ", compX + 5, compY + 2, Color.WHITE.getRGB());
        String valueText = String.format("%.2f", numberSetting.getValue());
        int valueWidth = mc.font.width(valueText);
        graphics.drawString(mc.font, valueText, compX + width - valueWidth - 5, compY + 2, Color.WHITE.getRGB());

        super.draw(graphics, mouseX, mouseY);
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