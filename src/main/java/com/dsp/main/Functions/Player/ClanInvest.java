package com.dsp.main.Functions.Player;

import com.dsp.main.Core.Event.ClientPacketReceiveEvent;
import com.dsp.main.Core.Event.OnUpdate;
import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.Input;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClanInvest extends Module {
    public static Input investAmount = new Input("Invest Amount", "");
    public static Input minBalanceToInvest = new Input("Required Balance", "");

    private int lastInvestTime = 0;
    private int checkTimer = 0;
    private int playerBalance = 0;
    private boolean waitingForBalanceResponse = false;
    private long lastBalanceRequestTime = 0;

    public ClanInvest() {
        super("Clan Invest", 0, Category.PLAYER, "Automatic clan investment");
        addSettings(investAmount, minBalanceToInvest);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        lastInvestTime = 0;
        checkTimer = 0;
        playerBalance = 0;
    }

    private void sendInvestCommand() {
        if (Minecraft.getInstance().player == null) return;
        try {
            int minBalance = Integer.parseInt(minBalanceToInvest.getValue());
            if (playerBalance < minBalance) return;

            int amount = Integer.parseInt(investAmount.getValue());
            Minecraft.getInstance().player.connection.sendCommand("clan invest " + amount);
            lastInvestTime = (int) (System.currentTimeMillis() / 1000);
        } catch (NumberFormatException ignored) {}
    }

    private void requestPlayerBalance() {
        if (Minecraft.getInstance().player == null) return;
        if (waitingForBalanceResponse) {
            if (System.currentTimeMillis() - lastBalanceRequestTime > 10000) {
                waitingForBalanceResponse = false;
            } else {
                return;
            }
        }
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastBalanceRequestTime < 2000) return;
        Minecraft.getInstance().player.connection.sendCommand("bal");
        waitingForBalanceResponse = true;
        lastBalanceRequestTime = currentTime;
    }

    @SubscribeEvent
    public void onUpdate(OnUpdate e) {
        int currentTime = (int) (System.currentTimeMillis() / 1000);
        if (currentTime % 5 == 0 && checkTimer != currentTime) {
            checkTimer = currentTime;
            if (!waitingForBalanceResponse && currentTime - lastInvestTime >= 5) {
                requestPlayerBalance();
            }
        }
    }

    @SubscribeEvent
    public void onPacketReceive(ClientPacketReceiveEvent e) {
        if (!(e.getPacket() instanceof ClientboundSystemChatPacket packet)) return;
        if (!waitingForBalanceResponse) return;
        Component component = packet.content();
        String message = component.getString();
        String clean = message.replaceAll("§[0-9a-fk-or]", "");

        if (clean.contains("balance") || clean.contains("$")) {
            Pattern currencyPattern = Pattern.compile("\\$\\s*([\\d,\\s]+(?:\\.\\d+)?)");
            Matcher matcher = currencyPattern.matcher(clean);
            if (matcher.find()) {
                String digits = matcher.group(1).replaceAll("[,\\s]", "").split("\\.")[0];
                playerBalance = Integer.parseInt(digits);
            } else if (clean.matches(".*\\d+.*")) {
                String digits = clean.replaceAll("[^0-9]", "");
                playerBalance = Integer.parseInt(digits);
            }
            waitingForBalanceResponse = false;

            int currentTime = (int) (System.currentTimeMillis() / 1000);
            if (currentTime - lastInvestTime >= 5) {
                sendInvestCommand();
            }
        }
    }
}