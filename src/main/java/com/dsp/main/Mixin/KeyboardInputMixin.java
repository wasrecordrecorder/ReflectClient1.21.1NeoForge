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
            at = @At("TAIL")
    )
    public void onTick(CallbackInfo ci) {
        //System.out.println("test");
        KeyboardInput input = (KeyboardInput)(Object)this;

        MoveInputEvent event = new MoveInputEvent();
        event.forward = input.forwardImpulse;
        event.strafe = input.leftImpulse;
        event.jump = input.keyPresses.jump();
        event.sneaking = input.keyPresses.shift();
        event.sneakSlow = 0.3f;

        //NeoForge.EVENT_BUS.post(event);
        //
        //if (event.isCanceled()) {
            //    input.forwardImpulse = event.forward;
            //    input.leftImpulse = event.strafe;
            //    input.keyPresses = new net.minecraft.world.entity.player.Input(
                    //            input.keyPresses.forward(),
            //            input.keyPresses.backward(),
            //            input.keyPresses.left(),
            //            input.keyPresses.right(),
            //            event.jump,
            //            event.sneaking,
            //            input.keyPresses.sprint()
                    //    );
            //}
    }
}