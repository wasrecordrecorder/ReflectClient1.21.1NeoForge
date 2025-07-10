package com.dsp.main.Utils;

import com.google.gson.*;
import net.minecraft.client.Minecraft;
import com.dsp.main.UI.MainMenu.MainMenuScreen.Account;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class AltConfig {
    private static final String FOLDER_PATH = System.getProperty("user.home") + "/AppData/Roaming/Some";
    private static final File FILE = new File(FOLDER_PATH + "/alts.rfcl");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void loadAlts(List<Account> alts) {
        alts.clear();
        if (!FILE.exists()) {
            try {
                FILE.getParentFile().mkdirs();
                FILE.createNewFile();
                JsonObject json = new JsonObject();
                JsonArray altsArray = new JsonArray();
                JsonObject defaultAccount = new JsonObject();
                defaultAccount.addProperty("username", "dev");
                defaultAccount.addProperty("favorite", false);
                altsArray.add(defaultAccount);
                json.add("alts", altsArray);
                try (PrintWriter writer = new PrintWriter(new FileWriter(FILE))) {
                    writer.println(GSON.toJson(json));
                }
                alts.add(new Account("dev", false));
            } catch (IOException e) {
                System.err.println("AltConfig: Failed to create alts.json: " + e.getMessage());
                e.printStackTrace();
            }
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE))) {
            StringBuilder jsonContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonContent.append(line);
            }

            if (jsonContent.length() == 0) {
                JsonObject json = new JsonObject();
                JsonArray altsArray = new JsonArray();
                JsonObject defaultAccount = new JsonObject();
                defaultAccount.addProperty("username", "dev");
                defaultAccount.addProperty("favorite", false);
                altsArray.add(defaultAccount);
                json.add("alts", altsArray);
                try (PrintWriter writer = new PrintWriter(new FileWriter(FILE))) {
                    writer.println(GSON.toJson(json));
                }
                alts.add(new Account("dev", false));
                return;
            }

            try {
                JsonObject json = JsonParser.parseString(jsonContent.toString()).getAsJsonObject();
                if (json.has("alts")) {
                    JsonArray altsArray = json.getAsJsonArray("alts");
                    for (JsonElement element : altsArray) {
                        if (element.isJsonObject()) {
                            JsonObject accountObj = element.getAsJsonObject();
                            if (accountObj.has("username") && accountObj.get("username").isJsonPrimitive()) {
                                String username = accountObj.get("username").getAsString();
                                boolean isFavorite = accountObj.has("favorite") && accountObj.get("favorite").getAsBoolean();
                                if (!alts.stream().anyMatch(a -> a.getUsername().equals(username))) {
                                    alts.add(new Account(username, isFavorite));
                                }
                            }
                        } else if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                            String username = element.getAsString();
                            if (!alts.stream().anyMatch(a -> a.getUsername().equals(username))) {
                                alts.add(new Account(username, false));
                            }
                        }
                    }
                }
            } catch (JsonParseException e) {
                System.err.println("AltConfig: Failed to parse alts.json, resetting to default: " + e.getMessage());
                JsonObject json = new JsonObject();
                JsonArray altsArray = new JsonArray();
                JsonObject defaultAccount = new JsonObject();
                defaultAccount.addProperty("username", "dev");
                defaultAccount.addProperty("favorite", false);
                altsArray.add(defaultAccount);
                json.add("alts", altsArray);
                try (PrintWriter writer = new PrintWriter(new FileWriter(FILE))) {
                    writer.println(GSON.toJson(json));
                }
                alts.add(new Account("dev", false));
            }
        } catch (IOException e) {
            System.err.println("AltConfig: Failed to read alts.json: " + e.getMessage());
            e.printStackTrace();
            alts.add(new Account("dev", false));
        }
    }

    public static void saveAlts(List<Account> alts) {
        try {
            FILE.getParentFile().mkdirs();
            JsonObject json = new JsonObject();
            JsonArray altsArray = new JsonArray();
            for (Account account : alts) {
                JsonObject accountObj = new JsonObject();
                accountObj.addProperty("username", account.getUsername());
                accountObj.addProperty("favorite", account.isFavorite());
                altsArray.add(accountObj);
            }
            json.add("alts", altsArray);
            try (PrintWriter writer = new PrintWriter(new FileWriter(FILE))) {
                writer.println(GSON.toJson(json));
            }
        } catch (IOException e) {
            System.err.println("AltConfig: Failed to save alts.json: " + e.getMessage());
            e.printStackTrace();
        }
    }
}