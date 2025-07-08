package com.dsp.main.UI.ClickGui;

import com.dsp.main.Module;
import com.dsp.main.Utils.Font.builders.Builder;
import com.dsp.main.Utils.Font.renderers.impl.BuiltText;
import com.dsp.main.Utils.Render.Blur.DrawShader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4f;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import static com.dsp.main.Main.BIKO_FONT;
import static com.dsp.main.Main.ICONS;

public class Frame {
    private static final int PADDING = 1;
    private static final float ROUNDING = 10.0f;
    private static final int BUTTON_PADDING = 4;
    private static final float SCROLLBAR_WIDTH = 2.0f;
    private static final float SCROLLBAR_ROUNDING = 3.0f;
    private static final float SCROLLBAR_ANIMATION_SPEED = 0.2f;
    private static final long SCROLLBAR_FADE_DELAY = 1500; // 1.5 seconds in milliseconds

    private final List<Button> buttons = new ArrayList<>();
    private final Module.Category category;
    private final Minecraft mc = Minecraft.getInstance();

    private final int finalX;
    private final int finalY;
    private int currentX;
    private int currentY;
    private final int width = 130;
    private final int height;
    private final int headerHeight;
    private final int footerHeight;
    private final int buttonBaseHeight;
    private final int maxVisibleHeight;
    private final int index;

    private int scrollOffset = 0;
    private float scrollBarOpacity = 0.0f;
    private long lastScrollTime = 0;
    private boolean isScrolling = false;

    public Frame(int x, int y, int height, Module.Category category, int index) {
        this.finalX = x;
        this.finalY = y;
        this.currentX = x;
        this.currentY = mc.getWindow().getGuiScaledHeight() / 2 + 300;
        this.height = height;
        this.category = category;
        this.index = index;
        this.headerHeight = height;
        this.footerHeight = height / 2;
        this.buttonBaseHeight = height;
        this.maxVisibleHeight = (int)(mc.getWindow().getGuiScaledHeight() * 0.8);

        initButtons();
    }

    private void initButtons() {
        int buttonY = finalY + headerHeight + BUTTON_PADDING;
        for (Module module : Module.getModulesByCategory(category)) {
            buttons.add(new Button(module, finalX + PADDING, buttonY, width - 2 * PADDING, buttonBaseHeight, this));
            buttonY += buttonBaseHeight + BUTTON_PADDING;
        }
    }

    public void updatePosition(long globalElapsed) {
        float delay = index * 100;
        float animationDuration = 300;

        float t = Math.max(0, Math.min(1, (globalElapsed - delay) / animationDuration));
        float easedProgress = t * t * t * (t * (6 * t - 15) + 10);

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int startX = screenWidth / 2;
        int startY = screenHeight / 2 + 300;

        currentX = (int) (startX + (finalX - startX) * easedProgress);
        currentY = (int) (startY + (finalY - startY) * easedProgress);
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        renderFrameBackground(guiGraphics);
        renderTitle(guiGraphics);
        renderButtons(guiGraphics, mouseX, mouseY, partialTicks);
        renderScrollBar(guiGraphics, partialTicks);
    }

    private void renderFrameBackground(GuiGraphics guiGraphics) {
        PoseStack ps = guiGraphics.pose();
        ps.pushPose();
        int frameHeight = headerHeight + maxVisibleHeight + footerHeight;
        DrawShader.drawRoundBlur(ps, currentX - 2, currentY, width + 4, frameHeight + 4, ROUNDING, new Color(5, 15, 25).hashCode(), 90, 0.6f);
        ps.popPose();
    }

    private void renderTitle(GuiGraphics guiGraphics) {
        String name = category.name();
        int textWidth = (int) BIKO_FONT.get().getWidth(name, 10f);
        int textX = currentX + width / 2 - textWidth / 2;
        int textY = currentY + 10;

        BuiltText text = Builder.text()
                .font(BIKO_FONT.get())
                .text(name)
                .color(Color.WHITE)
                .size(10f)
                .thickness(0.05f)
                .build();
        text.render(new Matrix4f(), textX, textY);

        String iconChar;
        switch (category) {
            case COMBAT -> iconChar = "a";
            case MISC -> iconChar = "e";
            case MOVEMENT -> iconChar = "K";
            case RENDER -> iconChar = "c";
            case PLAYER -> iconChar = "B";
            default -> iconChar = "";
        }
        BuiltText icon = Builder.text()
                .font(ICONS.get())
                .text(iconChar)
                .color(Color.WHITE)
                .size(10f)
                .thickness(0.05f)
                .build();
        int iconX = textX - 12;
        icon.render(new Matrix4f(), iconX, textY - 1);
    }

    private void renderButtons(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        int totalContentHeight = buttons.stream().mapToInt(b -> (int) (b.getHeightWithComponents() + BUTTON_PADDING)).sum() - BUTTON_PADDING;
        int visibleContentHeight = Math.min(totalContentHeight, maxVisibleHeight - 5);

        int contentTop = currentY + headerHeight + 10;
        int contentBottom = contentTop + visibleContentHeight;

        guiGraphics.enableScissor(currentX, contentTop, currentX + width, contentBottom);

        int buttonY = contentTop - scrollOffset;
        for (Button b : buttons) {
            b.setPosition(currentX + PADDING, buttonY);
            b.setWidth(width - 2 * PADDING);
            if (buttonY < contentBottom && buttonY + b.getHeightWithComponents() > contentTop) {
                b.render(guiGraphics, mouseX, mouseY, partialTicks);
            }
            buttonY += b.getHeightWithComponents() + BUTTON_PADDING;
        }

        guiGraphics.disableScissor();
    }

    private void renderScrollBar(GuiGraphics guiGraphics, float partialTicks) {
        int totalContentHeight = buttons.stream().mapToInt(b -> (int) (b.getHeightWithComponents() + BUTTON_PADDING)).sum() - BUTTON_PADDING;
        int visibleContentHeight = Math.min(totalContentHeight, maxVisibleHeight - 5);

        if (totalContentHeight <= visibleContentHeight) {
            scrollBarOpacity = lerp(scrollBarOpacity, 0.0f, SCROLLBAR_ANIMATION_SPEED * partialTicks);
            return;
        }
        long currentTime = System.currentTimeMillis();
        if (isScrolling && currentTime - lastScrollTime > SCROLLBAR_FADE_DELAY) {
            scrollBarOpacity = lerp(scrollBarOpacity, 0.0f, SCROLLBAR_ANIMATION_SPEED * partialTicks);
        } else {
            scrollBarOpacity = lerp(scrollBarOpacity, 1.0f, SCROLLBAR_ANIMATION_SPEED * partialTicks);
        }

        if (scrollBarOpacity <= 0.01f) return; // Don't render if fully transparent

        // Calculate scrollbar dimensions
        float scrollBarHeight = ((float) visibleContentHeight / totalContentHeight) * visibleContentHeight;
        float maxScrollOffset = totalContentHeight - visibleContentHeight;
        float scrollBarY = currentY + headerHeight + 10 + (scrollOffset / (float) maxScrollOffset) * (visibleContentHeight - scrollBarHeight);

        // Render scrollbar
        DrawShader.drawRoundBlur(
                guiGraphics.pose(),
                currentX + width + 3, // 2px padding from right edge
                scrollBarY,
                SCROLLBAR_WIDTH,
                scrollBarHeight,
                SCROLLBAR_ROUNDING,
                new Color(128, 132, 150, (int)(scrollBarOpacity * 150)).hashCode(),
                90,
                0.7f
        );
    }

    public void mouseClicked(double mx, double my, int btn) {
        for (Button b : buttons) {
            if (b.mouseClicked(mx, my, btn)) {
                return;
            }
        }
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        buttons.forEach(b -> b.mouseReleased(mouseX, mouseY, button));
    }

    public void mouseScrolled(double mouseX, double mouseY, double delta) {
        if (isHovered(mouseX, mouseY)) {
            int totalContentHeight = buttons.stream().mapToInt(b -> (int) (b.getHeightWithComponents() + BUTTON_PADDING)).sum() - BUTTON_PADDING;
            int visibleContentHeight = Math.min(totalContentHeight, maxVisibleHeight);
            int maxScrollOffset = Math.max(0, totalContentHeight - visibleContentHeight);
            scrollOffset += delta > 0 ? -20 : 20;
            scrollOffset = Math.max(0, Math.min(scrollOffset, maxScrollOffset));
            isScrolling = true;
            lastScrollTime = System.currentTimeMillis();
        }
    }

    public void mouseMoved(double mouseX, double mouseY) {
        if (isScrolling && !isHovered(mouseX, mouseY)) {
            isScrolling = false;
        }
    }

    public void keyPressed(int keyCode) {
        buttons.forEach(b -> b.keyPressed(keyCode));
    }

    private boolean isHovered(double mouseX, double mouseY) {
        int totalContentHeight = buttons.stream().mapToInt(b -> (int) (b.getHeightWithComponents() + BUTTON_PADDING)).sum() - BUTTON_PADDING;
        int visibleContentHeight = Math.min(totalContentHeight, maxVisibleHeight);
        int frameHeight = headerHeight + visibleContentHeight + footerHeight;
        return mouseX >= currentX && mouseX <= currentX + width && mouseY >= currentY && mouseY <= currentY + frameHeight;
    }

    private float lerp(float start, float end, float t) {
        return start + t * (end - start);
    }

    public int getX() { return currentX; }
    public int getY() { return currentY; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}