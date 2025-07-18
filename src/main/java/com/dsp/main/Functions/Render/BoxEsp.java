package com.dsp.main.Functions.Render;

import com.dsp.main.Managers.FrndSys.FriendManager;
import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Settings.Mode;
import com.dsp.main.UI.Themes.ThemesUtil;
import com.dsp.main.Utils.Render.ColorUtil;
import com.dsp.main.Utils.Render.DrawHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;

import static com.dsp.main.Api.mc;
import static com.dsp.main.Utils.Render.ColorUtil.getColor;
import static com.dsp.main.Utils.Render.ColorUtil.getColor2;

public class BoxEsp extends Module {

    Mode tools = new Mode("EspMode", "Box", "Effect Glow");

    public BoxEsp() {
        super("Esp", 0, Category.RENDER, "Draw Esp box around player");
        addSetting(tools);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mc.level != null) {
            List<AbstractClientPlayer> players = mc.level.players();
            for (Player entity : players) {
                if (entity.equals(mc.player)) continue;
                entity.setGlowingTag(false);
            }
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;
        Minecraft mc = Minecraft.getInstance();
        Camera camera = event.getCamera();
        PoseStack poseStack = event.getPoseStack();
        Matrix4f matrix = poseStack.last().pose();

        for (Player entity : mc.level.players()) {
            if (entity == mc.player || entity.isInvisible()) continue;

            AABB box = entity.getBoundingBox();
            List<Vec3> corners = getBoxCorners(box);

            double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
            double maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE;

            for (Vec3 point : corners) {
                Vec3 projected = projectTo2D(point, event.getPartialTick().getGameTimeDeltaTicks(), camera);
                if (projected == null) continue;

                minX = Math.min(minX, projected.x);
                minY = Math.min(minY, projected.y);
                maxX = Math.max(maxX, projected.x);
                maxY = Math.max(maxY, projected.y);
            }

            if (minX < maxX && minY < maxY) {
                DrawHelper.rectangle(event.getPoseStack(), (float) minX, (float) minY, (float) maxX, (float) maxY, 3, 0x80FF0000);
            }
        }
    }
    private static List<Vec3> getBoxCorners(AABB box) {
        return List.of(
                new Vec3(box.minX, box.minY, box.minZ),
                new Vec3(box.minX, box.minY, box.maxZ),
                new Vec3(box.maxX, box.minY, box.minZ),
                new Vec3(box.maxX, box.minY, box.maxZ),
                new Vec3(box.minX, box.maxY, box.minZ),
                new Vec3(box.minX, box.maxY, box.maxZ),
                new Vec3(box.maxX, box.maxY, box.minZ),
                new Vec3(box.maxX, box.maxY, box.maxZ)
        );
    }
    @Nullable
    private static Vec3 projectTo2D(Vec3 worldPos, float partialTicks, Camera camera) {
        Minecraft mc = Minecraft.getInstance();
        double camX = Mth.lerp(partialTicks, camera.getPosition().x, camera.getPosition().x);
        double camY = Mth.lerp(partialTicks, camera.getPosition().y, camera.getPosition().y);
        double camZ = Mth.lerp(partialTicks, camera.getPosition().z, camera.getPosition().z);

        Vec3 relative = worldPos.subtract(camX, camY, camZ);

        Matrix4f matrix = RenderSystem.getProjectionMatrix();
        matrix.mul(RenderSystem.getModelViewMatrix());

        Vector4f pos = new Vector4f((float) relative.x, (float) relative.y, (float) relative.z, 1.0f);
        pos.mulTranspose(matrix);

        if (pos.w() <= 0.0f) return null;

        float screenWidth = mc.getWindow().getGuiScaledWidth();
        float screenHeight = mc.getWindow().getGuiScaledHeight();

        float screenX = (pos.x() / pos.w() * 0.5f + 0.5f) * screenWidth;
        float screenY = (1.0f - (pos.y() / pos.w() * 0.5f + 0.5f)) * screenHeight;

        return new Vec3(screenX, screenY, 0);
    }
}