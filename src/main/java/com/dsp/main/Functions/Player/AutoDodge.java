package com.dsp.main.Functions.Player;

import com.dsp.main.Core.Event.MoveInputEvent;
import com.dsp.main.Core.Event.OnUpdate;
import com.dsp.main.Core.Other.FreeLook;
import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.CheckBox;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.Mode;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.MultiCheckBox;
import com.dsp.main.Utils.Minecraft.Client.InvUtil;
import com.dsp.main.Utils.Minecraft.Client.MoveUtil;
import com.dsp.main.Utils.TimerUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.entity.projectile.windcharge.AbstractWindCharge;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;

import java.util.*;

import static com.dsp.main.Api.mc;

public class AutoDodge extends Module {
    private static final String FREELOOK_REQUEST_ID = "AutoDodge";

    private final Mode mode;
    private final MultiCheckBox dodgeTypes;
    private final Map<Entity, ProjectileData> trackedProjectiles = new HashMap<>();
    private final TimerUtil plastCooldownTimer = new TimerUtil();
    private final InvUtil invUtil = new InvUtil();

    private ProjectileData currentThreat = null;
    private float targetYaw = 0;
    private float targetPitch = 0;
    private boolean isDodging = false;
    private int dodgeTicks = 0;

    public AutoDodge() {
        super("AutoDodge", 0, Category.PLAYER, "Automatically dodge projectiles");

        mode = new Mode("Mode", "Auto", "Dodge", "Plast");

        dodgeTypes = new MultiCheckBox("Dodge Types", Arrays.asList(
                new CheckBox("Potions", true),
                new CheckBox("Arrows", true),
                new CheckBox("Tridents", true),
                new CheckBox("Fireballs", true),
                new CheckBox("Eggs", false),
                new CheckBox("Snowballs", false),
                new CheckBox("Shulker Bullets", true),
                new CheckBox("Wind Charges", true)
        ));

        addSettings(mode, dodgeTypes);
    }

    @SubscribeEvent
    public void onUpdate(OnUpdate event) {
        if (mc.player == null || mc.level == null) return;

        trackedProjectiles.clear();
        scanProjectiles();

        currentThreat = findMostDangerousProjectile();

        if (currentThreat != null) {
            FreeLook.requestFreeLook(FREELOOK_REQUEST_ID);
            handleThreat(currentThreat);
        } else {
            if (!isDodging) {
                FreeLook.releaseFreeLook(FREELOOK_REQUEST_ID);
            }
        }

        if (isDodging) {
            dodgeTicks++;
            if (dodgeTicks > 20 || currentThreat == null) {
                isDodging = false;
                dodgeTicks = 0;
                FreeLook.releaseFreeLook(FREELOOK_REQUEST_ID);
            }
        }
    }

    @SubscribeEvent
    public void onMoveInput(MoveInputEvent event) {
        if (isDodging && FreeLook.isFreeLookEnabled) {
            event.setForward(1.0F);
            event.setStrafe(0.0F);

            MoveUtil.fixMovement(event, targetYaw);

            mc.player.setYRot(targetYaw);
            mc.player.setXRot(targetPitch);
        }
    }

    @SubscribeEvent
    public void onChatReceived(ClientChatReceivedEvent event) {
        String message = event.getMessage().getString();
        if (message.contains("Здесь уже стоит трапка") ||
                message.contains("уже стоит") ||
                message.contains("пласт")) {
            plastCooldownTimer.reset();
        }
    }

    private void scanProjectiles() {
        if (mc.level == null || mc.player == null) return;

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof Projectile projectile) {
                if (projectile.getOwner() == mc.player) continue;
            }

            ProjectileData data = null;

            if (entity instanceof ThrownPotion && dodgeTypes.isOptionEnabled("Potions")) {
                if (!isProjectileMoving(entity)) continue;
                data = simulateTrajectory(entity);
                if (data != null && isPotionDangerous((ThrownPotion) entity, data)) {
                    trackedProjectiles.put(entity, data);
                }
            } else if (entity instanceof AbstractArrow && dodgeTypes.isOptionEnabled("Arrows")) {
                AbstractArrow arrow = (AbstractArrow) entity;
                if (arrow.onGround()) continue;
                if (!isArrowFlying(arrow)) continue;
                data = simulateTrajectory(entity);
                if (data != null && isDirectHitDangerous(data)) {
                    trackedProjectiles.put(entity, data);
                }
            } else if (entity instanceof ThrownTrident && dodgeTypes.isOptionEnabled("Tridents")) {
                ThrownTrident trident = (ThrownTrident) entity;
                if (trident.onGround()) continue;
                if (!isTridentFlying(trident)) continue;
                data = simulateTrajectory(entity);
                if (data != null && isDirectHitDangerous(data)) {
                    trackedProjectiles.put(entity, data);
                }
            } else if (entity instanceof Fireball && dodgeTypes.isOptionEnabled("Fireballs")) {
                if (!isProjectileMoving(entity)) continue;
                data = simulateTrajectory(entity);
                if (data != null && isAreaDangerous(data, 5.0)) {
                    trackedProjectiles.put(entity, data);
                }
            } else if (entity instanceof ThrownEgg && dodgeTypes.isOptionEnabled("Eggs")) {
                if (!isProjectileMoving(entity)) continue;
                data = simulateTrajectory(entity);
                if (data != null && isAreaDangerous(data, 3.0)) {
                    trackedProjectiles.put(entity, data);
                }
            } else if (entity instanceof Snowball && dodgeTypes.isOptionEnabled("Snowballs")) {
                if (!isProjectileMoving(entity)) continue;
                data = simulateTrajectory(entity);
                if (data != null && isDirectHitDangerous(data)) {
                    trackedProjectiles.put(entity, data);
                }
            } else if (entity instanceof ShulkerBullet && dodgeTypes.isOptionEnabled("Shulker Bullets")) {
                if (!isProjectileMoving(entity)) continue;
                data = simulateTrajectory(entity);
                if (data != null && isDirectHitDangerous(data)) {
                    trackedProjectiles.put(entity, data);
                }
            } else if (entity instanceof AbstractWindCharge && dodgeTypes.isOptionEnabled("Wind Charges")) {
                if (!isProjectileMoving(entity)) continue;
                data = simulateTrajectory(entity);
                if (data != null && isAreaDangerous(data, 6.0)) {
                    trackedProjectiles.put(entity, data);
                }
            }
        }
    }

    private boolean isProjectileMoving(Entity entity) {
        Vec3 motion = entity.getDeltaMovement();
        double speed = Math.sqrt(motion.x * motion.x + motion.y * motion.y + motion.z * motion.z);
        return speed > 0.1;
    }

    private boolean isArrowFlying(AbstractArrow arrow) {
        if (arrow.onGround()) return false;
        Vec3 motion = arrow.getDeltaMovement();
        double horizontalSpeed = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
        return horizontalSpeed > 0.15 || Math.abs(motion.y) > 0.15;
    }

    private boolean isTridentFlying(ThrownTrident trident) {
        if (trident.onGround()) return false;
        Vec3 motion = trident.getDeltaMovement();
        double horizontalSpeed = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
        return horizontalSpeed > 0.15 || Math.abs(motion.y) > 0.15;
    }

    private ProjectileData simulateTrajectory(Entity projectile) {
        if (mc.player == null || mc.level == null) return null;

        List<Vec3> trajectory = new ArrayList<>();
        Vec3 pos = projectile.position();
        Vec3 motion = projectile.getDeltaMovement();

        trajectory.add(pos);

        for (int i = 0; i < 150; i++) {
            Vec3 prevPos = pos;
            pos = pos.add(motion);
            trajectory.add(pos);

            BlockHitResult hitResult = mc.level.clip(new net.minecraft.world.level.ClipContext(
                    prevPos,
                    pos,
                    net.minecraft.world.level.ClipContext.Block.COLLIDER,
                    net.minecraft.world.level.ClipContext.Fluid.NONE,
                    projectile
            ));

            if (hitResult.getType() == HitResult.Type.BLOCK) {
                ProjectileData data = new ProjectileData();
                data.entity = projectile;
                data.landingPos = hitResult.getLocation();
                data.trajectory = trajectory;
                data.ticksToImpact = i;
                return data;
            }

            motion = calculateNextMotion(projectile, motion);

            if (pos.y < mc.level.getMinY()) break;
        }

        return null;
    }

    private Vec3 calculateNextMotion(Entity entity, Vec3 motion) {
        motion = motion.scale(0.99);

        if (!entity.isNoGravity()) {
            double gravity = 0.05;
            if (entity instanceof Projectile) {
                gravity = 0.03;
            }
            if (entity instanceof Fireball) {
                gravity = 0.0;
            }
            motion = motion.add(0, -gravity, 0);
        }

        return motion;
    }

    private boolean isPotionDangerous(ThrownPotion potion, ProjectileData data) {
        if (mc.player == null) return false;

        if (!checkPotionIsHarmful(potion)) return false;

        double distance = mc.player.position().distanceTo(data.landingPos);
        return distance < 7;
    }

    private boolean isDirectHitDangerous(ProjectileData data) {
        if (mc.player == null) return false;

        AABB playerBox = mc.player.getBoundingBox().inflate(0.8);
        return data.trajectory.stream().anyMatch(playerBox::contains);
    }

    private boolean isAreaDangerous(ProjectileData data, double radius) {
        if (mc.player == null) return false;

        double distance = mc.player.position().distanceTo(data.landingPos);
        return distance < radius;
    }

    private boolean checkPotionIsHarmful(ThrownPotion potion) {
        PotionContents contents = potion.getItem().get(net.minecraft.core.component.DataComponents.POTION_CONTENTS);
        if (contents == null) return false;

        for (MobEffectInstance inst : contents.getAllEffects()) {
            var holder = inst.getEffect();
            if (holder == null) continue;
            var effect = holder.value();
            if (effect.getCategory() == MobEffectCategory.HARMFUL || effect == MobEffects.JUMP) {
                return true;
            }
        }
        return false;
    }

    private ProjectileData findMostDangerousProjectile() {
        if (trackedProjectiles.isEmpty()) return null;

        return trackedProjectiles.values().stream()
                .min(Comparator.comparingInt(data -> data.ticksToImpact))
                .orElse(null);
    }

    private void handleThreat(ProjectileData threat) {
        if (mc.player == null) return;

        if (threat.entity instanceof ThrownPotion ||
                threat.entity instanceof Fireball ||
                threat.entity instanceof ThrownEgg ||
                threat.entity instanceof AbstractWindCharge) {
            handleAreaThreat(threat);
        } else if (threat.entity instanceof AbstractArrow ||
                threat.entity instanceof ThrownTrident ||
                threat.entity instanceof Snowball ||
                threat.entity instanceof ShulkerBullet) {
            handleDirectThreat(threat);
        }
    }

    private void handleAreaThreat(ProjectileData data) {
        if (mc.player == null) return;

        double distance = mc.player.position().distanceTo(data.entity.position());
        double dodgeDistance = 2.0;

        if (data.entity instanceof Fireball || data.entity instanceof AbstractWindCharge) {
            dodgeDistance = 3.0;
        }

        if (mode.isMode("Auto")) {
            boolean canUsePlast = !mc.player.getCooldowns().isOnCooldown(new ItemStack(Items.DRIED_KELP)) &&
                    plastCooldownTimer.hasReached(2000) &&
                    distance > 3 &&
                    distance < 7 &&
                    data.entity.tickCount > 1;

            if (canUsePlast) {
                usePlast(data);
            } else {
                dodgeAwayFromTrajectory(data, dodgeDistance);
            }
        } else if (mode.isMode("Dodge")) {
            dodgeAwayFromTrajectory(data, dodgeDistance);
        } else if (mode.isMode("Plast")) {
            if (distance > 3 && distance < 7 && data.entity.tickCount > 1) {
                usePlast(data);
            }
        }
    }

    private void handleDirectThreat(ProjectileData data) {
        if (mc.player == null || mc.level == null) return;

        Vec3 projectileMotion = data.entity.getDeltaMovement().normalize();
        Vec3 perpendicular1 = new Vec3(-projectileMotion.z, 0, projectileMotion.x).normalize();
        Vec3 perpendicular2 = perpendicular1.reverse();

        Vec3 playerPos = mc.player.position();
        Vec3 pos1 = playerPos.add(perpendicular1.scale(1.5));
        Vec3 pos2 = playerPos.add(perpendicular2.scale(1.5));

        boolean safe1 = isSafePosition(pos1, data.trajectory) && !hasObstacle(playerPos, pos1);
        boolean safe2 = isSafePosition(pos2, data.trajectory) && !hasObstacle(playerPos, pos2);

        if (safe1 && safe2) {
            double dist1 = pos1.distanceTo(data.entity.position());
            double dist2 = pos2.distanceTo(data.entity.position());

            Vec3 dodgeDirection = dist1 > dist2 ? perpendicular1 : perpendicular2;
            targetYaw = getYawFromDirection(dodgeDirection);
            targetPitch = mc.player.getXRot();
            isDodging = true;
            dodgeTicks = 0;
        } else if (safe1) {
            targetYaw = getYawFromDirection(perpendicular1);
            targetPitch = mc.player.getXRot();
            isDodging = true;
            dodgeTicks = 0;
        } else if (safe2) {
            targetYaw = getYawFromDirection(perpendicular2);
            targetPitch = mc.player.getXRot();
            isDodging = true;
            dodgeTicks = 0;
        }

        if (mode.isMode("Auto") || mode.isMode("Plast")) {
            double distance = mc.player.position().distanceTo(data.entity.position());
            if (distance > 3 && distance < 7 &&
                    !mc.player.getCooldowns().isOnCooldown(new ItemStack(Items.DRIED_KELP)) &&
                    plastCooldownTimer.hasReached(2000)) {
                usePlast(data);
            }
        }
    }

    private boolean isSafePosition(Vec3 pos, List<Vec3> trajectory) {
        AABB testBox = new AABB(pos.add(-0.8, 0, -0.8), pos.add(0.8, 1.8, 0.8));
        return trajectory.stream().noneMatch(testBox::contains);
    }

    private boolean hasObstacle(Vec3 from, Vec3 to) {
        if (mc.level == null || mc.player == null) return true;

        BlockHitResult result = mc.level.clip(new net.minecraft.world.level.ClipContext(
                from.add(0, 0.5, 0),
                to.add(0, 0.5, 0),
                net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.NONE,
                mc.player
        ));

        return result.getType() == HitResult.Type.BLOCK;
    }

    private void dodgeAwayFromTrajectory(ProjectileData data, double dodgeDistance) {
        if (mc.player == null || mc.level == null) return;

        Vec3 playerPos = mc.player.position();

        Vec3 currentDir = playerPos.subtract(data.entity.position()).normalize();
        Vec3 perpendicular1 = new Vec3(-currentDir.z, 0, currentDir.x).normalize();
        Vec3 perpendicular2 = perpendicular1.reverse();

        Vec3 option1 = currentDir;
        Vec3 option2 = perpendicular1;
        Vec3 option3 = perpendicular2;
        Vec3 option4 = currentDir.add(perpendicular1).normalize();
        Vec3 option5 = currentDir.add(perpendicular2).normalize();
        Vec3 option6 = currentDir.scale(-1);
        Vec3 option7 = currentDir.add(perpendicular1.scale(2)).normalize();
        Vec3 option8 = currentDir.add(perpendicular2.scale(2)).normalize();

        List<Vec3> options = Arrays.asList(option1, option2, option3, option4, option5, option6, option7, option8);
        Vec3 bestDirection = null;
        double maxMinDistance = 0;

        for (Vec3 direction : options) {
            Vec3 targetPos = playerPos.add(direction.scale(dodgeDistance));

            if (hasObstacle(playerPos, targetPos)) continue;

            double minDistToTrajectory = getMinDistanceToTrajectory(targetPos, data.trajectory);

            if (minDistToTrajectory > maxMinDistance) {
                maxMinDistance = minDistToTrajectory;
                bestDirection = direction;
            }
        }

        if (bestDirection != null) {
            targetYaw = getYawFromDirection(bestDirection);
            targetPitch = mc.player.getXRot();
            isDodging = true;
            dodgeTicks = 0;
        }
    }

    private double getMinDistanceToTrajectory(Vec3 point, List<Vec3> trajectory) {
        double minDistance = Double.MAX_VALUE;

        for (Vec3 trajectoryPoint : trajectory) {
            double distance = point.distanceTo(trajectoryPoint);
            if (distance < minDistance) {
                minDistance = distance;
            }
        }

        return minDistance;
    }

    private void usePlast(ProjectileData data) {
        if (mc.player == null) return;

        int slot = invUtil.getSlotInInventory(Items.DRIED_KELP);
        if (slot == -1) return;

        if (!InvUtil.ValidateItem(mc.player.getInventory().getItem(slot))) return;

        Vec2 rotation = RotationUtil.getRotation(data.entity.position());

        targetYaw = rotation.x;
        targetPitch = rotation.y;

        mc.player.setYRot(rotation.x);
        mc.player.setXRot(rotation.y);

        invUtil.findItemAndThrow(Items.DRIED_KELP, rotation.x, rotation.y);
    }

    private float getYawFromDirection(Vec3 direction) {
        return (float) Math.toDegrees(Mth.atan2(direction.z, direction.x)) - 90.0F;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        trackedProjectiles.clear();
        currentThreat = null;
        isDodging = false;
        FreeLook.releaseFreeLook(FREELOOK_REQUEST_ID);
    }

    private static class ProjectileData {
        Entity entity;
        Vec3 landingPos;
        List<Vec3> trajectory;
        int ticksToImpact;
    }

    private static class RotationUtil {
        static Vec2 getRotation(Vec3 target) {
            if (mc.player == null) return new Vec2(0, 0);

            Vec3 eyePos = mc.player.getEyePosition();
            double deltaX = target.x - eyePos.x;
            double deltaY = target.y - eyePos.y;
            double deltaZ = target.z - eyePos.z;

            double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
            float yaw = (float) Math.toDegrees(Mth.atan2(deltaZ, deltaX)) - 90.0F;
            float pitch = (float) -Math.toDegrees(Mth.atan2(deltaY, distance));

            float sensitivity = (float) Math.pow(mc.options.sensitivity().get(), 1.5) * 0.05F + 0.1F;
            float gcd = sensitivity * sensitivity * sensitivity * 1.2F;

            yaw -= yaw % gcd;
            pitch -= pitch % (gcd * sensitivity);

            return new Vec2(yaw, pitch);
        }
    }
}