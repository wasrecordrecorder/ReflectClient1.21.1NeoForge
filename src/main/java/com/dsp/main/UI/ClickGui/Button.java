package com.dsp.main.UI.ClickGui;

import com.dsp.main.UI.ClickGui.Components.*;
import com.dsp.main.UI.ClickGui.Components.Component;
import com.dsp.main.UI.ClickGui.Settings.*;
import com.dsp.main.Module;
import com.dsp.main.Utils.Color.ColorHelper;
import com.dsp.main.Utils.Font.builders.Builder;
import com.dsp.main.Utils.Font.builders.states.QuadColorState;
import com.dsp.main.Utils.Font.builders.states.QuadRadiusState;
import com.dsp.main.Utils.Font.builders.states.SizeState;
import com.dsp.main.Utils.Font.renderers.impl.BuiltBorder;
import com.dsp.main.Utils.Font.renderers.impl.BuiltText;
import com.dsp.main.Utils.Render.Blur.DrawShader;
import com.dsp.main.Utils.Render.DrawHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static com.dsp.main.Api.mc;
import static com.dsp.main.Main.BIKO_FONT;
import static com.dsp.main.Main.ICONS;

public class Button {
    private static final float ANIMATION_SPEED = 0.2f;
    private static final int PADDING = 3;
    private static final float ROUNDING = 4.0f;
    private static final int COMPONENT_PADDING = 2; // Padding above and below components

    private final Module module;
    private int x;
    private int y;
    private int width;
    private int height;
    private boolean extended;
    private boolean binding;
    private float animationProgress = 0.0f;
    private final List<Component> components = new ArrayList<>();

    public Button(Module module, int x, int y, int width, int height, Frame parent) {
        this.module = module;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        initComponents();
    }

    private void initComponents() {
        for (Setting setting : module.getSettings()) {
            if (setting instanceof CheckBox)
                components.add(new CheckBoxComponent((CheckBox) setting, this));
            else if (setting instanceof BindCheckBox)
                components.add(new BindCheckBoxComponent(setting, this));
            else if (setting instanceof Mode)
                components.add(new ModeComponent((Mode) setting, this));
            else if (setting instanceof Slider)
                components.add(new SliderComponent((Slider) setting, this, (float) ((Slider) setting).getDefaultvalue()));
            else if (setting instanceof Input)
                components.add(new InputComponent(setting, this));
            else if (setting instanceof MultiCheckBox)
                components.add(new MultiCheckBoxComponent((MultiCheckBox) setting, this));
        }
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        PoseStack poseStack = graphics.pose();
        float targetProgress = extended ? 1.0f : 0.0f;
        animationProgress = lerp(animationProgress, targetProgress, ANIMATION_SPEED * partialTicks);
        Color baseBgColor = new Color(18, 30, 60, 200);
        int r = baseBgColor.getRed();
        int g = baseBgColor.getGreen();
        int b = baseBgColor.getBlue();
        int a = baseBgColor.getAlpha();
        if (isHovered(mouseX, mouseY)) {
            r = Math.min(255, r + 20);
            g = Math.min(255, g + 20);
            b = Math.min(255, b + 20);
        }
        int bgColor = (a << 24) | (r << 16) | (g << 8) | b;
        DrawShader.drawRoundBlur(poseStack, x, y, width, height, ROUNDING, bgColor, 90, 0.7f);

        QuadColorState borderColorState;
        if (module.isEnabled()) {
            Color neonWhite1 = ColorHelper.twoColorEffect(Color.WHITE, new Color(104, 141, 175, 220), 255);
            Color neonWhite2 = ColorHelper.twoColorEffect(new Color(104, 141, 175, 220), Color.WHITE, 255);
            borderColorState = new QuadColorState(neonWhite1, neonWhite2, neonWhite1, neonWhite2);
        } else {
            Color neonWhite1 = ColorHelper.twoColorEffect(Color.DARK_GRAY, new Color(r, g, b, a), 255);
            Color neonWhite2 = ColorHelper.twoColorEffect(new Color(r, g, b, a), Color.DARK_GRAY, 255);
            borderColorState = new QuadColorState(neonWhite1, neonWhite2, neonWhite1, neonWhite2);
        }
        BuiltBorder border = Builder.border()
                .size(new SizeState(width, height))
                .color(borderColorState)
                .radius(new QuadRadiusState(2f, 2f, 2f, 2f))
                .thickness(0.01f)
                .smoothness(0.5f, 0.5f)
                .build();
        border.render(new Matrix4f(), x, y);
        int textColor;
        if (isHovered(mouseX, mouseY)) {
            textColor = new Color(230, 230, 230, (int) (255 * 0.7)).getRGB();
        } else if (module.isEnabled()) {
            textColor = new Color(255, 255, 255, 255).getRGB();
        } else {
            textColor = new Color(200, 200, 200, (int) (255 * 0.7)).getRGB();
        }

        String label = binding ? "> Press Key <" : module.getName();
        int textX = x + PADDING;
        int textY = y + (height - mc.font.lineHeight + 1) / 2;
        BuiltText text = Builder.text()
                .font(BIKO_FONT.get())
                .text(label)
                .color(textColor)
                .size(9f)
                .thickness(0.05f)
                .build();
        text.render(new Matrix4f(), textX, textY);

        if (!module.getSettings().isEmpty() && module.isEnabled()) {
            BuiltText text1 = Builder.text()
                    .font(ICONS.get())
                    .text("+")
                    .color(textColor)
                    .size(9f)
                    .thickness(0.05f)
                    .build();
            text1.render(new Matrix4f(), x + width - 15, textY);
        }

        if (animationProgress > 0.01f) {
            // Calculate total height including padding between components
            float totalComponentHeight = components.stream()
                    .filter(c -> c.getSetting().isVisible())
                    .map(Component::getHeight)
                    .reduce(0f, Float::sum);
            int visibleComponents = (int) components.stream().filter(c -> c.getSetting().isVisible()).count();
            totalComponentHeight += (visibleComponents > 0 ? (visibleComponents - 1) * 2 * COMPONENT_PADDING : 0);
            float animatedHeight = totalComponentHeight * animationProgress;
            float offsetY = y + height;

            graphics.enableScissor(x, y + height, x + width, (int) (y + height + animatedHeight + 2 * COMPONENT_PADDING));

            for (Component comp : components) {
                if (comp.getSetting().isVisible()) {
                    comp.setX(x + PADDING);
                    comp.setY(offsetY);
                    comp.draw(graphics, mouseX, mouseY);
                    offsetY += comp.getHeight() + 2 * COMPONENT_PADDING;
                }
            }

            graphics.disableScissor();
        }

        if (isHovered(mouseX, mouseY) && !module.getDescription().isEmpty()) {
            String desc = module.getDescription();
            float descWidth = mc.font.width(desc);
            float tooltipX = x + width + 5;
            float tooltipY = y + (height - mc.font.lineHeight) / 2f;
            DrawHelper.rectangle(poseStack, tooltipX - 2, tooltipY - 2, descWidth + 4, mc.font.lineHeight + 4, 2,
                    new Color(35, 50, 72, 220).getRGB());
            graphics.drawString(mc.font, desc, (int) tooltipX, (int) tooltipY, Color.WHITE.getRGB(), false);
        }
    }

    public boolean mouseClicked(double mx, double my, int button) {
        if (extended && animationProgress > 0.5f) {
            for (Component c : components) {
                if (c.isHovered(mx, my) && c.getSetting().isVisible()) {
                    c.mouseClicked((int) mx, (int) my, button);
                    return true;
                }
            }
        }
        if (isHovered(mx, my)) {
            if (button == 0) module.toggle();
            else if (button == 1 && !module.getSettings().isEmpty()) extended = !extended;
            else if (button == 2) binding = !binding;
            return true;
        }
        return false;
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        components.forEach(c -> c.mouseReleased(mouseX, mouseY, button));
    }

    public void keyPressed(int keyCode) {
        if (binding) {
            module.keyCode = (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_DELETE) ? 0 : keyCode;
            binding = false;
        }
        components.forEach(c -> {
            if (c instanceof BindCheckBoxComponent bcc)
                bcc.handleKeyPress(keyCode);
            if (c instanceof InputComponent ic)
                ic.keyPressed(keyCode);
        });
    }

    public boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setWidth(int w) {
        this.width = w;
    }

    public float getHeightWithComponents() {
        float componentHeight = components.stream()
                .filter(c -> c.getSetting().isVisible())
                .map(Component::getHeight)
                .reduce(0f, Float::sum);
        int visibleComponents = (int) components.stream().filter(c -> c.getSetting().isVisible()).count();
        componentHeight += (visibleComponents > 0 ? (visibleComponents - 1) * 2 * COMPONENT_PADDING : 0);
        return height + (animationProgress * componentHeight);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    private float lerp(float start, float end, float t) {
        return start + t * (end - start);
    }
}