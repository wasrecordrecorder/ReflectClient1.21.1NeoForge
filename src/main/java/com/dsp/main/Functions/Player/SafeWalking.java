package com.dsp.main.Functions.Player;

import com.dsp.main.Core.Event.UpdateInputEvent;
import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.Mode;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.Slider;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;

public class SafeWalking extends Module {
    private static Mode mod = new Mode("Mode", "Stop", "Invert", "Center", "Shift");
    private static Slider edgeDist = new Slider("Edge Distance", 0.1, 0.5, 0.1, 0.1);
    private static Slider keepTicks = new Slider("Keep Ticks", 1, 10, 3, 1);
    private final Minecraft mc = Minecraft.getInstance();
    private Vec3 safePosition = null;
    private int overwriteTicks = 0;

    public SafeWalking() {
        super("SafeWalking", 0, Category.PLAYER, "Prevents you from falling off edges by sneaking or adjusting movement.");
        addSettings(mod, edgeDist, keepTicks);
    }

    @SubscribeEvent
    public void onInput(UpdateInputEvent e) {
        if (mc.player == null || mc.level == null) return;
        if (mc.player.onGround()) {
            boolean isOnEdge = isCloseToEdge(e.getForwardImpulse(), e.getLeftImpulse(), edgeDist.getValueFloat());
            if (isOnEdge) {
                if (overwriteTicks == 0) {
                    overwriteTicks = keepTicks.getValueInt();
                }
            }
            //mc.options.keyShift.setDown(mod.isMode("Shift") && overwriteTicks > 0);
            mc.options.keyShift.setDown(mod.isMode("Shift") && mc.level.getBlockState(mc.player.blockPosition().below()).isAir() && mc.player.onGround());
            if (overwriteTicks > 0) {
                overwriteTicks--;
                switch (mod.getMode()) {
                    case "Stop":
                        e.setForwardImpulse(0);
                        e.setLeftImpulse(0);
                        break;
                    case "Invert":
                        e.setForwardImpulse(-e.getForwardImpulse());
                        e.setLeftImpulse(-e.getLeftImpulse());
                        break;
                    case "Center":
                        if (safePosition != null) {
                            double deltaX = safePosition.x - mc.player.getX();
                            double deltaZ = safePosition.z - mc.player.getZ();
                            float yaw = mc.player.getYRot();
                            float degrees = (float) Math.toDegrees(Mth.atan2(deltaZ, deltaX)) - 90.0f;
                            float normalizedDegrees = Mth.wrapDegrees(degrees - yaw);
                            if (Math.abs(normalizedDegrees) < 60.0f) {
                                e.setForwardImpulse(0);
                                e.setLeftImpulse(0);
                            } else {
                                float forward = normalizedDegrees > 0 ? -1 : 1;
                                e.setForwardImpulse(forward);
                                e.setLeftImpulse(0);
                            }
                        } else {
                            e.setForwardImpulse(0);
                            e.setLeftImpulse(0);
                        }
                        break;

                }
            }
        }
        updateSafePosition();
    }

    private boolean isCloseToEdge(float forward, float strafe, double maxDistance) {
        if (mc.player == null || mc.level == null) return false;
        BlockPos playerPos = mc.player.blockPosition();
        BlockPos belowPos = playerPos.below();
        BlockState blockBelow = mc.level.getBlockState(belowPos);
        if (blockBelow.isAir()) return false;
        double speed = Math.sqrt(forward * forward + strafe * strafe);
        if (speed > maxDistance) {
            speed = maxDistance;
        }
        Vec3 playerPosVec = mc.player.position();
        double motionX = forward * Math.cos(Math.toRadians(mc.player.getYRot() + 90)) + strafe * Math.sin(Math.toRadians(mc.player.getYRot() + 90));
        double motionZ = forward * Math.sin(Math.toRadians(mc.player.getYRot() + 90)) - strafe * Math.cos(Math.toRadians(mc.player.getYRot() + 90));
        double checkPosX = playerPosVec.x + motionX * speed;
        double checkPosZ = playerPosVec.z + motionZ * speed;
        BlockPos checkPos = new BlockPos((int) Math.floor(checkPosX), playerPos.getY() - 1, (int) Math.floor(checkPosZ));
        return mc.level.getBlockState(checkPos).isAir();
    }

    private void updateSafePosition() {
        if (mc.player == null || mc.level == null) return;
        BlockPos blockPos = mc.player.blockPosition();
        BlockPos belowPos = blockPos.below();
        if (!mc.level.getBlockState(belowPos).isAir()) {
            safePosition = new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5);
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        safePosition = null;
        overwriteTicks = 0;
    }
}