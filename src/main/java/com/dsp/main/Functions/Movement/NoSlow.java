package com.dsp.main.Functions.Movement;

import com.dsp.main.Managers.Event.OnUpdate;
import com.dsp.main.Managers.Event.SlowWalkingEvent;
import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Settings.Mode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

import static com.dsp.main.Api.mc;

public class NoSlow extends Module {

    private final Mode mode = new Mode("Mode", "Grim", "ReallyWorld", "Funtime", "Grim New", "NCP");
    private int usingItemTicks = 0;
    private boolean wasUsingItem = false;

    public NoSlow() {
        super("NoSlow", 0, Category.MOVEMENT, "Canceling slow walking");
        addSettings(mode);
    }


    @SubscribeEvent
    public void onTick(OnUpdate event) {
        if (mc.player == null) return;
        if (mc.player.isUsingItem()) {
            usingItemTicks++;
        } else {
            usingItemTicks = 0;
        }
        wasUsingItem = mc.player.isUsingItem();
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onSlowWalk(SlowWalkingEvent event) {
        if (shouldSkip()) return;

        switch (mode.getMode()) {
            case "Grim":
                handleGrimMode(event);
                break;
            case "ReallyWorld":
                handleReallyWorldMode(event);
                break;
            case "Funtime":
                handleFuntimeMode(event);
                break;
            case "Grim New":
                handleGrimNewMode(event);
                break;
            case "NCP":
                handleNCPMode(event);
                break;
        }
    }

    private boolean shouldSkip() {
        return mc.player == null ||
                mc.player.isFallFlying() ||
                (!mc.player.isUsingItem() && !wasUsingItem);
    }

    private void handleGrimMode(SlowWalkingEvent event) {
        if (shouldCancelGrim()) return;
        sendUseItemPackets();
        event.setCanceled(true);
    }

    private boolean shouldCancelGrim() {
        ItemStack offhand = mc.player.getOffhandItem();
        return (offhand.getUseAnimation() == UseAnim.BLOCK ||
                offhand.getUseAnimation() == UseAnim.EAT) &&
                mc.player.getUsedItemHand() == InteractionHand.MAIN_HAND;
    }

    private void sendUseItemPackets() {
        InteractionHand hand = mc.player.getUsedItemHand();
        InteractionHand oppositeHand = hand == InteractionHand.MAIN_HAND ?
                InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;

        mc.player.connection.send(new ServerboundUseItemPacket(
                hand, 0, mc.player.getXRot(), mc.player.getYRot()));
        mc.player.connection.send(new ServerboundUseItemPacket(
                oppositeHand, 0, mc.player.getXRot(), mc.player.getYRot()));
    }

    private void handleReallyWorldMode(SlowWalkingEvent event) {
        mc.player.connection.send(new ServerboundPlayerActionPacket(
                ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK,
                mc.player.blockPosition().above(),
                Direction.NORTH));
        event.setCanceled(true);
    }

    private void handleFuntimeMode(SlowWalkingEvent event) {
        if (isOnSpecialBlock()) {
            event.setCanceled(true);
        }
    }

    private boolean isOnSpecialBlock() {
        BlockPos pos = BlockPos.containing(mc.player.position());
        return mc.level.getBlockState(pos).getBlock() instanceof CarpetBlock ||
                mc.level.getBlockState(pos).getBlock() instanceof SnowLayerBlock;
    }

    private void handleGrimNewMode(SlowWalkingEvent event) {
        if (usingItemTicks >= 2) {
            event.setCanceled(true);
            usingItemTicks = 0;
        }
    }

    private void handleNCPMode(SlowWalkingEvent event) {
        if (mc.player.onGround()) {
            event.setCanceled(true);
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        usingItemTicks = 0;
        wasUsingItem = false;
    }
}