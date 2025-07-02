package com.dsp.main.Utils.Font;


import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class FontRenderers {
    public static FontRenderer umbrellatext14;
    public static FontRenderer umbrellatext15;
    public static FontRenderer umbrellatext16;
    public static FontRenderer umbrellatext17;
    public static FontRenderer umbrellatext18;
    public static FontRenderer umbrellatext20;
    public static FontRenderer umbrellatext22;
    public void init() {
        try {
            InputStream is = getClass().getResourceAsStream("/assets/dsp/font/umbrella.ttf");
            if (is == null) {
                throw new IOException("Не удалось найти файл шрифта: /assets/dsp/font/umbrella.ttf");
            }
            Font baseFont = Font.createFont(Font.TRUETYPE_FONT, is);
            is.close();
            umbrellatext16 = new FontRenderer(baseFont.deriveFont(Font.PLAIN, 16 / 2f), 16 / 2f);
            umbrellatext14 = new FontRenderer(baseFont.deriveFont(Font.PLAIN, 14 / 2f), 14 / 2f);
            umbrellatext15 = new FontRenderer(baseFont.deriveFont(Font.PLAIN, 15 / 2f), 15 / 2f);
            umbrellatext17 = new FontRenderer(baseFont.deriveFont(Font.PLAIN, 17 / 2f), 17 / 2f);
            umbrellatext18 = new FontRenderer(baseFont.deriveFont(Font.PLAIN, 18 / 2f), 18 / 2f);
            umbrellatext20 = new FontRenderer(baseFont.deriveFont(Font.PLAIN, 20 / 2f), 20 / 2f);
            umbrellatext22 = new FontRenderer(baseFont.deriveFont(Font.PLAIN, 22 / 2f), 22 / 2f);
        } catch (IOException | FontFormatException e) {
            e.printStackTrace();
            throw new RuntimeException("Ошибка при инициализации шрифтов: " + e.getMessage());
        }
    }
}
