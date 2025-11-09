package com.dsp.main.Functions.Render.XRay;

import com.dsp.main.UI.Themes.ThemesUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.CoreShaders;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.awt.Color;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

import static com.dsp.main.Api.mc;

public class XRayRenderer {
    private static ScheduledExecutorService EXECUTOR = null;
    private static final Map<BlockPos, BlockState> cachedBlocks = new ConcurrentHashMap<>();
    private static final Object cacheLock = new Object();
    private static volatile int currentRadius = 64;
    private static volatile Set<Block> targetBlocks = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private static volatile boolean cacheNeedsUpdate = true;
    private static ScheduledFuture<?> cacheTask = null;

    public static void init() {
        if (EXECUTOR != null && !EXECUTOR.isShutdown()) {
            return;
        }

        EXECUTOR = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "XRay-Cache-Thread");
            thread.setDaemon(true);
            thread.setPriority(Thread.MIN_PRIORITY);
            return thread;
        });

        cacheTask = EXECUTOR.scheduleAtFixedRate(() -> {
            try {
                if (mc.level == null || mc.player == null || !cacheNeedsUpdate) return;
                updateCache();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
    }

    public static void shutdown() {
        if (cacheTask != null) {
            cacheTask.cancel(false);
            cacheTask = null;
        }

        if (EXECUTOR != null) {
            EXECUTOR.shutdownNow();
            try {
                if (!EXECUTOR.awaitTermination(1, TimeUnit.SECONDS)) {
                    EXECUTOR.shutdownNow();
                }
            } catch (InterruptedException e) {
                EXECUTOR.shutdownNow();
                Thread.currentThread().interrupt();
            }
            EXECUTOR = null;
        }

        synchronized (cacheLock) {
            cachedBlocks.clear();
        }
    }

    public static void setRadius(int radius) {
        currentRadius = Math.max(16, Math.min(256, radius));
        markDirty();
    }

    public static void setTargetBlocks(Set<Block> blocks) {
        synchronized (cacheLock) {
            targetBlocks.clear();
            targetBlocks.addAll(blocks);
            markDirty();
        }
    }

    public static void markDirty() {
        cacheNeedsUpdate = true;
    }

    private static void updateCache() {
        if (mc.level == null || mc.player == null) return;

        Level level = mc.level;
        BlockPos playerPos = mc.player.blockPosition();
        Map<BlockPos, BlockState> newCache = new ConcurrentHashMap<>();
        Set<Block> currentTargets = Collections.newSetFromMap(new ConcurrentHashMap<>());
        currentTargets.addAll(targetBlocks);

        if (currentTargets.isEmpty()) {
            synchronized (cacheLock) {
                cachedBlocks.clear();
                cacheNeedsUpdate = false;
            }
            return;
        }

        int radius = currentRadius;
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x * x + y * y + z * z > radius * radius) continue;

                    mutablePos.set(playerPos.getX() + x, playerPos.getY() + y, playerPos.getZ() + z);

                    if (!level.isLoaded(mutablePos)) continue;

                    BlockState state = level.getBlockState(mutablePos);
                    if (state.isAir()) continue;

                    if (currentTargets.contains(state.getBlock())) {
                        newCache.put(mutablePos.immutable(), state);
                    }
                }
            }
        }

        synchronized (cacheLock) {
            cachedBlocks.clear();
            cachedBlocks.putAll(newCache);
            cacheNeedsUpdate = false;
        }
    }

    public static void render(PoseStack poseStack, Camera camera) {
        if (cachedBlocks.isEmpty()) return;

        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.lineWidth(2.0f);
        RenderSystem.setShader(CoreShaders.POSITION_TEX_COLOR);

        Vec3 camPos = camera.getPosition();
        Color themeColor1 = new Color(ThemesUtil.getCurrentStyle().getColor(1));
        Color themeColor2 = new Color(ThemesUtil.getCurrentStyle().getColor(2));

        float time = (System.currentTimeMillis() % 10000) / 10000.0f;

        poseStack.pushPose();
        Matrix4f matrix = poseStack.last().pose();

        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        synchronized (cacheLock) {
            int index = 0;
            for (Map.Entry<BlockPos, BlockState> entry : cachedBlocks.entrySet()) {
                BlockPos pos = entry.getKey();

                double renderX = pos.getX() - camPos.x;
                double renderY = pos.getY() - camPos.y;
                double renderZ = pos.getZ() - camPos.z;

                float colorMix = (time + index * 0.01f) % 1.0f;
                Color lineColor = interpolateColor(themeColor1, themeColor2, colorMix);

                float r = lineColor.getRed() / 255.0f;
                float g = lineColor.getGreen() / 255.0f;
                float b = lineColor.getBlue() / 255.0f;

                drawAllCubeLines(buffer, matrix, renderX, renderY, renderZ, r, g, b);

                index++;
            }
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());

        poseStack.popPose();

        RenderSystem.lineWidth(1.0f);
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private static void drawAllCubeLines(BufferBuilder buffer, Matrix4f matrix, double x, double y, double z, float r, float g, float b) {
        float x0 = (float) x;
        float y0 = (float) y;
        float z0 = (float) z;
        float x1 = (float) (x + 1);
        float y1 = (float) (y + 1);
        float z1 = (float) (z + 1);

        buffer.addVertex(matrix, x0, y0, z0).setColor(r, g, b, 1.0f);
        buffer.addVertex(matrix, x1, y0, z0).setColor(r, g, b, 1.0f);

        buffer.addVertex(matrix, x1, y0, z0).setColor(r, g, b, 1.0f);
        buffer.addVertex(matrix, x1, y1, z0).setColor(r, g, b, 1.0f);

        buffer.addVertex(matrix, x1, y1, z0).setColor(r, g, b, 1.0f);
        buffer.addVertex(matrix, x0, y1, z0).setColor(r, g, b, 1.0f);

        buffer.addVertex(matrix, x0, y1, z0).setColor(r, g, b, 1.0f);
        buffer.addVertex(matrix, x0, y0, z0).setColor(r, g, b, 1.0f);

        buffer.addVertex(matrix, x0, y0, z1).setColor(r, g, b, 1.0f);
        buffer.addVertex(matrix, x1, y0, z1).setColor(r, g, b, 1.0f);

        buffer.addVertex(matrix, x1, y0, z1).setColor(r, g, b, 1.0f);
        buffer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, 1.0f);

        buffer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, 1.0f);
        buffer.addVertex(matrix, x0, y1, z1).setColor(r, g, b, 1.0f);

        buffer.addVertex(matrix, x0, y1, z1).setColor(r, g, b, 1.0f);
        buffer.addVertex(matrix, x0, y0, z1).setColor(r, g, b, 1.0f);

        buffer.addVertex(matrix, x0, y0, z0).setColor(r, g, b, 1.0f);
        buffer.addVertex(matrix, x0, y0, z1).setColor(r, g, b, 1.0f);

        buffer.addVertex(matrix, x1, y0, z0).setColor(r, g, b, 1.0f);
        buffer.addVertex(matrix, x1, y0, z1).setColor(r, g, b, 1.0f);

        buffer.addVertex(matrix, x1, y1, z0).setColor(r, g, b, 1.0f);
        buffer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, 1.0f);

        buffer.addVertex(matrix, x0, y1, z0).setColor(r, g, b, 1.0f);
        buffer.addVertex(matrix, x0, y1, z1).setColor(r, g, b, 1.0f);
    }

    private static Color interpolateColor(Color c1, Color c2, float ratio) {
        int r = (int) (c1.getRed() * (1 - ratio) + c2.getRed() * ratio);
        int g = (int) (c1.getGreen() * (1 - ratio) + c2.getGreen() * ratio);
        int b = (int) (c1.getBlue() * (1 - ratio) + c2.getBlue() * ratio);
        return new Color(r, g, b);
    }

    public static int getCachedBlockCount() {
        return cachedBlocks.size();
    }
}