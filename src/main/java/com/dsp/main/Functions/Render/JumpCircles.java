package com.dsp.main.Functions.Render;

import com.dsp.main.Core.Event.OnUpdate;
import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.Slider;
import com.dsp.main.UI.Themes.ThemesUtil;
import com.dsp.main.Utils.Render.AnimFromRockstarClient.Animation;
import com.dsp.main.Utils.Render.AnimFromRockstarClient.Easing;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.CoreShaders;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import static com.dsp.main.Api.mc;

public class JumpCircles extends Module {
    private static final ResourceLocation CIRCLE_TEXTURE = ResourceLocation.fromNamespaceAndPath("dsp", "textures/circle.png");

    private final Slider speed;
    private final Slider size;
    private final List<Circle> circles = new ArrayList<>();

    public JumpCircles() {
        super("JumpCircles", 0, Category.RENDER, "Renders circles when you jump");

        speed = new Slider("Speed", 25, 100, 75, 5);
        size = new Slider("Size", 1.5f, 3.5f, 2.0f, 0.1f);

        addSettings(speed, size);
    }

    @SubscribeEvent
    public void onLivingJump(LivingEvent.LivingJumpEvent event) {
        if (mc.player == null || mc.level == null) return;
        if (event.getEntity() == mc.player) {
            Vec3 pos = mc.player.position().add(0, 0.05, 0);
            circles.add(new Circle(pos));
        }
    }

    @SubscribeEvent
    public void onUpdate(OnUpdate event) {
        if (mc.player == null || mc.level == null) return;
        circles.removeIf(Circle::update);
    }

    @SubscribeEvent
    public void onRender3D(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        if (mc.player == null || mc.level == null) return;
        if (circles.isEmpty()) return;

        PoseStack poseStack = event.getPoseStack();
        Camera camera = event.getCamera();
        Vec3 camPos = camera.getPosition();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShaderTexture(0, CIRCLE_TEXTURE);
        RenderSystem.setShader(CoreShaders.POSITION_TEX_COLOR);

        Tesselator tessellator = Tesselator.getInstance();

        for (Circle circle : circles) {
            Vec3 worldPos = circle.vector;
            Vec3 renderPos = worldPos.subtract(camPos);

            float circleSize = (float) (size.getValueFloat() * circle.getAnimation());
            float alpha = (float) circle.getAlphaAnimation();

            if (alpha <= 0.01f || circleSize <= 0.01f) continue;

            poseStack.pushPose();
            poseStack.translate(renderPos.x, renderPos.y, renderPos.z);
            poseStack.mulPose(new Quaternionf().rotationX((float) Math.toRadians(90)));

            for (int i = 0; i < 14; i++) {
                poseStack.translate(0, 0, -0.03f);
                float layerAlpha = alpha * (1 - (i / 14f)) / 2f;

                if (layerAlpha > 0.01f) {
                    Color c1 = new Color(ThemesUtil.getCurrentStyle().getColor(90), true);
                    Color c2 = new Color(ThemesUtil.getCurrentStyle().getColor(180), true);
                    Color c3 = new Color(ThemesUtil.getCurrentStyle().getColor(240), true);
                    Color c4 = new Color(ThemesUtil.getCurrentStyle().getColor(360), true);

                    c1 = applyAlpha(c1, layerAlpha);
                    c2 = applyAlpha(c2, layerAlpha);
                    c3 = applyAlpha(c3, layerAlpha);
                    c4 = applyAlpha(c4, layerAlpha);

                    BufferBuilder buffer = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
                    drawTexturedQuad(poseStack, buffer, -circleSize / 2, -circleSize / 2, circleSize, circleSize, c1, c2, c3, c4);
                    BufferUploader.drawWithShader(buffer.buildOrThrow());
                }
            }

            poseStack.popPose();
        }

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }

    private Color applyAlpha(Color color, float alpha) {
        int a = (int) (alpha * 255);
        a = Math.max(0, Math.min(255, a));
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), a);
    }

    private void drawTexturedQuad(PoseStack poseStack, BufferBuilder buffer, float x, float y, float width, float height, Color c1, Color c2, Color c3, Color c4) {
        Matrix4f matrix = poseStack.last().pose();

        buffer.addVertex(matrix, x, y + height, 0)
                .setUv(0, 1)
                .setColor(c1.getRed(), c1.getGreen(), c1.getBlue(), c1.getAlpha());

        buffer.addVertex(matrix, x + width, y + height, 0)
                .setUv(1, 1)
                .setColor(c2.getRed(), c2.getGreen(), c2.getBlue(), c2.getAlpha());

        buffer.addVertex(matrix, x + width, y, 0)
                .setUv(1, 0)
                .setColor(c3.getRed(), c3.getGreen(), c3.getBlue(), c3.getAlpha());

        buffer.addVertex(matrix, x, y, 0)
                .setUv(0, 0)
                .setColor(c4.getRed(), c4.getGreen(), c4.getBlue(), c4.getAlpha());
    }

    @Override
    public void onDisable() {
        super.onDisable();
        circles.clear();
    }

    private class Circle {
        private final Vec3 vector;
        private double tick;
        private final Animation anim;
        private final Animation out;

        public Circle(Vec3 vector) {
            this.vector = vector;
            this.tick = speed.getValueFloat();
            this.anim = new Animation().setEasing(Easing.EASE_OUT_BACK).setSpeed(700);
            this.out = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(1000);
            this.anim.setForward(true);
        }

        public double getAlphaAnimation() {
            return 1 - out.get();
        }

        public double getAnimation() {
            return anim.get();
        }

        public boolean update() {
            if (anim.finished() && this.tick <= 30) {
                this.out.setForward(true);
            }
            tick = tick - 0.75;
            return tick <= 0;
        }
    }
}