package com.dsp.main.UI.ClickGui;

import com.dsp.main.Module;
import com.dsp.main.UI.Themes.ThemesUtil;
import com.dsp.main.Utils.Color.ColorHelper;
import com.dsp.main.Utils.Font.builders.Builder;
import com.dsp.main.Utils.Font.renderers.impl.BuiltText;
import com.dsp.main.Utils.Render.Blur.DrawShader;
import com.dsp.main.Utils.Render.ColorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4f;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.dsp.main.Main.BIKO_FONT;
import static com.dsp.main.Main.ICONS;

public class Frame {
    private static final int PADDING = 1;
    private static final float ROUNDING = 10.0f;
    private static final int BUTTON_PADDING = 4;
    private static final float SCROLLBAR_WIDTH = 2.0f;
    private static final float SCROLLBAR_ROUNDING = 3.0f;
    private static final float SCROLLBAR_ANIMATION_SPEED = 0.2f;
    private static final float SCROLL_ANIMATION_SPEED = 0.3f;
    private static final long SCROLLBAR_FADE_DELAY = 1500;

    private final List<Button> buttons = new ArrayList<>();
    private final Module.Category category;
    private final Minecraft mc = Minecraft.getInstance();
    private final float scaleFactor;

    private final int finalX;
    private final int finalY;
    private int currentX;
    private int currentY;
    private final int width;
    private final int height;
    private final int headerHeight;
    private final int footerHeight;
    private final int buttonBaseHeight;
    private final int maxVisibleHeight;
    private final int index;
    private float scrollOffset = 0;
    private float targetScrollOffset = 0;
    private float scrollBarOpacity = 0.0f;
    private long lastScrollTime = 0;
    private boolean isScrolling = false;
    private String searchQuery = "";

    public Frame(int x, int y, int height, Module.Category category, int index, float scaleFactor) {
        this.scaleFactor = scaleFactor;
        this.finalX = (int) (x * scaleFactor);
        this.finalY = (int) (y * scaleFactor);
        this.currentX = (int) (x * scaleFactor);
        this.currentY = (int) (mc.getWindow().getGuiScaledHeight() / 2 + 300 * scaleFactor);
        this.width = (int) (117 * scaleFactor);
        this.height = (int) (height * scaleFactor);
        this.category = category;
        this.index = index;
        this.headerHeight = (int) (height * scaleFactor);
        this.footerHeight = (int) (height / 2 * scaleFactor);
        this.buttonBaseHeight = (int) (height * scaleFactor);
        this.maxVisibleHeight = (int) (mc.getWindow().getGuiScaledHeight() * 0.65 * scaleFactor);

        initButtons();
    }

    private void initButtons() {
        int buttonY = (int) (finalY + headerHeight + BUTTON_PADDING * scaleFactor);
        for (Module module : Module.getModulesByCategory(category)) {
            buttons.add(new Button(module, (int) (finalX + PADDING * scaleFactor), buttonY, (int) (width - 2 * PADDING * scaleFactor), buttonBaseHeight, this, scaleFactor));
            buttonY += (int) (buttonBaseHeight + BUTTON_PADDING * scaleFactor);
        }
    }

    public void updatePosition(long globalElapsed) {
        float delay = index * 100;
        float animationDuration = 300;

        float t = Math.max(0, Math.min(1, (globalElapsed - delay) / animationDuration));
        float easedProgress = t * t * t * (t * (6 * t - 15) + 10);

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int startX = (int) (screenWidth / 2 * scaleFactor);
        int startY = (int) (screenHeight / 2 + 300 * scaleFactor);

        currentX = (int) (startX + (finalX - startX) * easedProgress);
        currentY = (int) (startY + (finalY - startY) * easedProgress);
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        // Smoothly interpolate scrollOffset towards targetScrollOffset
        scrollOffset = lerp(scrollOffset, targetScrollOffset, SCROLL_ANIMATION_SPEED * partialTicks);

        renderFrameBackground(guiGraphics);
        renderTitle(guiGraphics);
        renderButtons(guiGraphics, mouseX, mouseY, partialTicks);
        renderScrollBar(guiGraphics, partialTicks);
    }

    private void renderFrameBackground(GuiGraphics guiGraphics) {
        PoseStack ps = guiGraphics.pose();
        ps.pushPose();
        int frameHeight = (int) (headerHeight + maxVisibleHeight + footerHeight);
        DrawShader.drawRoundBlur(ps, currentX - (int) (2 * scaleFactor), currentY, (int) (width + 4 * scaleFactor), (int) (frameHeight + 4 * scaleFactor), ROUNDING * scaleFactor, new Color(5, 15, 25).hashCode(), 90, 0.6f);
        ps.popPose();
    }

    private void renderTitle(GuiGraphics guiGraphics) {
        String name = category.name();
        int textWidth = (int) (BIKO_FONT.get().getWidth(name, 10f * scaleFactor) * scaleFactor);
        int textX = (int) (currentX + width / 2 - textWidth / 2);
        int textY = (int) (currentY + 10 * scaleFactor);

        BuiltText text = Builder.text()
                .font(BIKO_FONT.get())
                .text(name)
                .color(Color.WHITE)
                .size(10f * scaleFactor)
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
                .color(ColorHelper.gradient(ThemesUtil.getCurrentStyle().getColorLowSpeed(1), ThemesUtil.getCurrentStyle().getColorLowSpeed(2), 20, 10))
                .size(10f * scaleFactor)
                .thickness(0.05f)
                .build();
        int iconX = (int) (textX - 12 * scaleFactor);
        icon.render(new Matrix4f(), iconX, (int) (textY - 1 * scaleFactor));
    }

    private void renderButtons(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        List<Button> visibleButtons = buttons.stream()
                .filter(b -> b.getModule().getName().toLowerCase().contains(searchQuery.toLowerCase()))
                .collect(Collectors.toList());

        int totalContentHeight = visibleButtons.stream().mapToInt(b -> (int) (b.getHeightWithComponents() + BUTTON_PADDING * scaleFactor)).sum() - (int) (BUTTON_PADDING * scaleFactor);
        int visibleContentHeight = Math.min(totalContentHeight, (int) (maxVisibleHeight - 5 * scaleFactor));

        int contentTop = (int) (currentY + headerHeight + 10 * scaleFactor);
        int contentBottom = (int) (contentTop + visibleContentHeight);

        guiGraphics.enableScissor(currentX, contentTop, currentX + width, contentBottom + 1);

        int buttonY = (int) (contentTop - scrollOffset);
        for (Button b : visibleButtons) {
            b.setPosition((int) (currentX + PADDING * scaleFactor), buttonY);
            b.setWidth((int) (width - 2 * PADDING * scaleFactor));
            if (buttonY < contentBottom && buttonY + b.getHeightWithComponents() > contentTop) {
                b.render(guiGraphics, mouseX, mouseY, partialTicks);
            }
            buttonY += (int) (b.getHeightWithComponents() + BUTTON_PADDING * scaleFactor);
        }

        guiGraphics.disableScissor();
    }

    private void renderScrollBar(GuiGraphics guiGraphics, float partialTicks) {
        List<Button> visibleButtons = buttons.stream()
                .filter(b -> b.getModule().getName().toLowerCase().contains(searchQuery.toLowerCase()))
                .collect(Collectors.toList());

        int totalContentHeight = visibleButtons.stream().mapToInt(b -> (int) (b.getHeightWithComponents() + BUTTON_PADDING * scaleFactor)).sum() - (int) (BUTTON_PADDING * scaleFactor);
        int visibleContentHeight = Math.min(totalContentHeight, (int) (maxVisibleHeight - 5 * scaleFactor));

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

        if (scrollBarOpacity <= 0.01f) return;

        float scrollBarHeight = ((float) visibleContentHeight / totalContentHeight) * visibleContentHeight;
        float maxScrollOffset = totalContentHeight - visibleContentHeight;
        float scrollBarY = (int) (currentY + headerHeight + 10 * scaleFactor + (scrollOffset / (float) maxScrollOffset) * (visibleContentHeight - scrollBarHeight));
        DrawShader.drawRoundBlur(
                guiGraphics.pose(),
                (int) (currentX + width + 3 * scaleFactor),
                scrollBarY,
                SCROLLBAR_WIDTH * scaleFactor,
                scrollBarHeight,
                SCROLLBAR_ROUNDING * scaleFactor,
                new Color(128, 132, 150, (int) (scrollBarOpacity * 150)).hashCode(),
                90,
                0.7f
        );
    }

    public boolean mouseClicked(double mx, double my, int btn) {
        for (Button b : buttons) {
            if (b.mouseClicked(mx, my, btn)) {
                return true;
            }
        }
        return false;
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        buttons.forEach(b -> b.mouseReleased(mouseX, mouseY, button));
    }

    public void mouseScrolled(double mouseX, double mouseY, double delta) {
        if (isHovered(mouseX, mouseY)) {
            int totalContentHeight = buttons.stream().mapToInt(b -> (int) (b.getHeightWithComponents() + BUTTON_PADDING * scaleFactor)).sum() - (int) (BUTTON_PADDING * scaleFactor);
            int visibleContentHeight = Math.min(totalContentHeight, (int) (maxVisibleHeight) - 4);
            int maxScrollOffset = Math.max(0, totalContentHeight - visibleContentHeight);

            // Update targetScrollOffset instead of scrollOffset directly
            targetScrollOffset += (float) (delta > 0 ? -20 * scaleFactor : 20 * scaleFactor);
            targetScrollOffset = Math.max(0, Math.min(targetScrollOffset, maxScrollOffset));

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
        int totalContentHeight = buttons.stream().mapToInt(b -> (int) (b.getHeightWithComponents() + BUTTON_PADDING * scaleFactor)).sum() - (int) (BUTTON_PADDING * scaleFactor);
        int visibleContentHeight = Math.min(totalContentHeight, maxVisibleHeight);
        int frameHeight = (int) (headerHeight + visibleContentHeight + footerHeight);
        return mouseX >= currentX && mouseX <= currentX + width && mouseY >= currentY && mouseY <= currentY + frameHeight;
    }

    private float lerp(float start, float end, float t) {
        return start + t * (end - start);
    }

    public int getX() { return currentX; }
    public int getY() { return currentY; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public List<Button> getButtons() {
        return buttons;
    }

    public void setSearchQuery(String query) {
        this.searchQuery = query;
    }
}