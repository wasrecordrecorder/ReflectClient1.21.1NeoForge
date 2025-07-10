package com.dsp.main.Mixin;

import com.dsp.main.Managers.Event.ClientPacketSendEvent;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.PacketFlow;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public class ConnectionMixin {
    @Inject(
            method = "send(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;Z)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void interceptSendPacket(Packet<?> packet, PacketSendListener listener, boolean flush, CallbackInfo ci) {
        Connection instance = (Connection) (Object) this;
        if (instance.getSending() == PacketFlow.SERVERBOUND) {
            ClientPacketSendEvent event = new ClientPacketSendEvent(packet);
            NeoForge.EVENT_BUS.post(event);
            if (event.isCanceled()) {
                ci.cancel();
            }
        }
    }
}