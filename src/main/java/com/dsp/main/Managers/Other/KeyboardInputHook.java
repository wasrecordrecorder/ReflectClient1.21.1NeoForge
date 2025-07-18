package com.dsp.main.Managers.Other;

import com.dsp.main.Api;
import com.dsp.main.Functions.Movement.ScreenWalk;
import com.dsp.main.Managers.Event.MoveInputEvent;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.client.player.Input;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

import static com.dsp.main.Api.mc;

@OnlyIn(Dist.CLIENT)
public class KeyboardInputHook extends Input {
    private final Options options;

    public KeyboardInputHook(Options options) {
        this.options = options;
    }

    private static float calculateImpulse(boolean input, boolean otherInput) {
        if (input == otherInput) {
            return 0.0F;
        } else {
            return input ? 1.0F : -1.0F;
        }
    }

    @Override
    public void tick(boolean isSneaking, float sneakingSpeedMultiplier) {
        this.up = isDown(options.keyUp.getKey().getValue());
        this.down = isDown(options.keyDown.getKey().getValue());
        this.left = isDown(options.keyLeft.getKey().getValue());
        this.right = isDown(options.keyRight.getKey().getValue());
        this.forwardImpulse = calculateImpulse(this.up, this.down);
        this.leftImpulse = calculateImpulse(this.left, this.right);
        this.jumping = isDown(options.keyJump.getKey().getValue());
        this.shiftKeyDown = ScreenWalk.shift.isEnabled() ? isDown(options.keyShift.getKey().getValue()) : options.keyShift.isDown();
        if (isSneaking) {
            this.leftImpulse *= sneakingSpeedMultiplier;
            this.forwardImpulse *= sneakingSpeedMultiplier;
        }
        MoveInputEvent event = new MoveInputEvent();
        event.forward = this.forwardImpulse;
        event.strafe = this.leftImpulse;
        event.jump = this.jumping;
        event.sneaking = this.shiftKeyDown;
        event.sneakSlow = sneakingSpeedMultiplier;
        NeoForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            this.forwardImpulse = event.forward;
            this.leftImpulse = event.strafe;
            this.jumping = event.jump;
            this.shiftKeyDown = event.sneaking;
        }
    }
    private boolean isDown(int a) {
        boolean isPressed = GLFW.glfwGetKey(mc.getWindow().getWindow(), a) == GLFW.GLFW_PRESS;
        if (Api.isEnabled("ScreenWalk")) {
            return (!(mc.screen instanceof ChatScreen) && !(mc.screen instanceof AnvilScreen)) && isPressed;
        } else {
            return (mc.screen == null && isPressed);
        }
    }
}
