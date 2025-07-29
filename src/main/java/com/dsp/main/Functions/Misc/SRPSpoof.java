package com.dsp.main.Functions.Misc;

import com.dsp.main.Core.Event.ClientPacketReceiveEvent;
import com.dsp.main.Core.Event.ClientPacketSendEvent;
import com.dsp.main.Module;
import net.minecraft.network.protocol.common.ClientboundResourcePackPopPacket;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.neoforged.bus.api.SubscribeEvent;

import static com.dsp.main.Api.mc;

public class SRPSpoof extends Module {
    public SRPSpoof() {
        super("SRPSpoof", 0, Category.MISC, "Skipping installation of server rp");
    }

    @SubscribeEvent
    public void onSend(ClientPacketSendEvent e) {
        if (e.getPacket() instanceof ServerboundResourcePackPacket pac) {
            e.setCanceled(true);
        }
    }
    @SubscribeEvent
    public void onSend(ClientPacketReceiveEvent e) {
        if (e.getPacket() instanceof ClientboundResourcePackPopPacket) {
            e.setCanceled(true);
        }
        if (e.getPacket() instanceof ClientboundResourcePackPushPacket) {
            e.setCanceled(true);
        }
    }
}