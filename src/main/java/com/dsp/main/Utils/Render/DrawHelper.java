package com.dsp.main.Utils.Render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.joml.Matrix4f;


import java.awt.*;
import java.util.Objects;

public class DrawHelper implements Mine {
    static Shader RECTANGLE_SHADER = Shader.create("rectangle", DefaultVertexFormat.POSITION_TEX);


    public static void scale(PoseStack ms, float posX, float posY, float width, float height, float scale, Runnable runnable) {
        float centerX = posX + width / 2;
        float centerY = posY + height / 2;

        ms.pushPose();
        ms.translate(centerX, centerY, 0);
        ms.scale(scale, scale, scale);
        ms.translate(-centerX, -centerY, 0);
        runnable.run();
        ms.popPose();
    }

    public static void rotate(PoseStack ms, float posX, float posY, float width, float height, float angleDegrees, Runnable runnable) {
        float centerX = posX + width / 2;
        float centerY = posY + height / 2;

        ms.translate(centerX, centerY, 0);
        ms.mulPose(Axis.ZP.rotationDegrees(angleDegrees));
        ms.translate(-centerX, -centerY, 0);
        runnable.run();
    }

    public static void scale(Matrix4f ms, float posX, float posY, float width, float height, float scale, Runnable runnable) {
        float centerX = posX + width / 2;
        float centerY = posY + height / 2;

        // ms.pushPose();
        ms.translate(centerX, centerY, 0);
        ms.scale(scale, scale, scale);
        ms.translate(-centerX, -centerY, 0);
        runnable.run();
        //ms.popPose();
    }

    public static void rotate(Matrix4f ms, float posX, float posY, float width, float height, float angleDegrees, Runnable runnable) {
        float centerX = posX + width / 2;
        float centerY = posY + height / 2;

        ms.translate(centerX, centerY, 0);
        ms.rotate(Axis.ZP.rotationDegrees(angleDegrees));
        ms.translate(-centerX, -centerY, 0);
        runnable.run();
    }

    public static void endRender() {
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    public static void rectangle(PoseStack matrices, float x, float y, float width, float height, float rounding, int color) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        Tesselator tessellator = RenderSystem.renderThreadTesselator();

        Window window = mc.getWindow();
        float guiScale = (float) window.getGuiScale();

        RECTANGLE_SHADER.uniform("position").set(x * guiScale, window.getHeight() - (y * guiScale) - (height * guiScale));

        RECTANGLE_SHADER.uniform("size").set(width * guiScale, height * guiScale);
        RECTANGLE_SHADER.uniform("rounding").set(rounding * guiScale, rounding * guiScale, rounding * guiScale, rounding * guiScale);

        RECTANGLE_SHADER.uniform("smoothness").set(0F, 2F);

        RECTANGLE_SHADER.uniform("color1").set(
                ColorUtil.getRed(color) / 255F,
                ColorUtil.getGreen(color) / 255F,
                ColorUtil.getBlue(color) / 255F,
                ColorUtil.getAlpha(color) / 255F
        );

        RECTANGLE_SHADER.uniform("color2").set(
                ColorUtil.getRed(color) / 255F,
                ColorUtil.getGreen(color) / 255F,
                ColorUtil.getBlue(color) / 255F,
                ColorUtil.getAlpha(color) / 255F
        );

        RECTANGLE_SHADER.uniform("color3").set(
                ColorUtil.getRed(color) / 255F,
                ColorUtil.getGreen(color) / 255F,
                ColorUtil.getBlue(color) / 255F,
                ColorUtil.getAlpha(color) / 255F
        );

        RECTANGLE_SHADER.uniform("color4").set(
                ColorUtil.getRed(color) / 255F,
                ColorUtil.getGreen(color) / 255F,
                ColorUtil.getBlue(color) / 255F,
                ColorUtil.getAlpha(color) / 255F
        );

        RECTANGLE_SHADER.bind();

        Matrix4f model = matrices.last().pose();
        BufferBuilder bufferBuilder = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);


        bufferBuilder.addVertex(model, x, y, 0);
        bufferBuilder.addVertex(model, x, y + height, 0);
        bufferBuilder.addVertex(model, x + width, y + height, 0);
        bufferBuilder.addVertex(model, x + width, y, 0);

        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());

        RECTANGLE_SHADER.unbind();

        RenderSystem.disableBlend();
    }

    public void drawSemiRoundRect(PoseStack matrices, float x, float y, float width, float height, float rounding1, float rounding2,float rounding3,float rounding4, int color) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        Tesselator tessellator = RenderSystem.renderThreadTesselator();

        Window window = mc.getWindow();
        float guiScale = (float) window.getGuiScale();

        RECTANGLE_SHADER.uniform("position").set(x * guiScale, window.getHeight() - (y * guiScale) - (height * guiScale));

        RECTANGLE_SHADER.uniform("size").set(width * guiScale, height * guiScale);
        RECTANGLE_SHADER.uniform("rounding").set(rounding1 * guiScale, rounding2 * guiScale, rounding3 * guiScale, rounding4 * guiScale);

        RECTANGLE_SHADER.uniform("smoothness").set(0F, 2F);

        RECTANGLE_SHADER.uniform("color1").set(
                ColorUtil.getRed(color) / 255F,
                ColorUtil.getGreen(color) / 255F,
                ColorUtil.getBlue(color) / 255F,
                ColorUtil.getAlpha(color) / 255F
        );

        RECTANGLE_SHADER.uniform("color2").set(
                ColorUtil.getRed(color) / 255F,
                ColorUtil.getGreen(color) / 255F,
                ColorUtil.getBlue(color) / 255F,
                ColorUtil.getAlpha(color) / 255F
        );

        RECTANGLE_SHADER.uniform("color3").set(
                ColorUtil.getRed(color) / 255F,
                ColorUtil.getGreen(color) / 255F,
                ColorUtil.getBlue(color) / 255F,
                ColorUtil.getAlpha(color) / 255F
        );

        RECTANGLE_SHADER.uniform("color4").set(
                ColorUtil.getRed(color) / 255F,
                ColorUtil.getGreen(color) / 255F,
                ColorUtil.getBlue(color) / 255F,
                ColorUtil.getAlpha(color) / 255F
        );

        RECTANGLE_SHADER.bind();

        Matrix4f model = matrices.last().pose();
        BufferBuilder bufferBuilder = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);


        bufferBuilder.addVertex(model, x, y, 0);
        bufferBuilder.addVertex(model, x, y + height, 0);
        bufferBuilder.addVertex(model, x + width, y + height, 0);
        bufferBuilder.addVertex(model, x + width, y, 0);

        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());

        RECTANGLE_SHADER.unbind();

        RenderSystem.disableBlend();
    }



    public static void drawTexture(ResourceLocation resourceLocation, Matrix4f matrix4f, float x, float y, float width, float height) {
        RenderSystem.setShaderTexture(0, resourceLocation);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        drawQuadsTex(matrix4f, x, y, width, height);
    }

    public static void drawQuadsTex(Matrix4f matrix, float x, float y, float width, float height) {
        Tesselator tesselator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.addVertex(matrix, x, y, 0).setUv(0.0F, 0.0F);
        bufferBuilder.addVertex(matrix, x, y + height, 0).setUv(0.0F, 1.0F);
        bufferBuilder.addVertex(matrix, x + width, y + height, 0).setUv(1.0F, 1.0F);
        bufferBuilder.addVertex(matrix, x + width, y, 0).setUv(1.0F, 0.0F);
        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
    }



    public static void drawHead(PoseStack ms, Player player, float x, float y, float width, float height) {
        ResourceLocation texture = Objects.requireNonNull((Objects.requireNonNull(mc.getConnection())).getPlayerInfo(player.getUUID())).getSkin().texture();
        RenderSystem.clearColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.blendFunc(770, 771);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        Matrix4f matrix4f = ms.last().pose();
        BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.addVertex(matrix4f, x, y, 0).setUv(1.125f, 1.125f);
        bufferbuilder.addVertex(matrix4f, x, y + height, 0).setUv(1.125f, 1.25f);
        bufferbuilder.addVertex(matrix4f, x + width, y + height, 0).setUv(1.25f, 1.25f);
        bufferbuilder.addVertex(matrix4f, x + width, y, 0).setUv(1.25f, 1.125f);
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
    }

    public void rectRGB(PoseStack matrices, float x, float y, float width, float height, float rounding, int color, int color2, int color3, int color4) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        Tesselator tessellator = RenderSystem.renderThreadTesselator();

        Window window = mc.getWindow();
        float guiScale = (float) window.getGuiScale();

        RECTANGLE_SHADER.uniform("position").set(x * guiScale, window.getHeight() - (y * guiScale) - (height * guiScale));

        RECTANGLE_SHADER.uniform("size").set(width * guiScale, height * guiScale);
        RECTANGLE_SHADER.uniform("rounding").set(rounding * guiScale, rounding * guiScale, rounding * guiScale, rounding * guiScale);

        RECTANGLE_SHADER.uniform("smoothness").set(0F, 2F);

        RECTANGLE_SHADER.uniform("color1").set(
                ColorUtil.getRed(color) / 255F,
                ColorUtil.getGreen(color) / 255F,
                ColorUtil.getBlue(color) / 255F,
                ColorUtil.getAlpha(color) / 255F
        );

        RECTANGLE_SHADER.uniform("color2").set(
                ColorUtil.getRed(color2) / 255F,
                ColorUtil.getGreen(color2) / 255F,
                ColorUtil.getBlue(color2) / 255F,
                ColorUtil.getAlpha(color2) / 255F
        );

        RECTANGLE_SHADER.uniform("color3").set(
                ColorUtil.getRed(color3) / 255F,
                ColorUtil.getGreen(color3) / 255F,
                ColorUtil.getBlue(color3) / 255F,
                ColorUtil.getAlpha(color3) / 255F
        );

        RECTANGLE_SHADER.uniform("color4").set(
                ColorUtil.getRed(color4) / 255F,
                ColorUtil.getGreen(color4) / 255F,
                ColorUtil.getBlue(color4) / 255F,
                ColorUtil.getAlpha(color4) / 255F
        );

        RECTANGLE_SHADER.bind();

        Matrix4f model = matrices.last().pose();
        BufferBuilder bufferBuilder = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);


        bufferBuilder.addVertex(model, x, y, 0);
        bufferBuilder.addVertex(model, x, y + height, 0);
        bufferBuilder.addVertex(model, x + width, y + height, 0);
        bufferBuilder.addVertex(model, x + width, y, 0);

        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());

        RECTANGLE_SHADER.unbind();

        RenderSystem.disableBlend();
    }


    public static int reAlphaInt(final int color,
                                 final int alpha) {
        return (Mth.clamp(alpha, 0, 255) << 24) | (color & 16777215);
    }



    public static boolean isInRegion(final double mouseX,
                                     final double mouseY,
                                     final float x,
                                     final float y,
                                     final float width,
                                     final float height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }



}
