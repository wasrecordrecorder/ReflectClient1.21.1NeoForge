package com.dsp.main.Functions.Combat;

import com.dsp.main.Core.FrndSys.FriendManager;
import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.CheckBox;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.Slider;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

import java.util.Comparator;

import static com.dsp.main.Api.mc;

public class AimAssistant extends Module {
    private static final Slider AIM_RANGE = new Slider("Aim Range", 3, 8, 4, 1);
    private static final Slider AIM_SPEED = new Slider("Aim Speed", 1, 10, 2, 1);
    private static final CheckBox LOCK_TARGET = new CheckBox("Lock Target", false);
    private static final CheckBox THROUGH_WALLS = new CheckBox("Through Walls", false);
    private static final CheckBox FRIEND_CHECK = new CheckBox("Check Friend", false);

    private LivingEntity currentTarget;

    public AimAssistant() {
        super("AimAssistant", 0, Category.COMBAT, "Smoothly aims at the nearest valid target");
        addSettings(AIM_RANGE, AIM_SPEED, LOCK_TARGET, THROUGH_WALLS, FRIEND_CHECK);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        currentTarget = null;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        currentTarget = null;
    }

    @SubscribeEvent
    public void onClientTick(RenderFrameEvent.Pre event) {
        if (mc.player == null || mc.level == null) {
            return;
        }
        if (LOCK_TARGET.isEnabled() && isValidTarget(currentTarget)) {
            LegitRot(currentTarget);
        } else {
            currentTarget = findNearestTarget();
            if (currentTarget != null) {
                LegitRot(currentTarget);
            }
        }
    }

    private boolean isValidTarget(LivingEntity entity) {
        if (entity == null || !entity.isAlive() || entity == mc.player) {
            return false;
        }
        double distance = mc.player.distanceTo(entity);
        if (distance > AIM_RANGE.getValue()) {
            return false;
        }
        if (!THROUGH_WALLS.isEnabled() && !hasClearLineOfSight(mc.player, entity)) {
            return false;
        }
        if (FRIEND_CHECK.isEnabled() && FriendManager.isFriend(entity.getName().getString())) {
            return false;
        }
        return true;
    }

    private LivingEntity findNearestTarget() {
        double range = AIM_RANGE.getValue();
        Vec3 playerPos = mc.player.getEyePosition();
        AABB searchBox = new AABB(playerPos, playerPos).inflate(range);

        return mc.level.getEntitiesOfClass(LivingEntity.class, searchBox, entity -> entity != mc.player && entity.isAlive())
                .stream()
                .filter(this::isValidTarget)
                .min(Comparator.comparingDouble(mc.player::distanceTo))
                .orElse(null);
    }

    public static void LegitRot(Entity target) {
        double deltaX = target.getX() - mc.player.getX();
        double deltaY = target.getY() - (mc.player.getY() + mc.player.getEyeHeight()) + 0.7D;
        double deltaZ = target.getZ() - mc.player.getZ();
        double distance = Mth.sqrt((float) (deltaX * deltaX + deltaZ * deltaZ));
        float yaw = (float) (Mth.atan2(deltaZ, deltaX) * (180D / Math.PI)) - 90F;
        float pitch = (float) -(Mth.atan2(deltaY, distance) * (180D / Math.PI));

        float yawOffset = Mth.wrapDegrees(yaw - mc.player.getYRot());
        mc.player.setYRot(mc.player.getYRot() + yawOffset * AIM_SPEED.getValueInt() * 0.05f);
    }

    private boolean hasClearLineOfSight(Player player, Entity target) {
        Vec3 playerEyePos = player.getEyePosition();
        Vec3 targetPos = target.position().add(0, target.getBbHeight() / 2, 0);
        ClipContext context = new ClipContext(
                playerEyePos,
                targetPos,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player
        );
        return mc.level.clip(context).getType() != HitResult.Type.BLOCK;
    }
}