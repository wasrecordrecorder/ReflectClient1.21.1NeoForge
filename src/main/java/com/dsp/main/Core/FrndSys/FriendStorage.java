package com.dsp.main.Core.FrndSys;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.nio.file.*;
import java.util.HashSet;
import java.util.Set;

public class FriendStorage {
    private final Set<String> friends;
    private final String folderPath = System.getProperty("user.home") + "/AppData/Roaming/Some";
    private final String filePath = folderPath + "/friends.rfcl";
    private final Gson gson;

    public FriendStorage() {
        this.friends = new HashSet<>();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
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
                BufferedReader reader = Files.newBufferedReader(path);
                Set<String> loadedFriends = gson.fromJson(reader, new TypeToken<Set<String>>(){}.getType());
                if (loadedFriends != null) {
                    friends.addAll(loadedFriends);
                }
                reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveFriends() {
        try {
            BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath));
            gson.toJson(friends, writer);
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