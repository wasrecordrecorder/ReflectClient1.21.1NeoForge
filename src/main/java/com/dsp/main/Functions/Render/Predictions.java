package com.dsp.main.Functions.Render;

import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.CheckBox;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.MultiCheckBox;
import com.dsp.main.UI.Themes.ThemesUtil;
import com.dsp.main.Utils.Font.builders.Builder;
import com.dsp.main.Utils.Font.renderers.impl.BuiltText;
import com.dsp.main.Utils.Render.ColorUtil;
import com.dsp.main.Utils.Render.DrawHelper;
import com.dsp.main.Utils.Render.Other.ESPUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Vector3d;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.dsp.main.Main.BIKO_FONT;

public class Predictions extends Module {
    private static final Minecraft mc = Minecraft.getInstance();
    private static MultiCheckBox Modi = new MultiCheckBox("Options", Arrays.asList(
            new CheckBox("Ender Pearls", false),
            new CheckBox("Trident", false),
            new CheckBox("Arrows", false)
    ));
    private static CheckBox DrawTrajectory = new CheckBox("Render Trajectory", false).setVisible(() -> Modi.hasAnyEnabled());
    public Predictions() {
        super("Predictions", 0, Category.RENDER, "Rendering predicted pos to Pearls and other");
        addSettings(Modi, DrawTrajectory);
    }
    @SubscribeEvent
    public void onRenderLevelStage(RenderGuiEvent.Post event) {
        if (mc.level == null || mc.player == null) {
            return;
        }
        if (Modi.isOptionEnabled("Ender Pearls")) {
            renderEnderPearls(event);
        }
        if (Modi.isOptionEnabled("Trident")) {
            renderTridents(event);
        }
        if (Modi.isOptionEnabled("Arrows")) {
            renderArrows(event);
        }
    }
    public void renderArrows(RenderGuiEvent event) {
        assert mc.level != null;
        for (Entity ent : mc.level.getEntities((Entity) null, mc.player.getBoundingBox().inflate(128.0), e -> e instanceof Arrow)) {
            Arrow pearl = (Arrow) ent;
            Vec3 landingPos = predictLandingPosition(pearl);
            double timeToLand = predictTimeToLand(pearl);
            if (timeToLand == 0) return;
            Vector3d screenPos = ESPUtils.toScreen(landingPos);
            if (screenPos.z == 0) continue;
            String text = String.format("Arrow (%.1f)", timeToLand);
            float width = BIKO_FONT.get().getWidth(text, 8f);
            int X = (int) (screenPos.x - 5);
            float height = BIKO_FONT.get().getMetrics().baselineHeight() * 8f + 5;
            DrawHelper.rectangle(event.getGuiGraphics().pose(), (float) (X - width / 2) - 5, (float) screenPos.y - 2, width + 19, height + 4, 3f, new Color(30, 30, 30, 150).getRGB());
            ResourceLocation itemTexture = ResourceLocation.fromNamespaceAndPath("dsp", "textures/item/arrow.png");
            DrawHelper.drawTexture(itemTexture, event.getGuiGraphics().pose().last().pose(), (float) (X - width / 2) - 5, (float) screenPos.y - 1, 12, 12);
            BuiltText builtText = Builder.text()
                    .font(BIKO_FONT.get())
                    .text(text)
                    .color(Color.WHITE)
                    .size(8f)
                    .thickness(0.05f)
                    .build();
            builtText.render(new Matrix4f(event.getGuiGraphics().pose().last().pose()), (float) (X - width / 2) + 9, (float) screenPos.y + 2);
        }
    }
    public void renderTridents(RenderGuiEvent event) {
        assert mc.level != null;
        for (Entity ent : mc.level.getEntities((Entity) null, mc.player.getBoundingBox().inflate(128.0), e -> e instanceof ThrownTrident)) {
            ThrownTrident pearl = (ThrownTrident) ent;
            Vec3 landingPos = predictLandingPosition(pearl);
            double timeToLand = predictTimeToLand(pearl);
            if (timeToLand == 0 || pearl.getOwner() == null) return;
            Vector3d screenPos = ESPUtils.toScreen(landingPos);
            if (screenPos.z == 0) continue;
            String text = String.format("Trident (%.1f)", timeToLand);
            float width = BIKO_FONT.get().getWidth(text, 8f);
            int X = (int) (screenPos.x - 5);
            float height = BIKO_FONT.get().getMetrics().baselineHeight() * 8f + 5;
            DrawHelper.rectangle(event.getGuiGraphics().pose(), (float) (X - width / 2) - 5, (float) screenPos.y - 2, width + 19, height + 4, 3f, new Color(30, 30, 30, 150).getRGB());
            ResourceLocation itemTexture = ResourceLocation.fromNamespaceAndPath("dsp", "textures/item/trident.png");
            DrawHelper.drawTexture(itemTexture, event.getGuiGraphics().pose().last().pose(), (float) (X - width / 2) - 5, (float) screenPos.y - 1, 12, 12);
            BuiltText builtText = Builder.text()
                    .font(BIKO_FONT.get())
                    .text(text)
                    .color(Color.WHITE)
                    .size(8f)
                    .thickness(0.05f)
                    .build();
            builtText.render(new Matrix4f(event.getGuiGraphics().pose().last().pose()), (float) (X - width / 2) + 9, (float) screenPos.y + 2);
            String OwnerName = pearl.getOwner().getName().getString().equals(mc.player.getName().getString()) ? "From You" : pearl.getOwner().getName().getString();
            float ownerWidth = BIKO_FONT.get().getWidth(OwnerName, 6f);
            float ownerBoxWidth = ownerWidth + 14 + 5;
            float ownerStartX = (float) (X - ownerBoxWidth / 2) + 3.5f;
            float ownerY = (float) (screenPos.y + height + 0.7f);
            DrawHelper.drawSemiRoundRect(event.getGuiGraphics().pose(), ownerStartX + 1, ownerY + 1, ownerBoxWidth, height + 1, 3f, 0, 3f, 0, new Color(30, 30, 30, 150).getRGB());
            DrawHelper.drawHead(event.getGuiGraphics().pose(), (Player) pearl.getOwner(), ownerStartX + 3, ownerY + 1, 10, 10);
            BuiltText builtText1 = Builder.text()
                    .font(BIKO_FONT.get())
                    .text(OwnerName)
                    .color(Color.WHITE)
                    .size(6f)
                    .thickness(0.05f)
                    .build();
            builtText1.render(new Matrix4f(event.getGuiGraphics().pose().last().pose()), ownerStartX + 16, ownerY + 4);
        }
    }
    public void renderEnderPearls(RenderGuiEvent event) {
        assert mc.level != null;
        for (Entity ent : mc.level.getEntities((Entity) null, mc.player.getBoundingBox().inflate(128.0), e -> e instanceof ThrownEnderpearl)) {
            ThrownEnderpearl pearl = (ThrownEnderpearl) ent;
            Vec3 landingPos = predictLandingPosition(pearl);
            double timeToLand = predictTimeToLand(pearl);
            Vector3d screenPos = ESPUtils.toScreen(landingPos);
            if (screenPos.z == 0) continue;
            if (pearl.getOwner() == null) continue;
            String text = String.format("Ender Pearl (%.1f)", timeToLand);
            float width = BIKO_FONT.get().getWidth(text, 8f);
            int X = (int) (screenPos.x - 5);
            float height = BIKO_FONT.get().getMetrics().baselineHeight() * 8f + 5;
            DrawHelper.rectangle(event.getGuiGraphics().pose(), (float) (X - width / 2) - 5, (float) screenPos.y - 2, width + 19, height + 4, 3f, new Color(30, 30, 30, 150).getRGB());
            ResourceLocation itemTexture = ResourceLocation.fromNamespaceAndPath("dsp", "textures/item/ender_pearl.png");
            DrawHelper.drawTexture(itemTexture, event.getGuiGraphics().pose().last().pose(), (float) (X - width / 2) - 5, (float) screenPos.y - 1, 12, 12);
            BuiltText builtText = Builder.text()
                    .font(BIKO_FONT.get())
                    .text(text)
                    .color(Color.WHITE)
                    .size(8f)
                    .thickness(0.05f)
                    .build();
            builtText.render(new Matrix4f(event.getGuiGraphics().pose().last().pose()), (float) (X - width / 2) + 9, (float) screenPos.y + 2);

            String OwnerName = pearl.getOwner().getName().getString().equals(mc.player.getName().getString()) ? "From You" : pearl.getOwner().getName().getString();
            float ownerWidth = BIKO_FONT.get().getWidth(OwnerName, 6f);
            float ownerBoxWidth = ownerWidth + 14 + 5;
            float ownerStartX = (float) (X - ownerBoxWidth / 2) + 3.5f;
            float ownerY = (float) (screenPos.y + height + 0.7f);
            DrawHelper.drawSemiRoundRect(event.getGuiGraphics().pose(), ownerStartX + 1, ownerY + 1, ownerBoxWidth, height + 1, 3f, 0, 3f, 0, new Color(30, 30, 30, 150).getRGB());
            DrawHelper.drawHead(event.getGuiGraphics().pose(), (Player) pearl.getOwner(), ownerStartX + 3, ownerY + 1, 10, 10);
            BuiltText builtText1 = Builder.text()
                    .font(BIKO_FONT.get())
                    .text(OwnerName)
                    .color(Color.WHITE)
                    .size(6f)
                    .thickness(0.05f)
                    .build();
            builtText1.render(new Matrix4f(event.getGuiGraphics().pose().last().pose()), ownerStartX + 16, ownerY + 4);
        }
    }
    private Vec3 predictLandingPosition(ThrownEnderpearl pearl) {
        Vec3 pos = pearl.position().add(0, 0.5, 0);
        Vec3 vel = pearl.getDeltaMovement();
        Level level = pearl.level();
        for (int i = 0; i < 100; i++) {
            Vec3 nextPos = pos.add(vel);
            HitResult hitResult = level.clip(new ClipContext(pos, nextPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, pearl));
            if (hitResult.getType() != HitResult.Type.MISS) {
                return hitResult.getLocation();
            }
            pos = nextPos;
            vel = vel.add(0, -0.05, 0).scale(0.99);
        }
        return pos;
    }
    private double predictTimeToLand(ThrownEnderpearl pearl) {
        Vec3 pos = pearl.position();
        Vec3 vel = pearl.getDeltaMovement();
        Level level = pearl.level();
        int ticks = 0;
        for (int i = 0; i < 100; i++) {
            Vec3 nextPos = pos.add(vel);
            HitResult hitResult = level.clip(new ClipContext(pos, nextPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, pearl));
            if (hitResult.getType() != HitResult.Type.MISS) {
                return ticks / 20.0;
            }
            pos = nextPos;
            vel = vel.add(0, -0.05, 0).scale(0.99);
            ticks++;
        }
        return ticks / 20.0;
    }
    private Vec3 predictLandingPosition(ThrownTrident pearl) {
        Vec3 pos = pearl.position().add(0, 0.5, 0);
        Vec3 vel = pearl.getDeltaMovement();
        Level level = pearl.level();
        for (int i = 0; i < 100; i++) {
            Vec3 nextPos = pos.add(vel);
            HitResult hitResult = level.clip(new ClipContext(pos, nextPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, pearl));
            if (hitResult.getType() != HitResult.Type.MISS) {
                return hitResult.getLocation();
            }
            pos = nextPos;
            vel = vel.add(0, -0.05, 0).scale(0.99);
        }
        return pos;
    }
    private Vec3 predictLandingPosition(Arrow pearl) {
        Vec3 pos = pearl.position().add(0, 0.5, 0);
        Vec3 vel = pearl.getDeltaMovement();
        Level level = pearl.level();
        for (int i = 0; i < 100; i++) {
            Vec3 nextPos = pos.add(vel);
            HitResult hitResult = level.clip(new ClipContext(pos, nextPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, pearl));
            if (hitResult.getType() != HitResult.Type.MISS) {
                return hitResult.getLocation();
            }
            pos = nextPos;
            vel = vel.add(0, -0.05, 0).scale(0.99);
        }
        return pos;
    }
    private double predictTimeToLand(ThrownTrident pearl) {
        Vec3 pos = pearl.position();
        Vec3 vel = pearl.getDeltaMovement();
        Level level = pearl.level();
        int ticks = 0;
        for (int i = 0; i < 100; i++) {
            Vec3 nextPos = pos.add(vel);
            HitResult hitResult = level.clip(new ClipContext(pos, nextPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, pearl));
            if (hitResult.getType() != HitResult.Type.MISS) {
                return ticks / 20.0;
            }
            pos = nextPos;
            vel = vel.add(0, -0.05, 0).scale(0.99);
            ticks++;
        }
        return ticks / 20.0;
    }
    private double predictTimeToLand(Arrow pearl) {
        Vec3 pos = pearl.position();
        Vec3 vel = pearl.getDeltaMovement();
        Level level = pearl.level();
        int ticks = 0;
        for (int i = 0; i < 100; i++) {
            Vec3 nextPos = pos.add(vel);
            HitResult hitResult = level.clip(new ClipContext(pos, nextPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, pearl));
            if (hitResult.getType() != HitResult.Type.MISS) {
                return ticks / 20.0;
            }
            pos = nextPos;
            vel = vel.add(0, -0.05, 0).scale(0.99);
            ticks++;
        }
        return ticks / 20.0;
    }
    @SubscribeEvent
    public void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS) return;
        if (mc.level == null || mc.player == null) return;
        renderAllTrajectories(event.getPoseStack(), mc.renderBuffers().bufferSource());
    }
    private void renderAllTrajectories(PoseStack poseStack, MultiBufferSource.BufferSource bufSource) {
        if (!DrawTrajectory.isEnabled()) return;
        Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
        for (Entity ent : mc.level.getEntities((Entity) null, mc.player.getBoundingBox().inflate(128.0), e -> e instanceof ThrownEnderpearl)) {
            if (Modi.isOptionEnabled("Ender Pearls")) {
                ThrownEnderpearl pearl = (ThrownEnderpearl) ent;
                renderTrajectory(pearl, poseStack, bufSource, cam);
            }
        }
        for (Entity ent : mc.level.getEntities((Entity) null, mc.player.getBoundingBox().inflate(128.0), e -> e instanceof ThrownTrident)) {
            if (Modi.isOptionEnabled("Trident")) {
                ThrownTrident trident = (ThrownTrident) ent;
                renderTrajectory(trident, poseStack, bufSource, cam);
            }
        }
        for (Entity ent : mc.level.getEntities((Entity) null, mc.player.getBoundingBox().inflate(128.0), e -> e instanceof Arrow)) {
            if (Modi.isOptionEnabled("Arrows")) {
                Arrow arrow = (Arrow) ent;
                renderTrajectory(arrow, poseStack, bufSource, cam);
            }
        }
        bufSource.endBatch();
    }
    private List<Vec3> generateTrajectoryPoints(ThrownEnderpearl pearl, Vec3 landingPos) {
        List<Vec3> points = new ArrayList<>();
        Vec3 pos = pearl.position();
        Vec3 vel = pearl.getDeltaMovement();
        Level level = pearl.level();
        points.add(pos);

        for (int i = 0; i < 150; i++) {
            Vec3 nextPos = pos.add(vel);
            points.add(nextPos);

            if (nextPos.distanceToSqr(landingPos) < 0.01) {
                points.add(landingPos);
                break;
            }
            HitResult hitResult = level.clip(new ClipContext(pos, nextPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, pearl));
            if (hitResult.getType() != HitResult.Type.MISS) {
                points.add(landingPos);
                break;
            }

            pos = nextPos;
            vel = vel.add(0, -0.05, 0).scale(0.99);
        }

        return points;
    }
    private List<Vec3> generateTrajectoryPoints(ThrownTrident pearl, Vec3 landingPos) {
        List<Vec3> points = new ArrayList<>();
        Vec3 pos = pearl.position();
        Vec3 vel = pearl.getDeltaMovement();
        Level level = pearl.level();
        points.add(pos);

        for (int i = 0; i < 150; i++) {
            Vec3 nextPos = pos.add(vel);
            points.add(nextPos);

            if (nextPos.distanceToSqr(landingPos) < 0.01) {
                points.add(landingPos);
                break;
            }
            HitResult hitResult = level.clip(new ClipContext(pos, nextPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, pearl));
            if (hitResult.getType() != HitResult.Type.MISS) {
                points.add(landingPos);
                break;
            }

            pos = nextPos;
            vel = vel.add(0, -0.05, 0).scale(0.99);
        }

        return points;
    }
    private List<Vec3> generateTrajectoryPoints(Arrow pearl, Vec3 landingPos) {
        List<Vec3> points = new ArrayList<>();
        Vec3 pos = pearl.position();
        Vec3 vel = pearl.getDeltaMovement();
        Level level = pearl.level();
        points.add(pos);

        for (int i = 0; i < 150; i++) {
            Vec3 nextPos = pos.add(vel);
            points.add(nextPos);

            if (nextPos.distanceToSqr(landingPos) < 0.01) {
                points.add(landingPos);
                break;
            }
            HitResult hitResult = level.clip(new ClipContext(pos, nextPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, pearl));
            if (hitResult.getType() != HitResult.Type.MISS) {
                points.add(landingPos);
                break;
            }

            pos = nextPos;
            vel = vel.add(0, -0.05, 0).scale(0.99);
        }

        return points;
    }
    private void renderTrajectory(ThrownEnderpearl pearl, PoseStack poseStack, MultiBufferSource.BufferSource bufSource, Vec3 camPos) {
        Vec3 landingPos = predictLandingPosition(pearl);
        List<Vec3> trajectoryPoints = generateTrajectoryPoints(pearl, landingPos);

        VertexConsumer vc = bufSource.getBuffer(RenderType.lines());
        Matrix4f m = poseStack.last().pose();
        RenderSystem.lineWidth(5);
        Color themeColor = new Color(ColorUtil.gradient(
                ThemesUtil.getCurrentStyle().getColor(1),
                ThemesUtil.getCurrentStyle().getColor(2),
                10, 10
        ), true);
        for (int i = 0; i < trajectoryPoints.size() - 1; i++) {
            Vec3 a = trajectoryPoints.get(i).subtract(camPos);
            Vec3 b = trajectoryPoints.get(i + 1).subtract(camPos);

            vc.addVertex(m, (float)a.x, (float)a.y, (float)a.z)
                    .setColor(themeColor.getRed(), themeColor.getGreen(), themeColor.getBlue(), themeColor.getAlpha())
                    .setNormal(poseStack.last(), 0, 1, 0);
            vc.addVertex(m, (float)b.x, (float)b.y, (float)b.z)
                    .setColor(themeColor.getRed(), themeColor.getGreen(), themeColor.getBlue(), themeColor.getAlpha())
                    .setNormal(poseStack.last(), 0, 1, 0);
        }
        RenderSystem.lineWidth(1);
    }
    private void renderTrajectory(ThrownTrident pearl, PoseStack poseStack, MultiBufferSource.BufferSource bufSource, Vec3 camPos) {
        Vec3 landingPos = predictLandingPosition(pearl);
        List<Vec3> trajectoryPoints = generateTrajectoryPoints(pearl, landingPos);

        VertexConsumer vc = bufSource.getBuffer(RenderType.lines());
        Matrix4f m = poseStack.last().pose();
        RenderSystem.lineWidth(5);
        Color themeColor = new Color(ColorUtil.gradient(
                ThemesUtil.getCurrentStyle().getColor(1),
                ThemesUtil.getCurrentStyle().getColor(2),
                10, 10
        ), true);
        for (int i = 0; i < trajectoryPoints.size() - 1; i++) {
            Vec3 a = trajectoryPoints.get(i).subtract(camPos);
            Vec3 b = trajectoryPoints.get(i + 1).subtract(camPos);

            vc.addVertex(m, (float)a.x, (float)a.y, (float)a.z)
                    .setColor(themeColor.getRed(), themeColor.getGreen(), themeColor.getBlue(), themeColor.getAlpha())
                    .setNormal(poseStack.last(), 0, 1, 0);
            vc.addVertex(m, (float)b.x, (float)b.y, (float)b.z)
                    .setColor(themeColor.getRed(), themeColor.getGreen(), themeColor.getBlue(), themeColor.getAlpha())
                    .setNormal(poseStack.last(), 0, 1, 0);
        }
        RenderSystem.lineWidth(1);
    }
    private void renderTrajectory(Arrow pearl, PoseStack poseStack, MultiBufferSource.BufferSource bufSource, Vec3 camPos) {
        Vec3 landingPos = predictLandingPosition(pearl);
        List<Vec3> trajectoryPoints = generateTrajectoryPoints(pearl, landingPos);

        VertexConsumer vc = bufSource.getBuffer(RenderType.lines());
        Matrix4f m = poseStack.last().pose();
        RenderSystem.lineWidth(5);
        Color themeColor = new Color(ColorUtil.gradient(
                ThemesUtil.getCurrentStyle().getColor(1),
                ThemesUtil.getCurrentStyle().getColor(2),
                10, 10
        ), true);
        for (int i = 0; i < trajectoryPoints.size() - 1; i++) {
            Vec3 a = trajectoryPoints.get(i).subtract(camPos);
            Vec3 b = trajectoryPoints.get(i + 1).subtract(camPos);

            vc.addVertex(m, (float)a.x, (float)a.y, (float)a.z)
                    .setColor(themeColor.getRed(), themeColor.getGreen(), themeColor.getBlue(), themeColor.getAlpha())
                    .setNormal(poseStack.last(), 0, 1, 0);
            vc.addVertex(m, (float)b.x, (float)b.y, (float)b.z)
                    .setColor(themeColor.getRed(), themeColor.getGreen(), themeColor.getBlue(), themeColor.getAlpha())
                    .setNormal(poseStack.last(), 0, 1, 0);
        }
        RenderSystem.lineWidth(1);
    }
}