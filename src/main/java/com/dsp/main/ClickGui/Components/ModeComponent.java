package com.dsp.main.ClickGui.Components;

import com.dsp.main.ClickGui.Button;
import com.dsp.main.ClickGui.Settings.Mode;
import com.dsp.main.ClickGui.Settings.Setting;
import com.dsp.main.Utils.Font.FontRenderers;
import com.dsp.main.Utils.Render.DrawHelper;
import com.mojang.blaze3d.vertex.PoseStack;

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
    public void draw(PoseStack m, int mouseX, int mouseY) {
        int textShifting = ((parent.parent.getHeight() / 2) - mc.font.lineHeight / 2);
        boolean isHovered = isHovered(mouseX, mouseY);

        hoverAnimation += isHovered ? (1 - hoverAnimation) * 0.2f : (0 - hoverAnimation) * 0.2f;
        hoverAnimation = Math.min(1.0f, Math.max(0.0f, hoverAnimation));

        int hoverColorShift = (int) (hoverAnimation * 255);
        int r = Math.min(255, Math.max(0, 0x33 + hoverColorShift));
        int g = Math.min(255, Math.max(0, 0x33 + hoverColorShift));
        int b = Math.min(255, Math.max(0, 0x33 + hoverColorShift));

        Color backgroundColor = new Color(r, g, b, 100);

        DrawHelper.rectangle(new PoseStack(), (float) (parent.parent.getX() + 2),
                (float) (getY() + 15.5f),
                parent.parent.getWidth() - 4,
                parent.parent.getHeight(), 3,
                backgroundColor.hashCode());

        FontRenderers.umbrellatext15.drawString(m, modeSetting.getName() + ":",
                parent.parent.getX() + textShifting,
                getY() + textShifting + 4,
                Color.WHITE);

        int textY = (int) (getY() + textShifting + 8);

        if (isAnimating) {
            animationProgress += 0.1f;
            if (animationProgress >= 1.0f) {
                isAnimating = false;
                animationProgress = 1.0f;
            }

            int currentX = parent.parent.getX() + parent.getWidth() - (int) mc.font.width(modeSetting.getMode()) - 5;
            float interpolated = easeInOut(animationProgress);
            int offset = (int) (interpolated * 50);

            if (isAnimatingForward) {
                FontRenderers.umbrellatext15.drawString(m, previousMode,
                        previousX - offset, textY,
                        new Color(255, 255, 255, (int) ((1 - interpolated) * 255)));

                FontRenderers.umbrellatext15.drawString(m, modeSetting.getMode(),
                        currentX + (50 - offset), textY,
                        new Color(255, 255, 255, (int) (interpolated * 255)));
            } else {
                FontRenderers.umbrellatext15.drawString(m, previousMode,
                        previousX + offset, textY,
                        new Color(255, 255, 255, (int) ((1 - interpolated) * 255)));

                FontRenderers.umbrellatext15.drawString(m, modeSetting.getMode(),
                        currentX - (50 - offset), textY,
                        new Color(255, 255, 255, (int) (interpolated * 255)));
            }
        } else {
            int currentX = parent.parent.getX() + parent.getWidth() - (int) mc.font.width(modeSetting.getMode()) - 5;
            FontRenderers.umbrellatext15.drawString(m, modeSetting.getMode(),
                    currentX, textY, Color.WHITE);
        }

        super.draw(m, mouseX, mouseY);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (isHovered(mouseX, mouseY)) {
            previousMode = modeSetting.getMode();
            int previousModeWidth = (int) mc.font.width(previousMode);
            previousX = parent.parent.getX() + parent.getWidth() - previousModeWidth - 5;

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
