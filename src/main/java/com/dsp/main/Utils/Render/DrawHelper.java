package com.dsp.main.Utils.Render;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.CoreShaders;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.renderer.CompiledShaderProgram;
import org.joml.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.Objects;

public class DrawHelper implements Mine {

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

        ms.translate(centerX, centerY, 0);
        ms.scale(scale, scale, scale);
        ms.translate(-centerX, -centerY, 0);
        runnable.run();
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
        if (ModShaders.RECTANGLE_SHADER == null) {
            return;
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        CompiledShaderProgram compiled = RenderSystem.setShader(ModShaders.RECTANGLE_SHADER);
        if (compiled == null) {
            RenderSystem.disableBlend();
            return;
        }

        Window window = mc.getWindow();
        float guiScale = (float) window.getGuiScale();

        var posUniform = compiled.getUniform("position");
        if (posUniform != null) {
            posUniform.set(x * guiScale, window.getHeight() - (y * guiScale) - (height * guiScale));
        }

        var sizeUniform = compiled.getUniform("size");
        if (sizeUniform != null) {
            sizeUniform.set(width * guiScale, height * guiScale);
        }

        var roundingUniform = compiled.getUniform("rounding");
        if (roundingUniform != null) {
            roundingUniform.set(rounding * guiScale, rounding * guiScale, rounding * guiScale, rounding * guiScale);
        }

        var smoothnessUniform = compiled.getUniform("smoothness");
        if (smoothnessUniform != null) {
            smoothnessUniform.set(0F, 2F);
        }

        var color1Uniform = compiled.getUniform("color1");
        if (color1Uniform != null) {
            color1Uniform.set(
                    ColorUtil.getRed(color) / 255F,
                    ColorUtil.getGreen(color) / 255F,
                    ColorUtil.getBlue(color) / 255F,
                    ColorUtil.getAlpha(color) / 255F
            );
        }

        var color2Uniform = compiled.getUniform("color2");
        if (color2Uniform != null) {
            color2Uniform.set(
                    ColorUtil.getRed(color) / 255F,
                    ColorUtil.getGreen(color) / 255F,
                    ColorUtil.getBlue(color) / 255F,
                    ColorUtil.getAlpha(color) / 255F
            );
        }

        var color3Uniform = compiled.getUniform("color3");
        if (color3Uniform != null) {
            color3Uniform.set(
                    ColorUtil.getRed(color) / 255F,
                    ColorUtil.getGreen(color) / 255F,
                    ColorUtil.getBlue(color) / 255F,
                    ColorUtil.getAlpha(color) / 255F
            );
        }

        var color4Uniform = compiled.getUniform("color4");
        if (color4Uniform != null) {
            color4Uniform.set(
                    ColorUtil.getRed(color) / 255F,
                    ColorUtil.getGreen(color) / 255F,
                    ColorUtil.getBlue(color) / 255F,
                    ColorUtil.getAlpha(color) / 255F
            );
        }

        Matrix4f model = matrices.last().pose();
        Tesselator tessellator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferBuilder = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);

        bufferBuilder.addVertex(model, x, y, 0);
        bufferBuilder.addVertex(model, x, y + height, 0);
        bufferBuilder.addVertex(model, x + width, y + height, 0);
        bufferBuilder.addVertex(model, x + width, y, 0);

        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());

        RenderSystem.disableBlend();
    }

    public static void drawSemiRoundRect(PoseStack matrices, float x, float y, float width, float height, float rounding1, float rounding2, float rounding3, float rounding4, int color) {
        if (ModShaders.RECTANGLE_SHADER == null) {
            return;
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        CompiledShaderProgram compiled = RenderSystem.setShader(ModShaders.RECTANGLE_SHADER);
        if (compiled == null) {
            RenderSystem.disableBlend();
            return;
        }

        Window window = mc.getWindow();
        float guiScale = (float) window.getGuiScale();

        var posUniform = compiled.getUniform("position");
        if (posUniform != null) {
            posUniform.set(x * guiScale, window.getHeight() - (y * guiScale) - (height * guiScale));
        }

        var sizeUniform = compiled.getUniform("size");
        if (sizeUniform != null) {
            sizeUniform.set(width * guiScale, height * guiScale);
        }

        var roundingUniform = compiled.getUniform("rounding");
        if (roundingUniform != null) {
            roundingUniform.set(rounding1 * guiScale, rounding2 * guiScale, rounding3 * guiScale, rounding4 * guiScale);
        }

        var smoothnessUniform = compiled.getUniform("smoothness");
        if (smoothnessUniform != null) {
            smoothnessUniform.set(0F, 2F);
        }

        var color1Uniform = compiled.getUniform("color1");
        if (color1Uniform != null) {
            color1Uniform.set(
                    ColorUtil.getRed(color) / 255F,
                    ColorUtil.getGreen(color) / 255F,
                    ColorUtil.getBlue(color) / 255F,
                    ColorUtil.getAlpha(color) / 255F
            );
        }

        var color2Uniform = compiled.getUniform("color2");
        if (color2Uniform != null) {
            color2Uniform.set(
                    ColorUtil.getRed(color) / 255F,
                    ColorUtil.getGreen(color) / 255F,
                    ColorUtil.getBlue(color) / 255F,
                    ColorUtil.getAlpha(color) / 255F
            );
        }

        var color3Uniform = compiled.getUniform("color3");
        if (color3Uniform != null) {
            color3Uniform.set(
                    ColorUtil.getRed(color) / 255F,
                    ColorUtil.getGreen(color) / 255F,
                    ColorUtil.getBlue(color) / 255F,
                    ColorUtil.getAlpha(color) / 255F
            );
        }

        var color4Uniform = compiled.getUniform("color4");
        if (color4Uniform != null) {
            color4Uniform.set(
                    ColorUtil.getRed(color) / 255F,
                    ColorUtil.getGreen(color) / 255F,
                    ColorUtil.getBlue(color) / 255F,
                    ColorUtil.getAlpha(color) / 255F
            );
        }

        Matrix4f model = matrices.last().pose();
        Tesselator tessellator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferBuilder = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);

        bufferBuilder.addVertex(model, x, y, 0);
        bufferBuilder.addVertex(model, x, y + height, 0);
        bufferBuilder.addVertex(model, x + width, y + height, 0);
        bufferBuilder.addVertex(model, x + width, y, 0);

        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());

        RenderSystem.disableBlend();
    }

    public static void drawTexture(ResourceLocation resourceLocation, Matrix4f matrix4f, float x, float y, float width, float height) {
        RenderSystem.setShaderTexture(0, resourceLocation);
        RenderSystem.setShader(CoreShaders.POSITION_TEX);
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
        RenderSystem.setShader(CoreShaders.POSITION_TEX);
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
        if (ModShaders.RECTANGLE_SHADER == null) {
            return;
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        CompiledShaderProgram compiled = RenderSystem.setShader(ModShaders.RECTANGLE_SHADER);
        if (compiled == null) {
            RenderSystem.disableBlend();
            return;
        }

        Window window = mc.getWindow();
        float guiScale = (float) window.getGuiScale();

        var posUniform = compiled.getUniform("position");
        if (posUniform != null) {
            posUniform.set(x * guiScale, window.getHeight() - (y * guiScale) - (height * guiScale));
        }

        var sizeUniform = compiled.getUniform("size");
        if (sizeUniform != null) {
            sizeUniform.set(width * guiScale, height * guiScale);
        }

        var roundingUniform = compiled.getUniform("rounding");
        if (roundingUniform != null) {
            roundingUniform.set(rounding * guiScale, rounding * guiScale, rounding * guiScale, rounding * guiScale);
        }

        var smoothnessUniform = compiled.getUniform("smoothness");
        if (smoothnessUniform != null) {
            smoothnessUniform.set(0F, 2F);
        }

        var color1Uniform = compiled.getUniform("color1");
        if (color1Uniform != null) {
            color1Uniform.set(
                    ColorUtil.getRed(color) / 255F,
                    ColorUtil.getGreen(color) / 255F,
                    ColorUtil.getBlue(color) / 255F,
                    ColorUtil.getAlpha(color) / 255F
            );
        }

        var color2Uniform = compiled.getUniform("color2");
        if (color2Uniform != null) {
            color2Uniform.set(
                    ColorUtil.getRed(color2) / 255F,
                    ColorUtil.getGreen(color2) / 255F,
                    ColorUtil.getBlue(color2) / 255F,
                    ColorUtil.getAlpha(color2) / 255F
            );
        }

        var color3Uniform = compiled.getUniform("color3");
        if (color3Uniform != null) {
            color3Uniform.set(
                    ColorUtil.getRed(color3) / 255F,
                    ColorUtil.getGreen(color3) / 255F,
                    ColorUtil.getBlue(color3) / 255F,
                    ColorUtil.getAlpha(color3) / 255F
            );
        }

        var color4Uniform = compiled.getUniform("color4");
        if (color4Uniform != null) {
            color4Uniform.set(
                    ColorUtil.getRed(color4) / 255F,
                    ColorUtil.getGreen(color4) / 255F,
                    ColorUtil.getBlue(color4) / 255F,
                    ColorUtil.getAlpha(color4) / 255F
            );
        }

        Matrix4f model = matrices.last().pose();
        Tesselator tessellator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferBuilder = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);

        bufferBuilder.addVertex(model, x, y, 0);
        bufferBuilder.addVertex(model, x, y + height, 0);
        bufferBuilder.addVertex(model, x + width, y + height, 0);
        bufferBuilder.addVertex(model, x + width, y, 0);

        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());

        RenderSystem.disableBlend();
    }

    public static int reAlphaInt(final int color, final int alpha) {
        return (Mth.clamp(alpha, 0, 255) << 24) | (color & 16777215);
    }

    public static boolean isInRegion(final double mouseX, final double mouseY, final float x, final float y, final float width, final float height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private static final float LINE_WIDTH = 1.0f;

    public static void drawBox(PoseStack matrixStack, double x, double y, double z, double height, Color color) {
        matrixStack.pushPose();
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.lineWidth(LINE_WIDTH);
        float r = color.getRed() / 255.0f;
        float g = color.getGreen() / 255.0f;
        float b = color.getBlue() / 255.0f;
        float a = color.getAlpha() / 255.0f;
        RenderSystem.setShaderColor(r, g, b, a);

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tessellator.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);

        bufferBuilder.addVertex((float) x, (float) y, (float) z).setColor(r, g, b, a);
        bufferBuilder.addVertex((float) (x + 1), (float) y, (float) z).setColor(r, g, b, a);

        bufferBuilder.addVertex((float) (x + 1), (float) y, (float) z).setColor(r, g, b, a);
        bufferBuilder.addVertex((float) (x + 1), (float) y, (float) (z + 1)).setColor(r, g, b, a);

        bufferBuilder.addVertex((float) (x + 1), (float) y, (float) (z + 1)).setColor(r, g, b, a);
        bufferBuilder.addVertex((float) x, (float) y, (float) (z + 1)).setColor(r, g, b, a);

        bufferBuilder.addVertex((float) x, (float) y, (float) (z + 1)).setColor(r, g, b, a);
        bufferBuilder.addVertex((float) x, (float) y, (float) z).setColor(r, g, b, a);

        double topY = y + height;
        bufferBuilder.addVertex((float) x, (float) topY, (float) z).setColor(r, g, b, a);
        bufferBuilder.addVertex((float) (x + 1), (float) topY, (float) z).setColor(r, g, b, a);

        bufferBuilder.addVertex((float) (x + 1), (float) topY, (float) z).setColor(r, g, b, a);
        bufferBuilder.addVertex((float) (x + 1), (float) topY, (float) (z + 1)).setColor(r, g, b, a);

        bufferBuilder.addVertex((float) (x + 1), (float) topY, (float) (z + 1)).setColor(r, g, b, a);
        bufferBuilder.addVertex((float) x, (float) topY, (float) (z + 1)).setColor(r, g, b, a);

        bufferBuilder.addVertex((float) x, (float) topY, (float) (z + 1)).setColor(r, g, b, a);
        bufferBuilder.addVertex((float) x, (float) topY, (float) z).setColor(r, g, b, a);

        bufferBuilder.addVertex((float) x, (float) y, (float) z).setColor(r, g, b, a);
        bufferBuilder.addVertex((float) x, (float) topY, (float) z).setColor(r, g, b, a);

        bufferBuilder.addVertex((float) (x + 1), (float) y, (float) z).setColor(r, g, b, a);
        bufferBuilder.addVertex((float) (x + 1), (float) topY, (float) z).setColor(r, g, b, a);

        bufferBuilder.addVertex((float) (x + 1), (float) y, (float) (z + 1)).setColor(r, g, b, a);
        bufferBuilder.addVertex((float) (x + 1), (float) topY, (float) (z + 1)).setColor(r, g, b, a);

        bufferBuilder.addVertex((float) x, (float) y, (float) (z + 1)).setColor(r, g, b, a);
        bufferBuilder.addVertex((float) x, (float) topY, (float) (z + 1)).setColor(r, g, b, a);

        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());

        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        matrixStack.popPose();
    }

    public static void drawBox(PoseStack matrixStack, double x, double y, double width, double height, double size, int color) {
        matrixStack.pushPose();
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        drawRectBuilding(matrixStack, x + size, y, width - size, y + size, color);
        drawRectBuilding(matrixStack, x, y, x + size, height, color);
        drawRectBuilding(matrixStack, width - size, y, width, height, color);
        drawRectBuilding(matrixStack, x + size, height - size, width - size, height, color);
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        matrixStack.popPose();
    }

    public static void drawBoxTest(PoseStack matrixStack, double x, double y, double width, double height, double size, Vector4f colors) {
        matrixStack.pushPose();
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        drawMCHorizontalBuilding(matrixStack, x + size, y, width - size, y + size, (int) colors.x(), (int) colors.z());
        drawMCVerticalBuilding(matrixStack, x, y, x + size, height, (int) colors.z(), (int) colors.x());
        drawMCVerticalBuilding(matrixStack, width - size, y, width, height, (int) colors.x(), (int) colors.z());
        drawMCHorizontalBuilding(matrixStack, x + size, height - size, width - size, height, (int) colors.z(), (int) colors.x());
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        matrixStack.popPose();
    }

    public static void drawRectBuilding(PoseStack matrixStack, double left, double top, double right, double bottom, int color) {
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
        RenderSystem.enableBlend();
        RenderSystem.setShader(CoreShaders.POSITION_TEX_COLOR);
        BufferBuilder bufferbuilder = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferbuilder.addVertex((float) left, (float) bottom, 0.0F).setColor(f, f1, f2, f3);
        bufferbuilder.addVertex((float) right, (float) bottom, 0.0F).setColor(f, f1, f2, f3);
        bufferbuilder.addVertex((float) right, (float) top, 0.0F).setColor(f, f1, f2, f3);
        bufferbuilder.addVertex((float) left, (float) top, 0.0F).setColor(f, f1, f2, f3);
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
        RenderSystem.disableBlend();
    }

    public static void drawMCHorizontalBuilding(PoseStack matrixStack, double x, double y, double width, double height, int start, int end) {
        float f = (float) (start >> 24 & 255) / 255.0F;
        float f1 = (float) (start >> 16 & 255) / 255.0F;
        float f2 = (float) (start >> 8 & 255) / 255.0F;
        float f3 = (float) (start & 255) / 255.0F;
        float f4 = (float) (end >> 24 & 255) / 255.0F;
        float f5 = (float) (end >> 16 & 255) / 255.0F;
        float f6 = (float) (end >> 8 & 255) / 255.0F;
        float f7 = (float) (end & 255) / 255.0F;

        Tesselator tessellator = Tesselator.getInstance();
        RenderSystem.enableBlend();
        RenderSystem.setShader(CoreShaders.POSITION_TEX_COLOR);
        BufferBuilder bufferbuilder = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferbuilder.addVertex((float) x, (float) height, 0f).setColor(f1, f2, f3, f);
        bufferbuilder.addVertex((float) width, (float) height, 0f).setColor(f5, f6, f7, f4);
        bufferbuilder.addVertex((float) width, (float) y, 0f).setColor(f5, f6, f7, f4);
        bufferbuilder.addVertex((float) x, (float) y, 0f).setColor(f1, f2, f3, f);
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
        RenderSystem.disableBlend();
    }

    public static void drawMCVerticalBuilding(PoseStack matrixStack, double x, double y, double width, double height, int start, int end) {
        float f = (float) (start >> 24 & 255) / 255.0F;
        float f1 = (float) (start >> 16 & 255) / 255.0F;
        float f2 = (float) (start >> 8 & 255) / 255.0F;
        float f3 = (float) (start & 255) / 255.0F;
        float f4 = (float) (end >> 24 & 255) / 255.0F;
        float f5 = (float) (end >> 16 & 255) / 255.0F;
        float f6 = (float) (end >> 8 & 255) / 255.0F;
        float f7 = (float) (end & 255) / 255.0F;

        Tesselator tessellator = Tesselator.getInstance();
        RenderSystem.enableBlend();
        RenderSystem.setShader(CoreShaders.POSITION_TEX_COLOR);
        BufferBuilder bufferbuilder = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferbuilder.addVertex((float) x, (float) height, 0f).setColor(f1, f2, f3, f);
        bufferbuilder.addVertex((float) width, (float) height, 0f).setColor(f1, f2, f3, f);
        bufferbuilder.addVertex((float) width, (float) y, 0f).setColor(f5, f6, f7, f4);
        bufferbuilder.addVertex((float) x, (float) y, 0f).setColor(f5, f6, f7, f4);
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
        RenderSystem.disableBlend();
    }

    public static void drawBlockOutline(PoseStack poseStack, BlockPos blockPos, Color lineColor, Color fillColor, float lineWidth) {
        Vec3 camPos = mc.gameRenderer.getMainCamera().getPosition();
        drawBlockOutline(poseStack, blockPos.getX() - camPos.x, blockPos.getY() - camPos.y, blockPos.getZ() - camPos.z, 1, 1, 1, lineColor, fillColor, lineWidth);
    }

    public static void drawBlockOutline(PoseStack poseStack, AABB aabb, Color lineColor, Color fillColor, float lineWidth) {
        Vec3 camPos = mc.gameRenderer.getMainCamera().getPosition();
        drawBlockOutline(poseStack,
                aabb.minX - camPos.x, aabb.minY - camPos.y, aabb.minZ - camPos.z,
                aabb.maxX - aabb.minX, aabb.maxY - aabb.minY, aabb.maxZ - aabb.minZ,
                lineColor, fillColor, lineWidth);
    }

    public static void drawBlockOutline(PoseStack poseStack, double x, double y, double z, double sizeX, double sizeY, double sizeZ, Color lineColor, Color fillColor, float lineWidth) {
        poseStack.pushPose();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();

        Matrix4f matrix = poseStack.last().pose();
        Tesselator tessellator = Tesselator.getInstance();

        float minX = (float) x;
        float minY = (float) y;
        float minZ = (float) z;
        float maxX = (float) (x + sizeX);
        float maxY = (float) (y + sizeY);
        float maxZ = (float) (z + sizeZ);

        float fr = fillColor.getRed() / 255.0f;
        float fg = fillColor.getGreen() / 255.0f;
        float fb = fillColor.getBlue() / 255.0f;
        float fa = fillColor.getAlpha() / 255.0f;

        RenderSystem.setShader(CoreShaders.POSITION_TEX_COLOR);
        BufferBuilder fillBuffer = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        fillBuffer.addVertex(matrix, minX, minY, minZ).setColor(fr, fg, fb, fa);
        fillBuffer.addVertex(matrix, maxX, minY, minZ).setColor(fr, fg, fb, fa);
        fillBuffer.addVertex(matrix, maxX, minY, maxZ).setColor(fr, fg, fb, fa);
        fillBuffer.addVertex(matrix, minX, minY, maxZ).setColor(fr, fg, fb, fa);

        fillBuffer.addVertex(matrix, minX, maxY, minZ).setColor(fr, fg, fb, fa);
        fillBuffer.addVertex(matrix, minX, maxY, maxZ).setColor(fr, fg, fb, fa);
        fillBuffer.addVertex(matrix, maxX, maxY, maxZ).setColor(fr, fg, fb, fa);
        fillBuffer.addVertex(matrix, maxX, maxY, minZ).setColor(fr, fg, fb, fa);

        fillBuffer.addVertex(matrix, minX, minY, minZ).setColor(fr, fg, fb, fa);
        fillBuffer.addVertex(matrix, minX, maxY, minZ).setColor(fr, fg, fb, fa);
        fillBuffer.addVertex(matrix, maxX, maxY, minZ).setColor(fr, fg, fb, fa);
        fillBuffer.addVertex(matrix, maxX, minY, minZ).setColor(fr, fg, fb, fa);

        fillBuffer.addVertex(matrix, minX, minY, maxZ).setColor(fr, fg, fb, fa);
        fillBuffer.addVertex(matrix, maxX, minY, maxZ).setColor(fr, fg, fb, fa);
        fillBuffer.addVertex(matrix, maxX, maxY, maxZ).setColor(fr, fg, fb, fa);
        fillBuffer.addVertex(matrix, minX, maxY, maxZ).setColor(fr, fg, fb, fa);

        fillBuffer.addVertex(matrix, minX, minY, minZ).setColor(fr, fg, fb, fa);
        fillBuffer.addVertex(matrix, minX, minY, maxZ).setColor(fr, fg, fb, fa);
        fillBuffer.addVertex(matrix, minX, maxY, maxZ).setColor(fr, fg, fb, fa);
        fillBuffer.addVertex(matrix, minX, maxY, minZ).setColor(fr, fg, fb, fa);

        fillBuffer.addVertex(matrix, maxX, minY, minZ).setColor(fr, fg, fb, fa);
        fillBuffer.addVertex(matrix, maxX, maxY, minZ).setColor(fr, fg, fb, fa);
        fillBuffer.addVertex(matrix, maxX, maxY, maxZ).setColor(fr, fg, fb, fa);
        fillBuffer.addVertex(matrix, maxX, minY, maxZ).setColor(fr, fg, fb, fa);

        BufferUploader.drawWithShader(fillBuffer.buildOrThrow());

        float lr = lineColor.getRed() / 255.0f;
        float lg = lineColor.getGreen() / 255.0f;
        float lb = lineColor.getBlue() / 255.0f;
        float la = lineColor.getAlpha() / 255.0f;

        RenderSystem.lineWidth(lineWidth);
        BufferBuilder lineBuffer = tessellator.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);

        lineBuffer.addVertex(matrix, minX, minY, minZ).setColor(lr, lg, lb, la);
        lineBuffer.addVertex(matrix, maxX, minY, minZ).setColor(lr, lg, lb, la);
        lineBuffer.addVertex(matrix, maxX, minY, minZ).setColor(lr, lg, lb, la);
        lineBuffer.addVertex(matrix, maxX, minY, maxZ).setColor(lr, lg, lb, la);
        lineBuffer.addVertex(matrix, maxX, minY, maxZ).setColor(lr, lg, lb, la);
        lineBuffer.addVertex(matrix, minX, minY, maxZ).setColor(lr, lg, lb, la);
        lineBuffer.addVertex(matrix, minX, minY, maxZ).setColor(lr, lg, lb, la);
        lineBuffer.addVertex(matrix, minX, minY, minZ).setColor(lr, lg, lb, la);

        lineBuffer.addVertex(matrix, minX, maxY, minZ).setColor(lr, lg, lb, la);
        lineBuffer.addVertex(matrix, maxX, maxY, minZ).setColor(lr, lg, lb, la);
        lineBuffer.addVertex(matrix, maxX, maxY, minZ).setColor(lr, lg, lb, la);
        lineBuffer.addVertex(matrix, maxX, maxY, maxZ).setColor(lr, lg, lb, la);
        lineBuffer.addVertex(matrix, maxX, maxY, maxZ).setColor(lr, lg, lb, la);
        lineBuffer.addVertex(matrix, minX, maxY, maxZ).setColor(lr, lg, lb, la);
        lineBuffer.addVertex(matrix, minX, maxY, maxZ).setColor(lr, lg, lb, la);
        lineBuffer.addVertex(matrix, minX, maxY, minZ).setColor(lr, lg, lb, la);

        lineBuffer.addVertex(matrix, minX, minY, minZ).setColor(lr, lg, lb, la);
        lineBuffer.addVertex(matrix, minX, maxY, minZ).setColor(lr, lg, lb, la);
        lineBuffer.addVertex(matrix, maxX, minY, minZ).setColor(lr, lg, lb, la);
        lineBuffer.addVertex(matrix, maxX, maxY, minZ).setColor(lr, lg, lb, la);
        lineBuffer.addVertex(matrix, maxX, minY, maxZ).setColor(lr, lg, lb, la);
        lineBuffer.addVertex(matrix, maxX, maxY, maxZ).setColor(lr, lg, lb, la);
        lineBuffer.addVertex(matrix, minX, minY, maxZ).setColor(lr, lg, lb, la);
        lineBuffer.addVertex(matrix, minX, maxY, maxZ).setColor(lr, lg, lb, la);

        lineBuffer.addVertex(matrix, minX, minY, minZ).setColor(lr, lg, lb, la);
        lineBuffer.addVertex(matrix, maxX, maxY, maxZ).setColor(lr, lg, lb, la);
        lineBuffer.addVertex(matrix, maxX, minY, minZ).setColor(lr, lg, lb, la);
        lineBuffer.addVertex(matrix, minX, maxY, maxZ).setColor(lr, lg, lb, la);

        lineBuffer.addVertex(matrix, minX, minY, maxZ).setColor(lr, lg, lb, la);
        lineBuffer.addVertex(matrix, maxX, maxY, minZ).setColor(lr, lg, lb, la);
        lineBuffer.addVertex(matrix, maxX, minY, maxZ).setColor(lr, lg, lb, la);
        lineBuffer.addVertex(matrix, minX, maxY, minZ).setColor(lr, lg, lb, la);

        lineBuffer.addVertex(matrix, minX, maxY, minZ).setColor(lr, lg, lb, la);
        lineBuffer.addVertex(matrix, maxX, minY, maxZ).setColor(lr, lg, lb, la);
        lineBuffer.addVertex(matrix, maxX, maxY, minZ).setColor(lr, lg, lb, la);
        lineBuffer.addVertex(matrix, minX, minY, maxZ).setColor(lr, lg, lb, la);

        lineBuffer.addVertex(matrix, minX, maxY, maxZ).setColor(lr, lg, lb, la);
        lineBuffer.addVertex(matrix, maxX, minY, minZ).setColor(lr, lg, lb, la);
        lineBuffer.addVertex(matrix, maxX, maxY, maxZ).setColor(lr, lg, lb, la);
        lineBuffer.addVertex(matrix, minX, minY, minZ).setColor(lr, lg, lb, la);

        BufferUploader.drawWithShader(lineBuffer.buildOrThrow());

        RenderSystem.lineWidth(1.0f);
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        poseStack.popPose();
    }
}