package com.dsp.main.Mixin;

import com.dsp.main.Api;
import com.dsp.main.Functions.Player.FastExp;
import com.dsp.main.Managers.Event.ClientPacketReceiveEvent;
import com.dsp.main.Managers.Event.OnUpdate;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.dsp.main.Main.isDetect;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Shadow
    private int rightClickDelay;
    @Inject(method = "createTitle", at = @At("RETURN"), cancellable = true)
    private void onTitleCreating(CallbackInfoReturnable<String> cir) {
        if (!isDetect) {
            cir.setReturnValue("Reflect Client 3.3 - Release");
        }
    }
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void tick(CallbackInfo ci) {
        OnUpdate event = new OnUpdate();
        NeoForge.EVENT_BUS.post(event);
        if (!isDetect && Api.isEnabled("FastExp")) {
            this.rightClickDelay = 1;
        }
    }
}