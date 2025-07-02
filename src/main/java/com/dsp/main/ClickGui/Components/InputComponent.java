package com.dsp.main.ClickGui.Components;

import com.dsp.main.ClickGui.Button;
import com.dsp.main.ClickGui.Settings.Input;
import com.dsp.main.ClickGui.Settings.Setting;
import com.dsp.main.Utils.Font.FontRenderers;
import com.dsp.main.Utils.Render.DrawHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

import static com.dsp.main.Api.mc;

public class InputComponent extends Component {
    private StringBuilder text;
    private boolean typing;
    private final Input textSetting;

    public InputComponent(Setting setting, Button parent) {
        super(setting, parent);
        this.textSetting = (Input) setting;
        this.text = new StringBuilder(textSetting.getValue());
    }

    @Override
    public void draw(PoseStack m, int mouseX, int mouseY) {
        int textShifting = ((parent.parent.getHeight() / 2) - mc.font.lineHeight / 2);
        this.text = new StringBuilder(textSetting.getValue());

        Color borderColor = isHovered(mouseX, mouseY) ? new Color(0x575551) : new Color(30, 30, 30, 200);
        DrawHelper.rectangle(new PoseStack(), (float) (parent.parent.getX() + 2), (float) (getY() + 16),
                parent.parent.getWidth() - 4, parent.parent.getHeight(), 2, borderColor.hashCode());

        String displayText = text.length() > 0 ? text.toString() : textSetting.getName();
        int colorDisp = text.length() > 0 ? Color.WHITE.getRGB() : Color.GRAY.getRGB();

        FontRenderers.umbrellatext15.drawString(m, displayText,
                parent.parent.getX() + textShifting,
                getY() + textShifting + 9,
                new Color(colorDisp));

        super.draw(m, mouseX, mouseY);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        typing = isHovered(mouseX, mouseY) && button == 0;
        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isHovered(double mouseX, double mouseY) {
        return mouseX > parent.parent.getX() && mouseX < parent.parent.getX() + parent.parent.getWidth()
                && mouseY > getY() && mouseY < getY() + parent.parent.getHeight();
    }

    public void keyPressed(int keyCode) {
        if (typing) {
            if (keyCode >= GLFW.GLFW_KEY_A && keyCode <= GLFW.GLFW_KEY_Z) {
                char typedChar;
                if (GLFW.glfwGetKey(GLFW.glfwGetCurrentContext(), GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS ||
                        GLFW.glfwGetKey(GLFW.glfwGetCurrentContext(), GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS) {
                    typedChar = (char) (keyCode + 'A' - GLFW.GLFW_KEY_A);
                } else {
                    typedChar = (char) (keyCode + 'a' - GLFW.GLFW_KEY_A);
                }
                if (text.length() < 15) {
                    text.append(typedChar);
                }
            } else if (keyCode >= GLFW.GLFW_KEY_0 && keyCode <= GLFW.GLFW_KEY_9) {
                char typedChar = (char) (keyCode + '0' - GLFW.GLFW_KEY_0);
                if (text.length() < 15) {
                    text.append(typedChar);
                }
            } else if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (text.length() > 0) {
                    text.deleteCharAt(text.length() - 1);
                }
            } else if (keyCode == GLFW.GLFW_KEY_ENTER) {
                typing = false;
            }
            textSetting.setValue(text.toString());
        }
    }

    public String getText() {
        return text.toString();
    }

    public void setText(String text) {
        this.text = new StringBuilder(text);
    }
}
