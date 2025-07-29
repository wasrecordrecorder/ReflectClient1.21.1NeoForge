package com.dsp.main.UI.Themes;


import com.dsp.main.Utils.Render.ColorUtils;

public class Theme {
    public String name;
    public int[] colors;

    public String getName() {
        return name;
    }

    public Theme(String name, int... colors) {
        this.name = name;
        this.colors = colors;
    }

    public int getColor(int index) {
        return ColorUtils.gradient(25,
                index, colors);
    }

    public int getColorLowSpeed(int index) {
        return ColorUtils.gradient(50,
                index, colors);
    }
}