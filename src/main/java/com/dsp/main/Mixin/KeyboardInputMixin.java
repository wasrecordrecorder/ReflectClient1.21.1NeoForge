package com.dsp.main.Mixin;

import com.dsp.main.Core.Event.MoveInputEvent;
import net.minecraft.client.player.KeyboardInput;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@OnlyIn(Dist.CLIENT)
@Mixin(KeyboardInput.class)
public class KeyboardInputMixin {
    @Inject(
            method = "tick",
            at = @At("TAIL"),
            cancellable = true
    )
    public void tick(boolean isSneaking, float sneakingSpeedMultiplier, CallbackInfo ci) {
        KeyboardInput input = (KeyboardInput) (Object) this;
        MoveInputEvent event = new MoveInputEvent();
        event.forward = input.forwardImpulse;
        event.strafe = input.leftImpulse;
        event.jump = input.jumping;
        event.sneaking = input.shiftKeyDown;
        event.sneakSlow = sneakingSpeedMultiplier;
        NeoForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            input.forwardImpulse = event.forward;
            input.leftImpulse = event.strafe;
            input.jumping = event.jump;
            input.shiftKeyDown = event.sneaking;
            ci.cancel();
        }
    }
}
