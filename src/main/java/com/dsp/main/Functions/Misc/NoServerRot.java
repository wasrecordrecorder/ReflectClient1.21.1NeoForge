package com.dsp.main.Functions.Misc;

import com.dsp.main.Core.Event.ClientPacketReceiveEvent;
import com.dsp.main.Module;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.neoforged.bus.api.SubscribeEvent;

import static com.dsp.main.Api.mc;

public class NoServerRot extends Module {
    public NoServerRot() {
        super("NoServerRot", 0, Category.MISC, "Disabling server access to rotate your camera");
    }

    @SubscribeEvent
    public void onReceive(ClientPacketReceiveEvent e) {
        if (mc.player == null) return;
        if (!(e.getPacket() instanceof ClientboundPlayerPositionPacket pkt)) return;
        double x = pkt.getX();
        double y = pkt.getY();
        double z = pkt.getZ();
        var relatives = pkt.getRelativeArguments();
        int id = pkt.getId();
        mc.execute(() -> {
            ClientboundPlayerPositionPacket fixed = new ClientboundPlayerPositionPacket(
                    x, y, z,
                    mc.player.getYRot(), mc.player.getXRot(),
                    relatives, id
            );
            mc.player.connection.handleMovePlayer(fixed);
        });
        e.setCanceled(true);
    }
}