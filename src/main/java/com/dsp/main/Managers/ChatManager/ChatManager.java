package com.dsp.main.Managers.ChatManager;


import com.dsp.main.Managers.Event.OnUpdate;
import com.dsp.main.Utils.Minecraft.Chat.ChatUtil;
import net.minecraft.client.multiplayer.ServerData;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientChatEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.ServerChatEvent;

import static com.dsp.main.Main.isDetect;
import static com.dsp.main.Api.mc;
import static com.dsp.main.Managers.ConfigSystem.CfgManager.*;
import static com.dsp.main.Managers.FrndSys.FriendManager.*;
import static com.dsp.main.Utils.Minecraft.UserSession.UserSessionUtil.setNameSession;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ChatManager {
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void ClientChat(ClientChatEvent event) throws InterruptedException {
        if (isDetect || mc.player == null) return;
        String playerName = mc.player.getName().getString();
        ServerData serverData = mc.getCurrentServer();
        if (serverData == null) return;
        String serverName = serverData.ip.toLowerCase();
        String msg = event.getMessage();
        String[] args = msg.split(" ");
        if (msg.startsWith(".") && !msg.startsWith(".staff")) {
            event.setCanceled(true);
        }
        if (msg.equalsIgnoreCase(".gps off")) {
            //Triangles.canWork = false;
            event.setCanceled(true);
        } else if (msg.startsWith(".gps add")) {
            String[] split = msg.split(" ");
            if (split.length == 3 || split.length == 4) {
                try {
                    int x = Integer.parseInt(split[2]);
                    int z = Integer.parseInt(split[3]);
                    //Triangles.canWork = true;
                    //Triangles.x = x;
                    //Triangles.z = z;
                    event.setCanceled(true);
                } catch (NumberFormatException ex) {
                    //ChatUtil.sendMessage("Координаты неверны: " + split[2] + " " + split[3]);
                }
            }
        } else if (msg.startsWith("/ah sell")) {
            Pattern pattern = Pattern.compile("(\\d+)\\s*([+\\-*/])\\s*(\\d+)");
            Matcher matcher = pattern.matcher(msg);

            if (matcher.find()) {
                int num1 = Integer.parseInt(matcher.group(1));
                String operator = matcher.group(2);
                int num2 = Integer.parseInt(matcher.group(3));
                int result = 0;
                switch (operator) {
                    case "+":
                        result = num1 + num2;
                        break;
                    case "-":
                        result = num1 - num2;
                        break;
                    case "*":
                        result = num1 * num2;
                        break;
                    case "/":
                        if (num2 != 0) {
                            result = num1 / num2;
                        } else {
                            ChatUtil.sendMessage("Ошибка: деление на ноль.");
                            event.setCanceled(true);
                            return;
                        }
                        break;
                }
                String newMessage = "/ah sell " + result;
                event.setMessage(newMessage);
            }
        } else if (msg.startsWith("/ah me")) {
            event.setMessage("/ah " + mc.player.getName().getString());
        } else if (msg.startsWith(".cfg save ")) {
            String[] split = msg.split(" ");
            if (split.length >= 3) {
                String configName = split[2];
                saveCfg(configName);
                ChatUtil.sendMessage("Конфиг '" + configName + "' успешно сохранен. Используйте '.cfg load " + configName + "' для загрузки конфигурации.");
                event.setCanceled(true);
            }
        } else if (msg.startsWith(".cfg load ")) {
            String[] split = msg.split(" ");
            if (split.length >= 3) {
                String configName = split[2];
                loadCfg(configName);
                ChatUtil.sendMessage("Конфиг '" + configName + "' успешно загружен.");
                event.setCanceled(true);
            }
        } else if (msg.startsWith(".cfg del ") || msg.startsWith(".cfg rem ") || msg.startsWith(".cfg remove ") || msg.startsWith(".cfg delete ")) {
            String[] split = msg.split(" ");
            if (split.length >= 3) {
                String configName = split[2];
                //deleteCfg(configName);
                event.setCanceled(true);
            }
        } else if (msg.startsWith(".cfg dir")) {
            try {
                //Runtime.getRuntime().exec("explorer \"" + CONFIG_DIR.replace("/", "\\") + "\"");
                ChatUtil.sendMessage("Папка с конфигурациями открыта.");
            } catch (Exception ex) {
                ChatUtil.sendMessage("Не удалось открыть папку с конфигурациями: " + ex.getMessage());
            }
            event.setCanceled(true);
        } else if (msg.startsWith(".cfg list")) {
            //String[] configs = getConfigs();
//            if (configs.length > 0) {
//                ChatUtil.sendMessage("Доступные конфиги: " + String.join(", ", configs));
//            } else {
//                ChatUtil.sendMessage("Конфигураций не найдено.");
//            }
            event.setCanceled(true);
    } else if (msg.equals(CmdPrefix + "помощь") || msg.equals(CmdPrefix + "help")) {
            ChatUtil.sendMessage("Доступные команды:");
            ChatUtil.sendMessage(".friend add <name> - add friend");
            ChatUtil.sendMessage(".friend remove <name> - remove friend");
            ChatUtil.sendMessage(".friend list - show friends list");
            ChatUtil.sendMessage(".macro add Клавиша \"Текст\"");
            ChatUtil.sendMessage(".macro rem Клавиша \"Текст\"");
            ChatUtil.sendMessage(".macro list");
            event.setCanceled(true);
        } else if (args[0].startsWith(CmdPrefix + "друг") || (args[0].startsWith(CmdPrefix + "friend"))) {
            if (args.length >= 2) {
                switch (args[1]) {
                    case "добавить":
                    case "add":
                        if (args.length == 3) {
                            addFriend(args[2]);
                        } else {
                            ChatUtil.sendMessage("Используйте: .друг добавить <ник>");
                        }
                        break;
                    case "удалить":
                    case "rem":
                        if (args.length == 3) {
                            removeFriend(args[2]);
                        } else {
                            ChatUtil.sendMessage("Используйте: .друг удалить <ник>");
                        }
                        break;
                    case "список":
                    case "list":
                        listFriends();
                        break;
                    default:
                        ChatUtil.sendMessage("Неизвестная команда. Используйте: .друг добавить, .друг удалить, .друг список");
                        break;
                }
                event.setCanceled(true);
            }
        }
    }
    public static boolean isOpeningSoon;
    public static boolean lastTimeEventa;

    String[] keywords = {
            "Маяк убийца",
            "Сундук Смерти",
            "Новогодний экспресс",
            "Вулкан",
            "Мистический Алтарь",
            "Мистический сундук",
            "Снежная бойня"
    };

    String[] lootLevels = {
            "Обычный",
            "Солидный",
            "Богатый",
            "Элитный",
            "Легендарный"
    };

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onIncomingPacket(ServerChatEvent event) {
        if (mc.player == null) return;
        String message = event.getMessage().getString();
        lastChatMessage = message;
//        if ((message.contains("Появился на координатах") ||
//                message.contains("Портал на резню активирован!") ||
//                message.contains("Появился на Арене Смерти (/darena)"))) {
//            if (message.contains("Появился на Арене Смерти (/darena)")) {
//                remainingTime = 118;
//            } else if (message.contains("Появился на координатах") && message.contains("Маяк убийца")) {
//                remainingTime = 358;
//            } else if (message.contains("Появился на координатах") && message.contains("Новогодний экспресс")) {
//                remainingTime = 298;
//            } else if (message.contains("Появился на координатах") && message.contains("Вулкан")) {
//                remainingTime = 298;
//            } else if (message.contains("Появился на координатах") && message.contains("Мистический Алтарь")) {
//                remainingTime = 358;
//            } else if (message.contains("Появился на координатах") && message.contains("Портал на резню активирован!")) {
//                remainingTime = 298;
//            } else {
//                remainingTime = 298;
//            }
//            lastUpdateTime = System.currentTimeMillis();
//            isEventSoon = false;
//            isOpeningSoon = true;
//        }
//        if (message.contains("Появится уже через") && message.contains("минуты!") || message.contains("Появился на координатах") || message.contains("Портал активируется через 3 минуты!")) {
//            for (String keyword : keywords) {
//                if (message.toLowerCase().contains(keyword.toLowerCase())) {
//                    whatEvent = keyword;
//                    break;
//                }
//            }
//        }
//        if (message.contains("[Ивенты]") && message.contains("[1]") && message.contains("Статус:")) {
//            for (String keyword : keywords) {
//                if (message.toLowerCase().contains(keyword.toLowerCase())) {
//                    whatEvent = keyword;
//                    break;
//                }
//            }
//            if (message.contains("Начнётся через") && message.contains("сек")) {
//                Pattern pattern = Pattern.compile("Начнётся через (\\d+) сек");
//                Matcher matcher = pattern.matcher(message);
//                remainingTime = Integer.parseInt(matcher.group(1));
//                lastUpdateTime = System.currentTimeMillis();
//                isEventSoon = true;
//                isOpeningSoon = false;
//            } else if (message.contains("Идёт страшный бой...") && message.contains("/warp portal")) {
//                remainingTime = 140;
//                isOpeningSoon = false;
//                lastTimeEventa = true;
//            } else if (message.contains("Еще не активирован, до извержения")) {
//                Pattern pattern = Pattern.compile("Еще не активирован, до извержения (\\d+) сек");
//                Matcher matcher = pattern.matcher(message);
//                remainingTime = Integer.parseInt(matcher.group(1));
//                lastUpdateTime = System.currentTimeMillis();
//                isEventSoon = false;
//                isOpeningSoon = true;
//            } else if (message.contains("Извергается, до конца:")) {
//                Pattern pattern = Pattern.compile("Извергается, до конца: (\\d+) сек");
//                Matcher matcher = pattern.matcher(message);
//                remainingTime = Integer.parseInt(matcher.group(1));
//                isOpeningSoon = false;
//                lastTimeEventa = true;
//            } else if (message.contains("Еще не активирован, до активации")) {
//                Pattern pattern = Pattern.compile("Еще не активирован, до активации (\\d+) сек");
//                Matcher matcher = pattern.matcher(message);
//                remainingTime = Integer.parseInt(matcher.group(1));
//                isOpeningSoon = true;
//                isEventSoon = false;
//                lastTimeEventa = false;
//            }
//        }
//        if (message.contains("Появился на координатах") || (message.contains("Появится уже через") && message.contains("минуты!"))) {
//            for (String keyword : keywords) {
//                if (message.contains(keyword)) {
//                    for (String lotLvl : lootLevels) {
//                        if (message.contains(lotLvl)) {
//                            lootLevel = lotLvl;
//                            break;
//                        }
//                    }
//                }
//            }
//        }
//        if (message.contains("Появился на координатах")) {
//            if (!isBetterChatEnabled) return;
//            Pattern pattern = Pattern.compile("Появился на координатах[\\s:]*(-?\\d+(?:\\.\\d+)?)\\s*([,\\s]+)\\s*(-?\\d+(?:\\.\\d+)?)\\s*([,\\s]+)\\s*(-?\\d+(?:\\.\\d+)?)");
//            Matcher matcher = pattern.matcher(message);
//            if (matcher.find()) {
//                int x = Integer.parseInt(matcher.group(1));
//                int y = Integer.parseInt(matcher.group(3));
//                int z = Integer.parseInt(matcher.group(5));
//                Triangles.canWork = true;
//                Triangles.x = x;
//                Triangles.z = z;
//                ChatUtil.sendMessage("Поставил GPS на: [" + x + " " + y + " " + z + "]");
//            }
//        } else if (message.contains("До следующего ивента:")) {
//            Pattern pattern = Pattern.compile("\\[(\\d+)] До следующего ивента: (\\d+) сек");
//            Matcher matcher = pattern.matcher(message);
//            if (matcher.find()) {
//                int totalSeconds = Integer.parseInt(matcher.group(2));
//                remainingTime = totalSeconds - 1;
//                isEventSoon = false;
//                isOpeningSoon = false;
//                lastTimeEventa = false;
//                whatEvent = "0";
//                lootLevel = "1";
//            }
//        } else if (message.contains("Появится уже через") && message.contains("минуты!") || message.contains("Портал активируется через") && message.contains("минуты!")) {
//            remainingTime = 177;
//            lastUpdateTime = System.currentTimeMillis();
//            isEventSoon = true;
//            isOpeningSoon = false;
//        }
//        if (message.contains("До следующего ивента:")) {
//            if (!isBetterChatEnabled) return;
//            Pattern pattern = Pattern.compile("\\[(\\d+)] До следующего ивента: (\\d+) сек");
//            Matcher matcher = pattern.matcher(message);
//            if (matcher.find()) {
//                String number = matcher.group(1);
//                int totalSeconds = Integer.parseInt(matcher.group(2));
//                FuncStorage.remainingTime = totalSeconds;
//                FuncStorage.isEventSoon = false;
//                int minutes = totalSeconds / 60;
//                int seconds = totalSeconds % 60;
//                String formattedTime = String.format("%d:%02d Мин.", minutes, seconds);
//                IFormattableTextComponent numberComponent = new StringTextComponent("[" + number + "]").withStyle(Style.EMPTY.withColor(TextFormatting.RED));
//                IFormattableTextComponent prefixComponent = new StringTextComponent(" До следующего ивента: ").withStyle(Style.EMPTY.withColor(TextFormatting.GOLD));
//                IFormattableTextComponent timeComponent = new StringTextComponent(formattedTime).withStyle(Style.EMPTY.withColor(TextFormatting.RED));
//                numberComponent.append(prefixComponent).append(timeComponent);
//                event.setMessage(numberComponent);
//            }
//        }
//        if (message.contains(mc.player.getName().getString())) {
//            NotificationManager.show(new Notification(NotificationType.WARNING,  "Вас упомянули в чате!", 2));
//        }
    }
    public static String lastChatMessage = "";

    @SubscribeEvent
    public void OnEvent(OnUpdate event) {
//        if (remainingTime == 0 && isOpeningSoon) {
//            remainingTime = 60;
//            isOpeningSoon = false;
//            lastTimeEventa = true;
//        } else if (remainingTime == 0 && lastTimeEventa) {
//            whatEvent = "0";
//            lootLevel = "1";
//            lastTimeEventa = false;
//            isEventSoon = false;
//            remainingTime = -1;
//        }
    }
}
