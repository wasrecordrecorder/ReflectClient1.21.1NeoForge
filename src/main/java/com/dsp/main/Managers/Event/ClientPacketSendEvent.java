package com.dsp.main.Managers.Event;

import net.minecraft.network.protocol.Packet;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public class ClientPacketSendEvent extends Event implements ICancellableEvent {
    private Packet<?> packet;

    public ClientPacketSendEvent(Packet<?> packet) {
        this.packet = packet;
    }

    public Packet<?> getPacket() {
        return packet;
    }

    public void setPacket(Packet<?> packet) {
        this.packet = packet;
    }
}