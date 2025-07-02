package com.dsp.main.Utils.Font;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import net.minecraft.client.renderer.texture.TextureManager;

public class CustomFontRenderer {
    private Font customFont;
    private final TextureManager textureManager;

    public CustomFontRenderer() {
        this.textureManager = Minecraft.getInstance().getTextureManager();
        loadFont();
    }
    private void loadFont() {
        try {
            InputStream is = getClass().getResourceAsStream("/assets/dsp/font/umbrella.ttf");
            if (is == null) {
                throw new RuntimeException("Не удалось найти файл шрифта в ресурсах!");
            }
            customFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(16f);
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
            customFont = new Font("Arial", Font.PLAIN, 16);
        }
    }
    public void drawString(String text, int x, int y, Color color, GuiGraphics guiGraphics) {
        BufferedImage image = renderTextToImage(text, color);
        NativeImage nativeImage = convertToNativeImage(image);
        DynamicTexture texture = new DynamicTexture(nativeImage);
        ResourceLocation textureLocation = ResourceLocation.fromNamespaceAndPath("dsp", "texts/" + text.hashCode() + color.getRGB());
        textureManager.register(textureLocation, texture);
        RenderSystem.setShaderColor(
                color.getRed() / 255.0f,
                color.getGreen() / 255.0f,
                color.getBlue() / 255.0f,
                color.getAlpha() / 255.0f
        );
        guiGraphics.blit(
                textureLocation,
                x, y,
                0, 0,
                nativeImage.getWidth(), nativeImage.getHeight(),
                nativeImage.getWidth(), nativeImage.getHeight()
        );
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
    private BufferedImage renderTextToImage(String text, Color color) {
        BufferedImage temp = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = temp.createGraphics();
        g.setFont(customFont);
        FontMetrics fm = g.getFontMetrics();
        int width = fm.stringWidth(text);
        int height = fm.getHeight();
        g.dispose();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setFont(customFont);
        g.setColor(color);
        g.drawString(text, 0, fm.getAscent());
        g.dispose();

        return image;
    }

    private NativeImage convertToNativeImage(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        NativeImage nativeImage = new NativeImage(NativeImage.Format.RGBA, width, height, false);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = image.getRGB(x, y);
                int a = (argb >> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;
                int abgr = (a << 24) | (b << 16) | (g << 8) | r;
                nativeImage.setPixelRGBA(x, y, abgr);
            }
        }

        return nativeImage;
    }
}