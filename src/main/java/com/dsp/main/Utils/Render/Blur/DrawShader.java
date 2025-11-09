package com.dsp.main.Utils.Render.Blur;

import com.dsp.main.Utils.Render.Mine;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import org.joml.Matrix4f;

public class DrawShader implements Mine {

    static BlurShader BLUR_SHADER = new BlurShader();

    public static void init() {
    }

    public static void drawRoundBlur(PoseStack matrices, float x, float y, float width, float height, float radius, int c1) {
        drawRoundBlur(matrices, x, y, width, height, radius, c1, 20, 0.55f);
    }

    public static void drawRoundBlur(PoseStack poseStack, float x, float y, float width, float height, float radius, int color, float blurStrenth, float blurOpacity) {
        setupRender();
        BLUR_SHADER.setParameters(x, y, width, height, radius, color, blurStrenth, blurOpacity);
        BLUR_SHADER.bind();
        drawQuadsTex(poseStack.last().pose(), x, y, width, height);
        BLUR_SHADER.unbind();
        endRender();
    }

    public static void setupRender() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    public static void endRender() {
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    public static void drawQuadsTex(Matrix4f matrix, float x, float y, float width, float height) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.addVertex(matrix, x, y, 0.0F).setUv(0.0F, 0.0F);
        bufferBuilder.addVertex(matrix, x, y + height, 0.0F).setUv(0.0F, 1.0F);
        bufferBuilder.addVertex(matrix, x + width, y + height, 0.0F).setUv(1.0F, 1.0F);
        bufferBuilder.addVertex(matrix, x + width, y, 0.0F).setUv(1.0F, 0.0F);

        MeshData meshData = bufferBuilder.build();
        if (meshData != null) {
            BufferUploader.drawWithShader(meshData);
        }
    }
}