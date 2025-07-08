package com.dsp.main.Utils.Font.renderers.impl;

import com.mojang.blaze3d.vertex.*;
import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.client.renderer.ShaderInstance;
import com.dsp.main.Utils.Font.builders.states.QuadColorState;
import com.dsp.main.Utils.Font.builders.states.QuadRadiusState;
import com.dsp.main.Utils.Font.builders.states.SizeState;
import com.dsp.main.Utils.Font.providers.ResourceProvider;
import com.dsp.main.Utils.Font.renderers.IRenderer;

public record BuiltBorder(
        SizeState size,
        QuadRadiusState radius,
        QuadColorState color,
        float thickness,
        float internalSmoothness,
        float externalSmoothness
) implements IRenderer {

    private static ShaderInstance borderShader;
    private static Tesselator tesselator;
    public static void setBorderShader(ShaderInstance shader) {
        borderShader = shader;
    }

    @Override
    public void render(Matrix4f matrix, float x, float y, float z) {
        if (borderShader == null) {
            try {
                ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
                ResourceLocation shaderLocation = ResourceProvider.getShaderIdentifier("border");
                borderShader = new ShaderInstance(resourceManager, shaderLocation, DefaultVertexFormat.POSITION_COLOR);
            } catch (Exception e) {
                throw new RuntimeException("Failed to load border shader", e);
            }
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();

        float width = this.size.width(), height = this.size.height();
        RenderSystem.setShader(() -> borderShader);
        borderShader.getUniform("Size").set(width, height);
        borderShader.getUniform("Radius").set(this.radius.radius1(), this.radius.radius2(),
                this.radius.radius3(), this.radius.radius4());
        borderShader.getUniform("Thickness").set(thickness);
        borderShader.getUniform("Smoothness").set(this.internalSmoothness, this.externalSmoothness);

        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        int c1 = this.color.color1();
        float r1 = ((c1 >> 16) & 0xFF) / 255.0f;
        float g1 = ((c1 >> 8) & 0xFF) / 255.0f;
        float b1 = (c1 & 0xFF) / 255.0f;
        float a1 = ((c1 >> 24) & 0xFF) / 255.0f;
        builder.addVertex(matrix, x, y, z).setColor(r1, g1, b1, a1);

        int c2 = this.color.color2();
        float r2 = ((c2 >> 16) & 0xFF) / 255.0f;
        float g2 = ((c2 >> 8) & 0xFF) / 255.0f;
        float b2 = (c2 & 0xFF) / 255.0f;
        float a2 = ((c2 >> 24) & 0xFF) / 255.0f;
        builder.addVertex(matrix, x, y + height, z).setColor(r2, g2, b2, a2);

        int c3 = this.color.color3();
        float r3 = ((c3 >> 16) & 0xFF) / 255.0f;
        float g3 = ((c3 >> 8) & 0xFF) / 255.0f;
        float b3 = (c3 & 0xFF) / 255.0f;
        float a3 = ((c3 >> 24) & 0xFF) / 255.0f;
        builder.addVertex(matrix, x + width, y + height, z).setColor(r3, g3, b3, a3);

        int c4 = this.color.color4();
        float r4 = ((c4 >> 16) & 0xFF) / 255.0f;
        float g4 = ((c4 >> 8) & 0xFF) / 255.0f;
        float b4 = (c4 & 0xFF) / 255.0f;
        float a4 = ((c4 >> 24) & 0xFF) / 255.0f;
        builder.addVertex(matrix, x + width, y, z).setColor(r4, g4, b4, a4);

        BufferUploader.drawWithShader(builder.buildOrThrow());

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }
}