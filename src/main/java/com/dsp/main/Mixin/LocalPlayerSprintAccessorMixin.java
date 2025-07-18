package com.dsp.main.Mixin;

import com.dsp.main.Managers.Other.LocalPlayerAccessor;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerSprintAccessorMixin implements LocalPlayerAccessor {
    @Shadow
    protected int sprintTriggerTime;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        System.out.println("LocalPlayerSprintAccessorMixin applied to LocalPlayer");
    }

    @Override
    public int getSprintTriggerTime() {
        return this.sprintTriggerTime;
    }

    @Override
    public void setSprintTriggerTime(int sprintTriggerTime) {
        this.sprintTriggerTime = sprintTriggerTime;
    }
}