package com.dsp.main.Core.Other.Hooks;

import com.dsp.main.Api;
import com.dsp.main.Core.Event.MoveInputEvent;
import com.dsp.main.Functions.Movement.ScreenWalk;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.client.player.ClientInput;
import net.minecraft.world.entity.player.Input;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

import static com.dsp.main.Api.mc;

@OnlyIn(Dist.CLIENT)
public class KeyboardInputHook extends ClientInput {
    private final Options options;

    public KeyboardInputHook(Options options) {
        this.options = options;
    }

    private static float calculateImpulse(boolean positive, boolean negative) {
        if (positive == negative) {
            return 0.0F;
        } else {
            return positive ? 1.0F : -1.0F;
        }
    }

    @Override
    public void tick() {
        boolean up = isDown(options.keyUp.getKey().getValue());
        boolean down = isDown(options.keyDown.getKey().getValue());
        boolean left = isDown(options.keyLeft.getKey().getValue());
        boolean right = isDown(options.keyRight.getKey().getValue());
        boolean jump = isDown(options.keyJump.getKey().getValue());
        boolean shift = ScreenWalk.shift.isEnabled() ?
                isDown(options.keyShift.getKey().getValue()) :
                options.keyShift.isDown();
        boolean sprint = isDown(options.keySprint.getKey().getValue());

        this.keyPresses = new Input(up, down, left, right, jump, shift, sprint);
        this.forwardImpulse = calculateImpulse(this.keyPresses.forward(), this.keyPresses.backward());
        this.leftImpulse = calculateImpulse(this.keyPresses.left(), this.keyPresses.right());

        MoveInputEvent event = new MoveInputEvent();
        event.forward = this.forwardImpulse;
        event.strafe = this.leftImpulse;
        event.jump = this.keyPresses.jump();
        event.sneaking = this.keyPresses.shift();
        event.sneakSlow = 0.3f;

        NeoForge.EVENT_BUS.post(event);

        if (event.isCanceled()) {
            this.forwardImpulse = event.forward;
            this.leftImpulse = event.strafe;
            this.keyPresses = new Input(
                    this.keyPresses.forward(),
                    this.keyPresses.backward(),
                    this.keyPresses.left(),
                    this.keyPresses.right(),
                    event.jump,
                    event.sneaking,
                    this.keyPresses.sprint()
            );
        }
    }

    private boolean isDown(int keyCode) {
        if (mc == null || mc.getWindow() == null) return false;

        try {
            boolean isPressed = GLFW.glfwGetKey(mc.getWindow().getWindow(), keyCode) == GLFW.GLFW_PRESS;
            if (Api.isEnabled("ScreenWalk")) {
                return (!(mc.screen instanceof ChatScreen) && !(mc.screen instanceof AnvilScreen)) && isPressed;
            } else {
                return (mc.screen == null && isPressed);
            }
        } catch (Exception e) {
            return false;
        }
    }
}