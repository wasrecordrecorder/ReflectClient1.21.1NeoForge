package com.dsp.main.Utils.Render.Blur;

import com.dsp.main.Utils.Render.Mine;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;


import java.io.IOException;

import static com.dsp.main.Api.mc;

public class Shader implements Mine {
    protected ShaderInstance program;

    public Shader(String name, VertexFormat vertexFormat) {
        try {
            this.program = new ShaderInstance(mc.getResourceManager(), name, vertexFormat);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }


    public static Shader create(String name, VertexFormat vertexFormat) {
        return new Shader(name, vertexFormat);
    }

    public Uniform uniform(String name) {
        return this.program.getUniform(name);
    }

    public void setSample(String name, int id) {
        this.program.setSampler(name, id);
    }

    public void bind() {
        RenderSystem.setShader(() -> this.program);
    }

    public void unbind() {
        RenderSystem.setShader(() -> null);
    }

    public static void drawQuads(Matrix4f matrix4f, float x, float y, float width, float height) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        bufferBuilder.addVertex(matrix4f, x, y, 0.0F);
        bufferBuilder.addVertex(matrix4f, x, y + height, 0.0F);
        bufferBuilder.addVertex(matrix4f, x + width, y + height, 0.0F);
        bufferBuilder.addVertex(matrix4f, x + width, y, 0.0F);
        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
    }

    public static void drawQuadsTex(Matrix4f matrix, float x, float y, float width, float height) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.addVertex(matrix, x, y, 0.0F).setUv(0.0F, 0.0F);
        bufferBuilder.addVertex(matrix, x, y + height, 0.0F).setUv(0.0F, 1.0F);
        bufferBuilder.addVertex(matrix, x + width, y + height, 0.0F).setUv(1.0F, 1.0F);
        bufferBuilder.addVertex(matrix, x + width, y, 0.0F).setUv(1.0F, 0.0F);
        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
    }
}