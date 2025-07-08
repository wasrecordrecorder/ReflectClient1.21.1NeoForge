package com.dsp.main.UI.ClickGui.Components;

import com.dsp.main.UI.ClickGui.Button;
import com.dsp.main.UI.ClickGui.Settings.Mode;
import com.dsp.main.UI.ClickGui.Settings.Setting;
import com.dsp.main.Utils.Render.DrawHelper;
import net.minecraft.client.gui.GuiGraphics;

import java.awt.*;

import static com.dsp.main.Api.mc;

public class ModeComponent extends Component {
    private final Mode modeSetting;
    private float hoverAnimation = 0;
    private boolean isAnimating = false;
    private float animationProgress = 0f;
    private String previousMode = "";
    private int previousX;
    private boolean isAnimatingForward = true;

    public ModeComponent(Setting setting, Button parent) {
        super(setting, parent);
        this.modeSetting = (Mode) setting;
    }

    @Override
    public void draw(GuiGraphics graphics, int mouseX, int mouseY) {
        int compX = (int) this.x;
        int compY = (int) this.y;
        int width = parent.getWidth();
        int height = parent.getHeight();

        boolean isHovered = isHovered(mouseX, mouseY);
        hoverAnimation += isHovered ? (1 - hoverAnimation) * 0.2f : (0 - hoverAnimation) * 0.2f;
        hoverAnimation = Math.min(1.0f, Math.max(0.0f, hoverAnimation));

        int hoverColorShift = (int) (hoverAnimation * 255);
        int r = Math.min(255, Math.max(0, 0x33 + hoverColorShift));
        int g = Math.min(255, Math.max(0, 0x33 + hoverColorShift));
        int b = Math.min(255, Math.max(0, 0x33 + hoverColorShift));
        Color backgroundColor = new Color(r, g, b, 100);
        DrawHelper.rectangle(graphics.pose(), compX, compY, width, height, 3, backgroundColor.hashCode());

        int textY = compY + (height - mc.font.lineHeight) / 2;
        graphics.drawString(mc.font, modeSetting.getName() + ":", compX + 5, textY, Color.WHITE.getRGB());

        if (isAnimating) {
            animationProgress += 0.1f;
            if (animationProgress >= 1.0f) {
                isAnimating = false;
                animationProgress = 1.0f;
            }

            int currentX = compX + width - mc.font.width(modeSetting.getMode()) - 5;
            float interpolated = easeInOut(animationProgress);
            int offset = (int) (interpolated * 50);

            if (isAnimatingForward) {
                graphics.drawString(mc.font, previousMode, previousX - offset, textY, new Color(255, 255, 255, (int)((1 - interpolated) * 255)).getRGB());
                graphics.drawString(mc.font, modeSetting.getMode(), currentX + (50 - offset), textY, new Color(255, 255, 255, (int)(interpolated * 255)).getRGB());
            } else {
                graphics.drawString(mc.font, previousMode, previousX + offset, textY, new Color(255, 255, 255, (int)((1 - interpolated) * 255)).getRGB());
                graphics.drawString(mc.font, modeSetting.getMode(), currentX - (50 - offset), textY, new Color(255, 255, 255, (int)(interpolated * 255)).getRGB());
            }
        } else {
            int currentX = compX + width - mc.font.width(modeSetting.getMode()) - 5;
            graphics.drawString(mc.font, modeSetting.getMode(), currentX, textY, Color.WHITE.getRGB());
        }

        super.draw(graphics, mouseX, mouseY);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (isHovered(mouseX, mouseY)) {
            previousMode = modeSetting.getMode();
            int previousModeWidth = mc.font.width(previousMode);
            previousX = (int) (x + parent.getWidth() - previousModeWidth - 5);

            if (mouseButton == 0) {
                modeSetting.cycle();
                isAnimatingForward = true;
            } else if (mouseButton == 1) {
                modeSetting.cycleBackward();
                isAnimatingForward = false;
            }
            isAnimating = true;
            animationProgress = 0f;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private float easeInOut(float t) {
        return t < 0.5f ? 2 * t * t : -1 + (4 - 2 * t) * t;
    }
}