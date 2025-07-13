package com.dsp.main.UI.Draggable.DragElements;

import com.dsp.main.Api;
import com.dsp.main.UI.Draggable.DraggableElement;
import com.dsp.main.UI.Themes.ThemesUtil;
import com.dsp.main.Utils.Font.builders.Builder;
import com.dsp.main.Utils.Font.builders.states.QuadColorState;
import com.dsp.main.Utils.Font.builders.states.QuadRadiusState;
import com.dsp.main.Utils.Font.builders.states.SizeState;
import com.dsp.main.Utils.Font.renderers.impl.BuiltBorder;
import com.dsp.main.Utils.Font.renderers.impl.BuiltText;
import com.dsp.main.Utils.Render.Blur.DrawShader;
import com.dsp.main.Utils.Color.ColorHelper;
import com.dsp.main.Utils.Render.DrawHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix4f;
import java.awt.Color;

import static com.dsp.main.Api.mc;
import static com.dsp.main.Functions.Render.HudElement.HudElements;
import static com.dsp.main.Functions.Render.HudElement.IconColor;
import static com.dsp.main.Main.BIKO_FONT;
import static com.dsp.main.Main.ICONS;
import static com.dsp.main.Utils.Color.ColorHelper.*;

public class WaterMark extends DraggableElement {
    private static final int BASE_HEIGHT = 24;
    private static final int ICON_SIZE = 24;
    private static final int TEXT_HEIGHT = 10;

    public WaterMark(String name, float initialX, float initialY, boolean canBeDragged) {
        super(name, initialX, initialY, canBeDragged);
    }

    @Override
    public float getWidth() {
        String ping = getPing();
        String username = mc.player != null ? mc.player.getName().getString() : "Unknown";
        String fps = String.valueOf(mc.getFps());
        float usernameWidth = BIKO_FONT.get().getWidth(username, TEXT_HEIGHT);
        float pingWidth = BIKO_FONT.get().getWidth(ping, TEXT_HEIGHT);
        float fpsWidth = BIKO_FONT.get().getWidth(fps, TEXT_HEIGHT);
        float UserIcon = ICONS.get().getWidth("A", TEXT_HEIGHT);
        float PingIcon = ICONS.get().getWidth("J", TEXT_HEIGHT);
        float FpsIcon = ICONS.get().getWidth("O", TEXT_HEIGHT);
        return ICON_SIZE + 3 + 10 + usernameWidth + 10 + pingWidth + 10 + fpsWidth + 10 + UserIcon + PingIcon + FpsIcon - 8;
    }

    @Override
    public float getHeight() {
        return BASE_HEIGHT;
    }

    @Override
    public void render(GuiGraphics guiGraphics) {
        if (HudElements.isOptionEnabled("Watermark") && Api.isEnabled("Hud")) {
            String ping = getPing();
            String username = mc.player != null ? mc.player.getName().getString() : "Unknown";
            String fps = String.valueOf(mc.getFps());

            float usernameWidth = BIKO_FONT.get().getWidth(username, TEXT_HEIGHT);
            float pingWidth = BIKO_FONT.get().getWidth(ping, TEXT_HEIGHT);
            DrawShader.drawRoundBlur(new PoseStack(), xPos, yPos, ICON_SIZE, BASE_HEIGHT, 3, new Color(23, 29, 35, 255).getRGB(), 120, 0.4f);
            DrawShader.drawRoundBlur(new PoseStack(), xPos + ICON_SIZE + 3, yPos + 3, getWidth() - ICON_SIZE - 3, BASE_HEIGHT - 5.5f, 3, new Color(23, 29, 35, 255).getRGB(), 120, 0.4f);

            BuiltText ReflectLogo = Builder.text()
                    .font(ICONS.get())
                    .text("X")
                    .color(ColorHelper.gradient(ThemesUtil.getCurrentStyle().getColor(1), ThemesUtil.getCurrentStyle().getColor(2), 20, 10))
                    .size(18f)
                    .thickness(0.05f)
                    .build();
            ReflectLogo.render(new Matrix4f(), xPos + 0.5f, yPos + 3);

            BuiltText UserIcon = Builder.text()
                    .font(ICONS.get())
                    .text("A")
                    .color(ColorHelper.gradient(ThemesUtil.getCurrentStyle().getColor(1), ThemesUtil.getCurrentStyle().getColor(2), 20, 10))
                    .size(14f)
                    .thickness(0.05f)
                    .build();
            UserIcon.render(new Matrix4f(), xPos + ICON_SIZE + 5, yPos + 5);

            BuiltText UserText = Builder.text()
                    .font(BIKO_FONT.get())
                    .text(username)
                    .color(Color.WHITE)
                    .size(10f)
                    .thickness(0.05f)
                    .build();
            UserText.render(new Matrix4f(), xPos + ICON_SIZE + 18, yPos + 7);

            BuiltText PingIcon = Builder.text()
                    .font(ICONS.get())
                    .text("J")
                    .color(ColorHelper.gradient(ThemesUtil.getCurrentStyle().getColor(1), ThemesUtil.getCurrentStyle().getColor(2), 20, 10))
                    .size(12f)
                    .thickness(0.05f)
                    .build();
            PingIcon.render(new Matrix4f(), xPos + ICON_SIZE + usernameWidth + 26, yPos + 6.5);

            BuiltText PingText = Builder.text()
                    .font(BIKO_FONT.get())
                    .text(ping)
                    .color(Color.WHITE)
                    .size(10f)
                    .thickness(0.05f)
                    .build();
            PingText.render(new Matrix4f(), xPos + ICON_SIZE + usernameWidth + 41, yPos + 8);

            BuiltText FpsIcon = Builder.text()
                    .font(ICONS.get())
                    .text("e")
                    .color(ColorHelper.gradient(ThemesUtil.getCurrentStyle().getColor(1), ThemesUtil.getCurrentStyle().getColor(2), 20, 10))
                    .size(12f)
                    .thickness(0.05f)
                    .build();
            FpsIcon.render(new Matrix4f(), xPos + ICON_SIZE + usernameWidth + pingWidth + 48, yPos + 6);

            BuiltText FpsText = Builder.text()
                    .font(BIKO_FONT.get())
                    .text(fps)
                    .color(Color.WHITE)
                    .size(10f)
                    .thickness(0.05f)
                    .build();
            FpsText.render(new Matrix4f(), xPos + ICON_SIZE + usernameWidth + pingWidth + 61, yPos + 8);
        }
    }

    private String getPing() {
        if (mc.getConnection() != null && mc.player != null && mc.level != null) {
            var playerInfo = mc.getConnection().getPlayerInfo(mc.player.getUUID());
            if (playerInfo != null) {
                return String.valueOf(playerInfo.getLatency());
            }
        }
        return "0";
    }
}