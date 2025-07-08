package com.dsp.main.Utils.Font.msdf;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;

import com.dsp.main.Utils.Font.msdf.FontData.BoundsData;
import com.dsp.main.Utils.Font.msdf.FontData.GlyphData;

public final class MsdfGlyph {

	private final int code;
	private final float minU, maxU, minV, maxV;
	private final float advance, topPosition, width, height;
	
	public MsdfGlyph(GlyphData data, float atlasWidth, float atlasHeight) {
		this.code = data.unicode();
		this.advance = data.advance();
		
		BoundsData atlasBounds = data.atlasBounds();
		if (atlasBounds != null) {
			this.minU = atlasBounds.left() / atlasWidth;
			this.maxU = atlasBounds.right() / atlasWidth;
			this.minV = 1.0F - atlasBounds.top() / atlasHeight;
			this.maxV = 1.0F - atlasBounds.bottom() / atlasHeight;
		} else {
			this.minU = this.maxU = this.minV = this.maxV = 0.0f;
		}

		BoundsData planeBounds = data.planeBounds();
		if (planeBounds != null) {
			this.width = planeBounds.right() - planeBounds.left();
			this.height = planeBounds.top() - planeBounds.bottom();
			this.topPosition = planeBounds.top();
		} else {
			this.width = this.height = this.topPosition = 0.0f;
		}
	}

	public float apply(Matrix4f matrix, VertexConsumer consumer, float size, float x, float y, float z, int color) {
		y -= this.topPosition * size;
		float width = this.width * size;
		float height = this.height * size;

		// Unpack color into RGBA components
		float red = ((color >> 16) & 0xFF) / 255.0f;
		float green = ((color >> 8) & 0xFF) / 255.0f;
		float blue = (color & 0xFF) / 255.0f;
		float alpha = ((color >> 24) & 0xFF) / 255.0f;

		// Render quad vertices
		consumer.addVertex(matrix, x, y, z).setColor(red, green, blue, alpha).setUv(this.minU, this.minV);
		consumer.addVertex(matrix, x, y + height, z).setColor(red, green, blue, alpha).setUv(this.minU, this.maxV);
		consumer.addVertex(matrix, x + width, y + height, z).setColor(red, green, blue, alpha).setUv(this.maxU, this.maxV);
		consumer.addVertex(matrix, x + width, y, z).setColor(red, green, blue, alpha).setUv(this.maxU, this.minV);

		return this.advance * size;
	}
	
	public float getWidth(float size) {
		return this.advance * size;
	}

	public int getCharCode() {
		return code;
	}

}