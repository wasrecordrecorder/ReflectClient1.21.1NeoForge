package com.dsp.main.Utils.Color;

import net.minecraft.util.Mth;

import java.awt.Color;

public class ColorHelper {
	
	// alpha [0, 255]
	public static Color injectAlpha(Color color, int alpha) {
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
	}
	
	// alpha [0, 1]
	public static Color injectAlpha(Color color, float alpha) {
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(alpha * 255.0f));
	}
	
	public static Color getColor(int color) {
		int r = color >> 16 & 0xFF;
        int g = color >> 8 & 0xFF;
        int b = color & 0xFF;
        int a = color >> 24 & 0xFF;
		return new Color(r, g, b, a);
	}
	
	public static float[] getColorComps(Color color) {
		return new float[] {color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f};
	}
	public static float r(int color) {
		return (float) (color >> 16 & 255) / 255.0F;
	}
	public static float g(int color) {
		return (float) (color >> 8 & 255) / 255.0F;
	}
	public static float b(int color) {
		return (float) (color & 255) / 255.0F;
	}
	public static float a(int color) {
		return (float) (color >> 24 & 255) / 255.0F;
	}
	public static float lerp(float a, float b, float f) {
		return a + f * (b - a);
	}
	public static double interpolate(double current, double old, double scale) {
		return old + (current - old) * scale;
	}

	public static int rgba(int r, int g, int b, int a) {
		return a << 24 | r << 16 | g << 8 | b;
	}
	public static float[] rgba(final int color) {
		return new float[] {
				(color >> 16 & 0xFF) / 255f,
				(color >> 8 & 0xFF) / 255f,
				(color & 0xFF) / 255f,
				(color >> 24 & 0xFF) / 255f
		};
	}

	public static int gradient(int start, int end, int index, int speed) {
		int angle = (int) ((System.currentTimeMillis() / speed + index) % 360);
		angle = (angle > 180 ? 360 - angle : angle) + 180;
		int color = interpolate(start, end, Mth.clamp(angle / 180f - 1, 0, 1));
		float[] hs = rgba(color);
		float[] hsb = Color.RGBtoHSB((int) (hs[0] * 255), (int) (hs[1] * 255), (int) (hs[2] * 255), null);

		hsb[1] *= 1.5F;
		hsb[1] = Math.min(hsb[1], 1.0f);

		return Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
	}
	public static int interpolate(int start, int end, float value) {
		float[] startColor = rgba(start);
		float[] endColor = rgba(end);

		return rgba((int) interpolate(startColor[0] * 255, endColor[0] * 255, value),
				(int) interpolate(startColor[1] * 255, endColor[1] * 255, value),
				(int) interpolate(startColor[2] * 255, endColor[2] * 255, value),
				(int) interpolate(startColor[3] * 255, endColor[3] * 255, value));
	}
	public static int getColor(int index, float mult) {
		int color1 = rgb(109, 10, 40);
		int color2 = rgb(239, 96, 136);
//		if (themeCvet.isEnabled()) {
//			return gradient(Theme.colorOne.getRGB(), Theme.colorTwo.getRGB(), (int) (index * mult), 10);
//		} else {
			return gradient(color1, color2, (int) (index * mult), 10);
//		}
	}

	public static int getColor2(int index, float mult) {
		int color1 = rgb(17, 109, 10);
		int color2 = rgb(122, 239, 96);
		return gradient(color1, color2, (int) (index * mult), 10);
	}

	public static int rgb(int r, int g, int b) {
		return 255 << 24 | r << 16 | g << 8 | b;
	}


	public static Color twoColorEffectTh(Color color1, Color color2, int blendFactor) {
		int red = (color1.getRed() * blendFactor + color2.getRed() * (255 - blendFactor)) / 255;
		int green = (color1.getGreen() * blendFactor + color2.getGreen() * (255 - blendFactor)) / 255;
		int blue = (color1.getBlue() * blendFactor + color2.getBlue() * (255 - blendFactor)) / 255;
		return new Color(red, green, blue);
	}
	public static Color twoColorEffect(Color color1, Color color2, float alpha) {
		float val = Mth.clamp((float) Math.sin(19 * ((Math.abs(System.currentTimeMillis() / 15) / 100.2) / 4 % 1)) / 2 + 0.5f, 0, 1);
		return new Color(lerp((float) color1.getRed() / 255, (float) color2.getRed() / 255, val), lerp((float) color1.getGreen() / 255, (float) color2.getGreen() / 255, val), lerp((float) color1.getBlue() / 255, (float) color2.getBlue() / 255, val), alpha / 255);
	}

	public static Color TwoColoreffect(Color color1, Color color2, int i) {
		float ratio = i / 255.0f;
		int red = (int) ((1 - ratio) * color1.getRed() + ratio * color2.getRed());
		int green = (int) ((1 - ratio) * color1.getGreen() + ratio * color2.getGreen());
		int blue = (int) ((1 - ratio) * color1.getBlue() + ratio * color2.getBlue());
		return new Color(red, green, blue);
	}
}
