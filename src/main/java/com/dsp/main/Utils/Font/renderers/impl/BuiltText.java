package com.dsp.main.Utils.Font.renderers.impl;

import com.dsp.main.Utils.Font.msdf.MsdfFont;
import com.dsp.main.Utils.Font.providers.ColorProvider;
import com.dsp.main.Utils.Font.renderers.IRenderer;
import com.dsp.main.Utils.Render.ModShaders;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.CompiledShaderProgram;
import org.joml.Matrix4f;

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

	@Override
	public void render(Matrix4f matrix, float x, float y, float z) {
		if (text == null || text.isEmpty()) {
			return;
		}

		if (ModShaders.MSDF_FONT_SHADER == null) {
			return;
		}

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableCull();

		RenderSystem.setShaderTexture(0, this.font.getTextureId());

		CompiledShaderProgram compiled = RenderSystem.setShader(ModShaders.MSDF_FONT_SHADER);
		if (compiled == null) {
			RenderSystem.enableCull();
			RenderSystem.disableBlend();
			return;
		}

		boolean outlineEnabled = (this.outlineThickness > 0.0f);

		try {
			var rangeUniform = compiled.getUniform("Range");
			if (rangeUniform != null) {
				rangeUniform.set(this.font.getAtlas().range());
			}
		} catch (Exception ignored) {}

		try {
			var thicknessUniform = compiled.getUniform("Thickness");
			if (thicknessUniform != null) {
				thicknessUniform.set(this.thickness);
			}
		} catch (Exception ignored) {}

		try {
			var smoothnessUniform = compiled.getUniform("Smoothness");
			if (smoothnessUniform != null) {
				smoothnessUniform.set(this.smoothness);
			}
		} catch (Exception ignored) {}

		try {
			var outlineUniform = compiled.getUniform("Outline");
			if (outlineUniform != null) {
				outlineUniform.set(outlineEnabled ? 1 : 0);
			}
		} catch (Exception ignored) {}

		if (outlineEnabled) {
			try {
				var outlineThicknessUniform = compiled.getUniform("OutlineThickness");
				if (outlineThicknessUniform != null) {
					outlineThicknessUniform.set(this.outlineThickness);
				}
			} catch (Exception ignored) {}

			try {
				float[] outlineComponents = ColorProvider.normalize(this.outlineColor);
				var outlineColorUniform = compiled.getUniform("OutlineColor");
				if (outlineColorUniform != null) {
					outlineColorUniform.set(outlineComponents[0], outlineComponents[1],
							outlineComponents[2], outlineComponents[3]);
				}
			} catch (Exception ignored) {}
		}

		BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
		this.font.applyGlyphs(matrix, builder, this.text, this.size,
				(this.thickness + this.outlineThickness * 0.5f) * 0.5f * this.size, this.spacing,
				x, y + this.font.getMetrics().baselineHeight() * this.size, z, this.color);

		MeshData meshData = builder.build();
		if (meshData != null) {
			BufferUploader.drawWithShader(meshData);
		}

		RenderSystem.setShaderTexture(0, 0);
		RenderSystem.enableCull();
		RenderSystem.disableBlend();
	}
}