package com.dsp.main.Functions.Combat.Aura;

import com.dsp.main.Api;
import com.dsp.main.Module;
import com.dsp.main.Core.Event.MoveInputEvent;
import com.dsp.main.Core.Event.OnUpdate;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.*;
import com.dsp.main.Utils.Minecraft.Client.ClientFallDistance;
import com.dsp.main.Utils.Minecraft.Client.InvUtil;
import com.dsp.main.Utils.Minecraft.Player.PredictUtility;
import com.dsp.main.Utils.TimerUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static com.dsp.main.Api.mc;
import static com.dsp.main.Functions.Combat.AutoTotem.health;

public class ElytraTarget extends Module {

    public final MultiCheckBox utilities = new MultiCheckBox("Utilities", Arrays.asList(
            new CheckBox("Freeze Dummy", false),
            new CheckBox("Pearl Following", true),
            new CheckBox("Switch to Attacker", true),
            new CheckBox("Firework on Slowdown", true),
            new CheckBox("Swap Elytra", true),
            new CheckBox("Resolver", true),
            new CheckBox("Predict", true),
            new CheckBox("Change Chest on Fall", false)
    ));

    public final CheckBox predictDefensive = new CheckBox("Anti-Defensive", true);
    public final CheckBox span = new CheckBox("Span", true);
    public final CheckBox doubleRot = new CheckBox("Double Rotation", false);

    public final MultiCheckBox autoLeave = new MultiCheckBox("Auto Leave", Arrays.asList(
            new CheckBox("Low Health", false),
            new CheckBox("On Cooldown", true),
            new CheckBox("On Item Use", true),
            new CheckBox("On Bind Press", false)
    ));

    public final CheckBox minimizeFireworkUsage = new CheckBox("Minimize Firework Usage", true);
    public final Slider fireworkCooldown = new Slider("Firework Cooldown", 50, 3000, 500, 50);
    public final CheckBox swapChestOnFreeze = new CheckBox("Swap Chest on Freeze", true);
    public final Slider leaveCooldown = new Slider("Leave Firework Cooldown", 50, 3000, 500, 50);
    public final CheckBox onlyGroundLeave = new CheckBox("Only Ground Leave", true);
    public final Slider leaveHealth = new Slider("Leave Health", 1F, 20F, 10F, 0.5F);

    public final MultiCheckBox visuals = new MultiCheckBox("Visuals", Arrays.asList(
            new CheckBox("Leave Lines", true),
            new CheckBox("Target Line", true),
            new CheckBox("Show Misses", false),
            new CheckBox("Resolver Position", true)
    ));

    public final MultiCheckBox leaveVectors = new MultiCheckBox("Leave Vectors", Arrays.asList(
            new CheckBox("Up", true),
            new CheckBox("Down", false),
            new CheckBox("East", true),
            new CheckBox("West", true),
            new CheckBox("North", true),
            new CheckBox("South", true)
    ));

    public final CheckBox swapVectorOnHit = new CheckBox("Swap Vector on Hit", true);
    public final CheckBox defensiveDesync = new CheckBox("Defensive Desync", false);
    public final Slider fireworkSlot = new Slider("Firework Slot", 1, 9, 7, 1);

    public final MultiCheckBox boost = new MultiCheckBox("Boost", Arrays.asList(
            new CheckBox("Enable", true),
            new CheckBox("To Target", true),
            new CheckBox("Smart Boost", true)
    ));

    public final Slider fallingSpeed = new Slider("Falling Speed", 0F, 0.4F, 0.3F, 0.05F);
    public final Slider jumpSpeed = new Slider("Jump Speed", 0F, 0.4F, 0.3F, 0.05F);
    public final Slider groundSpeed = new Slider("Ground Speed", 0F, 0.4F, 0F, 0.05F);
    public final Slider centrifugalForce = new Slider("Centrifugal Force", 0F, 0.3F, 0F, 0.05F);
    public final Slider multiplier = new Slider("Boost Multiplier", -0.2F, 3F, -0.1F, 0.05F);

    private final TimerUtil useTimer = new TimerUtil();
    private final TimerUtil defensiveTimer = new TimerUtil();
    private final TimerUtil switchTimer = new TimerUtil();

    private Entity pearlEntity = null;
    private Vec3 lastPearlPos = null;
    private boolean prevFreezed = false;

    private Vec3 leaveVec = Vec3.ZERO;
    private Vec3 lastVec = Vec3.ZERO;

    private Vec3 defensivePos = null;
    private boolean defensiveActive = false;
    private boolean lastDefensive = false;
    private final ArrayList<Packet<?>> packets = new ArrayList<>();

    private ItemStack currentChestStack = ItemStack.EMPTY;
    private boolean bindLeaving = false;
    private double lastSpeed = 0;
    private LivingEntity lastAttacker = null;

    private final InvUtil invUtil = new InvUtil();

    private float lastYaw = 0;
    private float lastPitch = 0;

    public ElytraTarget() {
        super("ElytraTarget", 0, Category.COMBAT, "Elytra combat targeting");
        addSettings(
                utilities, predictDefensive, span, doubleRot, autoLeave,
                minimizeFireworkUsage, fireworkCooldown, swapChestOnFreeze,
                leaveCooldown, onlyGroundLeave, leaveHealth, visuals,
                leaveVectors, swapVectorOnHit, defensiveDesync, fireworkSlot,
                boost, fallingSpeed, jumpSpeed, groundSpeed, centrifugalForce, multiplier
        );
    }

    @SubscribeEvent
    public void onUpdate(OnUpdate event) {
        if (mc.player == null || mc.level == null) return;
        if (!Api.isEnabled("Aura")) {
            resetState();
            return;
        }

        LivingEntity target = (LivingEntity) Aura.Target;

        currentChestStack = mc.player.getItemBySlot(EquipmentSlot.CHEST);

        if (utilities.isOptionEnabled("Change Chest on Fall") && target != null) {
            handleChestChange(target);
        }

        if (InvUtil.getFireWorks() == -1) {
            return;
        }

        handleSpeedTracking();
        handleAttackerSwitch();
        handlePearlTracking();

        if (target != null || lastPearlPos != null) {
            processTargeting(target);
        }

        if (boost.isOptionEnabled("Enable") && boost.isOptionEnabled("To Target") && target != null) {
            applyBoost(target);
        }
    }

    @SubscribeEvent
    public void onMoveInput(MoveInputEvent event) {
        if (mc.player == null || mc.level == null) return;
        if (!Api.isEnabled("Aura")) return;

        LivingEntity target = (LivingEntity) Aura.Target;
        if (target == null && lastPearlPos == null) return;

        boolean leave = canLeave(target);

        Vec3 targetVec = lastPearlPos != null ? lastPearlPos : getTargetPosition(target);
        if (targetVec == null && target != null) {
            targetVec = target.getEyePosition();
        }

        List<Vec3> leaveVectorsList = getLeaveVectorsList(target);

        if (leave && leaveVec.equals(Vec3.ZERO)) {
            for (Vec3 vector : leaveVectorsList) {
                if (canSeeVector(vector) && vector.y < 400 &&
                        (!swapVectorOnHit.isEnabled() || !lastVec.equals(vector))) {
                    leaveVec = vector;
                    break;
                }
            }
        }

        Vec3 lookTarget = leave ? leaveVec : targetVec;
        if (lookTarget != null && !lookTarget.equals(Vec3.ZERO) && mc.player.isFallFlying()) {
            applyRotation(lookTarget);
        }

        for (Vec3 vector : leaveVectorsList) {
            if (vector.equals(leaveVec)) {
                leaveVec = vector;
                break;
            }
        }

        boolean canFreeze = calculateFreezeCondition(target, leave);

        if (canFreeze) {
            event.setForward(0);
            event.setStrafe(0);
        }

        handleElytraSwap(canFreeze);

        prevFreezed = canFreeze;
    }

    @SubscribeEvent
    public void onAttack(AttackEntityEvent event) {
        if (event.getEntity() == mc.player) {
            handlePlayerAttack(event);
        } else {
            handleOtherAttack(event);
        }
    }

    @SubscribeEvent
    public void onEntityJoin(EntityJoinLevelEvent event) {
        if (!utilities.isOptionEnabled("Pearl Following")) return;

        if (event.getEntity() instanceof ThrownEnderpearl pearl) {
            handlePearlSpawn(pearl);
        }
    }

    @SubscribeEvent
    public void onRender(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        LivingEntity target = (LivingEntity) Aura.Target;
        if (target == null && lastPearlPos == null) return;

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();

        renderVisuals(poseStack, buffer, target);

        buffer.endBatch();
    }

    private void applyBoost(LivingEntity target) {
        if (!mc.player.isFallFlying() || target == null) return;

        Vec3 playerPos = mc.player.position();
        Vec3 targetPos = utilities.isOptionEnabled("Predict") ?
                PredictUtility.predictElytraPos(target, 5) : target.position();

        Vec3 direction = targetPos.subtract(playerPos).normalize();

        float speed = getBoostSpeed();

        if (centrifugalForce.getValueFloat() > 0) {
            float gradus = System.currentTimeMillis() / 100f;
            float centrifugal = centrifugalForce.getValueFloat();
            float deviationX = (float) Math.cos(Math.toRadians(gradus)) * centrifugal;
            float deviationZ = (float) Math.sin(Math.toRadians(gradus)) * centrifugal;
            direction = direction.add(deviationX, 0, deviationZ);
        }

        Vec3 currentMotion = mc.player.getDeltaMovement();
        double newX = currentMotion.x + direction.x * speed;
        double newZ = currentMotion.z + direction.z * speed;

        mc.player.setDeltaMovement(newX, currentMotion.y, newZ);
    }

    private float getBoostSpeed() {
        if (!boost.isOptionEnabled("Smart Boost")) {
            return mc.player.onGround() ? groundSpeed.getValueFloat() :
                    ClientFallDistance.get() > 0 ? fallingSpeed.getValueFloat() : jumpSpeed.getValueFloat();
        }

        float yaw = lastYaw;
        float pitch = lastPitch;

        int[] yawVectors = {-45, 45, 135, -135};
        int[] addVectors = {-90, 90, 180, -180, 0};
        int[] pitchVectors = {-45, 45};

        int minDist = findClosestVector(yaw, yawVectors);
        float maxDist = Math.abs(Mth.wrapDegrees(yaw) - yawVectors[minDist]);

        int addMinDist = findClosestVector(yaw, addVectors);
        float addMaxDist = Math.abs(Mth.wrapDegrees(yaw) - addVectors[addMinDist]);

        float countableSpeed = 0.3f - maxDist * 0.15f / 45f;

        if (addMaxDist < 10) {
            countableSpeed += 0.05f - 0.05f * addMaxDist / 10f;
        }

        int pitchMinDist = findClosestVector(pitch, pitchVectors);
        float pitchMaxDist = Math.abs(Math.abs(pitch) - Math.abs(pitchVectors[pitchMinDist]));

        if (pitchMaxDist < 26) {
            countableSpeed = Math.max(0.25f, countableSpeed);
            countableSpeed += 0.02f - pitchMaxDist * 0.02f / 26f;
        }

        countableSpeed *= (1 + multiplier.getValueFloat());

        return Math.max(0, Math.min(0.4f, countableSpeed));
    }

    private int findClosestVector(float angle, int[] vectors) {
        int index = 0;
        int minDistIndex = -1;
        float minDist = Float.MAX_VALUE;

        for (int vector : vectors) {
            float dist = Math.abs(Mth.wrapDegrees(angle) - vector);
            if (dist < minDist) {
                minDist = dist;
                minDistIndex = index;
            }
            index++;
        }

        return minDistIndex;
    }

    private void handleChestChange(LivingEntity target) {
        Vec3 playerMotion = mc.player.getDeltaMovement();
        Vec3 targetMotion = target.getDeltaMovement();

        if (playerMotion.y > 4 && targetMotion.y > 4 &&
                Math.abs(mc.player.getY() - target.getY()) > 5) {
            changeChestplate(currentChestStack);
        } else if (targetMotion.y < 5) {
            changeChestplate(currentChestStack);
        }
    }

    private void handleSpeedTracking() {
        Vec3 playerPos = mc.player.position();
        Vec3 prevPos = new Vec3(mc.player.xOld, mc.player.yOld, mc.player.zOld);

        double motion = Math.sqrt(
                Math.pow(playerPos.y - prevPos.y, 2) +
                        Math.pow(playerPos.x - prevPos.x, 2) +
                        Math.pow(playerPos.z - prevPos.z, 2)
        ) * 20;

        if (utilities.isOptionEnabled("Firework on Slowdown") && motion < lastSpeed && mc.player.isFallFlying()) {
            useFirework();
        }
        lastSpeed = motion;
    }

    private void handleAttackerSwitch() {
        if (!utilities.isOptionEnabled("Switch to Attacker")) return;
        if (lastAttacker == null || switchTimer.hasReached(400)) return;

        if (mc.player.distanceTo(lastAttacker) < 6) {
            Aura.Target = lastAttacker;
        }
    }

    private void handlePearlTracking() {
        if (pearlEntity != null && pearlEntity.isAlive() && mc.level.getEntity(pearlEntity.getId()) != null) {
            lastPearlPos = pearlEntity.position();
        } else {
            if (lastPearlPos != null && mc.player.position().distanceTo(lastPearlPos) < 3) {
                lastPearlPos = null;
                pearlEntity = null;
            }
        }
    }

    private void processTargeting(LivingEntity target) {
        boolean leave = canLeave(target);

        for (Entity entity : mc.level.getEntities(mc.player, mc.player.getBoundingBox().inflate(10))) {
            if (entity instanceof EndCrystal) {
                if (mc.player.position().distanceTo(entity.position()) < 10) {
                    leave = true;
                    break;
                }
            }
        }

        defensiveActive = !leave;

        handleDefensive(target);
        handleElytraFlight(target, leave);
    }

    private void handleDefensive(LivingEntity target) {
        if (!defensiveDesync.isEnabled() || mc.hasSingleplayerServer()) return;

        boolean canUseDefensive = target != null &&
                mc.player.position().distanceTo(target.position()) < 20 &&
                !isTargetLeaving(target);

        if (!defensiveDesync.isEnabled() || !defensiveActive ||
                defensiveTimer.hasReached(1000) || !canUseDefensive) {

            for (Packet<?> p : packets) {
                if (mc.getConnection() != null) {
                    mc.getConnection().send(p);
                }
            }
            packets.clear();
            defensivePos = mc.player.position();
            defensiveTimer.reset();
        }

        if (!lastDefensive && defensiveActive) {
            defensivePos = mc.player.position();
            defensiveTimer.reset();
        }

        lastDefensive = defensiveActive;
    }

    private void handleElytraFlight(LivingEntity target, boolean leave) {
        Item chestItem = getCurrentChestItem();
        boolean hasElytra = chestItem == Items.ELYTRA;

        if (!hasElytra || mc.player.isUsingItem()) return;

        if (mc.player.isFallFlying()) {
            processElytraFlying(target, leave);
        } else {
            startElytraFlying();
        }
    }

    private void processElytraFlying(LivingEntity target, boolean leave) {
        boolean canFreeze = calculateFreezeCondition(target, leave);
        boolean canUse = !(canFreeze && minimizeFireworkUsage.isEnabled());

        long cooldown = (leave && target != null && mc.player.position().distanceTo(target.position()) < 6) ?
                (long)leaveCooldown.getValue() : (long)fireworkCooldown.getValue();

        if (useTimer.hasReached(cooldown) && canUse) {
            useFirework();
        }
    }

    private void startElytraFlying() {
        if (mc.player.onGround() && !mc.options.keyJump.isDown()) {
            mc.player.jumpFromGround();
        } else {
            if (ClientFallDistance.get() > 0.08f) {
                mc.player.startFallFlying();
                if (mc.getConnection() != null) {
                    mc.getConnection().send(new ServerboundPlayerCommandPacket(
                            mc.player,
                            ServerboundPlayerCommandPacket.Action.START_FALL_FLYING
                    ));
                }
                useFirework();
            }
        }
    }

    private boolean calculateFreezeCondition(LivingEntity target, boolean leave) {
        if (target == null) return false;

        double distToTarget = mc.player.position().distanceTo(target.position());
        double distToLeave = leaveVec.equals(Vec3.ZERO) ? Double.MAX_VALUE : mc.player.position().distanceTo(leaveVec);

        return (utilities.isOptionEnabled("Freeze Dummy") && distToTarget < 3 && !leave) ||
                (leave && distToLeave < 1);
    }

    private void handleElytraSwap(boolean canFreeze) {
        if (!utilities.isOptionEnabled("Swap Elytra")) return;

        if (canFreeze && getCurrentChestItem() == Items.ELYTRA) {
            swapToChestplate();
        }

        if (!canFreeze && getCurrentChestItem() != Items.ELYTRA) {
            swapToElytra();
        }
    }

    private void handlePlayerAttack(AttackEntityEvent event) {
        if (!(event.getTarget() instanceof LivingEntity target)) return;
        if (event.getTarget() != Aura.Target) return;

        boolean leave = canLeave(target);
        List<Vec3> leaveVectorsList = getLeaveVectorsList(target);

        for (Vec3 vector : leaveVectorsList) {
            if (canSeeVector(vector) && vector.y < 255 &&
                    (!swapVectorOnHit.isEnabled() || !lastVec.equals(vector.subtract(target.getEyePosition())))) {
                leaveVec = vector;
                break;
            }
        }

        if (doubleRot.isEnabled() && target != null && mc.player.isFallFlying() && isTargetLeaving(target)) {
            double distToCurrent = mc.player.position().distanceTo(target.position());
            double distToPredicted = mc.player.position().distanceTo(target.position().add(target.getDeltaMovement()));

            if (distToCurrent > distToPredicted) {
                Vec3 pos = mc.player.position();
                float[] rotation = getRotationTo(target.position().add(0, target.getBbHeight() / 2, 0));
                sendPositionPacket(pos.x, pos.y - 1e-6, pos.z, rotation[0], rotation[1]);
            }
        }

        lastVec = leaveVec.subtract(target.getEyePosition());
    }

    private void handleOtherAttack(AttackEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity attacker)) return;
        if (event.getTarget() != mc.player) return;

        lastAttacker = attacker;
        switchTimer.reset();
    }

    private void handlePearlSpawn(ThrownEnderpearl pearl) {
        LivingEntity target = (LivingEntity) Aura.Target;
        if (target == null) return;

        List<AbstractClientPlayer> nearbyPlayers = mc.level.players().stream()
                .filter(p -> p != mc.player)
                .filter(p -> p.position().distanceTo(pearl.position()) <= 5)
                .sorted(Comparator.comparingDouble(p -> p.position().distanceTo(pearl.position())))
                .toList();

        if (!nearbyPlayers.isEmpty() && nearbyPlayers.get(0) == target) {
            if (pearl.position().distanceTo(target.position()) < 5) {
                pearlEntity = pearl;
            }
        }
    }

    private void renderVisuals(PoseStack poseStack, MultiBufferSource.BufferSource buffer, LivingEntity target) {
        boolean leave = canLeave(target);
        List<Vec3> leaveVectorsList = getLeaveVectorsList(target);

        if (visuals.isOptionEnabled("Leave Lines") && leave && !leaveVec.equals(Vec3.ZERO) && target != null) {
            renderLeaveLines(poseStack, buffer, target, leaveVectorsList);
        }

        if (visuals.isOptionEnabled("Target Line") && !leave && target != null) {
            renderTargetLine(poseStack, buffer, target);
        }
    }

    private void renderLeaveLines(PoseStack poseStack, MultiBufferSource.BufferSource buffer,
                                  LivingEntity target, List<Vec3> vectors) {
        Vec3 startPos = target.position().add(0, mc.player.getBbHeight() / 2, 0);

        for (Vec3 vector : vectors) {
            boolean isGood = canSeeVector(vector) && vector.y < 400 &&
                    (!swapVectorOnHit.isEnabled() || !leaveVec.equals(vector));

            float r = isGood ? 0 : 1;
            float g = isGood ? 1 : 0;

            drawLine(poseStack, buffer, startPos, vector, r, g, 0, 1);

            if (isGood) break;
        }
    }

    private void renderTargetLine(PoseStack poseStack, MultiBufferSource.BufferSource buffer, LivingEntity target) {
        Vec3 targetVec = getTargetPosition(target);
        if (targetVec == null) targetVec = target.getEyePosition();

        drawLine(poseStack, buffer,
                mc.player.position().add(0, mc.player.getBbHeight() / 2, 0),
                targetVec, 0, 1, 0, 1);
    }

    private void useFirework() {
        if (mc.player == null || !mc.player.isFallFlying()) return;

        Item currentChestItem = mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem();
        if (currentChestItem != Items.ELYTRA) {
            return;
        }

        if (InvUtil.getFireWorks() <= 8) {
            int old = mc.player.getInventory().selected;
            mc.player.getInventory().selected = InvUtil.getFireWorks();
            mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
            mc.player.swing(InteractionHand.MAIN_HAND);
            mc.player.getInventory().selected = old;
        } else {
            if (mc.player.getOffhandItem().getItem() != Items.FIREWORK_ROCKET &&
                    (Api.isEnabled("AutoTotem") ? (mc.player.getHealth() > health.getValueFloat()) : true)) {
                invUtil.swapHand(InvUtil.getFireWorks(), InteractionHand.OFF_HAND);
            }
            if (mc.player.getOffhandItem().getItem() == Items.FIREWORK_ROCKET) {
                mc.gameMode.useItem(mc.player, InteractionHand.OFF_HAND);
                mc.player.swing(InteractionHand.MAIN_HAND);
            }
        }

        useTimer.reset();
    }

    private Vec3 getTargetPosition(LivingEntity target) {
        if (target == null) return Vec3.ZERO;

        if (!utilities.isOptionEnabled("Resolver")) {
            return target.position();
        }

        Vec3 defaultPos = target.position();

        if (!utilities.isOptionEnabled("Predict")) {
            return defaultPos;
        }

        if (predictDefensive.isEnabled() && target.isFallFlying() && defensivePos != null) {
            defaultPos = defensivePos;
        }

        Vec3 leavePos = defaultPos;
        if (span.isEnabled() && isTargetLeaving(target)) {
            double multiplier = 2.0;
            leavePos = target.position().add(target.getDeltaMovement().scale(multiplier));
        }

        return leavePos;
    }

    private boolean isTargetLeaving(LivingEntity target) {
        return target.isFallFlying();
    }

    private boolean canLeave(LivingEntity target) {
        if (target == null || isTargetLeaving(target)) return false;

        if (autoLeave.isOptionEnabled("Low Health")) {
            if ((target.isFallFlying() || !onlyGroundLeave.isEnabled()) &&
                    (mc.player.getHealth() + mc.player.getAbsorptionAmount() < leaveHealth.getValueFloat())) {
                return true;
            }
        }

        if (autoLeave.isOptionEnabled("On Cooldown") &&
                mc.player.getAttackStrengthScale(0.5F) < 0.9F) {
            return true;
        }

        if (autoLeave.isOptionEnabled("On Item Use") && mc.player.isUsingItem()) {
            return true;
        }

        if (autoLeave.isOptionEnabled("On Bind Press") && bindLeaving) {
            return true;
        }

        return false;
    }

    private List<Vec3> getLeaveVectorsList(LivingEntity target) {
        List<Vec3> vectors = new ArrayList<>();
        if (target == null && lastPearlPos == null) return vectors;

        Vec3 base = target != null ? target.getEyePosition() : lastPearlPos;

        if (leaveVectors.isOptionEnabled("Up")) vectors.add(base.add(0, 20, 0));
        if (leaveVectors.isOptionEnabled("Down")) vectors.add(base.add(0, -20, 0));
        if (leaveVectors.isOptionEnabled("East")) vectors.add(base.add(20, 0, 0));
        if (leaveVectors.isOptionEnabled("West")) vectors.add(base.add(-20, 0, 0));
        if (leaveVectors.isOptionEnabled("South")) vectors.add(base.add(0, 0, 20));
        if (leaveVectors.isOptionEnabled("North")) vectors.add(base.add(0, 0, -20));

        return vectors;
    }

    private boolean canSeeVector(Vec3 vec) {
        if (mc.level == null || mc.player == null) return false;

        ClipContext context = new ClipContext(
                mc.player.getEyePosition(),
                vec,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                mc.player
        );

        BlockHitResult result = mc.level.clip(context);
        return result.getType() != HitResult.Type.BLOCK;
    }

    private void applyRotation(Vec3 target) {
        if (mc.player == null) return;

        float[] rotation = getRotationTo(target);
        lastYaw = rotation[0];
        lastPitch = rotation[1];

        mc.player.setYRot(rotation[0]);
        mc.player.setXRot(rotation[1]);
    }

    private float[] getRotationTo(Vec3 target) {
        Vec3 eyePos = mc.player.getEyePosition();

        double deltaX = target.x - eyePos.x;
        double deltaY = target.y - eyePos.y;
        double deltaZ = target.z - eyePos.z;

        double distanceXZ = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        float yaw = (float) Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90F;
        float pitch = (float) -Math.toDegrees(Math.atan2(deltaY, distanceXZ));

        yaw = normalizeAngle(yaw);
        pitch = Math.max(-90F, Math.min(90F, pitch));

        return new float[]{yaw, pitch};
    }

    private float normalizeAngle(float angle) {
        while (angle > 180F) angle -= 360F;
        while (angle <= -180F) angle += 360F;
        return angle;
    }

    private void sendPositionPacket(double x, double y, double z, float yaw, float pitch) {
        if (mc.getConnection() == null) return;
        mc.getConnection().send(new ServerboundMovePlayerPacket.PosRot(
                x, y, z, yaw, pitch, mc.player.onGround(), false
        ));
    }

    private void drawLine(PoseStack poseStack, MultiBufferSource buffer, Vec3 from, Vec3 to,
                          float r, float g, float b, float a) {
        poseStack.pushPose();

        Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();
        poseStack.translate(-camera.x, -camera.y, -camera.z);

        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer builder = buffer.getBuffer(RenderType.lines());

        builder.addVertex(matrix, (float) from.x, (float) from.y, (float) from.z)
                .setColor(r, g, b, a)
                .setNormal(1, 0, 0);

        builder.addVertex(matrix, (float) to.x, (float) to.y, (float) to.z)
                .setColor(r, g, b, a)
                .setNormal(1, 0, 0);

        poseStack.popPose();
    }

    private Item getCurrentChestItem() {
        return mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem();
    }

    private void changeChestplate(ItemStack current) {
        if (mc.screen != null) return;

        if (current.getItem() != Items.ELYTRA) {
            int elytraSlot = findItemSlot(Items.ELYTRA);
            if (elytraSlot >= 0) {
                moveItem(elytraSlot, 6);
                return;
            }
        }

        int armorSlot = findChestplateSlot();
        if (armorSlot >= 0) {
            moveItem(armorSlot, 6);
        }
    }

    private int findItemSlot(Item item) {
        for (int i = 0; i < 36; i++) {
            if (mc.player.getInventory().getItem(i).getItem() == item) {
                return i < 9 ? i + 36 : i;
            }
        }
        return -1;
    }

    private int findChestplateSlot() {
        Item[] items = {Items.NETHERITE_CHESTPLATE, Items.DIAMOND_CHESTPLATE};
        for (Item item : items) {
            int slot = findItemSlot(item);
            if (slot >= 0) return slot;
        }
        return -1;
    }

    private void swapToElytra() {
        int elytraSlot = findItemSlot(Items.ELYTRA);
        if (elytraSlot >= 0) {
            moveItem(elytraSlot, 6);
        }
    }

    private void swapToChestplate() {
        int armorSlot = findChestplateSlot();
        if (armorSlot >= 0) {
            moveItem(armorSlot, 6);
        }
    }

    private void moveItem(int from, int to) {
        if (mc.gameMode == null) return;
        mc.gameMode.handleInventoryMouseClick(
                mc.player.containerMenu.containerId, from, 0,
                net.minecraft.world.inventory.ClickType.PICKUP, mc.player
        );
        mc.gameMode.handleInventoryMouseClick(
                mc.player.containerMenu.containerId, to, 0,
                net.minecraft.world.inventory.ClickType.PICKUP, mc.player
        );
        mc.gameMode.handleInventoryMouseClick(
                mc.player.containerMenu.containerId, from, 0,
                net.minecraft.world.inventory.ClickType.PICKUP, mc.player
        );
    }

    private void resetState() {
        lastPearlPos = null;
        pearlEntity = null;
        leaveVec = Vec3.ZERO;
        lastVec = Vec3.ZERO;
        packets.clear();
        defensivePos = null;
        defensiveActive = false;
        lastDefensive = false;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (utilities.isOptionEnabled("Swap Elytra") && getCurrentChestItem() != Items.ELYTRA) {
            swapToElytra();
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (utilities.isOptionEnabled("Swap Elytra") && getCurrentChestItem() == Items.ELYTRA) {
            swapToChestplate();
        }
        resetState();
    }
}