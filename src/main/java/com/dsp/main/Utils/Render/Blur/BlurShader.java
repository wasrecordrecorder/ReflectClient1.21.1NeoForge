package com.dsp.main.Utils.Render.Blur;

import com.dsp.main.Utils.Render.ColorUtil;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL30;

public class BlurShader extends Shader {
    private final Uniform uSize;
    private final Uniform uLocation;
    private final Uniform topLeftRadius;
    private final Uniform topRightRadius;
    private final Uniform bottomRightRadius;
    private final Uniform bottomLeftRadius;
    private final Uniform inputResolution;
    private final Uniform brightness;
    private final Uniform quality;
    private final Uniform color1;
    private final Window window;
    private final RenderTarget input;

    public BlurShader() {
        super("blur", DefaultVertexFormat.POSITION);
        this.inputResolution = uniform("InputResolution");
        this.brightness = uniform("Brightness");
        this.quality = uniform("Quality");
        this.color1 = uniform("color1");
        this.uSize = uniform("uSize");
        this.uLocation = uniform("uLocation");
        this.topLeftRadius = uniform("topLeftRadius");
        this.topRightRadius = uniform("topRightRadius");
        this.bottomRightRadius = uniform("bottomRightRadius");
        this.bottomLeftRadius = uniform("bottomLeftRadius");
        this.window = Minecraft.getInstance().getWindow();
        this.input = new TextureTarget(window.getWidth(), window.getHeight(), false, Minecraft.ON_OSX);
    }

    public static void setupBuffer(RenderTarget frameBuffer) {
        if (frameBuffer.width != Minecraft.getInstance().getMainRenderTarget().width || frameBuffer.height != Minecraft.getInstance().getMainRenderTarget().height) {
            frameBuffer.resize(Minecraft.getInstance().getMainRenderTarget().width, Minecraft.getInstance().getMainRenderTarget().height, Minecraft.ON_OSX);
        } else {
            frameBuffer.clear(Minecraft.ON_OSX);
        }
    }

    public void setParameters(float x, float y, float width, float height, float radius, int color, float blurStrength, float blurOpacity) {
        setupBuffer(input);
        float guiScale = (float) window.getGuiScale();
        topLeftRadius.set(radius * guiScale);
        topRightRadius.set(radius * guiScale);
        bottomRightRadius.set(radius * guiScale);
        bottomLeftRadius.set(radius * guiScale);
        uLocation.set(x * guiScale, -y * guiScale + (float) window.getGuiScaledHeight() * guiScale - height * guiScale);
        uSize.set(width * guiScale, height * guiScale);
        brightness.set(blurOpacity);
        quality.set(blurStrength);
        color1.set(ColorUtil.r(color), ColorUtil.g(color), ColorUtil.b(color), ColorUtil.a(color));
    }

    public void setParameters(float x, float y, float width, float height, float topLeftRadiusValue, float topRightRadiusValue, float bottomRightRadiusValue, float bottomLeftRadiusValue, int color, float blurStrength, float blurOpacity) {
        setupBuffer(input);
        float guiScale = (float) window.getGuiScale();
        topLeftRadius.set(topLeftRadiusValue * guiScale);
        topRightRadius.set(topRightRadiusValue * guiScale);
        bottomRightRadius.set(bottomRightRadiusValue * guiScale);
        bottomLeftRadius.set(bottomLeftRadiusValue * guiScale);
        uLocation.set(x * guiScale, -y * guiScale + (float) window.getGuiScaledHeight() * guiScale - height * guiScale);
        uSize.set(width * guiScale, height * guiScale);
        brightness.set(blurOpacity);
        quality.set(blurStrength);
        color1.set(ColorUtil.r(color), ColorUtil.g(color), ColorUtil.b(color), ColorUtil.a(color));
    }

    @Override
    public void bind() {
        RenderTarget renderTarget = Minecraft.getInstance().getMainRenderTarget();
        input.bindWrite(false);
        GL30.glBindFramebuffer(36008, renderTarget.frameBufferId);
        GL30.glBlitFramebuffer(0, 0, renderTarget.width, renderTarget.height, 0, 0, renderTarget.width, renderTarget.height, 16384, 9729);
        renderTarget.bindWrite(false);
        inputResolution.set((float) renderTarget.width, (float) renderTarget.height);
        setSample("InputSampler", input.getColorTextureId());
        super.bind();
    }
}