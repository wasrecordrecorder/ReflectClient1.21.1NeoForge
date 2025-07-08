package com.dsp.main.UI.ClickGui.Components;

import com.dsp.main.UI.ClickGui.Button;
import com.dsp.main.UI.ClickGui.Settings.BindCheckBox;
import com.dsp.main.UI.ClickGui.Settings.Setting;
import com.dsp.main.Utils.Render.DrawHelper;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

import static com.dsp.main.Api.mc;
import static com.dsp.main.Utils.KeyName.getKeyName;

public class BindCheckBoxComponent extends Component {
    private final BindCheckBox bindSetting;
    private boolean binding;
    private float hoverAnimation = 0;
    private float toggleAnimation = 0;

    public BindCheckBoxComponent(Setting setting, Button parent) {
        super(setting, parent);
        this.bindSetting = (BindCheckBox) setting;
    }

    @Override
    public void draw(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!setting.isVisible()) return;

        int compX = (int) this.x;
        int compY = (int) this.y;
        int width = parent.getWidth();
        int height = parent.getHeight();

        boolean isHovered = isHovered(mouseX, mouseY);
        hoverAnimation += isHovered ? (1 - hoverAnimation) * 0.1f : (0 - hoverAnimation) * 0.1f;
        toggleAnimation += bindSetting.isEnabled() ? (1 - toggleAnimation) * 0.08f : (0 - toggleAnimation) * 0.08f;

        int hoverShift = (int) (hoverAnimation * 50);
        Color bgColor = new Color(20 + hoverShift, 30 + hoverShift, 50 + hoverShift);
        DrawHelper.rectangle(graphics.pose(), compX, compY, width, height, 4, bgColor.hashCode());

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
        graphics.drawString(mc.font, bindSetting.getName(), textX, textY, Color.WHITE.getRGB());

        String keyText = getString();
        int keyWidth = mc.font.width(keyText);
        int bindX = compX + width - keyWidth - 10;

        if (bindSetting.getBindKey() <= 7 && bindSetting.getBindKey() != 0) {
            DrawHelper.rectangle(graphics.pose(), bindX - 5, textY, keyWidth + 10, mc.font.lineHeight + 2, 2, new Color(99, 99, 99).hashCode());
            graphics.drawString(mc.font, keyText.toUpperCase(), bindX, textY, Color.WHITE.getRGB());
        } else {
            DrawHelper.rectangle(graphics.pose(), bindX - 3, textY, keyWidth + 6, mc.font.lineHeight + 2, 2, new Color(99, 99, 99).hashCode());
            graphics.drawString(mc.font, keyText.toUpperCase(), bindX, textY, Color.WHITE.getRGB());
        }
    }

    private String getString() {
        if (bindSetting.getBindKey() == 0) return "-";
        if (bindSetting.getBindKey() == 1) return "RMB";
        if (bindSetting.getBindKey() == 2) return "MMB";
        if (bindSetting.getBindKey() == 3) return "MB4";
        if (bindSetting.getBindKey() == 4) return "MB5";
        if (bindSetting.getBindKey() == 5) return "MB6";
        return binding ? "..." : getKeyName(bindSetting.getBindKey());
    }

    @Override
    public void mouseClicked(int mx, int my, int mb) {
        if (!setting.isVisible()) return;

        if (isHovered(mx, my)) {
            if (mb == 0 && !binding) {
                bindSetting.toggle();
            } else if (mb == 2) {
                binding = true;
                return;
            }
        }

        if (binding && mb != 0) {
            bindSetting.setBindKey(GLFW.GLFW_MOUSE_BUTTON_1 + mb);
            binding = false;
        }
    }

    public void handleKeyPress(int keyCode) {
        if (binding) {
            bindSetting.setBindKey(keyCode);
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_DELETE) {
                bindSetting.setBindKey(0);
            }
            binding = false;
        }
    }

    @Override
    public boolean isHovered(double mx, double my) {
        return mx > x && mx < x + parent.getWidth() &&
                my > y && my < y + parent.getHeight();
    }

    @Override
    public void setY(double y) {
        this.y = y;
    }

    @Override
    public void setX(double x) {
        this.x = x;
    }
}