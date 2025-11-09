package com.dsp.main.Utils.Render.Blur;

import com.dsp.main.Utils.Render.Mine;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.ShaderDefines;
import net.minecraft.client.renderer.ShaderProgram;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

import static com.dsp.main.Api.mc;

public class Shader implements Mine {
    protected final ShaderProgram descriptor;

    public Shader(String name, VertexFormat vertexFormat) {
        ResourceLocation location = ResourceLocation.parse(name);
        this.descriptor = new ShaderProgram(location, vertexFormat, ShaderDefines.EMPTY);
    }

    public Shader(ResourceLocation location, VertexFormat vertexFormat) {
        this.descriptor = new ShaderProgram(location, vertexFormat, ShaderDefines.EMPTY);
    }

    public static Shader create(String name, VertexFormat vertexFormat) {
        return new Shader(name, vertexFormat);
    }

    public static Shader create(ResourceLocation location, VertexFormat format) {
        return new Shader(location, format);
    }

    public ShaderProgram getDescriptor() {
        return this.descriptor;
    }

    public void bind() {
        RenderSystem.setShader(this.descriptor);
    }

    public void unbind() {
        RenderSystem.setShader((ShaderProgram) null);
    }

    public static void drawQuads(Matrix4f matrix4f, float x, float y, float width, float height) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        bufferBuilder.addVertex(matrix4f, x, y, 0.0F);
        bufferBuilder.addVertex(matrix4f, x, y + height, 0.0F);
        bufferBuilder.addVertex(matrix4f, x + width, y + height, 0.0F);
        bufferBuilder.addVertex(matrix4f, x + width, y, 0.0F);

        MeshData meshData = bufferBuilder.build();
        if (meshData != null) {
            BufferUploader.drawWithShader(meshData);
        }
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