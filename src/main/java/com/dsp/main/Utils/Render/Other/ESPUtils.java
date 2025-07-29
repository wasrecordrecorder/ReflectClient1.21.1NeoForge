package com.dsp.main.Utils.Render.Other;

import com.dsp.main.Utils.Render.Mine;
import net.minecraft.client.Camera;
import net.minecraft.world.phys.Vec3;
import org.joml.*;

import java.lang.Math;

public class ESPUtils implements Mine {
    public static Vector3d toScreen(double x, double y, double z) {
        if (mc.level == null) return new Vector3d(0, 0, 0);
        Camera cam = mc.getEntityRenderDispatcher().camera;
        Vec3 cPos = cam.getPosition();
        float fovRad = (float) Math.toRadians(mc.options.fov().get());
        float aspect = (float) mc.getWindow().getGuiScaledWidth() / mc.getWindow().getGuiScaledHeight();
        float near = 0.05f, far = 1000f;
        Matrix4f proj = new Matrix4f()
                .identity()
                .perspective(fovRad, aspect, near, far);
        Quaternionf mRot = cam.rotation();
        Quaternionf rot = new Quaternionf(
                mRot.x(), mRot.y(), mRot.z(), mRot.w()
        ).conjugate();
        Matrix4f view = new Matrix4f()
                .identity()
                .rotate(rot)
                .translate((float)-cPos.x, (float)-cPos.y, (float)-cPos.z);
        Matrix4f projView = new Matrix4f(proj).mul(view);
        Vector4f clip = new Vector4f((float)x, (float)y, (float)z, 1f)
                .mul(projView);
        if (clip.w <= 0.0001f) {
            return new Vector3d(0, 0, 0);
        }
        clip.div(clip.w);
        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();
        double sx = (clip.x * 0.5f + 0.5f) * sw;
        double sy = (1f - (clip.y * 0.5f + 0.5f)) * sh;
        boolean on = clip.x >= -1f && clip.x <= 1f && clip.y >= -1f && clip.y <= 1f;
        return new Vector3d(sx, sy, on ? 1.0 : 0.0);
    }
    public static Vector3d toScreen(Vector3d vec) {
        return toScreen(vec.x, vec.y, vec.z);
    }
    public static Vector3d toScreen(Vec3 vec) {
        return toScreen(vec.x, vec.y, vec.z);
    }
    public static Vector3d toScreen(Vector3i vec) {
        return toScreen(vec.x, vec.y, vec.z);
    }
}