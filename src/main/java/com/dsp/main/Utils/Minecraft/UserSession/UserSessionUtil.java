package com.dsp.main.Utils.Minecraft.UserSession;

import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.chat.LastSeenMessages;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Pattern;

public class UserSessionUtil {

    private static final Pattern VALID_USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,16}$");

    public static boolean setNameSession(String name) {
        if (name == null || !VALID_USERNAME_PATTERN.matcher(name).matches()) {
            System.err.println("Invalid username: " + name + ". Must be 3–16 characters, only letters, numbers, and underscores.");
            return false;
        }
        Minecraft mc = Minecraft.getInstance();

        try {
            User newUser = new User(
                    name,
                    UUID.randomUUID(),
                    "0",
                    Optional.empty(),
                    Optional.empty(),
                    User.Type.LEGACY
            );
            Field userField = null;
            for (Field field : Minecraft.class.getDeclaredFields()) {
                if (field.getType() == User.class) {
                    userField = field;
                    break;
                }
            }
            if (userField != null) {
                userField.setAccessible(true);
                userField.set(mc, newUser);
            } else {
                System.err.println("Field 'user' not found in Minecraft class");
                return false;
            }
            return true;
        } catch (IllegalAccessException e) {
            System.err.println("Failed to set user session: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    public static String generateRandomUsername() {
        Random r = new Random();
        StringBuilder name = new StringBuilder();

        String[][] parts = {
                {"Pro", "xX", "XX", "Mr", "Ms", "Dr", "Ded", "Top", "Lil", "Big", "Dark", "Neo", "Epic", "Ultra", "No", "Zer0", "x_", "_x", "iAm", "Not", "YT", "Twitch", "Senpai", "Ninja", "Pixel", "Craft", "Sky", "Red", "Blue", "Void", "Star", "Glitch"},
                {"ponos", "eban", "krip", "loh", "kek", "lul", "mem", "dota", "mine", "craft", "fan", "bug", "snus", "chmo", "gamer", "noob", "pro", "hacker", "creeper", "enderman", "steve", "alex", "zombie", "skelly", "pickaxe", "diamond", "redstone", "nether", "ender", "sushi", "kawaii", "shiba", "loli", "oni", "sigma", "chad", "vibe", "lit"},
                {"_YT", "_Pro", "_228", "_2010", "_4ik", "_nik", "_ok", "_chill", "_boy", "_guy", "_girl", "_1337", "_666", "_69", "_420", "_XX", "_Xx", "_TV", "_GG", "_EZ", "_Lad", "_Senpai", "_Kun", "_Chan", "_Sama", "_UwU", "_OwO"},
                {"virus", "moker", "killer", "slayer", "hacker", "admin", "noob", "boss", "crafter", "picker", "destroyer", "gamer", "venom", "phantom", "shadow", "knight", "wizard", "samurai", "ninja", "hero", "legend", "god", "king", "queen", "sniper", "blaze"},
                {"1", "2", "3", "4", "5", "7", "9", "13", "17", "21", "37", "69", "88", "95", "00", "01", "05", "10", "44", "77", "99", "228", "420", "1337", "2023", "2025"}
        };
        String[][] cyrillicParts = {
                {"sha", "ly", "ni", "ka", "tvo", "ma", "eb", "da", "che", "pz", "za", "su", "yo", "bly", "suka", "kot", "pes", "zlo", "dob", "kek", "lol", "vau"},
                {"Sha", "Ly", "Ni", "Ka", "Tvo", "Ma", "Eb", "Da", "Che", "Pz", "Za", "Su", "Yo", "Bly", "Suka", "Kot", "Pes", "Zlo", "Dob", "Kek", "Lol", "Vau"}
        };

        switch (r.nextInt(10)) {
            case 0:
                int length = r.nextInt(4) + 4;
                String vowels = "aeiouy";
                String consonants = "bcdfghjklmnpqrstvwxz";
                for (int i = 0; i < length; i++) {
                    name.append(i % 2 == 0 ? consonants.charAt(r.nextInt(consonants.length()))
                            : vowels.charAt(r.nextInt(vowels.length())));
                }
                if (r.nextBoolean()) name.append(parts[4][r.nextInt(parts[4].length)]);
                break;

            case 1:
                name.append(parts[0][r.nextInt(parts[0].length)])
                        .append(parts[1][r.nextInt(parts[1].length)])
                        .append(r.nextBoolean() ? parts[2][r.nextInt(parts[2].length)] : "");
                break;

            case 2:
                name.append(cyrillicParts[r.nextBoolean() ? 0 : 1][r.nextInt(cyrillicParts[0].length)])
                        .append(cyrillicParts[r.nextBoolean() ? 0 : 1][r.nextInt(cyrillicParts[0].length)])
                        .append(r.nextBoolean() ? parts[4][r.nextInt(parts[4].length)] : "");
                break;

            case 3:
                name.append(parts[0][r.nextInt(parts[0].length)])
                        .append(parts[3][r.nextInt(parts[3].length)])
                        .append(r.nextBoolean() ? parts[4][r.nextInt(parts[4].length)] : "");
                break;

            case 4:
                name.append(parts[1][r.nextInt(parts[1].length)])
                        .append(parts[2][r.nextInt(parts[2].length)])
                        .append(r.nextBoolean() ? String.valueOf((char)(r.nextInt(26) + 65)) : "");
                break;

            case 5:
                name.append(parts[1][r.nextInt(parts[1].length)])
                        .append(parts[4][r.nextInt(parts[4].length)]);
                break;

            case 6:
                name.append(parts[0][r.nextInt(parts[0].length)])
                        .append(parts[1][r.nextInt(parts[1].length)])
                        .append(r.nextBoolean() ? parts[2][r.nextInt(parts[2].length)] : "");
                break;

            case 7:
                name.append(r.nextBoolean() ? "_" : "")
                        .append(parts[0][r.nextInt(parts[0].length)])
                        .append(parts[1][r.nextInt(parts[1].length)])
                        .append(r.nextBoolean() ? "_" : "")
                        .append(parts[3][r.nextInt(parts[3].length)])
                        .append(r.nextBoolean() ? "_" + parts[4][r.nextInt(parts[4].length)] : "");
                break;

            case 8:
                name.append(parts[1][r.nextInt(parts[1].length)])
                        .append(parts[3][r.nextInt(parts[3].length)])
                        .append(r.nextBoolean() ? parts[2][r.nextInt(parts[2].length)] : "");
                break;

            case 9:
                name.append(r.nextBoolean() ? parts[0][r.nextInt(parts[0].length)] : "")
                        .append(parts[1][r.nextInt(parts[1].length)])
                        .append(r.nextBoolean() ? parts[2][r.nextInt(parts[2].length)] : "")
                        .append(r.nextBoolean() ? parts[3][r.nextInt(parts[3].length)] : "")
                        .append(r.nextBoolean() ? "_" + parts[4][r.nextInt(parts[4].length)] : "");
                break;
        }
        char[] chars = name.toString().toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (r.nextFloat() < 0.4f) {
                chars[i] = r.nextBoolean() ? Character.toUpperCase(chars[i]) : Character.toLowerCase(chars[i]);
            }
        }
        name = new StringBuilder(new String(chars));
        if (r.nextBoolean() && name.length() > 1) {
            int pos = r.nextInt(name.length());
            name.insert(pos, name.charAt(pos));
        }
        if (r.nextBoolean() && name.length() > 0) {
            name.insert(0, "_");
        }
        if (r.nextBoolean()) {
            name.append("_");
        }
        String result = name.toString()
                .replaceAll("[^a-zA-Z0-9_]", "")
                .replaceAll("_{2,}", "_");
        result = result.length() > 16 ? result.substring(0, 16) : result;
        return result.length() < 3 ? "QwizzOF" + r.nextInt(1000) : result;
    }
}