package com.dsp.main.UI.ClickGui;

import com.dsp.main.Module;
import com.dsp.main.Utils.Render.DrawHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Frame {
    private static final int SCROLL_MAX_HEIGHT = 800;
    private static final int ANIMATION_DURATION = 300;

    private final List<Button> buttons = new ArrayList<>();
    private final Module.Category category;
    private final Minecraft mc = Minecraft.getInstance();

    private int x, y;
    private final int width, height;

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    private boolean expanded = true;
    private float animationProgress;
    private long animationStartTime;
    private int scrollOffset = 0;

    public Frame(int x, int y, int width, int height, Module.Category category) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.category = category;
        this.animationStartTime = System.currentTimeMillis();

        initButtons();
    }

    private void initButtons() {
        int buttonY = y + 16;
        for (Module module : Module.getModulesByCategory(category)) {
            buttons.add(new Button(module, x, buttonY, width, height, this));
            buttonY += height;
        }
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks, float globalProgress) {
        updateAnimation();
        float progress = getEasedProgress();

        renderFrameBackground(guiGraphics, progress);
        renderTitle(guiGraphics);

        if (expanded) {
            renderButtons(guiGraphics, mouseX, mouseY, partialTicks, progress);
        }
    }

    private void updateAnimation() {
        long elapsed = System.currentTimeMillis() - animationStartTime;
        animationProgress = Math.min((float) elapsed / ANIMATION_DURATION, 1.0f);
    }

    private float getEasedProgress() {
        return expanded ? (float) (1 - Math.pow(1 - animationProgress, 3))
                : (float) (Math.pow(animationProgress - 1, 3) + 1);
    }

    private void renderFrameBackground(GuiGraphics guiGraphics, float progress) {
        PoseStack ps = guiGraphics.pose();
        ps.pushPose();

        int contentHeight = buttons.stream()
                .mapToInt(Button::getHeightWithComponents)
                .sum();
        int animatedHeight = (int)(height + (expanded ? contentHeight * progress : 0));
        DrawHelper.rectangle(ps, x, y, width, animatedHeight, 6, new Color(15, 25, 45).hashCode());

        ps.popPose();
    }


    private void renderTitle(GuiGraphics guiGraphics) {
        String name = category.name();
        int textWidth = mc.font.width(name);
        int textX = x + width / 2 - textWidth / 2;
        int textY = y + 5;
        guiGraphics.drawString(mc.font, name, textX, textY, Color.WHITE.getRGB(), false);
    }

    private static final int PADDING = 2;

    private void renderButtons(GuiGraphics guiGraphics, int mouseX, int mouseY,
                               float partialTicks, float progress) {
        int contentY = y + height;
        int contentHeight = (int)(buttons.size() * height * progress);
        int visibleEnd = y + height + contentHeight;

        for (Button b : buttons) {
            b.setPosition(x + PADDING, contentY);
            b.setWidth(width - 2 * PADDING);
            if (contentY >= y + height && contentY <= visibleEnd) {
                b.render(guiGraphics, mouseX, mouseY, partialTicks);
            }
            contentY += b.getHeightWithComponents();
        }
    }


    public void mouseClicked(double mx, double my, int btn) {
        if (expanded) {
            for (Button b : buttons) {
                if (b.mouseClicked(mx, my, btn)) {
                    return;
                }
            }
        }
    }


    public void mouseReleased(double mouseX, double mouseY, int button) {
        buttons.forEach(b -> b.mouseReleased(mouseX, mouseY, button));
    }

    public void mouseScrolled(double mouseX, double mouseY, double delta) {
        if (isHovered(mouseX, mouseY) && expanded) {
            scrollOffset += delta > 0 ? -20 : 20;
            scrollOffset = Math.max(0, Math.min(scrollOffset, getMaxScrollOffset()));
        }
    }

    private int getMaxScrollOffset() {
        int totalContentHeight = buttons.stream()
                .mapToInt(Button::getHeightWithComponents)
                .sum();
        return Math.max(0, totalContentHeight - SCROLL_MAX_HEIGHT);
    }

    public void keyPressed(int keyCode) {
        if (!expanded) return;
        buttons.forEach(b -> b.keyPressed(keyCode));
    }

    private boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public boolean isExpanded() { return expanded; }
}