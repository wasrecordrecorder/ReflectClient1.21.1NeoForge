package com.dsp.main.Utils.Engine.Particle;

import net.minecraft.world.phys.Vec3;

public final class SinusoidParticle {
    public final Vec3 origin;
    public Vec3 position;
    public Vec3 velocity;
    public final int lifeTime;
    public int age;
    public final Vec3 color;
    public final float startSize;
    public final boolean canBePunched;

    public static final int FADE_TICKS = 10;
    private static final Vec3 DARK_GRAY = new Vec3(0.10, 0.15, 0.10);

    private float extraSize = 0F;
    private float punchStrength = 0F;
    private int punchTime = 0;
    private int darkenTicks = 0;

    public SinusoidParticle(Vec3 origin, Vec3 velocity, int lifeTime,
                            float startSize, Vec3 color, boolean canBePunched) {
        if (origin == null) throw new IllegalArgumentException("origin null");
        if (velocity == null) throw new IllegalArgumentException("velocity null");
        if (lifeTime <= 0) throw new IllegalArgumentException("lifeTime must be > 0");
        if (color == null) throw new IllegalArgumentException("color null");
        this.origin = origin;
        this.position = origin;
        this.velocity = velocity;
        this.lifeTime = lifeTime;
        this.age = 0;
        this.startSize = startSize;
        this.color = color;
        this.canBePunched = canBePunched;
    }

    public boolean tick() {
        age++;
        if (punchStrength > 0) {
            int fadeTicks = Math.round(lifeTime * 0.4f);
            if (punchTime < fadeTicks) {
                float ratio = (float) punchTime / fadeTicks;
                velocity = velocity.scale(1f - ratio);
            } else {
                punchStrength = 0;
            }
            punchTime++;
        }
        extraSize *= 0.92f;
        if (extraSize < 0.01f) extraSize = 0f;
        if (darkenTicks > 0) darkenTicks--;

        return age <= lifeTime + 2 * FADE_TICKS;
    }

    public float normalizedAge() {
        int total = lifeTime + 2 * FADE_TICKS;
        return Math.max(0F, Math.min(1F, (float) age / total));
    }

    public float getCurrentSize() {
        int total = lifeTime + 2 * FADE_TICKS;
        float base;
        if (age <= FADE_TICKS) {
            base = startSize * age / (float) FADE_TICKS;
        } else if (age >= total - FADE_TICKS) {
            base = startSize * (total - age) / (float) FADE_TICKS;
        } else {
            base = startSize;
        }
        return Math.min(base + extraSize, startSize + 0.5F);
    }

    public Vec3 getCurrentColor() {
        if (darkenTicks <= 0) return color;
        float ratio = (float) darkenTicks / (lifeTime * 0.3f);
        return new Vec3(
                color.x * ratio + DARK_GRAY.x * (1f - ratio),
                color.y * ratio + DARK_GRAY.y * (1f - ratio),
                color.z * ratio + DARK_GRAY.z * (1f - ratio)
        );
    }

    public void punch(Vec3 playerLook, float strength) {
        if (!canBePunched) return;
        if (playerLook == null) return;
        velocity = playerLook.scale(strength);
        punchStrength = strength;
        punchTime = 0;
        extraSize = Math.min(extraSize + 0.2F, 0.5F);
        darkenTicks = Math.round(lifeTime * 0.3f);
    }
}