package com.dsp.main.UI.Notifications;

import com.dsp.main.UI.Themes.ThemesUtil;
import com.dsp.main.Utils.Color.ColorHelper;
import com.dsp.main.Utils.Font.builders.Builder;
import com.dsp.main.Utils.Font.renderers.impl.BuiltText;
import com.dsp.main.Utils.Render.Blur.DrawShader;
import com.dsp.main.Utils.Render.DrawHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix4f;

import java.awt.*;

import static com.dsp.main.Main.*;
import static com.dsp.main.Functions.Render.Notifications.renderPos;

public class Notification {

    public enum Type {
        ERROR, INFO, WARNING
    }

    private final Type type;
    private final String text;
    private long creationTime;
    private float targetY;
    float currentY;
    private float startY;
    private long animationStartTime;
    private final int width;
    private float currentWidth;
    private final int height = 19; // 4 (top) + 16 (icon/text) + 2 (bar) + 4 (bottom)
    private static final float ANIMATION_DURATION = 500; // 0.5 секунды в миллисекундах
    private boolean disappearing = false;
    private long disappearStartTime;

    public Notification(Type type, String text) {
        this.type = type;
        this.text = text;
        this.creationTime = 0; // Время будет установлено при активации
        Font font = Minecraft.getInstance().font;
        int textWidth = (int) RUS.get().getWidth(text, 8f);
        this.width = 4 + 16 + 4 + textWidth + 4;
        this.currentWidth = renderPos.isMode("Center of screen") ? 0 : width;
        this.currentY = renderPos.isMode("Center of screen") ? (Minecraft.getInstance().getWindow().getGuiScaledHeight() / 2) + 30 : -height; // Начальная позиция для Center of screen
        this.animationStartTime = System.currentTimeMillis(); // Инициализация анимации
    }

    public void activate() {
        this.creationTime = System.currentTimeMillis();
        this.animationStartTime = System.currentTimeMillis(); // Сброс анимации при активации
    }

    public void setTargetY(float targetY) {
        this.targetY = targetY;
        if (!renderPos.isMode("Center of screen")) {
            this.startY = this.currentY;
            this.animationStartTime = System.currentTimeMillis();
        } else {
            this.currentY = targetY; // Без анимации по Y
        }
    }

    public void update() {
        long currentTime = System.currentTimeMillis();
        if (!renderPos.isMode("Center of screen")) {
            float progress = (currentTime - animationStartTime) / ANIMATION_DURATION;
            if (progress < 1) {
                this.currentY = startY + (targetY - startY) * progress;
            } else {
                this.currentY = targetY;
            }
        }

        if (renderPos.isMode("Center of screen") && !disappearing) {
            float progress = (currentTime - animationStartTime) / ANIMATION_DURATION;
            this.currentWidth = progress < 1 ? progress * width : width;
        }

        if (disappearing) {
            float disappearProgress = (currentTime - disappearStartTime) / ANIMATION_DURATION;
            if (disappearProgress < 1) {
                this.currentWidth = width * (1 - disappearProgress);
            } else {
                this.currentWidth = 0;
            }
        }
    }

    public void render(GuiGraphics guiGraphics, int x, float y) {
        int renderWidth = (int) currentWidth;
        if (renderPos.isMode("Center of screen")) {
            x += (width - renderWidth) / 2;
        }
        DrawShader.drawRoundBlur(guiGraphics.pose(), x, (int)y, renderWidth, height, 4,
                new Color(21, 32, 64, 200).getRGB(), 90, 0.3f);
        int scissorX1 = x;
        int scissorY1 = (int)y;
        int scissorX2 = x + renderWidth;
        int scissorY2 = (int)y + height;
        guiGraphics.enableScissor(scissorX1, scissorY1, scissorX2, scissorY2);
        String icon = switch (type) {
            case ERROR   -> "x";
            case INFO    -> "v";
            default      -> "w";
        };
        Color iconColor = switch (type) {
            case ERROR   -> new Color(200, 0, 0);
            case INFO    -> new Color(98, 202, 5);
            default      -> new Color(200, 190, 0);
        };
        BuiltText closeIcon = Builder.text()
                .font(ICONS.get())
                .text(icon)
                .color(iconColor.getRGB())
                .size(12f)
                .thickness(0.05f)
                .build();
        closeIcon.render(new Matrix4f(), x, y + 1f);
        if (renderWidth > 24) {
            BuiltText textRender = Builder.text()
                    .font(RUS.get())
                    .text(text)
                    .color(Color.WHITE.getRGB())
                    .size(8f)
                    .thickness(0.05f)
                    .build();
            textRender.render(new Matrix4f(), x + 14, y + 2.5f);
        }
        guiGraphics.disableScissor();
        float timeElapsed = (System.currentTimeMillis() - creationTime) / 1000f;
        float progress = 1 - timeElapsed / 2f;
        if (progress > 0 && renderWidth > 6) {
            int barWidth = (int)(renderWidth * progress);
            DrawHelper.rectangle(guiGraphics.pose(),
                    x + 2, (int)y + 14,
                    barWidth - 4, 2, 1,
                    ColorHelper.gradient(
                            ThemesUtil.getCurrentStyle().getColorLowSpeed(1),
                            ThemesUtil.getCurrentStyle().getColorLowSpeed(2),
                            10, 10
                    )
            );
        }
    }


    public void startDisappearing() {
        disappearing = true;
        disappearStartTime = System.currentTimeMillis();
    }

    public boolean isDisappearing() {
        return disappearing;
    }

    public boolean isDisappeared() {
        return disappearing && currentWidth <= 0;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public float getCurrentWidth() {
        return currentWidth;
    }
}