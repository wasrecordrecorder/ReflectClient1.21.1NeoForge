package com.dsp.main.Functions.Combat.Aura.impl;

import com.dsp.main.Functions.Combat.Aura.Aura;
import com.dsp.main.Utils.Minecraft.Client.ClientPlayerUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

import static com.dsp.main.Api.mc;

public abstract class RotationAngle {
    private static final Map<String, RotationAngle> REGISTRY = new HashMap<>();
    public static void registerRotation(String name, RotationAngle rotation) {
        REGISTRY.put(name, rotation);
    }
    public static RotationAngle getRotation(String name) {
        return REGISTRY.getOrDefault(name, null);
    }
    public static String[] getRotationNames() {
        return REGISTRY.keySet().toArray(new String[0]);
    }
    public abstract void update(Aura aura, Entity target);

    public static float snapToMouseSensitivity(float angle) {
        float sensitivity = (float) mc.options.sensitivity().get().floatValue();
        float gcd = (float)(Math.pow((sensitivity * 0.6F + 0.2F), 3.0F) * 8.0F);
        return Math.round(angle / gcd) * gcd;
    }
    public static boolean isRayIntersectingAABB(Entity player, Entity entity) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getViewVector(1.0F).scale(6.0);
        Vec3 endPos = eyePos.add(lookVec);
        AABB entityBox = entity.getBoundingBox();
        return entityBox.clip(eyePos, endPos).isPresent();
    }
    public static boolean isInFov(LivingEntity target, float baseFov) {
        double fov = ClientPlayerUtil.calculateFOVFromCamera(target);
        return (Math.abs(fov) <= baseFov);
    }
}