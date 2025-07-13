package com.dsp.main.Utils.Render;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.*;
import org.lwjgl.opengl.GL11;


import java.awt.*;
import java.lang.Math;
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

    public static void drawSemiRoundRect(PoseStack matrices, float x, float y, float width, float height, float rounding1, float rounding2,float rounding3,float rounding4, int color) {
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

    public static void rectRGB(PoseStack matrices, float x, float y, float width, float height, float rounding, int color, int color2, int color3, int color4) {
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
    public static void rectangleOutline(PoseStack matrices, float x, float y, float width, float height, float lineWidth, int color) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.lineWidth(lineWidth);

        Matrix4f matrix = matrices.last().pose();
        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);

        float r = ColorUtil.getRed(color) / 255f;
        float g = ColorUtil.getGreen(color) / 255f;
        float b = ColorUtil.getBlue(color) / 255f;
        float a = ColorUtil.getAlpha(color) / 255f;

        // Определяем четыре угла прямоугольника
        bufferBuilder.addVertex(matrix, x, y, 0).setColor(r, g, b, a);
        bufferBuilder.addVertex(matrix, x + width, y, 0).setColor(r, g, b, a);
        bufferBuilder.addVertex(matrix, x + width, y + height, 0).setColor(r, g, b, a);
        bufferBuilder.addVertex(matrix, x, y + height, 0).setColor(r, g, b, a);

        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
        RenderSystem.disableBlend();
    }
    public static void drawCircle(PoseStack matrices, float centerX, float centerY, float radius, int color) {
        int segments = 32; // Количество сегментов для аппроксимации круга
        float angleStep = (float) (2 * Math.PI / segments);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = matrices.last().pose();
        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);

        float r = ColorUtil.getRed(color) / 255f;
        float g = ColorUtil.getGreen(color) / 255f;
        float b = ColorUtil.getBlue(color) / 255f;
        float a = ColorUtil.getAlpha(color) / 255f;

        // Центр круга
        bufferBuilder.addVertex(matrix, centerX, centerY, 0).setColor(r, g, b, a);

        // Вершины по окружности
        for (int i = 0; i <= segments; i++) {
            float angle = i * angleStep;
            float x = centerX + radius * (float) Math.cos(angle);
            float y = centerY + radius * (float) Math.sin(angle);
            bufferBuilder.addVertex(matrix, x, y, 0).setColor(r, g, b, a);
        }

        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
        RenderSystem.disableBlend();
    }
    private static final float LINE_WIDTH = 1.0f;
    public static void drawBox(PoseStack matrixStack, BlockPos pos, float partialTicks, Color color) {
        Vec3 playerPos = Minecraft.getInstance().player.getEyePosition(partialTicks);
        double renderX = pos.getX() - playerPos.x;
        double renderY = pos.getY() - playerPos.y;
        double renderZ = pos.getZ() - playerPos.z;
        if (playerPos.distanceTo(new Vec3(pos.getX(), pos.getY(), pos.getZ())) > 64) return;

        matrixStack.pushPose();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.lineWidth(LINE_WIDTH);
        RenderSystem.setShaderColor(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f);

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tessellator.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);
        drawCubeEdges(bufferBuilder, renderX, renderY, renderZ, color);
        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        matrixStack.popPose();
    }

    private static void drawCubeEdges(BufferBuilder bufferBuilder, double x, double y, double z, Color color) {
        double[][] vertices = {
                {x, y, z}, {x + 1, y, z}, {x + 1, y, z}, {x + 1, y + 1, z},
                {x + 1, y + 1, z}, {x, y + 1, z}, {x, y + 1, z}, {x, y, z},
                {x, y, z + 1}, {x + 1, y, z + 1}, {x + 1, y, z + 1}, {x + 1, y + 1, z + 1},
                {x + 1, y + 1, z + 1}, {x, y + 1, z + 1}, {x, y + 1, z + 1}, {x, y, z + 1},
                {x, y, z}, {x, y, z + 1}, {x + 1, y, z}, {x + 1, y, z + 1},
                {x + 1, y + 1, z}, {x + 1, y + 1, z + 1}, {x, y + 1, z}, {x, y + 1, z + 1},
        };
        float r = color.getRed() / 255.0f;
        float g = color.getGreen() / 255.0f;
        float b = color.getBlue() / 255.0f;
        float a = color.getAlpha() / 255.0f;
        for (int i = 0; i < vertices.length; i += 2) {
            bufferBuilder.addVertex((float) vertices[i][0], (float) vertices[i][1], (float) vertices[i][2]).setColor(r, g, b, a);
            bufferBuilder.addVertex((float)vertices[i + 1][0], (float)vertices[i + 1][1], (float)vertices[i + 1][2]).setColor(r, g, b, a);
        }
    }

    public static void drawBox(double x, double y, double width, double height, double size, int color) {
        drawRectBuilding(x + size, y, width - size, y + size, color);
        drawRectBuilding(x, y, x + size, height, color);

        drawRectBuilding(width - size, y, width, height, color);
        drawRectBuilding(x + size, height - size, width - size, height, color);
    }

    public static void drawBoxTest(double x, double y, double width, double height, double size, Vector4f colors) {
        drawMCHorizontalBuilding(x + size, y, width - size, y + size, (int) colors.x(), (int) colors.z());
        drawMCVerticalBuilding(x, y, x + size, height, (int) colors.z(), (int) colors.x());

        drawMCVerticalBuilding(width - size, y, width, height, (int) colors.x(), (int) colors.z());
        drawMCHorizontalBuilding(x + size, height - size, width - size, height, (int) colors.z(), (int) colors.x());
    }

    public static void drawRectBuilding(
            double left,
            double top,
            double right,
            double bottom,
            int color) {
        if (left < right) {
            double i = left;
            left = right;
            right = i;
        }

        if (top < bottom) {
            double j = top;
            top = bottom;
            bottom = j;
        }

        float f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferbuilder.addVertex((float)left, (float)bottom, 0.0F).setColor(f, f1, f2, f3);
        bufferbuilder.addVertex((float)right, (float)bottom, 0.0F).setColor(f, f1, f2, f3);
        bufferbuilder.addVertex((float)right, (float)top, 0.0F).setColor(f, f1, f2, f3);
        bufferbuilder.addVertex((float)left, (float)top, 0.0F).setColor(f, f1, f2, f3);
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
    }

    public static void drawMCHorizontalBuilding(double x, double y, double width, double height, int start, int end) {
        float f = (float) (start >> 24 & 255) / 255.0F;
        float f1 = (float) (start >> 16 & 255) / 255.0F;
        float f2 = (float) (start >> 8 & 255) / 255.0F;
        float f3 = (float) (start & 255) / 255.0F;
        float f4 = (float) (end >> 24 & 255) / 255.0F;
        float f5 = (float) (end >> 16 & 255) / 255.0F;
        float f6 = (float) (end >> 8 & 255) / 255.0F;
        float f7 = (float) (end & 255) / 255.0F;

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferbuilder.addVertex((float) x, (float) height, 0f).setColor(f1, f2, f3, f);
        bufferbuilder.addVertex((float) width, (float) height, 0f).setColor(f5, f6, f7, f4);
        bufferbuilder.addVertex((float) width, (float) y, 0f).setColor(f5, f6, f7, f4);
        bufferbuilder.addVertex((float) x, (float) y, 0f).setColor(f1, f2, f3, f);
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
    }

    public static void drawMCVerticalBuilding(double x, double y, double width, double height, int start, int end) {
        float f = (float) (start >> 24 & 255) / 255.0F;
        float f1 = (float) (start >> 16 & 255) / 255.0F;
        float f2 = (float) (start >> 8 & 255) / 255.0F;
        float f3 = (float) (start & 255) / 255.0F;
        float f4 = (float) (end >> 24 & 255) / 255.0F;
        float f5 = (float) (end >> 16 & 255) / 255.0F;
        float f6 = (float) (end >> 8 & 255) / 255.0F;
        float f7 = (float) (end & 255) / 255.0F;

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferbuilder.addVertex((float) x, (float) height, 0f).setColor(f1, f2, f3, f);
        bufferbuilder.addVertex((float) width, (float) height, 0f).setColor(f1, f2, f3, f);
        bufferbuilder.addVertex((float) width, (float) y, 0f).setColor(f5, f6, f7, f4);
        bufferbuilder.addVertex((float) x, (float) y, 0f).setColor(f5, f6, f7, f4);
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
    }
}
