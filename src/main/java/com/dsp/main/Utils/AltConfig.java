package com.dsp.main.Utils;

import com.dsp.main.Api;
import com.dsp.main.UI.MainMenu.MainMenuScreen.Account;
import com.google.gson.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

public class AltConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Path filePath() {
        return Api.getCustomConfigDir().resolve("alts.rfcl");
    }

    public static void loadAlts(List<Account> alts) {
        alts.clear();
        Path file = filePath();
        try {
            if (Files.notExists(file)) {
                Files.createDirectories(file.getParent());
                writeDefault(file, alts);
                return;
            }
            String content = Files.readString(file, StandardCharsets.UTF_8);
            if (content.isBlank()) {
                writeDefault(file, alts);
                return;
            }
            JsonObject json = JsonParser.parseString(content).getAsJsonObject();
            if (json.has("alts")) {
                JsonArray altsArray = json.getAsJsonArray("alts");
                for (JsonElement element : altsArray) {
                    if (element.isJsonObject()) {
                        JsonObject accountObj = element.getAsJsonObject();
                        if (accountObj.has("username") && accountObj.get("username").isJsonPrimitive()) {
                            String username = accountObj.get("username").getAsString();
                            boolean favorite = accountObj.has("favorite") && accountObj.get("favorite").getAsBoolean();
                            boolean exists = alts.stream().anyMatch(a -> a.getUsername().equals(username));
                            if (!exists) {
                                alts.add(new Account(username, favorite));
                            }
                        }
                    } else if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                        String username = element.getAsString();
                        boolean exists = alts.stream().anyMatch(a -> a.getUsername().equals(username));
                        if (!exists) {
                            alts.add(new Account(username, false));
                        }
                    }
                }
            }
        } catch (JsonParseException e) {
            System.err.println("AltConfig: Failed to parse alts.rfcl, resetting to default: " + e.getMessage());
            writeDefault(file, alts);
        } catch (IOException e) {
            System.err.println("AltConfig: Failed to read alts.rfcl: " + e.getMessage());
            e.printStackTrace();
            alts.add(new Account("dev", false));
        }
    }

    public static void saveAlts(List<Account> alts) {
        Path file = filePath();
        try {
            Files.createDirectories(file.getParent());
            JsonObject json = new JsonObject();
            JsonArray altsArray = new JsonArray();
            for (Account account : alts) {
                JsonObject accountObj = new JsonObject();
                accountObj.addProperty("username", account.getUsername());
                accountObj.addProperty("favorite", account.isFavorite());
                altsArray.add(accountObj);
            }
            json.add("alts", altsArray);
            try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                writer.write(GSON.toJson(json));
            }
        } catch (IOException e) {
            System.err.println("AltConfig: Failed to save alts.rfcl: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void writeDefault(Path file, List<Account> alts) {
        try {
            JsonObject json = new JsonObject();
            JsonArray altsArray = new JsonArray();
            JsonObject defaultAccount = new JsonObject();
            defaultAccount.addProperty("username", "dev");
            defaultAccount.addProperty("favorite", false);
            altsArray.add(defaultAccount);
            json.add("alts", altsArray);
            Files.createDirectories(file.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                writer.write(GSON.toJson(json));
            }
        } catch (IOException e) {
            System.err.println("AltConfig: Failed to create default alts.rfcl: " + e.getMessage());
            e.printStackTrace();
        }
        alts.add(new Account("dev", false));
    }
}