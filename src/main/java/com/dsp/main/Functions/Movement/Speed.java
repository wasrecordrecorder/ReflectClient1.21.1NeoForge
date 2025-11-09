package com.dsp.main.Functions.Movement;

import com.dsp.main.Core.Event.ClientPacketReceiveEvent;
import com.dsp.main.Core.Event.OnUpdate;
import com.dsp.main.Core.Other.FreeLook;
import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.Mode;
import com.dsp.main.Utils.Minecraft.Client.InvUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.common.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;

import static com.dsp.main.Api.mc;

public class Speed extends Module {
    private static final String FREELOOK_REQUEST_ID = "Speed";

    private static Mode mode = new Mode("Option", "FunTime");
    private int sequenceCounter = 0;
    private int originalSlot = -1;
    private boolean waterPlaced = false;
    private BlockPos waterPos = null;

    public Speed() {
        super("Speed", 0, Category.MOVEMENT, "Makes you running much faster");
        addSetting(mode);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        FreeLook.requestFreeLook(FREELOOK_REQUEST_ID);
        sequenceCounter = 0;
        originalSlot = -1;
        waterPlaced = false;
        waterPos = null;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        FreeLook.releaseFreeLook(FREELOOK_REQUEST_ID);
        if (originalSlot != -1 && mc.player != null) {
            mc.player.connection.send(new ServerboundSetCarriedItemPacket(originalSlot));
        }
        if (waterPlaced && waterPos != null) {
            cleanUpWater();
        }
    }

    @SubscribeEvent
    public void onUpdate(OnUpdate event) {
        if (mc.player == null || mc.level == null) return;

        switch (mode.getMode()) {
            case "FunTime":
                HandleFunTimeSpeed();
                break;
        }
    }

    public void HandleFunTimeSpeed() {
        int waterBucketSlot = InvUtil.getSlotIDFromItem(Items.WATER_BUCKET);
        if (waterBucketSlot < 0 || mc.player.isUsingItem() || !mc.player.onGround()) return;
        if (mc.player.getMainHandItem().getItem() != Items.WATER_BUCKET) return;

        mc.player.setXRot(90);
        mc.player.setYRot(FreeLook.getCameraYaw());
        if (originalSlot == -1) {
            originalSlot = mc.player.getInventory().selected;
        }
        BlockPos targetPos = mc.player.blockPosition().below();
        BlockHitResult hitResult = new BlockHitResult(
                new Vec3(targetPos.getX() + 0.5, targetPos.getY() + 1.0, targetPos.getZ() + 0.5),
                Direction.UP,
                targetPos,
                false
        );
        mc.player.connection.send(new ServerboundUseItemOnPacket(
                InteractionHand.MAIN_HAND,
                hitResult,
                sequenceCounter++
        ));

        waterPlaced = true;
        waterPos = targetPos;
        if (!mc.player.hasEffect(MobEffects.MOVEMENT_SPEED)) {
            double randomMultiplier = 1.11 + (Math.random() * 0.01);
            mc.player.setDeltaMovement(mc.player.getDeltaMovement().multiply(randomMultiplier, 1, randomMultiplier));
        } else {
            mc.player.setDeltaMovement(mc.player.getDeltaMovement().multiply(1.04, 1, 1.04));
        }
        if (waterPlaced) {
            cleanUpWater();
            waterPlaced = false;
            waterPos = null;
        }
    }

    private void cleanUpWater() {
        if (waterPos == null) return;
        mc.player.connection.send(new ServerboundPlayerActionPacket(
                ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK,
                waterPos,
                Direction.UP,
                sequenceCounter++
        ));
    }

    @SubscribeEvent
    public void onKeepAlive(ClientPacketReceiveEvent packet) {
        if (packet.getPacket() instanceof ClientboundKeepAlivePacket pac) {
            long lastKeepAliveId = pac.getId();
            mc.player.connection.send(new ServerboundKeepAlivePacket(lastKeepAliveId));
        }
    }
}