package com.dsp.main.UI.Draggable.DragElements;

import com.dsp.main.Api;
import com.dsp.main.UI.Draggable.DraggableElement;
import com.dsp.main.UI.Themes.ThemesUtil;
import com.dsp.main.Utils.Color.ColorHelper;
import com.dsp.main.Utils.Font.builders.Builder;
import com.dsp.main.Utils.Font.renderers.impl.BuiltText;
import com.dsp.main.Utils.Minecraft.Chat.ChatUtil;
import com.dsp.main.Utils.Render.Blur.DrawShader;
import com.dsp.main.Utils.Render.ColorUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import org.joml.Matrix4f;
import java.awt.Color;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.dsp.main.Api.mc;
import static com.dsp.main.Functions.Render.HudElement.HudElements;
import static com.dsp.main.Functions.Render.HudElement.IconColor;
import static com.dsp.main.Main.BIKO_FONT;
import static com.dsp.main.Main.ICONS;

public class StaffList extends DraggableElement {
    private static final int TEXT_HEIGHT = 8;
    private static final int PADDING = 5;
    private static final int ROUND_RADIUS = 5;
    private static final int ICON_SIZE = 11;
    private static final long ANIMATION_DURATION_MS = 300;
    private static final Path CONFIG_DIR = Paths.get(System.getenv("APPDATA"), "Some");
    private static final Path STAFF_DATA = CONFIG_DIR.resolve("staff.rfcl");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Pattern NICKNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,16}$");
    private static final Pattern NON_READABLE_PATTERN = Pattern.compile("[�\\p{Cntrl}]");

    @Expose
    private final Set<String> staffNicks = new HashSet<>();
    private float opacity = 0.0f;
    private float targetOpacity = 0.0f;
    private float currentWidth = 0.0f;
    private float currentHeight = 0.0f;
    private float targetWidth = 0.0f;
    private float targetHeight = 0.0f;
    private long animationStartTime = 0;
    private List<String> lastOnlineStaff = new ArrayList<>();

    public StaffList(String name, float initialX, float initialY, boolean canBeDragged) {
        super(name, initialX, initialY, canBeDragged);
        loadStaffList();
    }

    public void handleCommand(String command) {
        String[] parts = command.trim().split("\\s+");
        System.out.println("Команда: " + Arrays.toString(parts));
        if (parts.length == 0 || !parts[0].equals(".staff")) {
            return;
        }

        if (parts.length == 1) {
            ChatUtil.sendMessage("Использование: .staff <add | remove | list | clear | autodetect> [nick(s)]");
            return;
        }

        switch (parts[1].toLowerCase()) {
            case "add":
                if (parts.length < 3) {
                    ChatUtil.sendMessage("Использование: .staff add ник[,ник,...]");
                    return;
                }
                String[] nicksToAdd = parts[2].split(",");
                for (String nick : nicksToAdd) {
                    nick = nick.trim();
                    if (!nick.isEmpty()) {
                        if (NICKNAME_PATTERN.matcher(nick).matches()) {
                            staffNicks.add(nick);
                            ChatUtil.sendMessage("Добавлен " + nick + " в список стаффа");
                        } else {
                            ChatUtil.sendMessage("Недопустимый ник: " + nick + ". Ник должен быть 3-16 символов, только буквы, цифры или _");
                        }
                    }
                }
                saveStaffList();
                break;
            case "remove":
                if (parts.length < 3) {
                    ChatUtil.sendMessage("Использование: .staff remove ник[,ник,...]");
                    return;
                }
                String[] nicksToRemove = parts[2].split(",");
                for (String nick : nicksToRemove) {
                    nick = nick.trim();
                    if (staffNicks.remove(nick)) {
                        ChatUtil.sendMessage("Удален " + nick + " из списка стаффа");
                    } else {
                        ChatUtil.sendMessage(nick + " отсутствует в списке стаффа");
                    }
                }
                saveStaffList();
                break;
            case "list":
                if (staffNicks.isEmpty()) {
                    ChatUtil.sendMessage("Список стаффа пуст");
                } else {
                    ChatUtil.sendMessage("Стафф: " + String.join(", ", staffNicks));
                }
                savePlayerPrefixes();
                break;
            case "clear":
                staffNicks.clear();
                ChatUtil.sendMessage("Список стаффа очищен");
                break;
            case "prfsv":
                saveStaffList();
                break;
            default:
                ChatUtil.sendMessage("Неизвестная команда. Используйте: .staff <add|remove|list|clear|autodetect>");
                break;
        }
    }

    private void saveStaffList() {
        try {
            Files.createDirectories(CONFIG_DIR);
            Files.writeString(STAFF_DATA, GSON.toJson(staffNicks));
        } catch (IOException ex) {
            ex.printStackTrace();
            ChatUtil.sendMessage("Ошибка при сохранении списка стаффа");
        }
    }

    private void loadStaffList() {
        if (!Files.exists(STAFF_DATA)) {
            return;
        }
        try {
            String json = Files.readString(STAFF_DATA);
            if (json.trim().isEmpty()) {
                return;
            }
            String[] nicks = GSON.fromJson(json, String[].class);
            if (nicks != null) {
                for (String nick : nicks) {
                    if (NICKNAME_PATTERN.matcher(nick).matches()) {
                        staffNicks.add(nick);
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            ChatUtil.sendMessage("Ошибка при загрузке списка стаффа");
        }
    }

    @Override
    public float getWidth() {
        List<String> onlineStaff = getOnlineStaff();
        if (onlineStaff.isEmpty()) {
            return isChatOpen() ? BIKO_FONT.get().getWidth("No Staff Online", TEXT_HEIGHT) + 2 * PADDING : 0;
        }
        float maxNickWidth = onlineStaff.stream()
                .map(nick -> BIKO_FONT.get().getWidth(nick, TEXT_HEIGHT))
                .max(Float::compare)
                .orElse(0f);
        return ICON_SIZE + maxNickWidth + 2 * PADDING;
    }

    @Override
    public float getHeight() {
        List<String> onlineStaff = getOnlineStaff();
        if (onlineStaff.isEmpty()) {
            return isChatOpen() ? TEXT_HEIGHT + 2 * PADDING : 0;
        }
        return onlineStaff.size() * (TEXT_HEIGHT + 4) + 3;
    }

    @Override
    public void render(GuiGraphics guiGraphics) {
        if (mc.player == null || !HudElements.isOptionEnabled("StaffList") || !Api.isEnabled("Hud")) {
            targetOpacity = 0.0f;
        } else {
            List<String> onlineStaff = getOnlineStaff();
            if (onlineStaff.isEmpty() && !isChatOpen()) {
                targetOpacity = 0.0f;
            } else {
                targetOpacity = 1.0f;
            }
            if (!onlineStaff.equals(lastOnlineStaff)) {
                targetWidth = getWidth();
                targetHeight = getHeight();
                animationStartTime = System.currentTimeMillis();
                lastOnlineStaff = new ArrayList<>(onlineStaff);
            }
            long elapsed = System.currentTimeMillis() - animationStartTime;
            float t = Math.min((float) elapsed / ANIMATION_DURATION_MS, 1.0f);
            opacity = lerp(opacity, targetOpacity, t);
            currentWidth = lerp(currentWidth, targetWidth, t);
            currentHeight = lerp(currentHeight, targetHeight, t);
            if (opacity <= 0.01f || currentWidth <= 0.01f || currentHeight <= 0.01f) {
                return;
            }
            int alpha = (int) (opacity * 255);
            DrawShader.drawRoundBlur(guiGraphics.pose(), xPos, yPos, currentWidth, currentHeight, ROUND_RADIUS,
                    new Color(23, 29, 35, alpha).getRGB(), 120, 0.4f);
            float currentY = yPos + PADDING - 1;
            if (onlineStaff.isEmpty() && isChatOpen()) {
                Color textColorWithAlpha = new Color(255, 255, 255, alpha);
                BuiltText placeholderText = Builder.text()
                        .font(BIKO_FONT.get())
                        .text("No Staff Online")
                        .color(textColorWithAlpha)
                        .size(TEXT_HEIGHT)
                        .thickness(0.05f)
                        .build();
                placeholderText.render(new Matrix4f(), xPos + PADDING, currentY);
            } else {
                for (String displayName : onlineStaff) {
                    Color textColorWithAlpha = new Color(255, 255, 255, alpha);
                    BuiltText staffIcon = Builder.text()
                            .font(ICONS.get())
                            .text("I")
                            .color(ColorHelper.gradient(ThemesUtil.getCurrentStyle().getColor(1), ThemesUtil.getCurrentStyle().getColor(2), 20, 10))
                            .size(ICON_SIZE- 0.3f)
                            .thickness(0.05f)
                            .build();
                    staffIcon.render(new Matrix4f(), xPos + 0.5, currentY - 1.8f);
                    BuiltText nickText = Builder.text()
                            .font(BIKO_FONT.get())
                            .text(displayName)
                            .color(textColorWithAlpha)
                            .size(TEXT_HEIGHT)
                            .thickness(0.05f)
                            .build();
                    nickText.render(new Matrix4f(), xPos + PADDING + 9, currentY);
                    currentY += TEXT_HEIGHT + 4;
                }
            }
        }
    }
    private void savePlayerPrefixes() {
        if (mc.getConnection() == null) {
            ChatUtil.sendMessage("Не подключен к серверу");
            return;
        }
        List<String> prefixes = new ArrayList<>();
        for (PlayerInfo playerInfo : mc.getConnection().getOnlinePlayers()) {
            String nick = playerInfo.getProfile().getName();
            String prefix = playerInfo.getTeam() != null
                    ? playerInfo.getTeam().getPlayerPrefix().getString()
                    : "nothing";
            prefixes.add(nick + ": " + prefix);
        }
        try {
            Files.createDirectories(CONFIG_DIR);
            Files.write(CONFIG_DIR.resolve("prefixes.txt"), prefixes, StandardCharsets.UTF_8);
            ChatUtil.sendMessage("Префиксы игроков сохранены в " + CONFIG_DIR.resolve("prefixes.txt"));
        } catch (IOException ex) {
            ex.printStackTrace();
            ChatUtil.sendMessage("Ошибка при сохранении префиксов");
        }
    }

    private List<String> getOnlineStaff() {
        if (mc.getConnection() == null) {
            return new ArrayList<>();
        }
        List<String> onlineStaff = new ArrayList<>();
        for (PlayerInfo playerInfo : mc.getConnection().getOnlinePlayers()) {
            String nick = playerInfo.getProfile().getName();
            String displayName = playerInfo.getTeam() != null
                    ? playerInfo.getTeam().getPlayerPrefix().getString()
                    : "nothing";

            if (staffNicks.contains(nick) || check(displayName)) {
                onlineStaff.add(nick);
            }
        }
        onlineStaff.sort(String::compareToIgnoreCase);
        if (onlineStaff.isEmpty() && isChatOpen()) {
            onlineStaff.add("No Staff Online");
        }
        return onlineStaff;
    }

    public static boolean check(String name) {
        if (name == null) {
            return false;
        }
        String lowerName = name.toLowerCase();
        return lowerName.contains("helper") || lowerName.contains("moder") || lowerName.contains("admin") ||
                lowerName.contains("owner") || lowerName.contains("curator") || lowerName.contains("куратор") ||
                lowerName.contains("модер") || lowerName.contains("админ") || lowerName.contains("хелпер") ||
                lowerName.contains("поддержка") || lowerName.contains("сотрудник") || lowerName.contains("зам") ||
                lowerName.contains("стажёр") || lowerName.contains("ᴄᴛᴀжᴇᴘ") || lowerName.contains("ᴋуᴘᴀᴛᴏᴘ") || lowerName.contains("ᴄᴏᴛʀудʜиᴋ");
    }

    private float lerp(float start, float end, float t) {
        return start + (end - start) * t;
    }
}