package com.dsp.main.Utils.Engine.Particle;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

public final class SinusoidEngine {
    private static final List<SinusoidParticle> PARTICLES = new CopyOnWriteArrayList<>();
    private static volatile boolean initialized = false;

    private SinusoidEngine() {}

    public static void initOnce() {
        if (initialized) return;
        initialized = true;
    }

    public static void spawnParticle(Vec3 pos, int lifeTime, float startSize, Vec3 color, boolean canBePunched) {
        if (pos == null) throw new IllegalArgumentException("pos null");
        if (lifeTime <= 0) throw new IllegalArgumentException("lifeTime <= 0");
        if (color == null) throw new IllegalArgumentException("color null");
        initOnce();

        ThreadLocalRandom rng = ThreadLocalRandom.current();
        double vx = rng.nextDouble(-0.04, 0.04);
        double vy = rng.nextDouble(-0.02, 0.02);
        double vz = rng.nextDouble(-0.04, 0.04);
        Vec3 deltaMovement = new Vec3(vx, vy, vz);

        PARTICLES.add(new SinusoidParticle(pos, deltaMovement, lifeTime, startSize, color, canBePunched));
    }

    public static List<SinusoidParticle> getParticles() {
        return PARTICLES;
    }

    public static void tickAll(Level level) {
        if (level == null) return;
        for (SinusoidParticle p : PARTICLES) {
            if (!p.tick()) PARTICLES.remove(p);
            else {
                Vec3 next = p.position.add(p.velocity);
                AABB box = new AABB(next.x - p.startSize, next.y - p.startSize, next.z - p.startSize,
                        next.x + p.startSize, next.y + p.startSize, next.z + p.startSize);

                if (level.noCollision(box)) {
                    p.position = next;
                } else {
                    if (collides(level, p.position.add(p.velocity.x, 0, 0), p)) {
                        p.velocity = new Vec3(-p.velocity.x * 0.35, p.velocity.y, p.velocity.z);
                    }
                    if (collides(level, p.position.add(0, p.velocity.y, 0), p)) {
                        p.velocity = new Vec3(p.velocity.x, -p.velocity.y * 0.35, p.velocity.z);
                    }
                    if (collides(level, p.position.add(0, 0, p.velocity.z), p)) {
                        p.velocity = new Vec3(p.velocity.x, p.velocity.y, -p.velocity.z * 0.35);
                    }
                    p.position = p.position.add(p.velocity);
                }
            }
        }
    }

    public static void handleAttackInput() {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.level == null) return;

        Vec3 from = player.getEyePosition(1F);
        Vec3 look = player.getLookAngle();
        Vec3 to = from.add(look.scale(3.0));

        HitResult res = mc.level.clip(new net.minecraft.world.level.ClipContext(
                from, to,
                net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.NONE,
                player
        ));

        if (res.getType() != HitResult.Type.MISS) return;

        for (SinusoidParticle p : PARTICLES) {
            AABB box = new AABB(p.position.x - p.startSize,
                    p.position.y - p.startSize,
                    p.position.z - p.startSize,
                    p.position.x + p.startSize,
                    p.position.y + p.startSize,
                    p.position.z + p.startSize);
            if (box.clip(from, to).isPresent()) {
                p.punch(look, 0.28F);
            }
        }
    }

    private static boolean collides(Level level, Vec3 pos, SinusoidParticle p) {
        AABB box = new AABB(pos.x - p.startSize, pos.y - p.startSize, pos.z - p.startSize,
                pos.x + p.startSize, pos.y + p.startSize, pos.z + p.startSize);
        return !level.noCollision(box);
    }

    public static void clear() {
        PARTICLES.clear();
    }
}