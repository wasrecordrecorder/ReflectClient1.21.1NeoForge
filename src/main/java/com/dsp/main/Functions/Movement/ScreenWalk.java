package com.dsp.main.Functions.Movement;

import ca.weblite.objc.Client;
import com.dsp.main.Core.Event.ClientPacketSendEvent;
import com.dsp.main.Core.Event.OnUpdate;
import com.dsp.main.Core.Other.Hooks.InventoryScreenHook;
import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.CheckBox;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.neoforged.bus.api.SubscribeEvent;

import static com.dsp.main.Api.mc;

public class ScreenWalk extends Module {
    public static CheckBox shift = new CheckBox("UnlockShift", false);

    public ScreenWalk() {
        super("ScreenWalk", 0, Category.MOVEMENT, "Allows walking when any screen is opened");
        addSettings(shift);
    }

    @SubscribeEvent
    public void onTick(OnUpdate e) {
    }
    @SubscribeEvent
    public void onPacket(ClientPacketSendEvent e) {
        if (e.getPacket() instanceof ServerboundContainerClickPacket && mc.screen instanceof InventoryScreenHook && mc.player != null) {
            mc.player.connection.send(new ServerboundContainerClosePacket(mc.player.containerMenu.containerId));
        }
    }
}