package com.dsp.main.UI.ClickGui.Dropdown.Theme;

import com.dsp.main.UI.ClickGui.Dropdown.ClickGuiScreen;
import com.dsp.main.UI.Themes.Theme;
import com.dsp.main.UI.Themes.ThemesUtil;
import com.dsp.main.Utils.Font.builders.Builder;
import com.dsp.main.Utils.Font.builders.states.QuadColorState;
import com.dsp.main.Utils.Font.builders.states.QuadRadiusState;
import com.dsp.main.Utils.Font.builders.states.SizeState;
import com.dsp.main.Utils.Font.renderers.impl.BuiltBorder;
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
    private static final int PADDING = 4;
    private static final float ROUNDING = 8.0f;
    private static final int BUTTON_PADDING = 5;
    private static final float SCROLLBAR_WIDTH = 2.5f;
    private static final float SCROLLBAR_ROUNDING = 3.5f;
    private static final float SCROLLBAR_ANIMATION_SPEED = 0.2f;
    private static final float SCROLL_ANIMATION_SPEED = 0.3f;
    private static final long SCROLLBAR_FADE_DELAY = 1000;
    private static final float APPEAR_ANIMATION_SPEED = 0.15f;

    private final List<ThemeButton> buttons = new ArrayList<>();
    private final Minecraft mc = Minecraft.getInstance();
    private float scaleFactor;
    private int x;
    private int y;
    private int width;
    private int height;
    private int headerHeight;
    private float scrollOffset = 0;
    private float targetScrollOffset = 0;
    private float scrollBarOpacity = 0.0f;
    private long lastScrollTime = 0;
    private boolean isScrolling = false;
    private boolean isDragging = false;
    private int dragOffsetX, dragOffsetY;
    private final ClickGuiScreen parentScreen;

    private float animationProgress = 0f;
    private boolean isVisible = false;
    private boolean wasVisible = false;

    public ThemesFrame(int x, int y, float scaleFactor, ClickGuiScreen parentScreen) {
        this.scaleFactor = scaleFactor;
        this.x = x;
        this.y = y;
        this.width = (int) (130 * scaleFactor);
        this.height = (int) (mc.getWindow().getGuiScaledHeight() * 0.5 * scaleFactor);
        this.headerHeight = (int) (22 * scaleFactor);
        this.parentScreen = parentScreen;

        initButtons();
    }

    public void updateScale(float newScale) {
        this.scaleFactor = newScale;
        this.width = (int) (130 * scaleFactor);
        this.height = (int) (mc.getWindow().getGuiScaledHeight() * 0.5 * scaleFactor);
        this.headerHeight = (int) (22 * scaleFactor);

        buttons.clear();
        initButtons();
    }

    private void initButtons() {
        int buttonY = y + headerHeight + (int) (BUTTON_PADDING * scaleFactor);
        ThemesUtil themesUtil = new ThemesUtil();
        themesUtil.init();
        for (Theme theme : themesUtil.themes) {
            buttons.add(new ThemeButton(theme,
                    x + (int) (PADDING * scaleFactor),
                    buttonY,
                    width - (int) ((PADDING * 2 + 4) * scaleFactor),
                    (int) (20 * scaleFactor),
                    this,
                    scaleFactor));
            buttonY += (int) ((20 + BUTTON_PADDING) * scaleFactor);
        }
    }

    public void setVisible(boolean visible) {
        if (visible != wasVisible) {
            wasVisible = visible;
            isVisible = visible;
        }
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (isVisible && animationProgress < 1f) {
            animationProgress += APPEAR_ANIMATION_SPEED;
            if (animationProgress > 1f) animationProgress = 1f;
        } else if (!isVisible && animationProgress > 0f) {
            animationProgress -= APPEAR_ANIMATION_SPEED;
            if (animationProgress < 0f) animationProgress = 0f;
        }

        if (animationProgress <= 0f) return;

        scrollOffset = lerp(scrollOffset, targetScrollOffset, SCROLL_ANIMATION_SPEED * partialTicks);

        float scale = easeOutBack(animationProgress);
        int alpha = (int) (255 * animationProgress);

        int centerX = x + width / 2;
        int centerY = y + height / 2;

        int animatedWidth = (int) (width * scale);
        int animatedHeight = (int) (height * scale);
        int animatedX = centerX - animatedWidth / 2;
        int animatedY = centerY - animatedHeight / 2;

        guiGraphics.pose().pushPose();

        DrawShader.drawRoundBlur(guiGraphics.pose(),
                animatedX,
                animatedY,
                animatedWidth,
                animatedHeight,
                ROUNDING * scaleFactor,
                new Color(10, 20, 35, Math.min(alpha, 245)).hashCode(),
                90,
                0.7f);

        if (animationProgress > 0.3f) {
            renderBorder(guiGraphics, animatedX, animatedY, animatedWidth, animatedHeight, alpha);
        }

        if (animationProgress > 0.5f) {
            renderHeader(guiGraphics, animatedX, animatedY, animatedWidth, alpha);
            renderCloseButton(guiGraphics, animatedX, animatedY, animatedWidth, alpha);

            int contentTop = animatedY + (int) (headerHeight * scale);
            int contentBottom = animatedY + animatedHeight;

            guiGraphics.enableScissor(animatedX, contentTop, animatedX + animatedWidth, contentBottom);
            renderButtons(guiGraphics, mouseX, mouseY, partialTicks, animatedX, animatedY, scale, alpha);
            guiGraphics.disableScissor();

            renderScrollBar(guiGraphics, partialTicks, animatedX, animatedY, animatedWidth, animatedHeight, scale, alpha);
        }

        guiGraphics.pose().popPose();
    }

    private void renderBorder(GuiGraphics guiGraphics, int renderX, int renderY, int renderWidth, int renderHeight, int alpha) {
        int color1 = ThemesUtil.getCurrentStyle().getColor(1);
        int color2 = ThemesUtil.getCurrentStyle().getColor(2);
        int color3 = ThemesUtil.getCurrentStyle().getColor(1);
        int color4 = ThemesUtil.getCurrentStyle().getColor(2);

        Color c1 = new Color(color1);
        Color c2 = new Color(color2);
        Color c3 = new Color(color3);
        Color c4 = new Color(color4);

        float alphaMultiplier = alpha / 255f;

        BuiltBorder border = Builder.border()
                .size(new SizeState(renderWidth, renderHeight))
                .color(new QuadColorState(
                        new Color(c1.getRed(), c1.getGreen(), c1.getBlue(), (int) (c1.getAlpha() * alphaMultiplier)),
                        new Color(c2.getRed(), c2.getGreen(), c2.getBlue(), (int) (c2.getAlpha() * alphaMultiplier)),
                        new Color(c3.getRed(), c3.getGreen(), c3.getBlue(), (int) (c3.getAlpha() * alphaMultiplier)),
                        new Color(c4.getRed(), c4.getGreen(), c4.getBlue(), (int) (c4.getAlpha() * alphaMultiplier))
                ))
                .radius(new QuadRadiusState(
                        ROUNDING * scaleFactor,
                        ROUNDING * scaleFactor,
                        ROUNDING * scaleFactor,
                        ROUNDING * scaleFactor
                ))
                .thickness(0.015f)
                .smoothness(0.8f, 1f)
                .build();

        border.render(new Matrix4f(), renderX, renderY);
    }

    private void renderHeader(GuiGraphics guiGraphics, int renderX, int renderY, int renderWidth, int alpha) {
        BuiltText titleText = Builder.text()
                .font(BIKO_FONT.get())
                .text("Themes Selector")
                .color(new Color(255, 255, 255, alpha).getRGB())
                .size(11f * scaleFactor)
                .thickness(0.05f)
                .build();

        float textWidth = BIKO_FONT.get().getWidth("Themes Selector", 11f * scaleFactor);
        titleText.render(new Matrix4f(),
                renderX + (renderWidth - textWidth) / 2,
                renderY + (int) (6 * scaleFactor));
    }

    private void renderCloseButton(GuiGraphics guiGraphics, int renderX, int renderY, int renderWidth, int alpha) {
        int closeButtonSize = (int) (12 * scaleFactor);
        int closeButtonX = renderX + renderWidth - closeButtonSize - (int) (4 * scaleFactor);
        int closeButtonY = renderY + (int) (5 * scaleFactor);

        BuiltText closeIcon = Builder.text()
                .font(ICONS.get())
                .text("E")
                .color(new Color(255, 100, 100, alpha).getRGB())
                .size(12f * scaleFactor)
                .thickness(0.05f)
                .build();
        closeIcon.render(new Matrix4f(), closeButtonX, closeButtonY);
    }

    private void renderButtons(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks,
                               int renderX, int renderY, float scale, int alpha) {
        int totalContentHeight = buttons.stream()
                .mapToInt(b -> (int) (b.getHeight() + BUTTON_PADDING * scaleFactor))
                .sum() - (int) (BUTTON_PADDING * scaleFactor);
        int visibleContentHeight = Math.min(totalContentHeight,
                (int) (height - headerHeight - 10 * scaleFactor));

        int contentTop = renderY + (int) (headerHeight * scale + 5 * scaleFactor);
        int contentBottom = contentTop + (int) (visibleContentHeight * scale);

        float buttonY = contentTop - scrollOffset * scale;
        for (ThemeButton b : buttons) {
            b.setPosition(
                    renderX + (int) (PADDING * scaleFactor),
                    (int) buttonY
            );
            b.setWidth((int) ((width - (PADDING * 2 + 6) * scaleFactor)));
            b.setAlpha(alpha);

            if (buttonY < contentBottom && buttonY + b.getHeight() > contentTop) {
                b.render(guiGraphics, mouseX, mouseY, partialTicks);
            }
            buttonY += (b.getHeight() + BUTTON_PADDING * scaleFactor) * scale;
        }
    }

    private void renderScrollBar(GuiGraphics guiGraphics, float partialTicks,
                                 int renderX, int renderY, int renderWidth, int renderHeight,
                                 float scale, int alpha) {
        int totalContentHeight = buttons.stream()
                .mapToInt(b -> (int) (b.getHeight() + BUTTON_PADDING * scaleFactor))
                .sum() - (int) (BUTTON_PADDING * scaleFactor);
        int visibleContentHeight = Math.min(totalContentHeight,
                (int) (height - headerHeight - 10 * scaleFactor));

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

        float scrollBarHeight = ((float) visibleContentHeight / totalContentHeight) * visibleContentHeight * scale;
        float maxScrollOffset = totalContentHeight - visibleContentHeight;
        float scrollBarY = renderY + headerHeight * scale + 5 * scaleFactor +
                (scrollOffset / maxScrollOffset) * (visibleContentHeight * scale - scrollBarHeight);

        int scrollAlpha = (int) (scrollBarOpacity * 180 * (alpha / 255f));

        DrawShader.drawRoundBlur(
                guiGraphics.pose(),
                renderX + renderWidth - (int) (6 * scaleFactor),
                scrollBarY,
                SCROLLBAR_WIDTH * scaleFactor,
                scrollBarHeight,
                SCROLLBAR_ROUNDING * scaleFactor,
                new Color(128, 132, 150, scrollAlpha).hashCode(),
                90,
                0.7f
        );
    }

    public boolean mouseClicked(double mx, double my, int btn) {
        if (animationProgress < 1f) return false;

        int closeButtonSize = (int) (12 * scaleFactor);
        int closeButtonX = x + width - closeButtonSize - (int) (4 * scaleFactor);
        int closeButtonY = y + (int) (5 * scaleFactor);

        if (mx >= closeButtonX && mx <= closeButtonX + closeButtonSize &&
                my >= closeButtonY && my <= closeButtonY + closeButtonSize && btn == 0) {
            isVisible = false;
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
        if (isDragging && animationProgress >= 1f) {
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
        if (isHovered(mouseX, mouseY) && animationProgress >= 1f) {
            int totalContentHeight = buttons.stream()
                    .mapToInt(b -> (int) (b.getHeight() + BUTTON_PADDING * scaleFactor))
                    .sum() - (int) (BUTTON_PADDING * scaleFactor);
            int visibleContentHeight = (int) (height - headerHeight - 10 * scaleFactor);
            int maxScrollOffset = Math.max(0, totalContentHeight - visibleContentHeight);

            targetScrollOffset += delta > 0 ? -25 * scaleFactor : 25 * scaleFactor;
            targetScrollOffset = Math.max(0, Math.min(targetScrollOffset, maxScrollOffset));

            isScrolling = true;
            lastScrollTime = System.currentTimeMillis();
        }
    }

    private void updateButtonPositions() {
        float buttonY = y + headerHeight + BUTTON_PADDING * scaleFactor;
        for (ThemeButton b : buttons) {
            b.setPosition(x + (int) (PADDING * scaleFactor), (int) buttonY);
            buttonY += (20 + BUTTON_PADDING) * scaleFactor;
        }
    }

    public boolean isHovered(double mouseX, double mouseY) {
        if (animationProgress < 0.5f) return false;
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private float lerp(float start, float end, float t) {
        return start + t * (end - start);
    }

    private float easeOutBack(float t) {
        float c1 = 1.70158f;
        float c3 = c1 + 1;
        return 1 + c3 * (float) Math.pow(t - 1, 3) + c1 * (float) Math.pow(t - 1, 2);
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}