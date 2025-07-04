package com.dsp.main.UI.ClickGui;

import com.dsp.main.UI.ClickGui.Components.*;
import com.dsp.main.UI.ClickGui.Components.Component;
import com.dsp.main.UI.ClickGui.Settings.*;
import com.dsp.main.Module;
import com.dsp.main.Utils.Font.FontRenderers;
import com.dsp.main.Utils.Render.DrawHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static com.dsp.main.Api.mc;
import static com.dsp.main.Main.CustomFont;

public class Button {
    private final Module module;
    private int x;
    private int y;
    private int width;

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public int getY() {
        return y;
    }

    public int getX() {
        return x;
    }

    private int height;
    public final Frame parent;
    public boolean extended;
    public boolean binding;
    private final List<Component> components = new ArrayList<>();

    public Button(Module module, int x, int y, int width, int height, Frame parent) {
        this.module = module;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.parent = parent;

        int offset = height;
        for (Setting setting : module.getSettings()) {
            if (setting instanceof CheckBox)
                components.add(new CheckBoxComponent(setting, this));
            else if (setting instanceof BindCheckBox)
                components.add(new BindCheckBoxComponent(setting, this));
            else if (setting instanceof Mode)
                components.add(new ModeComponent(setting, this));
            else if (setting instanceof Slider)
                components.add(new SliderComponent(setting, this, ((Slider) setting).getDefaultvalue()));
            else if (setting instanceof Input)
                components.add(new InputComponent(setting, this));

            offset += height;
        }
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        PoseStack poseStack = graphics.pose();

        // Фон кнопки
        DrawHelper.rectangle(poseStack, x, y, width, height, 4, new Color(20, 30, 50).hashCode());

        // Заголовок
        String label = binding ? ">   <" : module.getName();
        int textColor = module.isEnabled() ? Color.GREEN.getRGB() : Color.WHITE.getRGB();

        FontRenderers.umbrellatext15.drawString(new PoseStack(), label, x + 12, y + 12, Color.WHITE);
        //CustomFont.drawString(label, x + 18, y + 18,Color.WHITE, graphics);
        graphics.drawString(mc.font, label, x + 6, y + 6, textColor, false);

        if (!module.getSettings().isEmpty()) {
            graphics.drawString(mc.font, extended ? "-" : "+", x + width - 10, y + 6, textColor, false);
        }
        if (extended) {
            int offsetY = y + height;
            for (Component comp : components) {
                if (comp.getSetting().isVisible()) {
                    comp.setY(offsetY);
                    comp.draw(poseStack, mouseX, mouseY);
                    offsetY += height;
                }
            }
        }
        if (isHovered(mouseX, mouseY)) {
            float descWidth = mc.font.width(module.getDescription());
            float tooltipX = (mc.getWindow().getGuiScaledWidth() - descWidth) / 2f - mc.getWindow().getGuiScaledWidth() / 4f;
            DrawHelper.rectangle(new PoseStack(), tooltipX - 2, 20, descWidth + 4, 12, 2, new Color(35, 50, 72).hashCode());
            graphics.drawString(mc.font, module.getDescription(), (int) tooltipX, 19, Color.WHITE.getRGB());
        }
    }

    public boolean mouseClicked(double mx, double my, int button) {
        if (extended) {
            for (Component c : components) {
                if (c.isHovered(mx, my)) {
                    c.mouseClicked((int) mx, (int) my, button);
                    return true;
                }
            }
        }
        if (isHovered(mx, my)) {
            if (button == 0) module.toggle();
            else if (button == 1) extended = !extended;
            else if (button == 2) binding = !binding;
            return true;
        }

        return false;
    }

    public void setWidth(int w) {
        this.width = w;
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
        return mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getHeightWithComponents() {
        return (int) (height + (extended ? components.stream().filter(Component::isVisible).count() * height : 0));
    }
}