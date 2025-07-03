package com.dsp.main.Utils.Minecraft.UserSession;

import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.chat.LastSeenMessages;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Optional;
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
}