package com.dsp.main.ClickGui.Components;

import com.dsp.main.ClickGui.Button;
import com.dsp.main.ClickGui.Settings.CheckBox;
import com.dsp.main.ClickGui.Settings.Setting;
import com.dsp.main.Utils.Font.FontRenderers;
import com.dsp.main.Utils.Render.DrawHelper;
import com.mojang.blaze3d.vertex.PoseStack;

import java.awt.*;

import static com.dsp.main.Api.mc;

public class CheckBoxComponent extends Component {
    private final CheckBox booleanSetting;
    private float hoverAnimation = 0;
    private float toggleAnimation = 0;

    public CheckBoxComponent(Setting setting, Button parent) {
        super(setting, parent);
        this.booleanSetting = (CheckBox) setting;
    }

    @Override
    public void draw(PoseStack m, int mouseX, int mouseY) {
        int textShifting = parent.parent.getHeight() / 2 - mc.font.lineHeight;
        int maxBoxSize = 10;

        boolean isHovered = isHovered(mouseX, mouseY);

        hoverAnimation += isHovered ? (1 - hoverAnimation) * 0.1f : (0 - hoverAnimation) * 0.1f;
        if (booleanSetting.isEnabled()) {
            toggleAnimation += (1 - toggleAnimation) * 0.08f;
        } else {
            toggleAnimation += (0 - toggleAnimation) * 0.08f;
        }

        int hoverColorShift = (int) (hoverAnimation * 50);
        Color backgroundColor = new Color(
                30 + hoverColorShift,
                30 + hoverColorShift,
                30 + hoverColorShift,
                200
        );

        DrawHelper.rectangle(new PoseStack(),
                parent.parent.getX() + 2,
                parent.parent.getY() + parent.getY() + 16,
                parent.parent.getWidth() - 4,
                parent.parent.getHeight() - 1,
                5,
                backgroundColor.hashCode()
        );

        int boxX = parent.parent.getX() + textShifting + 3;
        int boxY = parent.parent.getY() + parent.getY() + textShifting + 4;

        int currentBoxSize = (int) (maxBoxSize * toggleAnimation);
        int offsetX = (maxBoxSize - currentBoxSize) / 2;
        int offsetY = (maxBoxSize - currentBoxSize) / 2;

        DrawHelper.rectangle(new PoseStack(), boxX + 2, boxY + 11, maxBoxSize + 1, maxBoxSize + 1, 2, new Color(88, 88, 88).hashCode());
        if (toggleAnimation > 0) {
            DrawHelper.rectangle(new PoseStack(), boxX + offsetX + 3, boxY + offsetY + 10, currentBoxSize, currentBoxSize, 2, new Color(69, 239, 0).hashCode());
        }
        FontRenderers.umbrellatext15.drawString(m, booleanSetting.getName(), boxX + maxBoxSize + 4, boxY + 11, Color.WHITE);

        super.draw(m, mouseX, mouseY);
    }

    @Override
    public void mouseClicked(int mx, int my, int mb) {
        if (isHovered(mx, my)) {
            booleanSetting.toggle();
        }
        super.mouseClicked(mx, my, mb);
    }
}