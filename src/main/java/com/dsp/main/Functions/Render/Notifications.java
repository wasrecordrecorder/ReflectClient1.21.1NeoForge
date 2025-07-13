package com.dsp.main.Functions.Render;

import com.dsp.main.Api;
import com.dsp.main.Managers.Event.ClientPacketReceiveEvent;
import com.dsp.main.Managers.Event.OnUpdate;
import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Settings.CheckBox;
import com.dsp.main.UI.ClickGui.Settings.Mode;
import com.dsp.main.UI.ClickGui.Settings.MultiCheckBox;
import com.dsp.main.UI.Notifications.Notification;
import com.dsp.main.Utils.Minecraft.Chat.ChatUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.protocol.game.ClientboundDisguisedChatPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.time.Instant;
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
