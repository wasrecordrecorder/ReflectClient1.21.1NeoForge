package com.dsp.main.Functions.Misc;

import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.CheckBox;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.Mode;
import com.dsp.main.Utils.Minecraft.Chat.ChatUtil;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.dsp.main.Api.*;

public class AutoEZ extends Module {
    private static final List<String> CUSTOM_MESSAGES = new ArrayList<>();
    private static final Random RANDOM = new Random();

    private static final CheckBox global = new CheckBox("Global Chat", true);
    private static final Mode mode = new Mode("Mode", "Basic", "Custom");
    private static final Mode server = new Mode("Server", "Universal", "FunTime");

    private static final String[] BASIC_MESSAGES = {
            "%player% АНБРЕЙН ГЕТАЙ РЕФЛЕКТ",
            "%player% ТВОЯ МАТЬ БУДЕТ СЛЕДУЮЩЕЙ))))",
            "%player% БИЧАРА БЕЗ РФК",
            "%player% ЧЕ ТАК БЫСТРО СЛИЛСЯ ТО А?",
            "%player% ПЛАЧЬ ХУЙНЯ",
            "%player% УПССС ЗАБЫЛ КИЛЛКУ ВЫРУБИТЬ",
            "ОДНОКЛЕТОЧНЫЙ %player% БЫЛ ВПЕНЕН",
            "%player% ИЗИ БЛЯТЬ АХААХАХАХАХААХ",
            "%player% БОЖЕ МНЕ ТЕБЯ ЖАЛКО ЕБАНИ РФК",
            "%player% ОПРАВДЫВАЙСЯ В ХУЙ ЧЕ СДОХ ТО)))",
            "%player% СПС ЗА ОТСОС)))"
    };

    private Path customMessagesFile;
    private static long lastMessageTime = 0;
    private static final long MESSAGE_COOLDOWN = 1000;

    public AutoEZ() {
        super("AutoEZ", 0, Category.MISC, "Auto insult on kill");
        addSettings(global, mode, server);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        loadCustomMessages();
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (!isEnabled() || mc.player == null || mc.level == null) return;

        if (!"Universal".equals(server.getMode())) return;

        LivingEntity dead = event.getEntity();
        if (!(dead instanceof Player deadPlayer)) return;

        if (deadPlayer == mc.player) return;

        if (event.getSource().getEntity() == mc.player) {
            sendEZMessage(deadPlayer.getName().getString());
        }
    }

    private Object getFieldValue(Object obj, String fieldName) {
        try {
            java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
            return null;
        }
    }

    public static void onChatPacket(ClientboundSystemChatPacket packet) {
        if (mc.player == null) return;

        if (!"FunTime".equals(server.getMode())) return;

        String message = packet.content().getString();

        if (message.contains("Вы убили игрока") ||
                message.contains("You killed") ||
                message.contains("killed") ||
                message.contains("был убит")) {

            String playerName = extractPlayerName(message);
            if (playerName != null && !playerName.isEmpty()) {
                sendEZMessage(playerName);
            }
        }
    }

    private static String extractPlayerName(String message) {
        Pattern pattern1 = Pattern.compile("Вы убили игрока (.+?)(?:\\s|$|!)");
        Matcher matcher1 = pattern1.matcher(message);
        if (matcher1.find()) {
            return matcher1.group(1).trim();
        }

        Pattern pattern2 = Pattern.compile("You killed (.+?)(?:\\s|$|!)");
        Matcher matcher2 = pattern2.matcher(message);
        if (matcher2.find()) {
            return matcher2.group(1).trim();
        }

        Pattern pattern3 = Pattern.compile("killed (.+?)(?:\\s|$|!)");
        Matcher matcher3 = pattern3.matcher(message);
        if (matcher3.find()) {
            return matcher3.group(1).trim();
        }

        return null;
    }

    private static void sendEZMessage(String playerName) {
        if (playerName == null || playerName.isEmpty()) return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastMessageTime < MESSAGE_COOLDOWN) {
            return;
        }

        lastMessageTime = currentTime;

        String message = generateMessage(playerName);
        if (message == null || message.isEmpty()) return;

        if (global.isEnabled()) {
            message = "!" + message;
        }

        sendChatMessage(message);
    }

    private static String generateMessage(String playerName) {
        if ("Custom".equals(mode.getMode())) {
            if (CUSTOM_MESSAGES.isEmpty()) {
                ChatUtil.sendMessage("§cAutoEZ: Custom messages file is empty!");
                return null;
            }

            String message = CUSTOM_MESSAGES.get(RANDOM.nextInt(CUSTOM_MESSAGES.size()));
            return message.replace("%player%", playerName);
        } else {
            String message = BASIC_MESSAGES[RANDOM.nextInt(BASIC_MESSAGES.length)];
            return message.replace("%player%", playerName);
        }
    }

    private static void sendChatMessage(String message) {
        if (mc.player == null || mc.getConnection() == null) return;

        try {
            if (message.startsWith("/")) {
                mc.player.connection.sendCommand(message.substring(1));
            } else {
                mc.player.connection.sendChat(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadCustomMessages() {
        try {
            customMessagesFile = getCustomConfigDir().resolve("AutoEZ.txt");

            if (!Files.exists(customMessagesFile)) {
                Files.createDirectories(customMessagesFile.getParent());
                Files.createFile(customMessagesFile);

                List<String> defaultMessages = List.of(
                        "%player% GET GOOD",
                        "%player% EZ LMAO",
                        "GG %player% BETTER LUCK NEXT TIME",
                        "",
                        "Multi-line example",
                        "Split by empty line",
                        "",
                        "%player% REKT"
                );

                Files.write(customMessagesFile, defaultMessages, StandardCharsets.UTF_8);
            }

            loadMessagesFromFile();

        } catch (IOException e) {
            ChatUtil.sendMessage("§cAutoEZ: Failed to load custom messages - " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadMessagesFromFile() {
        try {
            List<String> lines = Files.readAllLines(customMessagesFile, StandardCharsets.UTF_8);

            CUSTOM_MESSAGES.clear();

            boolean hasEmptyLines = lines.stream().anyMatch(String::isEmpty);

            if (hasEmptyLines) {
                StringBuilder currentMessage = new StringBuilder();

                for (String line : lines) {
                    if (line.trim().isEmpty()) {
                        if (currentMessage.length() > 0) {
                            CUSTOM_MESSAGES.add(currentMessage.toString().trim());
                            currentMessage = new StringBuilder();
                        }
                    } else {
                        if (currentMessage.length() > 0) {
                            currentMessage.append(" ");
                        }
                        currentMessage.append(line.trim());
                    }
                }

                if (currentMessage.length() > 0) {
                    CUSTOM_MESSAGES.add(currentMessage.toString().trim());
                }
            } else {
                for (String line : lines) {
                    if (!line.trim().isEmpty()) {
                        CUSTOM_MESSAGES.add(line.trim());
                    }
                }
            }

            ChatUtil.sendMessage("§aAutoEZ: Loaded " + CUSTOM_MESSAGES.size() + " custom messages");

        } catch (IOException e) {
            ChatUtil.sendMessage("§cAutoEZ: Failed to read custom messages - " + e.getMessage());
        }
    }

    public void reloadMessages() {
        loadMessagesFromFile();
    }

    public static List<String> getCustomMessages() {
        return new ArrayList<>(CUSTOM_MESSAGES);
    }

    public int getCustomMessagesCount() {
        return CUSTOM_MESSAGES.size();
    }
}