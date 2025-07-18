package com.dsp.main.Functions.Misc;

import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Settings.Input;
import com.dsp.main.Utils.Minecraft.Chat.ChatUtil;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;

import static com.dsp.main.Api.mc;
import static com.dsp.main.Utils.Minecraft.Server.WhatServer.isFt;

public class AutoAuch extends Module {
    public static Input pass = new Input("Пароль", "123");
    public AutoAuch() {
        super("Auto Auch", 0, Category.MISC, "Automatically register / login you into account.");
        addSetting(pass);
    }

    @SubscribeEvent
    public void OnChatMessage(ClientChatReceivedEvent event) {
        if (mc.player == null) return;
        String message = event.getMessage().getString();
        if (message.contains("Зарегистрируйтесь") && message.contains("/reg <Пароль>") && isFt()) {
            if (!pass.getValue().isEmpty()) {
                mc.player.connection.sendCommand("reg " + pass.getValue());
            } else {
                ChatUtil.sendMessage("У вас не установлен пароль. Пожалуста установите пароль");
            }
        } else if (message.contains("/reg") || message.contains("/register") && !isFt()) {
            if (!pass.getValue().isEmpty()) {
                mc.player.connection.sendCommand("reg " + pass.getValue() + " " + pass.getValue());
            } else {
                ChatUtil.sendMessage("У вас не установлен пароль. Пожалуста установите пароль");
            }
        } else if (message.contains("Войдите в игру") && message.contains("/login <Пароль>") && isFt()) {
            if (!pass.getValue().isEmpty()) {
                mc.player.connection.sendCommand("login " + pass.getValue());
            } else {
                ChatUtil.sendMessage("У вас не установлен пароль. Пожалуста установите пароль");
            }
        } else if (message.contains("/login") || message.contains("/l") && !isFt() && !message.contains("/links")) {
            if (!pass.getValue().isEmpty()) {
                mc.player.connection.sendCommand("login " + pass.getValue());
            } else {
                ChatUtil.sendMessage("У вас не установлен пароль. Пожалуста установите пароль");
            }
        }
    }
}
