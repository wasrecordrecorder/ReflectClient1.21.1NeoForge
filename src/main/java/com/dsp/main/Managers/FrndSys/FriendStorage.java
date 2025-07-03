package com.dsp.main.Managers.FrndSys;

import java.io.*;
import java.nio.file.*;
import java.util.HashSet;
import java.util.Set;

public class FriendStorage {
    private final Set<String> friends;
    private final String folderPath = System.getProperty("user.home") + "/AppData/Roaming/Some";
    private final String filePath = folderPath + "/friends.txt";

    public FriendStorage() {
        this.friends = new HashSet<>();
        createFolderIfNotExists();
        loadFriends();
    }


    private void createFolderIfNotExists() {
        try {
            Path path = Paths.get(folderPath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void loadFriends() {
        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                BufferedReader reader = new BufferedReader(new FileReader(filePath));
                String line;
                while ((line = reader.readLine()) != null) {
                    friends.add(line);
                }
                reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveFriends() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
            for (String friend : friends) {
                writer.write(friend);
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addFriend(String nickname) {
        friends.add(nickname);
        saveFriends();
    }

    public void removeFriend(String nickname) {
        friends.remove(nickname);
        saveFriends();
    }

    public Set<String> getFriends() {
        return friends;
    }
}
