package com.dsp.main.Functions.Misc;

import com.dsp.main.Core.Event.ClientPacketReceiveEvent;
import com.dsp.main.Module;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.Relative;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.HashSet;
import java.util.Set;

import static com.dsp.main.Api.mc;

public class NoServerRot extends Module {
    public NoServerRot() {
        super("NoServerRot", 0, Category.MISC, "Disabling server access to rotate your camera");
    }

    @SubscribeEvent
    public void onReceive(ClientPacketReceiveEvent e) {
        if (mc.player == null) return;
        if (!(e.getPacket() instanceof ClientboundPlayerPositionPacket pkt)) return;

        PositionMoveRotation original = pkt.change();
        Set<Relative> relatives = pkt.relatives();
        int id = pkt.id();

        Set<Relative> fixedRelatives = new HashSet<>(relatives);
        fixedRelatives.remove(Relative.Y_ROT);
        fixedRelatives.remove(Relative.X_ROT);

        PositionMoveRotation fixed = new PositionMoveRotation(
                original.position(),
                original.deltaMovement(),
                mc.player.getYRot(),
                mc.player.getXRot()
        );

        ClientboundPlayerPositionPacket fixedPacket = new ClientboundPlayerPositionPacket(
                id,
                fixed,
                fixedRelatives
        );

        mc.execute(() -> {
            mc.player.connection.handleMovePlayer(fixedPacket);
        });

        e.setCanceled(true);
    }
}