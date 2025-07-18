package com.dsp.main.Functions.Combat;

import com.dsp.main.Managers.Event.OnUpdate;
import com.dsp.main.Managers.FreeLook;
import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Settings.CheckBox;
import com.dsp.main.UI.ClickGui.Settings.Slider;
import com.dsp.main.Utils.TimerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class HitBox extends Module {
    private static final Logger LOGGER = Logger.getLogger("HitBox");
    private static final Slider size = new Slider("Size", 0.1, 2, 0.5, 0.1);
    private static final CheckBox bypass = new CheckBox("Use Bypass", false);
    private static final Map<UUID, AABB> originalHitboxes = new HashMap<>();
    private static final Minecraft mc = Minecraft.getInstance();

    public HitBox() {
        super("HitBox", 0, Category.COMBAT, "Making other player hitbox much higher");
        addSettings(size, bypass);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        //mc.getEntityRenderDispatcher().setRenderHitBoxes(true);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        //mc.getEntityRenderDispatcher().setRenderHitBoxes(false);
        if (mc.level != null) {
            for (Player player : mc.level.players()) {
                if (player != mc.player) {
                    AABB original = originalHitboxes.get(player.getUUID());
                    if (original != null) {
                        player.setBoundingBox(original);
                    }
                }
            }
        }
        originalHitboxes.clear();
    }

    @SubscribeEvent
    public void onUpdate(RenderPlayerEvent.Pre e) {
        if (mc.level == null || mc.player == null) return;
        float expansion = size.getValueFloat();
        for (Player player : mc.level.players()) {
            if (player != mc.player && player.isAlive()) {
                if (!originalHitboxes.containsKey(player.getUUID())) {
                    originalHitboxes.put(player.getUUID(), player.getBoundingBox());
                }
                AABB newHitbox = new AABB(
                        player.getX() - expansion,
                        player.getY() - (bypass.isEnabled() ? 0 : expansion),
                        player.getZ() - expansion,
                        player.getX() + expansion,
                        player.getY() + player.getBbHeight() + (bypass.isEnabled() ? 0 : expansion),
                        player.getZ() + expansion
                );
                player.setBoundingBox(newHitbox);
            }
        }
    }
    @SubscribeEvent
    public void onAttack(AttackEntityEvent e) {
        if (mc.player == null || !bypass.isEnabled() || !(e.getTarget() instanceof Player)) return;
        if (!FreeLook.isFreeLookEnabled) FreeLook.enableFreeLook();
        Player target = (Player) e.getTarget();
        if (target == mc.player) return;
        Vec3 playerPos = mc.player.getEyePosition();
        Vec3 targetPos = target.position().add(0, target.getBbHeight() * 0.5, 0);
        double deltaX = targetPos.x - playerPos.x;
        double deltaY = targetPos.y - playerPos.y;
        double deltaZ = targetPos.z - playerPos.z;
        double distanceXZ = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        float targetYaw = (float) Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90F;
        float targetPitch = (float) -Math.toDegrees(Math.atan2(deltaY, distanceXZ));
        targetYaw = Mth.wrapDegrees(targetYaw);
        targetPitch = Mth.clamp(targetPitch, -90F, 90F);
        mc.player.setYRot(targetYaw);
        mc.player.setXRot(targetPitch);
        TimerUtil.sleepVoid(() -> {
            if (mc.player != null) {
                mc.player.setYRot(FreeLook.getCameraYaw());
                mc.player.setXRot(FreeLook.getCameraPitch());
            }
        }, 50);
        TimerUtil.sleepVoid(() -> {
            if (mc.player != null) {
                if (FreeLook.isFreeLookEnabled) FreeLook.disableFreeLook();
            }
        }, 60);
    }
}