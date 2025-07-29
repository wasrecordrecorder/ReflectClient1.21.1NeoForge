package com.dsp.main.Functions.Movement;

import com.dsp.main.Core.Event.OnUpdate;
import com.dsp.main.Core.Event.UpdateInputEvent;
import com.dsp.main.Core.Other.FreeLook;
import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.CheckBox;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.Mode;
import com.dsp.main.Utils.Minecraft.Client.InvUtil;
import com.dsp.main.Utils.TimerUtil;
import net.minecraft.client.CameraType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;

import static com.dsp.main.Api.mc;

public class Scaffhold extends Module {
    private static InvUtil invUtil = new InvUtil();
    private static TimerUtil timer = new TimerUtil();
    private static CheckBox shift = new CheckBox("Use Shift", false);
    private static Mode mod = new Mode("Place Mode", "Legit", "Default");

    public Scaffhold() {
        super("Scaffhold", 0, Category.MOVEMENT, "Fly with blocks !");
        addSettings(shift, mod);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (!FreeLook.isFreeLookEnabled) FreeLook.enableFreeLook();
        mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (FreeLook.isFreeLookEnabled) FreeLook.disableFreeLook();
        mc.options.keyUse.setDown(false);
        mc.options.keyShift.setDown(false);
        mc.options.setCameraType(CameraType.FIRST_PERSON);
    }

    private boolean isValidBlockItem(ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem blockItem)) return false;
        Block block = blockItem.getBlock();
        return switch (block) {
            case CakeBlock cakeBlock -> false;
            case CropBlock cropBlock -> false;
            case FallingBlock fallingBlock -> false;
            default -> true;
        };
    }

    @SubscribeEvent
    public void onUpdate(OnUpdate e) {
        if (mc.player == null) return;
        if (!FreeLook.isFreeLookEnabled) FreeLook.enableFreeLook();
        if (shift.isEnabled()) mc.options.keyShift.setDown(mc.level.getBlockState(mc.player.blockPosition().below()).isAir() && mc.player.onGround());
        mc.player.setXRot(81.7F);
        mc.player.setYRot(FreeLook.getCameraYaw() - 180);

        ItemStack mainHandItem = mc.player.getMainHandItem();
        if (!isValidBlockItem(mainHandItem)) {
            for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
                ItemStack itemI = mc.player.getInventory().getItem(i);
                if (isValidBlockItem(itemI)) {
                    if (i < 9) {
                        mc.player.getInventory().selected = i;
                    } else {
                        invUtil.swapHand(i, InteractionHand.MAIN_HAND);
                    }
                    break; // Прерываем цикл после нахождения подходящего блока
                }
            }
        }

        if (!isValidBlockItem(mc.player.getMainHandItem())) return;

        if (timer.hasReached(50)) {
            if (mod.isMode("Legit")) {
                mc.options.keyUse.setDown(true);
            } else {
                BlockPos below = mc.player.blockPosition().below();
                if (mc.level.getBlockState(below).isAir() && mc.player.onGround()) {
                    Direction face = mc.player.getDirection();
                    Vec3 hitVec = Vec3.atCenterOf(below);
                    BlockHitResult hitResult = new BlockHitResult(hitVec, face, below, false);
                    mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hitResult);
                    mc.player.swing(InteractionHand.MAIN_HAND);
                }
            }
            timer.reset();
        }
    }

    @SubscribeEvent
    public void onInput(UpdateInputEvent e) {
        e.setForwardImpulse(-e.getForwardImpulse());
        e.setLeftImpulse(-e.getLeftImpulse());
    }
}