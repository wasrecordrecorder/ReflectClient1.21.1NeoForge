package com.dsp.main.Functions.Misc;

import com.dsp.main.Core.Event.ClientPacketReceiveEvent;
import com.dsp.main.Module;
import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.neoforged.bus.api.SubscribeEvent;

import static com.dsp.main.Api.mc;

public class ItemSwapFix extends Module {
    public ItemSwapFix() {
        super("ItemSwapFix", 0, Category.MISC, "With this server can't swap your slots");
    }
    @SubscribeEvent
    public void packet(ClientPacketReceiveEvent e) {
        if (mc.player == null) return;
        if (e.getPacket() instanceof ClientboundSetHeldSlotPacket pac) {
            mc.player.connection.send(new ServerboundSetCarriedItemPacket(pac.slot()));
            e.setCanceled(true);
        }
    }
}
