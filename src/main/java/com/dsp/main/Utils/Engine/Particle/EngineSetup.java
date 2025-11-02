package com.dsp.main.Utils.Engine.Particle;

import com.dsp.main.Utils.Minecraft.Chat.ChatUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.io.IOException;

import static net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage.AFTER_PARTICLES;

@EventBusSubscriber(value = Dist.CLIENT)
public final class EngineSetup {

    private static ShaderInstance sinusShader;
    private static RenderType sinusRenderType;
    private static boolean shadersReady = false;

    private EngineSetup() {}

    @SubscribeEvent
    public static void onRegisterShaders(RegisterShadersEvent e) throws IOException {
        ResourceManager rm = Minecraft.getInstance().getResourceManager();
        ResourceLocation loc = ResourceLocation.fromNamespaceAndPath("minecraft", "sinusoid_core");
        sinusShader = new ShaderInstance(rm, loc, DefaultVertexFormat.POSITION_TEX_COLOR);
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(new RenderStateShard.ShaderStateShard(() -> sinusShader))
                .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                .setCullState(RenderStateShard.NO_CULL)
                .createCompositeState(false);
        sinusRenderType = RenderType.create("sinusoid_render",
                DefaultVertexFormat.POSITION_TEX_COLOR,
                VertexFormat.Mode.QUADS,
                256,
                false, true,
                state);
        shadersReady = true;
    }

    @SubscribeEvent
    public static void onRenderWorld(RenderLevelStageEvent e) {
        if (e.getStage() != AFTER_PARTICLES || !shadersReady || sinusShader == null || sinusRenderType == null || SinusoidEngine.getParticles().isEmpty())
            return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (mc.options.keyAttack.isDown()) {
            SinusoidEngine.handleAttackInput();
        }

        Vec3 cam = e.getCamera().getPosition();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(() -> sinusShader);

        try {
            var u = sinusShader.getUniform("ProjectionMatrix");
            if (u != null) u.set(RenderSystem.getProjectionMatrix());
        } catch (Throwable ignored) {}
        try {
            var u = sinusShader.getUniform("ViewModelMatrix");
            if (u != null) u.set(RenderSystem.getModelViewMatrix());
        } catch (Throwable ignored) {}
        try {
            var u = sinusShader.getUniform("Time");
            if (u != null) u.set((float) mc.level.getGameTime() * 0.05f);
        } catch (Throwable ignored) {}
        try {
            var u = sinusShader.getUniform("GlowIntensity");
            if (u != null) u.set(1.0f);
        } catch (Throwable ignored) {}

        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buf = tess.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        for (SinusoidParticle p : SinusoidEngine.getParticles()) {
            Vec3 pos = p.position;
            float na = p.normalizedAge();
            float s = p.getCurrentSize();
            float r = (float) p.getCurrentColor().x;
            float g = (float) p.getCurrentColor().y;
            float b = (float) p.getCurrentColor().z;
            float a = 0.5f;

            double cx = pos.x - cam.x;
            double cy = pos.y - cam.y;
            double cz = pos.z - cam.z;

            float h = s * 0.5f;
            buf.addVertex((float)(cx - h), (float)(cy - h), (float)(cz + h)).setColor(r, g, b, a).setUv(0, 0);
            buf.addVertex((float)(cx + h), (float)(cy - h), (float)(cz + h)).setColor(r, g, b, a).setUv(1, 0);
            buf.addVertex((float)(cx + h), (float)(cy + h), (float)(cz + h)).setColor(r, g, b, a).setUv(1, 1);
            buf.addVertex((float)(cx - h), (float)(cy + h), (float)(cz + h)).setColor(r, g, b, a).setUv(0, 1);
            buf.addVertex((float)(cx + h), (float)(cy - h), (float)(cz - h)).setColor(r, g, b, a).setUv(0, 0);
            buf.addVertex((float)(cx - h), (float)(cy - h), (float)(cz - h)).setColor(r, g, b, a).setUv(1, 0);
            buf.addVertex((float)(cx - h), (float)(cy + h), (float)(cz - h)).setColor(r, g, b, a).setUv(1, 1);
            buf.addVertex((float)(cx + h), (float)(cy + h), (float)(cz - h)).setColor(r, g, b, a).setUv(0, 1);
            buf.addVertex((float)(cx - h), (float)(cy + h), (float)(cz + h)).setColor(r, g, b, a).setUv(0, 0);
            buf.addVertex((float)(cx + h), (float)(cy + h), (float)(cz + h)).setColor(r, g, b, a).setUv(1, 0);
            buf.addVertex((float)(cx + h), (float)(cy + h), (float)(cz - h)).setColor(r, g, b, a).setUv(1, 1);
            buf.addVertex((float)(cx - h), (float)(cy + h), (float)(cz - h)).setColor(r, g, b, a).setUv(0, 1);
            buf.addVertex((float)(cx - h), (float)(cy - h), (float)(cz - h)).setColor(r, g, b, a).setUv(0, 0);
            buf.addVertex((float)(cx + h), (float)(cy - h), (float)(cz - h)).setColor(r, g, b, a).setUv(1, 0);
            buf.addVertex((float)(cx + h), (float)(cy - h), (float)(cz + h)).setColor(r, g, b, a).setUv(1, 1);
            buf.addVertex((float)(cx - h), (float)(cy - h), (float)(cz + h)).setColor(r, g, b, a).setUv(0, 1);
            buf.addVertex((float)(cx + h), (float)(cy - h), (float)(cz + h)).setColor(r, g, b, a).setUv(0, 0);
            buf.addVertex((float)(cx + h), (float)(cy - h), (float)(cz - h)).setColor(r, g, b, a).setUv(1, 0);
            buf.addVertex((float)(cx + h), (float)(cy + h), (float)(cz - h)).setColor(r, g, b, a).setUv(1, 1);
            buf.addVertex((float)(cx + h), (float)(cy + h), (float)(cz + h)).setColor(r, g, b, a).setUv(0, 1);
            buf.addVertex((float)(cx - h), (float)(cy - h), (float)(cz - h)).setColor(r, g, b, a).setUv(0, 0);
            buf.addVertex((float)(cx - h), (float)(cy - h), (float)(cz + h)).setColor(r, g, b, a).setUv(1, 0);
            buf.addVertex((float)(cx - h), (float)(cy + h), (float)(cz + h)).setColor(r, g, b, a).setUv(1, 1);
            buf.addVertex((float)(cx - h), (float)(cy + h), (float)(cz - h)).setColor(r, g, b, a).setUv(0, 1);
        }

        BufferUploader.drawWithShader(buf.buildOrThrow());
        RenderSystem.disableBlend();
    }
}