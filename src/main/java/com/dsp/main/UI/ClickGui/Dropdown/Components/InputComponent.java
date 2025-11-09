package com.dsp.main.UI.ClickGui.Dropdown.Components;

import com.dsp.main.UI.ClickGui.Dropdown.Button;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.Input;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.Setting;
import com.dsp.main.Utils.Font.builders.Builder;
import com.dsp.main.Utils.Font.builders.states.QuadColorState;
import com.dsp.main.Utils.Font.builders.states.QuadRadiusState;
import com.dsp.main.Utils.Font.builders.states.SizeState;
import com.dsp.main.Utils.Font.renderers.impl.BuiltBorder;
import com.dsp.main.Utils.Font.renderers.impl.BuiltText;
import com.dsp.main.Utils.Render.Blur.DrawShader;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

import static com.dsp.main.Api.mc;
import static com.dsp.main.Main.BIKO_FONT;

public class InputComponent extends Component {
    private StringBuilder text;
    private boolean typing;
    private final Input textSetting;

    public InputComponent(Setting setting, Button parent, float scaleFactor) {
        super(setting, parent, scaleFactor);
        this.textSetting = (Input) setting;
        this.text = new StringBuilder(textSetting.getValue());
    }

    @Override
    public void draw(GuiGraphics graphics, int mouseX, int mouseY) {
        int compX = (int) this.x;
        int compY = (int) (this.y + 3 * scaleFactor);
        int width = (int) (parent.getWidth() - 12 * scaleFactor);
        int height = (int) (parent.getHeight() - 2 * scaleFactor);

        this.text = new StringBuilder(textSetting.getValue());

        Color borderColor = new Color(20, 30, 50);
        DrawShader.drawRoundBlur(graphics.pose(), compX + 4 * scaleFactor, compY, width, height, 3 * scaleFactor, borderColor.hashCode());
        BuiltBorder border = Builder.border()
                .size(new SizeState(width, height))
                .color(new QuadColorState(Color.DARK_GRAY, Color.GRAY, Color.DARK_GRAY, Color.GRAY))
                .radius(new QuadRadiusState(2f * scaleFactor, 2f * scaleFactor, 2f * scaleFactor, 2f * scaleFactor))
                .thickness(0.01f)
                .smoothness(0.8f, 0.8f)
                .build();
        border.render(new Matrix4f(), compX + 4 * scaleFactor, compY);

        String displayText = text.length() > 0 ? text.toString() : textSetting.getName();
        int colorDisp = text.length() > 0 ? Color.WHITE.getRGB() : Color.GRAY.getRGB();
        int textY = (int) (compY + (height - mc.font.lineHeight * scaleFactor) / 2);

        BuiltText text = Builder.text()
                .font(BIKO_FONT.get())
                .text(displayText)
                .color(colorDisp)
                .size(7f * scaleFactor)
                .thickness(0.05f)
                .build();
        float textWidth = BIKO_FONT.get().getWidth(displayText, 7f * scaleFactor);
        float textX = compX + 1 * scaleFactor + (width - textWidth) / 2f;
        text.render(new Matrix4f(), textX, textY + 2 * scaleFactor);

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

    @Override
    public float getHeight() {
        float textHeight = BIKO_FONT.get().getMetrics().lineHeight() * 10.0f * scaleFactor + 4 * scaleFactor;
        return textHeight + 4 * scaleFactor;
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
            } else if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_ESCAPE) {
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

    public void deactivate() {
        typing = false;
    }

    @Override
    public boolean isInputActive() {
        return typing;
    }
}