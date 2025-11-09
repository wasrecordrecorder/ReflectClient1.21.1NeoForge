package com.dsp.main.Mixin;

import com.dsp.main.Api;
import com.dsp.main.Functions.Misc.AutoFish;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class MixinClientPacketListener extends ClientCommonPacketListenerImpl {

    protected MixinClientPacketListener(Minecraft minecraft, Connection connection, CommonListenerCookie commonListenerCookie) {
        super(minecraft, connection, commonListenerCookie);
    }

    @Inject(method = "handleSoundEvent", at = @At("HEAD"))
    private void onSoundPacket(ClientboundSoundPacket packet, CallbackInfo ci) {
        if (this.minecraft.isSameThread()) {
            if (Api.isEnabled("AutoFish")) {
                AutoFish.handleSoundPacket(packet);
            }
        }
    }
}