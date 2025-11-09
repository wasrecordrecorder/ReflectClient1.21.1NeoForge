package com.dsp.main.Utils.Engine.Particle;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.CoreShaders;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import static net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage.AFTER_PARTICLES;

@EventBusSubscriber(value = Dist.CLIENT)
public final class EngineSetup {

    private EngineSetup() {}

    // ❌ УДАЛИЛИ метод onRegisterShaders - больше не нужен!

    @SubscribeEvent
    public static void onRenderWorld(RenderLevelStageEvent event) {
        if (event.getStage() != AFTER_PARTICLES || SinusoidEngine.getParticles().isEmpty())
            return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        if (mc.options.keyAttack.isDown()) {
            SinusoidEngine.handleAttackInput();
        }

        Vec3 cam = event.getCamera().getPosition();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(CoreShaders.PARTICLE);  // ✅ Используем встроенный шейдер

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);  // ✅ Правильный формат

        renderParticles(buffer, cam);

        MeshData meshData = buffer.build();
        if (meshData != null) {
            BufferUploader.drawWithShader(meshData);
        }

        RenderSystem.disableBlend();
    }

    private static void renderParticles(BufferBuilder buffer, Vec3 cam) {
        for (SinusoidParticle p : SinusoidEngine.getParticles()) {
            Vec3 pos = p.position;
            float s = p.getCurrentSize();
            float r = (float) p.getCurrentColor().x;
            float g = (float) p.getCurrentColor().y;
            float b = (float) p.getCurrentColor().z;
            float a = 0.5f;

            double cx = pos.x - cam.x;
            double cy = pos.y - cam.y;
            double cz = pos.z - cam.z;

            float h = s * 0.5f;
            int lightmap = 15728880; // Полная яркость (блок света 15, небо 15)

            // ✅ ПРАВИЛЬНЫЙ ПОРЯДОК для PARTICLE формата:
            // addVertex(x, y, z).setUv(u, v).setColor(r, g, b, a).setLight(lightmap)

            // Передняя грань (Z+)
            buffer.addVertex((float)(cx - h), (float)(cy - h), (float)(cz + h)).setUv(0, 0).setColor(r, g, b, a).setLight(lightmap);
            buffer.addVertex((float)(cx + h), (float)(cy - h), (float)(cz + h)).setUv(1, 0).setColor(r, g, b, a).setLight(lightmap);
            buffer.addVertex((float)(cx + h), (float)(cy + h), (float)(cz + h)).setUv(1, 1).setColor(r, g, b, a).setLight(lightmap);
            buffer.addVertex((float)(cx - h), (float)(cy + h), (float)(cz + h)).setUv(0, 1).setColor(r, g, b, a).setLight(lightmap);

            // Задняя грань (Z-)
            buffer.addVertex((float)(cx + h), (float)(cy - h), (float)(cz - h)).setUv(0, 0).setColor(r, g, b, a).setLight(lightmap);
            buffer.addVertex((float)(cx - h), (float)(cy - h), (float)(cz - h)).setUv(1, 0).setColor(r, g, b, a).setLight(lightmap);
            buffer.addVertex((float)(cx - h), (float)(cy + h), (float)(cz - h)).setUv(1, 1).setColor(r, g, b, a).setLight(lightmap);
            buffer.addVertex((float)(cx + h), (float)(cy + h), (float)(cz - h)).setUv(0, 1).setColor(r, g, b, a).setLight(lightmap);

            // Верхняя грань (Y+)
            buffer.addVertex((float)(cx - h), (float)(cy + h), (float)(cz + h)).setUv(0, 0).setColor(r, g, b, a).setLight(lightmap);
            buffer.addVertex((float)(cx + h), (float)(cy + h), (float)(cz + h)).setUv(1, 0).setColor(r, g, b, a).setLight(lightmap);
            buffer.addVertex((float)(cx + h), (float)(cy + h), (float)(cz - h)).setUv(1, 1).setColor(r, g, b, a).setLight(lightmap);
            buffer.addVertex((float)(cx - h), (float)(cy + h), (float)(cz - h)).setUv(0, 1).setColor(r, g, b, a).setLight(lightmap);

            // Нижняя грань (Y-)
            buffer.addVertex((float)(cx - h), (float)(cy - h), (float)(cz - h)).setUv(0, 0).setColor(r, g, b, a).setLight(lightmap);
            buffer.addVertex((float)(cx + h), (float)(cy - h), (float)(cz - h)).setUv(1, 0).setColor(r, g, b, a).setLight(lightmap);
            buffer.addVertex((float)(cx + h), (float)(cy - h), (float)(cz + h)).setUv(1, 1).setColor(r, g, b, a).setLight(lightmap);
            buffer.addVertex((float)(cx - h), (float)(cy - h), (float)(cz + h)).setUv(0, 1).setColor(r, g, b, a).setLight(lightmap);

            // Правая грань (X+)
            buffer.addVertex((float)(cx + h), (float)(cy - h), (float)(cz + h)).setUv(0, 0).setColor(r, g, b, a).setLight(lightmap);
            buffer.addVertex((float)(cx + h), (float)(cy - h), (float)(cz - h)).setUv(1, 0).setColor(r, g, b, a).setLight(lightmap);
            buffer.addVertex((float)(cx + h), (float)(cy + h), (float)(cz - h)).setUv(1, 1).setColor(r, g, b, a).setLight(lightmap);
            buffer.addVertex((float)(cx + h), (float)(cy + h), (float)(cz + h)).setUv(0, 1).setColor(r, g, b, a).setLight(lightmap);
            // Левая грань (X-)
            buffer.addVertex((float)(cx - h), (float)(cy - h), (float)(cz - h)).setUv(0, 0).setColor(r, g, b, a).setLight(lightmap);
            buffer.addVertex((float)(cx - h), (float)(cy - h), (float)(cz + h)).setUv(1, 0).setColor(r, g, b, a).setLight(lightmap);
            buffer.addVertex((float)(cx - h), (float)(cy + h), (float)(cz + h)).setUv(1, 1).setColor(r, g, b, a).setLight(lightmap);
            buffer.addVertex((float)(cx - h), (float)(cy + h), (float)(cz - h)).setUv(0, 1).setColor(r, g, b, a).setLight(lightmap);
        }
    }
}