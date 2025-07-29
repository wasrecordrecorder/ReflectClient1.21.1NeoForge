package com.dsp.main.Mixin;

import com.dsp.main.Api;
import com.dsp.main.Functions.Player.NoPush;
import com.dsp.main.Core.Event.SlowWalkingEvent;
import com.dsp.main.Core.Event.UpdateInputEvent;
import com.dsp.main.Core.Other.LocalPlayerAccessor;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.dsp.main.Api.mc;
import static com.dsp.main.Functions.Player.LockSlot.isSlotLocked;
import static com.dsp.main.Main.isDetect;

@OnlyIn(Dist.CLIENT)
@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
    @Shadow
    protected int sprintTriggerTime = 0;
    @Shadow
    public Input input;

    @Inject(
            method = "moveTowardsClosestSpace",
            at = @At("HEAD"),
            cancellable = true
    )
    private void moveTowardsClosestSpace(double x, double z, CallbackInfo ci) {
        if (!isDetect && Api.isEnabled("NoPush") && NoPush.Options.isOptionEnabled("Blocks")) {
            ci.cancel();
        }
    }
    @Inject(
            method = "aiStep",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z",
                    shift = At.Shift.BEFORE
            )
    )
    private void onInputUpdate(CallbackInfo ci) {
        LocalPlayer player = (LocalPlayer)(Object)this;
        Input input = player.input;
        LocalPlayerAccessor accessor = (LocalPlayerAccessor)player;
        UpdateInputEvent event = new UpdateInputEvent(player, input, input.leftImpulse, input.forwardImpulse, accessor.getSprintTriggerTime());
        NeoForge.EVENT_BUS.post(event);
        input.leftImpulse = event.getLeftImpulse();
        input.forwardImpulse = event.getForwardImpulse();
        accessor.setSprintTriggerTime(event.getSprintTriggerTime());
    }
    @Redirect(
            method = "aiStep",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/player/Input;forwardImpulse:F",
                    opcode = org.objectweb.asm.Opcodes.PUTFIELD
            )
    )
    private void redirectForwardImpulse(Input input, float value) {
        if (((LocalPlayer)(Object)this).isUsingItem() && !((LocalPlayer)(Object)this).isPassenger()) {
            SlowWalkingEvent event = new SlowWalkingEvent(this.input.forwardImpulse, this.input.leftImpulse);
            NeoForge.EVENT_BUS.post(event);

            if (event.isCanceled()) {
                this.input.forwardImpulse = event.getForwardImpulse();
            } else {
                this.input.forwardImpulse = value;
            }
        } else {
            this.input.forwardImpulse = value;
        }
    }

    @Redirect(
            method = "aiStep",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/player/Input;leftImpulse:F",
                    opcode = org.objectweb.asm.Opcodes.PUTFIELD
            )
    )
    private void redirectLeftImpulse(Input input, float value) {
        if (((LocalPlayer)(Object)this).isUsingItem() && !((LocalPlayer)(Object)this).isPassenger()) {
            SlowWalkingEvent event = new SlowWalkingEvent(this.input.forwardImpulse, this.input.leftImpulse);
            NeoForge.EVENT_BUS.post(event);

            if (event.isCanceled()) {
                this.input.leftImpulse = event.getLeftImpulse();
            } else {
                this.input.leftImpulse = value;
            }
        } else {
            this.input.leftImpulse = value;
        }
    }
    @Inject(
            method = "drop",
            at = @At("HEAD"),
            cancellable = true
    )
    public void dowod(boolean fullStack, CallbackInfoReturnable<Boolean> ci) {
        if (isSlotLocked(mc.player.getInventory().selected)) {
            ci.cancel();
        }
    }
}
