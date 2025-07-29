package com.dsp.main.Functions.Render;

import com.dsp.main.Api;
import com.dsp.main.Core.Event.ClientPacketReceiveEvent;
import com.dsp.main.Core.Event.OnUpdate;
import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.CheckBox;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.Mode;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.MultiCheckBox;
import com.dsp.main.UI.Notifications.Notification;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.Arrays;

import static com.dsp.main.Api.mc;

public class Notifications extends Module {
    public static Mode renderPos = new Mode("Position", "Top of screen", "Center of screen", "Right corner");
    public static MultiCheckBox Option = new MultiCheckBox("Options", Arrays.asList(
            new CheckBox("Functions Toggle", true),
            new CheckBox("Chat Mention", false)
    ));
    public Notifications() {
        super("Notifications", 0, Category.RENDER, "Enabling client notifications");
        addSettings(renderPos, Option);
    }

    @SubscribeEvent
    public void tick(OnUpdate event) {

    }
    @SubscribeEvent
    public void onRecieve(ClientPacketReceiveEvent event) {
        if (event.getPacket() instanceof  ClientboundSystemChatPacket pac && mc.player != null) {
            if (pac.content().getString().contains(mc.player.getName().getString())) {
                Api.notificationManager.send(Notification.Type.WARNING, "You has been mentioned in chat !");
            }
        }
    }
}
