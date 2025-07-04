package com.dsp.main.UI.ClickGui.Components;

import com.dsp.main.UI.ClickGui.Button;
import com.dsp.main.UI.ClickGui.Settings.BindCheckBox;
import com.dsp.main.UI.ClickGui.Settings.Setting;
import com.dsp.main.Main;
import com.dsp.main.Utils.Font.FontRenderers;
import com.dsp.main.Utils.Render.DrawHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
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
    public void draw(PoseStack m, int mouseX, int mouseY) {
        if (!setting.isVisible()) return;

        int boxSize = 10;
        int textOffsetY = 4;

        boolean isHovered = isHovered(mouseX, mouseY);
        hoverAnimation += isHovered ? (1 - hoverAnimation) * 0.1f : (0 - hoverAnimation) * 0.1f;
        toggleAnimation += bindSetting.isEnabled() ? (1 - toggleAnimation) * 0.08f : (0 - toggleAnimation) * 0.08f;

        int hoverShift = (int) (hoverAnimation * 50);
        Color bgColor = new Color(30 + hoverShift, 30 + hoverShift, 30 + hoverShift, 200);

        DrawHelper.rectangle(new PoseStack(), (float) x + 2, (float) y + 15.5f,
                parent.parent.getWidth() - 4, parent.parent.getHeight(), 5, bgColor.hashCode());

        int currentBoxSize = (int) (boxSize * toggleAnimation);
        int offsetX = (boxSize - currentBoxSize) / 2;
        int offsetY = (boxSize - currentBoxSize) / 2;

        DrawHelper.rectangle(new PoseStack(), (float) x + 4, (float) y + textOffsetY + 9,
                boxSize + 1, boxSize + 1, 2, new Color(88, 88, 88).hashCode());

        if (toggleAnimation > 0) {
            DrawHelper.rectangle(new PoseStack(), (float) x + offsetX + 5, (float) y + offsetY + textOffsetY + 8,
                    currentBoxSize, currentBoxSize, 2, new Color(69, 239, 0).hashCode());
        }
        //Main.CustomFont.drawString(bindSetting.getName(), (float) x + boxSize + 10, (float) y + textOffsetY + 9, Color.WHITE, GuiGraphics);

        FontRenderers.umbrellatext15.drawString(m, bindSetting.getName(), (float) x + boxSize + 10, (float) y + textOffsetY + 9, Color.WHITE);
        String keyText = getString();
        int keyWidth = mc.font.width(keyText);
        int bindX = (int) (x + parent.parent.getWidth() - keyWidth - 10);

        if (bindSetting.getBindKey() <= 7 && bindSetting.getBindKey() != 0) {
            DrawHelper.rectangle(new PoseStack(), (float) (bindX - 5), (float) y + textOffsetY + 9.5f, boxSize + 16, boxSize + 2, 2, new Color(99, 99, 99).hashCode());
            FontRenderers.umbrellatext15.drawString(m , keyText.toUpperCase(), bindX - 4, (float) y + textOffsetY + 8.5f, Color.WHITE);
        } else {
            DrawHelper.rectangle(new PoseStack(), (float) (bindX - 3), (float) y + textOffsetY + 9.5f, boxSize + 2, boxSize + 2, 2, new Color(99, 99, 99).hashCode());
            FontRenderers.umbrellatext15.drawString(m, keyText.toUpperCase(), bindX, (float) y + textOffsetY + 8.5f, Color.WHITE);
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
        return mx > x && mx < x + parent.parent.getWidth() &&
                my > y && my < y + parent.parent.getHeight();
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
