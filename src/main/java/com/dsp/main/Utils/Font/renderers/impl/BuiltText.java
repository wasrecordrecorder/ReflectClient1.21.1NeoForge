package com.dsp.main.Utils.Font.renderers.impl;

import com.dsp.main.Utils.Font.msdf.MsdfFont;
import com.dsp.main.Utils.Font.providers.ColorProvider;
import com.dsp.main.Utils.Font.providers.ResourceProvider;
import com.dsp.main.Utils.Font.renderers.IRenderer;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

public record BuiltText(
		MsdfFont font,
		String text,
		float size,
		float thickness,
		int color,
		float smoothness,
		float spacing,
		int outlineColor,
		float outlineThickness
) implements IRenderer {

	private static ShaderInstance msdfFontShader;
	private static Tesselator tesselator;

	public static void setMsdfFontShader(ShaderInstance shader) {
		msdfFontShader = shader;
	}

	@Override
	public void render(Matrix4f matrix, float x, float y, float z) {
		if (text == null || text.isEmpty()) {
			return;
		}
		if (msdfFontShader == null) {
			try {
				ResourceLocation shaderLocation = ResourceProvider.getShaderIdentifier("msdf_font");
				msdfFontShader = new ShaderInstance(Minecraft.getInstance().getResourceManager(), shaderLocation, DefaultVertexFormat.POSITION_TEX_COLOR);
			} catch (Exception e) {
				throw new RuntimeException("Failed to load MSDF font shader", e);
			}
		}

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableCull();

		RenderSystem.setShaderTexture(0, this.font.getTextureId());

		boolean outlineEnabled = (this.outlineThickness > 0.0f);
		RenderSystem.setShader(() -> msdfFontShader);
		msdfFontShader.getUniform("Range").set(this.font.getAtlas().range());
		msdfFontShader.getUniform("Thickness").set(this.thickness);
		msdfFontShader.getUniform("Smoothness").set(this.smoothness);
		msdfFontShader.getUniform("Outline").set(outlineEnabled ? 1 : 0);

		if (outlineEnabled) {
			msdfFontShader.getUniform("OutlineThickness").set(this.outlineThickness);
			float[] outlineComponents = ColorProvider.normalize(this.outlineColor);
			msdfFontShader.getUniform("OutlineColor").set(outlineComponents[0], outlineComponents[1],
					outlineComponents[2], outlineComponents[3]);
		}

		BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
		this.font.applyGlyphs(matrix, builder, this.text, this.size,
				(this.thickness + this.outlineThickness * 0.5f) * 0.5f * this.size, this.spacing,
				x, y + this.font.getMetrics().baselineHeight() * this.size, z, this.color);

		try {
			BufferUploader.drawWithShader(builder.buildOrThrow());
		} catch (IllegalStateException e) {
			System.err.println("Failed to render text: '" + text + "'. Buffer was empty.");
		}

		RenderSystem.setShaderTexture(0, 0);

		RenderSystem.enableCull();
		RenderSystem.disableBlend();
	}
}