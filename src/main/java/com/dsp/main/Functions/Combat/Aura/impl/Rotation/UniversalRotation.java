package com.dsp.main.Functions.Combat.Aura.impl.Rotation;

import com.dsp.main.Functions.Combat.Aura.Aura;
import com.dsp.main.Functions.Combat.Aura.impl.RotationAngle;
import com.dsp.main.Utils.AI.AimPredictor;
import com.dsp.main.Utils.Render.Other.Vec2Vector;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector2f;
import org.joml.Vector3d;

import static com.dsp.main.Api.mc;
import static com.dsp.main.Api.partialTickAp;

public class UniversalRotation extends RotationAngle {
    private Vector2f prev;
    private static boolean useAI = false;

    @Override
    public void update(Aura aura, Entity target) {
        try {
            if (rotation == null) {
                rotation = new Vector2f(mc.player.getYRot(), mc.player.getXRot());
            }
            if (prev == null) {
                prev = new Vector2f(mc.player.getYRot(), mc.player.getXRot());
            }

            Vector2f currentRot = new Vector2f(mc.player.getYRot(), mc.player.getXRot());

            if (AimPredictor.isAiAvailable()) {
                Vector2f aiRotation = AimPredictor.predict(
                        (LivingEntity) target,
                        currentRot,
                        prev,
                        new Vector2f(60, 60)
                );

                if (aiRotation != null) {
                    rotation = aiRotation;
                    useAI = true;
                } else {
                    rotation = calculateBasicRotation(target);
                    useAI = false;
                }
            } else {
                rotation = calculateBasicRotation(target);
                useAI = false;
            }

            mc.player.setYRot(rotation.x);
            mc.player.setXRot(rotation.y);

            prev = new Vector2f(currentRot);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Vector2f calculateBasicRotation(Entity target) {
        if (target == null) return rotation;

        Vector3d targetPos = Vec2Vector.convert2(target.getPosition(partialTickAp));
        Vector2f targetRot = AimPredictor.get(targetPos);

        return AimPredictor.correctRotation(targetRot.x, targetRot.y);
    }

    public static boolean isUsingAI() {
        return useAI;
    }
}