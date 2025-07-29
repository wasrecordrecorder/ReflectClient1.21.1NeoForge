package com.dsp.main.UI.ClickGui.Dropdown.Theme;

import com.dsp.main.UI.ClickGui.Dropdown.ClickGuiScreen;
import com.dsp.main.UI.Themes.Theme;
import com.dsp.main.UI.Themes.ThemesUtil;
import com.dsp.main.Utils.Font.builders.Builder;
import com.dsp.main.Utils.Font.renderers.impl.BuiltText;
import com.dsp.main.Utils.Render.Blur.DrawShader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix4f;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import static com.dsp.main.Main.BIKO_FONT;
import static com.dsp.main.Main.ICONS;

public class ThemesFrame {
    private static final int PADDING = 2;
    private static final float ROUNDING = 6.0f;
    private static final int BUTTON_PADDING = 4;
    private static final float SCROLLBAR_WIDTH = 2.0f;
    private static final float SCROLLBAR_ROUNDING = 3.0f;
    private static final float SCROLLBAR_ANIMATION_SPEED = 0.2f;
    private static final float SCROLL_ANIMATION_SPEED = 0.3f; // Скорость анимации прокрутки
    private static final long SCROLLBAR_FADE_DELAY = 1000;

    private final List<ThemeButton> buttons = new ArrayList<>();
    private final Minecraft mc = Minecraft.getInstance();
    private final float scaleFactor;
    private int x;
    private int y;
    private final int width;
    private final int height;
    private final int headerHeight;
    private float scrollOffset = 0; // Изменено на float для плавности
    private float targetScrollOffset = 0; // Целевое значение прокрутки
    private float scrollBarOpacity = 0.0f;
    private long lastScrollTime = 0;
    private boolean isScrolling = false;
    private boolean isDragging = false;
    private int dragOffsetX, dragOffsetY;
    private final ClickGuiScreen parentScreen;

    public ThemesFrame(int x, int y, float scaleFactor, ClickGuiScreen parentScreen) {
        this.scaleFactor = scaleFactor;
        this.x = (int) (x * scaleFactor);
        this.y = (int) (y * scaleFactor);
        this.width = (int) (110 * scaleFactor);
        this.height = (int) (mc.getWindow().getGuiScaledHeight() * 0.4 * scaleFactor);
        this.headerHeight = (int) (18 * scaleFactor);
        this.parentScreen = parentScreen;

        initButtons();
    }

    private void initButtons() {
        int buttonY = (int) (y + headerHeight - 5 + BUTTON_PADDING * scaleFactor);
        ThemesUtil themesUtil = new ThemesUtil();
        themesUtil.init();
        for (Theme theme : themesUtil.themes) {
            buttons.add(new ThemeButton(theme, (int) (x + PADDING * scaleFactor), buttonY, (int) (width - 2 * PADDING * scaleFactor), (int) (18 * scaleFactor), this, scaleFactor));
            buttonY += (int) (18 * scaleFactor + BUTTON_PADDING * scaleFactor);
        }
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        // Анимация прокрутки
        scrollOffset = lerp(scrollOffset, targetScrollOffset, SCROLL_ANIMATION_SPEED * partialTicks);

        DrawShader.drawRoundBlur(guiGraphics.pose(), x, y, width, height, ROUNDING * scaleFactor, new Color(5, 15, 25).hashCode(), 90, 0.4f);
        renderCloseButton(guiGraphics);
        BuiltText titleText = Builder.text()
                .font(BIKO_FONT.get())
                .text("Themes Selector")
                .color(Color.WHITE.getRGB())
                .size(9f * scaleFactor)
                .thickness(0.05f)
                .build();
        titleText.render(new Matrix4f(), x + (int) (4 * scaleFactor), y + (int) (5 * scaleFactor));
        int contentTop = (int) (y + headerHeight + 5 * scaleFactor);
        int contentBottom = (int) (y + height);
        guiGraphics.enableScissor(x, contentTop, x + width, contentBottom);
        renderButtons(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.disableScissor();
        renderScrollBar(guiGraphics, partialTicks);
    }

    private void renderCloseButton(GuiGraphics guiGraphics) {
        int closeButtonSize = (int) (10 * scaleFactor);
        int closeButtonX = (int) (x + width - closeButtonSize - 2 * scaleFactor);
        int closeButtonY = (int) (y + 2 * scaleFactor);

        BuiltText closeIcon = Builder.text()
                .font(ICONS.get())
                .text("E")
                .color(Color.WHITE.getRGB())
                .size(10f * scaleFactor)
                .thickness(0.05f)
                .build();
        closeIcon.render(new Matrix4f(), closeButtonX, closeButtonY);
    }

    private void renderButtons(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        int totalContentHeight = buttons.stream().mapToInt(b -> (int) (b.getHeight() + BUTTON_PADDING * scaleFactor)).sum() - (int) (BUTTON_PADDING * scaleFactor);
        int visibleContentHeight = Math.min(totalContentHeight, (int) (height - headerHeight - 5 * scaleFactor));

        int contentTop = (int) (y + headerHeight + 5 * scaleFactor);
        int contentBottom = (int) (contentTop + visibleContentHeight);

        float buttonY = contentTop - scrollOffset;
        for (ThemeButton b : buttons) {
            b.setPosition((int) (x + PADDING * scaleFactor), (int) buttonY);
            b.setWidth((int) (width - 2 * PADDING * scaleFactor));
            if (buttonY < contentBottom && buttonY + b.getHeight() > contentTop) {
                b.render(guiGraphics, mouseX, mouseY, partialTicks);
            }
            buttonY += (b.getHeight() + BUTTON_PADDING * scaleFactor);
        }
    }

    private void renderScrollBar(GuiGraphics guiGraphics, float partialTicks) {
        int totalContentHeight = buttons.stream().mapToInt(b -> (int) (b.getHeight() + BUTTON_PADDING * scaleFactor)).sum() - (int) (BUTTON_PADDING * scaleFactor);
        int visibleContentHeight = Math.min(totalContentHeight, (int) (height - headerHeight - 5 * scaleFactor));

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
        float scrollBarY = (int) (y + headerHeight + 5 * scaleFactor + (scrollOffset / (float) maxScrollOffset) * (visibleContentHeight - scrollBarHeight));
        DrawShader.drawRoundBlur(
                guiGraphics.pose(),
                (int) (x + width - SCROLLBAR_WIDTH * scaleFactor),
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
        int closeButtonSize = (int) (10 * scaleFactor); // Согласован с renderCloseButton
        int closeButtonX = (int) (x + width - closeButtonSize - 2 * scaleFactor);
        int closeButtonY = (int) (y + 2 * scaleFactor);
        if (mx >= closeButtonX && mx <= closeButtonX + closeButtonSize && my >= closeButtonY && my <= closeButtonY + closeButtonSize && btn == 0) {
            parentScreen.setThemesFrameVisible(false);
            return true;
        }

        if (mx >= x && mx <= x + width && my >= y && my <= y + headerHeight && btn == 0) {
            isDragging = true;
            dragOffsetX = (int) (mx - x);
            dragOffsetY = (int) (my - y);
            return true;
        }

        for (ThemeButton b : buttons) {
            if (b.mouseClicked(mx, my, btn)) {
                return true;
            }
        }
        return isHovered(mx, my);
    }

    public void mouseDragged(double mouseX, double mouseY) {
        if (isDragging) {
            x = (int) (mouseX - dragOffsetX);
            y = (int) (mouseY - dragOffsetY);
            updateButtonPositions();
        }
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        isDragging = false;
        buttons.forEach(b -> b.mouseReleased(mouseX, mouseY, button));
    }

    public void mouseScrolled(double mouseX, double mouseY, double delta) {
        if (isHovered(mouseX, mouseY)) {
            int totalContentHeight = buttons.stream().mapToInt(b -> (int) (b.getHeight() + BUTTON_PADDING * scaleFactor)).sum() - (int) (BUTTON_PADDING * scaleFactor);
            int visibleContentHeight = (int) (height - headerHeight - 5 * scaleFactor);
            int maxScrollOffset = Math.max(0, totalContentHeight - visibleContentHeight);
            targetScrollOffset += delta > 0 ? -20 * scaleFactor : 20 * scaleFactor;
            targetScrollOffset = Math.max(0, Math.min(targetScrollOffset, maxScrollOffset));
            isScrolling = true;
            lastScrollTime = System.currentTimeMillis();
        }
    }

    private void updateButtonPositions() {
        float buttonY = y + headerHeight - 5 + BUTTON_PADDING * scaleFactor;
        for (ThemeButton b : buttons) {
            b.setPosition((int) (x + PADDING * scaleFactor), (int) buttonY);
            buttonY += (18 * scaleFactor + BUTTON_PADDING * scaleFactor);
        }
    }

    public boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private float lerp(float start, float end, float t) {
        return start + t * (end - start);
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}