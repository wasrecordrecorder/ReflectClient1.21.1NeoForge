package com.dsp.main.Managers.FrndSys;

import net.minecraft.client.Minecraft;
import com.dsp.main.Utils.Minecraft.Chat.ChatUtil;

import java.io.File;
import java.io.IOException;
public class FriendManager {
    public static final FriendStorage friendStorage = new FriendStorage();
    public static final Minecraft mc = Minecraft.getInstance();
    String folderPath = System.getProperty("user.home") + "/AppData/Roaming/Some";
    File folder = new File(folderPath);
    public static final String CmdPrefix = ".";
    public static void openCfgFolder() {
        String folderPath = System.getProperty("user.home") + "/AppData/Roaming/Some";
        File folder = new File(folderPath);

        if (folder.exists() && folder.isDirectory()) {
            try {
                java.awt.Desktop.getDesktop().open(folder);
            } catch (IOException ignored) {
            }
        }
    }
    public static void addFriend(String nickname) {
        if (!isFriend(nickname)) {
            friendStorage.addFriend(nickname);
        } else {
            ChatUtil.sendMessage(nickname + " уже в друзьях.");
        }
    }
    public static void removeFriend(String nickname) {
        if (isFriend(nickname)) {
            friendStorage.removeFriend(nickname);
        } else {
            ChatUtil.sendMessage(nickname + " не найден в списке друзей.");
        }
    }
    public static void listFriends() {
        StringBuilder friendList = new StringBuilder("Список друзей: ");
        for (String friend : friendStorage.getFriends()) {
            friendList.append(friend).append(", ");
        }
        if (friendList.length() > 0) {
            friendList.setLength(friendList.length() - 2);
        } else {
            friendList.append("Нет друзей.");
        }
        ChatUtil.sendMessage(friendList.toString());
    }
    public static boolean isFriend(String nickname) {
        return friendStorage.getFriends().contains(nickname);
    }
}
