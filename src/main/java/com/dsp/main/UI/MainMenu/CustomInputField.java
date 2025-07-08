package com.dsp.main.UI.MainMenu;

import com.dsp.main.Utils.Font.builders.Builder;
import com.dsp.main.Utils.Font.builders.states.QuadColorState;
import com.dsp.main.Utils.Font.builders.states.QuadRadiusState;
import com.dsp.main.Utils.Font.builders.states.SizeState;
import com.dsp.main.Utils.Font.renderers.impl.BuiltBorder;
import com.dsp.main.Utils.Font.renderers.impl.BuiltText;
import com.dsp.main.Utils.Render.Blur.DrawShader;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.joml.Matrix4f;
import java.awt.Color;

import static com.dsp.main.Main.BIKO_FONT;

public class CustomInputField {
    private float x, y, width, height;
    private String value = "";
    private String placeholder = "Enter nickname";
    private boolean focused;
    private int maxLength = 16;
    private float inputOpacity = 1.0F;
    private float hoverProgress = 0.0F;
    private float focusProgress = 0.0F;
    private long lastBlinkTime = 0;
    private boolean cursorVisible = false;

    public CustomInputField(float x, float y, float width, float height, Component title) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void setSize(float width, float height) {
        this.width = width;
        this.height = height;
    }

    public void setValue(String value) {
        if (value.length() <= maxLength && isValidMinecraftUsername(value)) {
            this.value = value;
        }
    }

    public String getValue() {
        return value;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
        if (value.length() > maxLength) {
            value = value.substring(0, maxLength);
        }
    }

    public boolean isFocused() {
        return focused;
    }

    public void setFocused(boolean focused) {
        this.focused = focused;
        if (focused) {
            lastBlinkTime = System.currentTimeMillis();
            cursorVisible = true;
        }
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, float externalOpacity) {
        boolean isHovered = isMouseOver(mouseX, mouseY, x, y, width, height);
        hoverProgress = lerp(hoverProgress, isHovered ? 1.0F : 0.0F, partialTick * 0.4F);
        focusProgress = lerp(focusProgress, focused ? 1.0F : 0.0F, partialTick * 0.4F);
        inputOpacity = lerp(inputOpacity, focused ? 1.0F : 0.7F, partialTick * 0.2F);
        // Reduced scaling by half (from 0.05 to 0.025)
        float scale = 1.0F + 0.025F * hoverProgress + 0.025F * focusProgress;
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x + width / 2, y + height / 2, 0);
        guiGraphics.pose().scale(scale, scale, 1.0F);
        guiGraphics.pose().translate(-(x + width / 2), -(y + height / 2), 0);

        // Render border with BuiltBorder
        int baseColor = new Color(30, 30, 36, (int)(64 * externalOpacity)).getRGB();
        int hoverColor = new Color(50, 50, 60, (int)(128 * externalOpacity)).getRGB();
        int focusColor = new Color(70, 70, 80, (int)(160 * externalOpacity)).getRGB();
        int borderColor = lerpColor(baseColor, isHovered ? hoverColor : focusColor, isHovered ? hoverProgress : focusProgress);
        if (BIKO_FONT.get() != null) {
            DrawShader.drawRoundBlur(guiGraphics.pose(), x, y, width, height, 3, new Color(8, 13, 23, 120).hashCode());
            BuiltBorder border = Builder.border()
                    .size(new SizeState(width, height))
                    .color(new QuadColorState(borderColor, borderColor, borderColor, borderColor))
                    .radius(new QuadRadiusState(3f, 3f, 3f, 3f))
                    .thickness(0.03f)
                    .smoothness(1f, 1f)
                    .build();
            border.render(guiGraphics.pose().last().pose(), x, y);
        } else {
            System.err.println("Border shader unavailable, skipping border rendering");
        }

        // Render text with BuiltText
        String displayText = focused ? value + (cursorVisible ? "_" : "") : (value.isEmpty() ? placeholder : value);
        int textAlpha = (int)(255 * inputOpacity * externalOpacity);
        int textColor = new Color(255, 255, 255, textAlpha).getRGB();
        if (BIKO_FONT.get() != null) {
            BuiltText inputText = Builder.text()
                    .font(BIKO_FONT.get())
                    .text(displayText)
                    .color(textColor)
                    .size(10f)
                    .thickness(0.05f)
                    .build();
            float textWidth = BIKO_FONT.get().getWidth(displayText, 10f);
            float textHeight = BIKO_FONT.get().getMetrics().lineHeight() * 10f;
            float textX = x + 5; // Left-aligned with padding
            float textY = y + (height - textHeight) / 2; // Vertically centered
            inputText.render(guiGraphics.pose().last().pose(), textX, textY + 1, 0);
        } else {
            System.err.println("Font is null, skipping input text rendering for: " + displayText);
        }

        guiGraphics.pose().popPose();

        // Update cursor blink
        if (focused) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastBlinkTime >= 500) {
                cursorVisible = !cursorVisible;
                lastBlinkTime = currentTime;
            }
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean isHovered = isMouseOver(mouseX, mouseY, x, y, width, height);
        if (button == 0) {
            setFocused(isHovered);
            return isHovered;
        }
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!focused) {
            return false;
        }
        if (keyCode == 259 && !value.isEmpty()) {
            value = value.substring(0, value.length() - 1);
            return true;
        } else if (keyCode == 257 && value.length() >= 3) { // Enter
            return true; // Handled by MainMenuScreen
        } else if (keyCode == 65 && (modifiers & 2) != 0) { // Ctrl+A
            // Select all (no visual selection, just for copy/paste)
            return true;
        } else if (keyCode == 67 && (modifiers & 2) != 0) { // Ctrl+C
            Minecraft.getInstance().keyboardHandler.setClipboard(value);
            return true;
        } else if (keyCode == 86 && (modifiers & 2) != 0) { // Ctrl+V
            String clipboard = Minecraft.getInstance().keyboardHandler.getClipboard();
            if (clipboard != null && isValidMinecraftUsername(clipboard)) {
                String newValue = value + clipboard;
                if (newValue.length() <= maxLength && BIKO_FONT.get() != null && BIKO_FONT.get().getWidth(newValue, 10f) < width - 10) {
                    value = newValue;
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    public boolean charTyped(char codePoint, int modifiers) {
        if (!focused) {
            return false;
        }
        // Allow only valid Minecraft username characters (A-Z, a-z, 0-9, _)
        if (isValidMinecraftChar(codePoint) && BIKO_FONT.get() != null && BIKO_FONT.get().getWidth(value + codePoint, 10f) < width - 10) {
            if (value.length() < maxLength) {
                value += codePoint;
                return true;
            }
        }
        return false;
    }

    private boolean isValidMinecraftChar(char c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_';
    }

    private boolean isValidMinecraftUsername(String username) {
        if (username.isEmpty()) return true; // Allow empty for initial state
        for (char c : username.toCharArray()) {
            if (!isValidMinecraftChar(c)) {
                return false;
            }
        }
        return true;
    }

    private boolean isMouseOver(double mouseX, double mouseY, float x, float y, float width, float height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private float lerp(float current, float target, float speed) {
        return current + (target - current) * Math.min(speed, 1.0F);
    }

    private int lerpColor(int color1, int color2, float t) {
        int r1 = (color1 >> 16) & 0xFF, g1 = (color1 >> 8) & 0xFF, b1 = color1 & 0xFF, a1 = (color1 >> 24) & 0xFF;
        int r2 = (color2 >> 16) & 0xFF, g2 = (color2 >> 8) & 0xFF, b2 = color2 & 0xFF, a2 = (color2 >> 24) & 0xFF;
        int r = (int)(r1 + (r2 - r1) * t);
        int g = (int)(g1 + (g2 - g1) * t);
        int b = (int)(b1 + (b2 - b1) * t);
        int a = (int)(a1 + (a2 - a1) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}