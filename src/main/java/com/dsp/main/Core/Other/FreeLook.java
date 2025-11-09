package com.dsp.main.Core.Other;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

import java.util.HashSet;
import java.util.Set;

public class FreeLook {
    private static final Minecraft mc = Minecraft.getInstance();
    public static boolean isFreeLookEnabled = false;
    private static float cameraYaw;
    private static float cameraPitch;

    public static float FreeLookXRot;
    public static float FreeLookYRot;
    private static final boolean bypassPitchClamp = false;
    private static final float sensitivity = 12.0F;
    private static double lastMouseX;
    private static double lastMouseY;

    private static final Set<String> activeRequests = new HashSet<>();

    public static void requestFreeLook(String requester) {
        if (requester == null || requester.isEmpty()) return;

        boolean wasEmpty = activeRequests.isEmpty();
        activeRequests.add(requester);

        if (wasEmpty && !isFreeLookEnabled) {
            enableFreeLook();
        }
    }

    public static void releaseFreeLook(String requester) {
        if (requester == null || requester.isEmpty()) return;

        activeRequests.remove(requester);

        if (activeRequests.isEmpty() && isFreeLookEnabled) {
            disableFreeLook();
        }
    }

    public static boolean hasActiveRequests() {
        return !activeRequests.isEmpty();
    }

    public static void enableFreeLook() {
        if (mc.player == null) return;
        isFreeLookEnabled = true;
        FreeLookXRot = mc.player.getXRot();
        FreeLookYRot = mc.player.getYRot();
        cameraYaw = mc.player.getYRot();
        cameraPitch = mc.player.getXRot();
        lastMouseX = mc.mouseHandler.xpos();
        lastMouseY = mc.mouseHandler.ypos();
    }

    public static void disableFreeLook() {
        if (mc.player != null) {
            isFreeLookEnabled = false;
            mc.player.setXRot(getCameraPitch());
            mc.player.setYRot(getCameraYaw());
            activeRequests.clear();
        }
    }

    @SubscribeEvent
    public void onClientTick(RenderFrameEvent.Pre event) {
        if (!isFreeLookEnabled || mc.player == null || mc.level == null || mc.screen != null) {
            return;
        }
        double currentMouseX = mc.mouseHandler.xpos();
        double currentMouseY = mc.mouseHandler.ypos();
        float sens = sensitivity / 100.0F;

        double deltaX = currentMouseX - lastMouseX;
        double deltaY = currentMouseY - lastMouseY;
        cameraYaw += (float) (deltaX * sens);
        cameraPitch += (float) (deltaY * sens);

        if (!bypassPitchClamp) {
            cameraPitch = Mth.clamp(cameraPitch, -90.0F, 90.0F);
        }

        lastMouseX = currentMouseX;
        lastMouseY = currentMouseY;

        if (mc.mouseHandler.isMouseGrabbed() && !mc.isPaused()) {
            mc.mouseHandler.grabMouse();
        }
    }

    public static float getCameraYaw() {
        return cameraYaw;
    }

    public static float getCameraPitch() {
        return cameraPitch;
    }
}