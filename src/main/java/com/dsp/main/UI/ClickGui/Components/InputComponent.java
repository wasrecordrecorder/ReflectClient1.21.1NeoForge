package com.dsp.main.UI.ClickGui.Components;

import com.dsp.main.UI.ClickGui.Button;
import com.dsp.main.UI.ClickGui.Settings.Input;
import com.dsp.main.UI.ClickGui.Settings.Setting;
import com.dsp.main.Utils.Render.DrawHelper;
import net.minecraft.client.gui.GuiGraphics;
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
    public void draw(GuiGraphics graphics, int mouseX, int mouseY) {
        int compX = (int) this.x;
        int compY = (int) this.y;
        int width = parent.getWidth();
        int height = parent.getHeight();

        this.text = new StringBuilder(textSetting.getValue());

        Color borderColor = isHovered(mouseX, mouseY) ? new Color(0x575551) : new Color(20, 30, 50);
        DrawHelper.rectangle(graphics.pose(), compX, compY, width, height, 2, borderColor.hashCode());

        String displayText = text.length() > 0 ? text.toString() : textSetting.getName();
        int colorDisp = text.length() > 0 ? Color.WHITE.getRGB() : Color.GRAY.getRGB();
        int textY = compY + (height - mc.font.lineHeight) / 2;
        graphics.drawString(mc.font, displayText, compX + 5, textY, colorDisp);

        super.draw(graphics, mouseX, mouseY);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        typing = isHovered(mouseX, mouseY) && button == 0;
        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isHovered(double mouseX, double mouseY) {
        return mouseX > x && mouseX < x + parent.getWidth()
                && mouseY > y && mouseY < y + parent.getHeight();
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