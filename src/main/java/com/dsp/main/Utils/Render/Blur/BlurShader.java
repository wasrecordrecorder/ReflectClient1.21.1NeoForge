package com.dsp.main.Utils.Render.Blur;

import com.dsp.main.Utils.Render.ColorUtil;
import com.dsp.main.Utils.Render.ModShaders;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.CompiledShaderProgram;
import org.lwjgl.opengl.GL30;

public class BlurShader {
    private final Window window;
    private final RenderTarget input;

    public BlurShader() {
        this.window = Minecraft.getInstance().getWindow();
        this.input = new TextureTarget(window.getWidth(), window.getHeight(), false, Minecraft.ON_OSX);
    }

    public static void setupBuffer(RenderTarget frameBuffer) {
        if (frameBuffer.width != Minecraft.getInstance().getMainRenderTarget().width || frameBuffer.height != (Minecraft.getInstance().getMainRenderTarget().height)) {
            frameBuffer.resize(Minecraft.getInstance().getMainRenderTarget().width, Minecraft.getInstance().getMainRenderTarget().height);
        } else {
            frameBuffer.clear();
        }
    }

    public void setParameters(float x, float y, float width, float height, float radius, int color, float quality, float brightness) {
        setupBuffer(input);

        CompiledShaderProgram compiled = RenderSystem.setShader(ModShaders.BLUR_SHADER);
        if (compiled == null) {
            return;
        }

        float guiScale = (float) window.getGuiScale();

        var radiusUniform = compiled.getUniform("radius");
        if (radiusUniform != null) {
            radiusUniform.set(radius * guiScale);
        }

        var uLocationUniform = compiled.getUniform("uLocation");
        if (uLocationUniform != null) {
            uLocationUniform.set(x * guiScale, -y * guiScale + (float) window.getGuiScaledHeight() * guiScale - height * guiScale);
        }

        var uSizeUniform = compiled.getUniform("uSize");
        if (uSizeUniform != null) {
            uSizeUniform.set(width * guiScale, height * guiScale);
        }

        var brightnessUniform = compiled.getUniform("Brightness");
        if (brightnessUniform != null) {
            brightnessUniform.set(brightness);
        }

        var qualityUniform = compiled.getUniform("Quality");
        if (qualityUniform != null) {
            qualityUniform.set(quality);
        }

        var color1Uniform = compiled.getUniform("color1");
        if (color1Uniform != null) {
            color1Uniform.set(ColorUtil.r(color), ColorUtil.g(color), ColorUtil.b(color), ColorUtil.a(color));
        }
    }

    public void bind() {
        if (ModShaders.BLUR_SHADER == null) {
            return;
        }

        RenderTarget renderTarget = Minecraft.getInstance().getMainRenderTarget();
        input.bindWrite(false);
        GL30.glBindFramebuffer(36008, renderTarget.frameBufferId);
        GL30.glBlitFramebuffer(0, 0, renderTarget.width, renderTarget.height, 0, 0, renderTarget.width, renderTarget.height, 16384, 9729);
        renderTarget.bindWrite(false);

        CompiledShaderProgram compiled = RenderSystem.setShader(ModShaders.BLUR_SHADER);
        if (compiled == null) {
            return;
        }

        var inputResolutionUniform = compiled.getUniform("InputResolution");
        if (inputResolutionUniform != null) {
            inputResolutionUniform.set((float) renderTarget.width, (float) renderTarget.height);
        }

        compiled.bindSampler("InputSampler", input.getColorTextureId());
    }

    public void unbind() {
        RenderSystem.setShader(ModShaders.BLUR_SHADER);
    }
}