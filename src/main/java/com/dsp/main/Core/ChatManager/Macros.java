package com.dsp.main.Core.ChatManager;

import com.dsp.main.Utils.Minecraft.Chat.ChatUtil;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientChatEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

import static com.dsp.main.Api.mc;
import static com.dsp.main.Core.FrndSys.FriendManager.CmdPrefix;
import static com.dsp.main.Utils.KeyName.getKeyName;

public class Macros {
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onClientChat(ClientChatEvent event) {
        String msg = event.getMessage();
        String[] args = msg.split(" ");
        if (args[0].equalsIgnoreCase(CmdPrefix + "macro") || args[0].equalsIgnoreCase(CmdPrefix + "макро")) {
            if (args.length >= 2) {
                switch (args[1].toLowerCase()) {
                    case "добавить":
                    case "add":
                        if (args.length >= 4) {
                            String keyName = args[2].toUpperCase();
                            String macroText = msg.substring(msg.indexOf("\"", msg.indexOf(keyName)) + 1, msg.lastIndexOf("\""));
                            if (!keyName.isEmpty() && !macroText.isEmpty()) {
                                macros.put(keyName, macroText);
                                ChatUtil.sendMessage("Макрос добавлен: " + keyName + " -> " + macroText);
                            } else {
                                ChatUtil.sendMessage("Ошибка при добавлении макроса. Проверьте параметры.");
                            }
                        } else {
                            ChatUtil.sendMessage("Используйте: .macro add <Клавиша> \"Текст\"");
                        }
                        break;
                    case "удалить":
                    case "remove":
                    case "rem":
                    case "del":
                    case "delete":
                        if (args.length == 3) {
                            String key = args[2].toUpperCase();
                            removeMacro(key);
                        } else {
                            ChatUtil.sendMessage("Используйте: .macro remove \"Клавиша\"");
                        }
                        break;
                    case "список":
                    case "list":
                        listMacros();
                        break;
                    default:
                        ChatUtil.sendMessage("Неизвестная команда. Используйте: .macro add \"Клавиша\" \"Текст\", .macro remove \"Клавиша\" \"Текст\", .macro list");
                        break;
                }
            } else {
                ChatUtil.sendMessage("Используйте: .macro add \"Клавиша\" \"Текст\", .macro remove \"Клавиша\" \"Текст\", .macro list");
            }
            event.setCanceled(true);
        }
    }

    public static final Map<String, String> macros = new HashMap<>();

    private void removeMacro(String key) {
        if (macros.containsKey(key)) {
            macros.remove(key);
            ChatUtil.sendMessage("Макрос для клавиши \"" + key + "\" удален.");
        } else {
            ChatUtil.sendMessage("Макрос для клавиши \"" + key + "\" не найден.");
        }
    }

    private void listMacros() {
        if (macros.isEmpty()) {
            ChatUtil.sendMessage("Нет добавленных макросов.");
        } else {
            ChatUtil.sendMessage("Список макросов:");
            macros.forEach((key, text) -> ChatUtil.sendMessage("Клавиша: " + key + ", Текст: " + text));
        }
    }
    @SubscribeEvent
    public void onKeyInput(InputEvent.Key event) {
        if (event.getAction() == GLFW.GLFW_PRESS) {
            if (mc.screen != null) return;
            String keyName = getKeyName(event.getKey()).toUpperCase();
            if (macros.containsKey(keyName)) {
                String macroText = macros.get(keyName);
                mc.player.connection.sendChat(macroText);
            }
        }
    }
}
