package com.dsp.main.Utils.Minecraft.Client;

import static com.dsp.main.Api.mc;

public class ClientPlayerUtil {
    public static boolean isPlayerFalling() {
        boolean cancelReason = mc.player.isInLava()
                || mc.player.isPassenger();
        boolean onGround = mc.player.onGround();
        float attackStrength = mc.player.getAttackStrengthScale(1.0f);
        return !cancelReason && attackStrength >= 0.92 && !onGround && mc.player.fallDistance > 0;
    }
}
