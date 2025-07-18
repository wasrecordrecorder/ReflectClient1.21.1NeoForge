package com.dsp.main.Functions.Combat;

import com.dsp.main.Managers.Event.MoveInputEvent;
import com.dsp.main.Managers.FreeLook;
import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Settings.CheckBox;
import com.dsp.main.UI.ClickGui.Settings.Slider;
import com.dsp.main.Utils.Minecraft.Client.MoveUtil;
import com.dsp.main.Utils.TimerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

import static com.dsp.main.Managers.FreeLook.enableFreeLook;
import static com.dsp.main.Managers.FreeLook.getCameraYaw;

public class AutoFlipFireball extends Module {
    private static Fireball closestFireball = null;
    public static boolean isFlippingFireball = false;
    private static Slider dist = new Slider("Distance", 1, 6, 3, 1);
    private static Slider clickCd = new Slider("Click Cd", 1, 3, 1, 1);
    private static CheckBox rot = new CheckBox("Rotate", false);
    private int attackCooldown = 0;

    public AutoFlipFireball() {
        super("AutoFlipFireball", 0, Category.COMBAT, "Automatically flips fireballs around you");
        addSettings(dist, clickCd, rot);
    }

    @SubscribeEvent
    public void onRenderFrame(RenderFrameEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        Level level = mc.level;
        if (player == null || level == null || !isEnabled()) return;
        if (attackCooldown > 0) {
            attackCooldown--;
            return;
        }
        double range = dist.getValue();
        AABB aabb = new AABB(
                player.getX() - range, player.getY() - range, player.getZ() - range,
                player.getX() + range, player.getY() + range, player.getZ() + range
        );
        double closestDistance = Double.MAX_VALUE;
        for (Fireball fireball : level.getEntitiesOfClass(Fireball.class, aabb)) {
            double distance = player.getEyePosition().distanceTo(fireball.position());
            if (distance <= range && distance < closestDistance) {
                closestDistance = distance;
                closestFireball = fireball;
            }
        }
        if (closestFireball == null) return;

        if (rot.isEnabled()) {
            isFlippingFireball = true;
            if (!FreeLook.isFreeLookEnabled) enableFreeLook();
            Vec3 playerEye = player.getEyePosition();
            Vec3 fireballPos = closestFireball.position().add(0, closestFireball.getBbHeight() / 2, 0);
            Vec3 lookVec = fireballPos.subtract(playerEye);
            double targetYaw = Math.toDegrees(Math.atan2(lookVec.z, lookVec.x)) - 90;
            double deltaY = lookVec.y;
            double deltaXZ = Math.sqrt(lookVec.x * lookVec.x + lookVec.z * lookVec.z);
            double targetPitch = -Math.toDegrees(Math.atan2(deltaY, deltaXZ));
            float currentYaw = player.getYRot();
            float currentPitch = player.getXRot();
            float lerpFactor = 0.5f;
            float newYaw = (float) lerp(currentYaw, targetYaw, lerpFactor);
            float newPitch = (float) lerp(currentPitch, targetPitch, lerpFactor);

            player.setYRot(newYaw);
            player.setXRot(newPitch);
        }
        mc.gameMode.attack(player, closestFireball);
        mc.player.swing(InteractionHand.MAIN_HAND);
        closestFireball = null;
        TimerUtil.sleepVoid(() -> isFlippingFireball = false, 50);
        TimerUtil.sleepVoid(() -> {
            if (mc.player != null) {
                mc.player.setYRot(FreeLook.getCameraYaw());
                mc.player.setXRot(FreeLook.getCameraPitch());
            }
        }, 70);
        TimerUtil.sleepVoid(() -> {
            if (mc.player != null) {
                if (FreeLook.isFreeLookEnabled) FreeLook.disableFreeLook();
            }
        }, 90);
        attackCooldown = (int) clickCd.getValue();
    }

    private double lerp(double start, double end, float factor) {
        return start + (end - start) * factor;
    }
    @SubscribeEvent
    public void OnMoveInput(MoveInputEvent event) {
        if (FreeLook.isFreeLookEnabled && closestFireball != null) {
            MoveUtil.fixMovement(event, getCameraYaw());
        }
    }
}