package com.dsp.main.UI.ClickGui.Dropdown;

import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Dropdown.Components.*;
import com.dsp.main.UI.ClickGui.Dropdown.Components.Component;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.*;
import com.dsp.main.UI.Themes.ThemesUtil;
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
    private static final int PADDING = 4;
    private static final float ROUNDING = 6.0f;
    private static final int COMPONENT_PADDING = 3;
    private static final float ANIMATION_DURATION = 200.0f;

    private final Module module;
    private int x;
    private int y;
    private int width;
    private int height;
    private boolean extended;
    private boolean binding;
    private float animationProgress = 0.0f;
    private long animationStartTime = 0;
    private boolean lastExtendedState = false;
    private final List<Component> components = new ArrayList<>();
    private final float scaleFactor;
    private Frame parentFrame;

    public Button(Module module, int x, int y, int width, int height, Frame parent, float scaleFactor) {
        this.module = module;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.scaleFactor = scaleFactor;
        this.parentFrame = parent;

        initComponents();
    }

    private void initComponents() {
        for (Setting setting : module.getSettings()) {
            if (setting instanceof CheckBox)
                components.add(new CheckBoxComponent((CheckBox) setting, this, scaleFactor));
            else if (setting instanceof BindCheckBox)
                components.add(new BindCheckBoxComponent(setting, this, scaleFactor));
            else if (setting instanceof ButtonSetting)
                components.add(new ButtonComponent(setting, this, scaleFactor));
            else if (setting instanceof Mode)
                components.add(new ModeComponent((Mode) setting, this, scaleFactor));
            else if (setting instanceof Slider)
                components.add(new SliderComponent((Slider) setting, this, (float) ((Slider) setting).getDefaultvalue(), scaleFactor));
            else if (setting instanceof Input)
                components.add(new InputComponent(setting, this, scaleFactor));
            else if (setting instanceof MultiCheckBox)
                components.add(new MultiCheckBoxComponent((MultiCheckBox) setting, this, scaleFactor));
            else if (setting instanceof BlockListSetting)
                components.add(new BlockListComponent((BlockListSetting) setting, this, scaleFactor));
            else if (setting instanceof ItemListSetting)
                components.add(new ItemListComponent((ItemListSetting) setting, this, scaleFactor));
        }
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        PoseStack poseStack = graphics.pose();
        if (extended != lastExtendedState) {
            animationStartTime = System.currentTimeMillis();
            lastExtendedState = extended;
            if (parentFrame != null) {
                parentFrame.validateScrollOffset();
            }
        }
        float targetProgress = extended ? 1.0f : 0.0f;
        if (animationStartTime > 0) {
            float elapsedTime = System.currentTimeMillis() - animationStartTime;
            animationProgress = Math.max(0.0f, Math.min(1.0f, elapsedTime / ANIMATION_DURATION));
            if (extended) {
                animationProgress = targetProgress * animationProgress;
            } else {
                animationProgress = targetProgress + (1.0f - animationProgress) * (1.0f - targetProgress);
            }
        } else {
            animationProgress = targetProgress;
        }

        Color baseBgColor = new Color(18, 30, 60, 200);
        int r = baseBgColor.getRed();
        int g = baseBgColor.getGreen();
        int b = baseBgColor.getBlue();
        int a = baseBgColor.getAlpha();
        if (isHovered(mouseX, mouseY)) {
            r = Math.min(255, r);
            g = Math.min(255, g);
            b = Math.min(255, b);
        }
        int bgColor = (a << 24) | (r << 16) | (g << 8) | b;
        DrawShader.drawRoundBlur(poseStack, x, y, width, height, ROUNDING * scaleFactor, bgColor, 90, 0.7f);

        QuadColorState borderColorState;
        if (module.isEnabled()) {
            borderColorState = new QuadColorState(ThemesUtil.getCurrentStyle().getColor(1), ThemesUtil.getCurrentStyle().getColor(2), ThemesUtil.getCurrentStyle().getColor(1), ThemesUtil.getCurrentStyle().getColor(2));
        } else {
            Color neonWhite1 = ColorHelper.twoColorEffect(Color.DARK_GRAY, new Color(r, g, b, a), 255);
            Color neonWhite2 = ColorHelper.twoColorEffect(new Color(r, g, b, a), Color.DARK_GRAY, 255);
            borderColorState = new QuadColorState(neonWhite1, neonWhite2, neonWhite1, neonWhite2);
        }
        if (module.isEnabled()) {
            BuiltBorder border = Builder.border()
                    .size(new SizeState(width, height))
                    .color(borderColorState)
                    .radius(new QuadRadiusState(4f * scaleFactor, 4f * scaleFactor, 4f * scaleFactor, 4f * scaleFactor))
                    .thickness(0.01f)
                    .smoothness(0.5f, 0.9f)
                    .build();
            border.render(new Matrix4f(), x, y);
        }

        int textColor;
        if (module.isEnabled()) {
            textColor = new Color(255, 255, 255, 255).getRGB();
        } else {
            textColor = new Color(200, 200, 200, (int) (255 * 0.7)).getRGB();
        }

        String label = binding ? "> Press Key <" : module.getName();
        int textX = (int) (x + PADDING * scaleFactor);
        int textY = (int) (y + (height - (BIKO_FONT.get().getMetrics().lineHeight() * 10f * scaleFactor) + 1 * scaleFactor) / 2);
        BuiltText text = Builder.text()
                .font(BIKO_FONT.get())
                .text(label)
                .color(textColor)
                .size(10f * scaleFactor)
                .thickness(0.05f)
                .build();
        text.render(new Matrix4f(), textX, textY);

        if (module.isEnabled()) {
            BuiltText text1 = Builder.text()
                    .font(ICONS.get())
                    .text("+")
                    .color(textColor)
                    .size(10f * scaleFactor)
                    .thickness(0.05f)
                    .build();
            text1.render(new Matrix4f(), (int) (x + width - 17 * scaleFactor), textY);
        }

        if (animationProgress > 0.01f) {
            float totalComponentHeight = components.stream()
                    .filter(c -> c.getSetting().isVisible())
                    .map(Component::getHeight)
                    .reduce(0f, Float::sum);
            int visibleComponents = (int) components.stream().filter(c -> c.getSetting().isVisible()).count();
            totalComponentHeight += (visibleComponents > 0 ? (visibleComponents - 1) * 2 * COMPONENT_PADDING * scaleFactor : 0);
            float animatedHeight = totalComponentHeight * animationProgress;
            float offsetY = y + height;

            graphics.enableScissor(x, y + height, x + width, (int) (y + height + animatedHeight + 2 * COMPONENT_PADDING * scaleFactor));

            for (Component comp : components) {
                if (comp.getSetting().isVisible()) {
                    comp.setX(x + PADDING * scaleFactor);
                    comp.setY(offsetY);
                    comp.draw(graphics, mouseX, mouseY);
                    offsetY += comp.getHeight() + 2 * COMPONENT_PADDING * scaleFactor;
                }
            }

            graphics.disableScissor();
        }

        if (isHovered(mouseX, mouseY) && !module.getDescription().isEmpty()) {
            String desc = module.getDescription();
            float descWidth = mc.font.width(desc) * scaleFactor;
            float tooltipX = x + width + 6 * scaleFactor;
            float tooltipY = y + (height - mc.font.lineHeight * scaleFactor) / 2f;
            DrawHelper.rectangle(poseStack, tooltipX - 3 * scaleFactor, tooltipY - 3 * scaleFactor, descWidth + 6 * scaleFactor, mc.font.lineHeight * scaleFactor + 6 * scaleFactor, 3 * scaleFactor,
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
            else if (button == 1 && !module.getSettings().isEmpty()) {
                extended = !extended;
                if (parentFrame != null) {
                    parentFrame.validateScrollOffset();
                }
            }
            else if (button == 2) binding = !binding;
            return true;
        }
        return false;
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        components.forEach(c -> c.mouseReleased(mouseX, mouseY, button));
    }

    public void mouseScrolled(double mouseX, double mouseY, double delta) {
        for (Component c : components) {
            if (c instanceof BlockListComponent && c.isHovered(mouseX, mouseY)) {
                ((BlockListComponent) c).mouseScrolled(mouseX, mouseY, delta);
                return;
            }
        }
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
            if (c instanceof BlockListComponent blc)
                blc.keyPressed(keyCode);
            if (c instanceof ItemListComponent ilc)
                ilc.keyPressed(keyCode);
        });
    }

    public void charTyped(char chr) {
        components.forEach(c -> {
            if (c instanceof BlockListComponent blc)
                blc.charTyped(chr);
            if (c instanceof ItemListComponent ilc)
                ilc.charTyped(chr);
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
        componentHeight += (visibleComponents > 0 ? (visibleComponents - 1) * 2 * COMPONENT_PADDING * scaleFactor : 0);
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

    public boolean isBinding() {
        return binding;
    }

    public Module getModule() {
        return module;
    }

    public List<Component> getComponents() {
        return components;
    }
}