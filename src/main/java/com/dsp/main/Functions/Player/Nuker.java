package com.dsp.main.Functions.Player;

import com.dsp.main.Core.Event.MoveInputEvent;
import com.dsp.main.Core.Event.OnUpdate;
import com.dsp.main.Core.Other.FreeLook;
import com.dsp.main.Mixin.Accesors.MouseHandlerAccessor;
import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.BlockListSetting;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.CheckBox;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.Mode;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.Slider;
import com.dsp.main.Utils.Minecraft.Client.MoveUtil;
import com.mojang.datafixers.types.templates.Check;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.*;

import static com.dsp.main.Api.mc;
import static com.dsp.main.Core.Other.FreeLook.getCameraYaw;

public class Nuker extends Module {
    private final CheckBox mineAll;
    private final BlockListSetting blocks;
    private final Mode priority;
    private final Slider radius;
    private final CheckBox rotate;
    private final CheckBox throughWalls;
    private final CheckBox correctionMove;

    private static final double MAX_MINE_DISTANCE = 4.5;
    private static final String FREELOOK_ID = "Nuker";

    private static final Set<Block> ORES = new HashSet<>(Arrays.asList(
            Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE,
            Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE,
            Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE,
            Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE,
            Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE,
            Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE,
            Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE,
            Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE,
            Blocks.ANCIENT_DEBRIS,
            Blocks.NETHER_GOLD_ORE,
            Blocks.NETHER_QUARTZ_ORE
    ));

    private static final Set<Block> WOOD = new HashSet<>(Arrays.asList(
            Blocks.OAK_LOG, Blocks.STRIPPED_OAK_LOG,
            Blocks.BIRCH_LOG, Blocks.STRIPPED_BIRCH_LOG,
            Blocks.SPRUCE_LOG, Blocks.STRIPPED_SPRUCE_LOG,
            Blocks.JUNGLE_LOG, Blocks.STRIPPED_JUNGLE_LOG,
            Blocks.ACACIA_LOG, Blocks.STRIPPED_ACACIA_LOG,
            Blocks.DARK_OAK_LOG, Blocks.STRIPPED_DARK_OAK_LOG,
            Blocks.MANGROVE_LOG, Blocks.STRIPPED_MANGROVE_LOG,
            Blocks.CHERRY_LOG, Blocks.STRIPPED_CHERRY_LOG,
            Blocks.WARPED_STEM, Blocks.STRIPPED_WARPED_STEM,
            Blocks.CRIMSON_STEM, Blocks.STRIPPED_CRIMSON_STEM
    ));

    private BlockPos currentTarget = null;
    private boolean isMining = false;

    public Nuker() {
        super("Nuker", 0, Category.PLAYER, "Auto mine blocks around you");
        correctionMove = new CheckBox("Move Correction", false);
        mineAll = new CheckBox("Mine All", false);
        blocks = new BlockListSetting("Blocks");
        blocks.setVisible(() -> !mineAll.isEnabled());

        priority = new Mode("Priority", "Nearest", "Ores", "Wood");
        priority.setVisible(() -> !mineAll.isEnabled());

        radius = new Slider("Radius", 1, 6, 4, 1);
        rotate = new CheckBox("Rotate", true);
        throughWalls = new CheckBox("Through Walls", false);

        addSettings(mineAll, blocks, priority, radius, rotate, throughWalls);
    }
    @SubscribeEvent
    public void OnMoveInput(MoveInputEvent event) {
        if (FreeLook.isFreeLookEnabled && correctionMove.isEnabled()) {
            MoveUtil.fixMovement(event, getCameraYaw());
        }
    }

    @SubscribeEvent
    public void onUpdate(OnUpdate event) {
        if (mc.player == null || mc.level == null || mc.gameMode == null) {
            stopMining();
            return;
        }

        if (isMining && currentTarget != null) {
            BlockState state = mc.level.getBlockState(currentTarget);

            if (state.isAir()) {
                finishMining();
                BlockPos nextTarget = findBestBlock();
                if (nextTarget != null) {
                    startMining(nextTarget);
                }
                return;
            }

            if (!isInReachDistance(currentTarget)) {
                stopMining();
                return;
            }

            if (!isValidTarget(currentTarget, state.getBlock())) {
                stopMining();
                return;
            }

            if (rotate.isEnabled()) {
                rotateToBlock(currentTarget);
            }

            continueDestroyBlock();
        } else {
            BlockPos target = findBestBlock();
            if (target != null) {
                startMining(target);
            }
        }
    }

    private BlockPos findBestBlock() {
        if (mc.player == null || mc.level == null) return null;

        List<BlockPos> validBlocks = new ArrayList<>();
        BlockPos playerPos = mc.player.blockPosition();
        int r = (int) radius.getValue();

        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    BlockPos pos = playerPos.offset(x, y, z);

                    if (!isInReachDistance(pos)) continue;

                    BlockState state = mc.level.getBlockState(pos);
                    if (state.isAir()) continue;

                    Block block = state.getBlock();
                    if (!isValidTarget(pos, block)) continue;

                    if (!throughWalls.isEnabled() && !canSeeBlock(pos)) continue;

                    validBlocks.add(pos);
                }
            }
        }

        if (validBlocks.isEmpty()) return null;

        return getBestBlockByPriority(validBlocks);
    }

    private BlockPos getBestBlockByPriority(List<BlockPos> blocks) {
        if (blocks.isEmpty()) return null;

        if (mineAll.isEnabled() || priority.getMode().equals("Nearest")) {
            return getNearest(blocks);
        }

        if (priority.getMode().equals("Ores")) {
            List<BlockPos> ores = new ArrayList<>();
            for (BlockPos pos : blocks) {
                Block block = mc.level.getBlockState(pos).getBlock();
                if (ORES.contains(block)) {
                    ores.add(pos);
                }
            }
            return ores.isEmpty() ? getNearest(blocks) : getNearest(ores);
        }

        if (priority.getMode().equals("Wood")) {
            List<BlockPos> wood = new ArrayList<>();
            for (BlockPos pos : blocks) {
                Block block = mc.level.getBlockState(pos).getBlock();
                if (WOOD.contains(block)) {
                    wood.add(pos);
                }
            }
            return wood.isEmpty() ? getNearest(blocks) : getNearest(wood);
        }

        return getNearest(blocks);
    }

    private BlockPos getNearest(List<BlockPos> blocks) {
        if (blocks.isEmpty()) return null;

        Vec3 playerPos = mc.player.position();
        BlockPos nearest = null;
        double minDist = Double.MAX_VALUE;

        for (BlockPos pos : blocks) {
            double dist = playerPos.distanceToSqr(Vec3.atCenterOf(pos));
            if (dist < minDist) {
                minDist = dist;
                nearest = pos;
            }
        }

        return nearest;
    }

    private boolean isValidTarget(BlockPos pos, Block block) {
        if (block == Blocks.AIR) return false;
        if (block == Blocks.BEDROCK) return false;

        if (mineAll.isEnabled()) {
            return true;
        }

        return blocks.isBlockSelected(block);
    }

    private boolean isInReachDistance(BlockPos pos) {
        if (mc.player == null) return false;
        Vec3 playerPos = mc.player.position().add(0, mc.player.getEyeHeight(), 0);
        Vec3 blockCenter = Vec3.atCenterOf(pos);
        double distance = playerPos.distanceTo(blockCenter);
        return distance <= MAX_MINE_DISTANCE;
    }

    private boolean canSeeBlock(BlockPos pos) {
        if (mc.player == null || mc.level == null) return false;

        Vec3 playerEye = mc.player.getEyePosition(1.0f);
        Vec3 blockCenter = Vec3.atCenterOf(pos);

        HitResult result = mc.level.clip(new net.minecraft.world.level.ClipContext(
                playerEye,
                blockCenter,
                net.minecraft.world.level.ClipContext.Block.OUTLINE,
                net.minecraft.world.level.ClipContext.Fluid.NONE,
                mc.player
        ));

        if (result.getType() == HitResult.Type.MISS) return true;
        if (result.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) result;
            return blockHit.getBlockPos().equals(pos);
        }

        return false;
    }

    private void startMining(BlockPos pos) {
        if (mc.player == null || mc.gameMode == null || mc.level == null) return;

        BlockState state = mc.level.getBlockState(pos);
        if (state.isAir()) return;

        if (isMining && currentTarget != null && !currentTarget.equals(pos)) {
            stopMining();
        }

        currentTarget = pos;
        isMining = true;

        if (rotate.isEnabled()) {
            FreeLook.requestFreeLook(FREELOOK_ID);
            rotateToBlock(pos);
        }

        pressLeftClick(true);
    }

    private void continueDestroyBlock() {
        if (mc.gameMode == null || currentTarget == null) return;

        if (rotate.isEnabled()) {
            rotateToBlock(currentTarget);
        }
    }

    private void finishMining() {
        if (mc.gameMode != null) {
            mc.gameMode.stopDestroyBlock();
        }

        if (rotate.isEnabled()) {
            FreeLook.releaseFreeLook(FREELOOK_ID);
        }

        pressLeftClick(false);

        currentTarget = null;
        isMining = false;
    }

    private void stopMining() {
        if (mc.gameMode != null) {
            mc.gameMode.stopDestroyBlock();
        }

        if (rotate.isEnabled()) {
            FreeLook.releaseFreeLook(FREELOOK_ID);
        }

        pressLeftClick(false);

        currentTarget = null;
        isMining = false;
    }

    private void pressLeftClick(boolean press) {
        if (mc.getWindow() == null || mc.mouseHandler == null) return;

        try {
            long window = mc.getWindow().getWindow();
            MouseHandlerAccessor mouse = (MouseHandlerAccessor) mc.mouseHandler;
            mouse.invokeOnPress(window, 0, press ? 1 : 0, 0);
        } catch (Exception e) {
        }
    }

    private void rotateToBlock(BlockPos pos) {
        if (mc.player == null) return;

        Vec3 playerEye = mc.player.getEyePosition(1.0f);
        Vec3 blockCenter = Vec3.atCenterOf(pos);
        Vec3 direction = blockCenter.subtract(playerEye).normalize();

        float yaw = (float) Math.toDegrees(Math.atan2(direction.z, direction.x)) - 90.0f;
        float pitch = (float) -Math.toDegrees(Math.asin(direction.y));

        mc.player.setYRot(yaw);
        mc.player.setXRot(pitch);
        mc.player.yRotO = yaw;
        mc.player.xRotO = pitch;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        currentTarget = null;
        isMining = false;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        stopMining();
        FreeLook.releaseFreeLook(FREELOOK_ID);
    }
}