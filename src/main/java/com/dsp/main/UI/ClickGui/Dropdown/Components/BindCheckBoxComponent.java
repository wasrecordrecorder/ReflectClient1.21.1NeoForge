package com.dsp.main.UI.ClickGui.Dropdown.Components;

import com.dsp.main.UI.ClickGui.Dropdown.Button;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.BindCheckBox;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.Setting;
import com.dsp.main.Utils.Font.builders.Builder;
import com.dsp.main.Utils.Font.builders.states.QuadColorState;
import com.dsp.main.Utils.Font.builders.states.QuadRadiusState;
import com.dsp.main.Utils.Font.builders.states.SizeState;
import com.dsp.main.Utils.Font.renderers.impl.BuiltBorder;
import com.dsp.main.Utils.Font.renderers.impl.BuiltText;
import com.dsp.main.Utils.Render.Blur.DrawShader;
import com.dsp.main.Utils.Render.DrawHelper;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

import static com.dsp.main.Main.BIKO_FONT;
import static com.dsp.main.Utils.KeyName.getKeyName;

public class BindCheckBoxComponent extends Component {
    private final BindCheckBox bindSetting;
    private boolean binding;
    private float hoverAnimation = 0;

    public BindCheckBoxComponent(Setting setting, Button parent, float scaleFactor) {
        super(setting, parent, scaleFactor);
        this.bindSetting = (BindCheckBox) setting;
    }

    @Override
    public float getHeight() {
        return BIKO_FONT.get().getMetrics().lineHeight() * 8f * scaleFactor + 2 * scaleFactor;
    }

    @Override
    public void draw(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!setting.isVisible()) return;

        int compX = (int) this.x;
        int compY = (int) this.y;
        int width = (int) (parent.getWidth());

        boolean isHovered = isHovered(mouseX, mouseY);
        hoverAnimation += isHovered ? (1 - hoverAnimation) * 0.1f : (0 - hoverAnimation) * 0.1f;
        int textX = (int) (compX + 5 * scaleFactor);
        int textY = (int) (compY + (getHeight() - BIKO_FONT.get().getMetrics().lineHeight() * 8f * scaleFactor) / 2);
        String name = bindSetting.getName();

        DrawShader.drawRoundBlur(graphics.pose(), (float) textX - 1 * scaleFactor, (float) textY - 1 * scaleFactor, BIKO_FONT.get().getWidth(name, 8f * scaleFactor) + 5 * scaleFactor, BIKO_FONT.get().getMetrics().lineHeight() * 8f * scaleFactor + 2 * scaleFactor, 3 * scaleFactor, new Color(29, 29, 31).hashCode(), 90, 0.7f);
        BuiltText bindSettName = Builder.text()
                .font(BIKO_FONT.get())
                .text(name)
                .color(Color.WHITE)
                .size(8f * scaleFactor)
                .thickness(0.05f)
                .build();
        bindSettName.render(new Matrix4f(), textX, textY);

        String keyText = getString();
        int keyWidth = (int) (BIKO_FONT.get().getWidth(keyText, 8f * scaleFactor) * scaleFactor);
        int bindX = (int) (compX + width - keyWidth - 12 * scaleFactor);
        BuiltBorder border = Builder.border()
                .size(new SizeState(BIKO_FONT.get().getWidth(keyText.toUpperCase(), 8f * scaleFactor) + 5.5f * scaleFactor, 11 * scaleFactor))
                .color(new QuadColorState(Color.DARK_GRAY, Color.GRAY, Color.DARK_GRAY, Color.GRAY))
                .radius(new QuadRadiusState(2f * scaleFactor, 2f * scaleFactor, 2f * scaleFactor, 2f * scaleFactor))
                .thickness(0.01f)
                .smoothness(1f, 1f)
                .build();
        border.render(new Matrix4f(), bindX - 2.25 * scaleFactor, textY - 2 * scaleFactor);
        BuiltText text2 = Builder.text()
                .font(BIKO_FONT.get())
                .text(keyText.toUpperCase())
                .color(Color.WHITE)
                .size(8f * scaleFactor)
                .thickness(0.05f)
                .build();
        text2.render(new Matrix4f(), bindX, textY);
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
        if (isHovered(mx, my)) {
            if (mb == 2 && !binding) {
                binding = true;
                return;
            }
        }
        if (binding && !(mb == 0)) {
            bindSetting.setBindKey(mb);
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
        return mx > x && mx < x + parent.getWidth()
                && my > y && my < y + getHeight();
    }

    @Override
    public boolean isInputActive() {
        return binding;
    }
}