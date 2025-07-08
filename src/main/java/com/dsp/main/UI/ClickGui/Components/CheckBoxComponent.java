package com.dsp.main.UI.ClickGui.Components;

import com.dsp.main.UI.ClickGui.Button;
import com.dsp.main.UI.ClickGui.Settings.CheckBox;
import com.dsp.main.UI.ClickGui.Settings.Setting;
import com.dsp.main.Utils.Render.DrawHelper;
import net.minecraft.client.gui.GuiGraphics;

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

    @ Override
    public void draw(GuiGraphics graphics, int mouseX, int mouseY) {
        int compX = (int) this.x;
        int compY = (int) this.y;
        int width = parent.getWidth();
        int height = parent.getHeight();

        boolean isHovered = isHovered(mouseX, mouseY);
        hoverAnimation += isHovered ? (1 - hoverAnimation) * 0.1f : (0 - hoverAnimation) * 0.1f;
        if (booleanSetting.isEnabled()) {
            toggleAnimation += (1 - toggleAnimation) * 0.08f;
        } else {
            toggleAnimation += (0 - toggleAnimation) * 0.08f;
        }

        int hoverColorShift = (int) (hoverAnimation * 50);
        Color backgroundColor = new Color(20 + hoverColorShift, 30 + hoverColorShift, 50 + hoverColorShift);
        DrawHelper.rectangle(graphics.pose(), compX, compY, width, height, 4, backgroundColor.hashCode());

        int boxSize = 10;
        int boxX = compX + 5;
        int boxY = compY + (height - boxSize) / 2;

        int currentBoxSize = (int) (boxSize * toggleAnimation);
        int offsetX = (boxSize - currentBoxSize) / 2;
        int offsetY = (boxSize - currentBoxSize) / 2;

        DrawHelper.rectangle(graphics.pose(), boxX, boxY, boxSize + 1, boxSize + 1, 2, new Color(88, 88, 88).hashCode());
        if (toggleAnimation > 0) {
            DrawHelper.rectangle(graphics.pose(), boxX + offsetX, boxY + offsetY, currentBoxSize, currentBoxSize, 2, new Color(69, 239, 0).hashCode());
        }

        int textX = boxX + boxSize + 5;
        int textY = compY + (height - mc.font.lineHeight) / 2;
        graphics.drawString(mc.font, booleanSetting.getName(), textX, textY, Color.WHITE.getRGB());

        super.draw(graphics, mouseX, mouseY);
    }

    @Override
    public void mouseClicked(int mx, int my, int mb) {
        if (isHovered(mx, my)) {
            booleanSetting.toggle();
        }
        super.mouseClicked(mx, my, mb);
    }
}