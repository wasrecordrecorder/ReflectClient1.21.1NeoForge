package com.dsp.main.Utils.Minecraft.Client;

import net.minecraft.world.phys.Vec2;

import static com.dsp.main.Api.mc;

public class ClientPlayerUtil {
    public static boolean isPlayerFalling() {
        boolean cancelReason = mc.player.isInLava()
                || mc.player.isPassenger();
        boolean onGround = mc.player.onGround();
        float attackStrength = mc.player.getAttackStrengthScale(1.0f);
        return !cancelReason && attackStrength >= 0.92 && !onGround && mc.player.fallDistance > 0;
    }
    public static boolean isMoving() {
        Vec2 vector2f = mc.player.input.getMoveVector();
        return vector2f.x != 0.0F || vector2f.y != 0.0F;
    }

    public static boolean hasEnoughImpulseToStartSprinting() {
        double d0 = 0.8D;
        return (double)mc.player.input.forwardImpulse >= d0;
    }
}
