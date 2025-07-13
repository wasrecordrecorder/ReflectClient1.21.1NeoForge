package com.dsp.main.UI.Notifications;

import com.dsp.main.Api;
import com.dsp.main.Utils.Minecraft.Chat.ChatUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static com.dsp.main.Functions.Render.Notifications.renderPos;

public class NotificationManager {
    private static final int MAX_ACTIVE = 5;
    private static final int SPACING = 4;
    private final List<Notification> active = new ArrayList<>();
    private final Queue<Notification> queue = new LinkedList<>();

    public void send(Notification.Type type, String text) {
        if (!Api.isEnabled("Notifications")) return;
        Notification notification = new Notification(type, text);
        if (active.size() < MAX_ACTIVE) {
            addToActive(notification);
        } else {
            queue.add(notification);
        }
    }

    private void addToActive(Notification notification) {
        notification.activate(); // Активируем уведомление, устанавливая creationTime
        active.add(0, notification);
        updatePositions();
    }

    private void updatePositions() {
        int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        int y;

        if (renderPos.isMode("Top of screen")) {
            y = 30;
            for (Notification notif : active) {
                notif.setTargetY(y);
                y += notif.getHeight() + SPACING;
            }
        } else if (renderPos.isMode("Center of screen")) {
            y = (screenHeight / 2)  + 30;
            for (Notification notif : active) {
                notif.setTargetY(y);
                y += notif.getHeight() + SPACING;
            }
        } else if (renderPos.isMode("Right corner")) {
            y = 30;
            for (Notification notif : active) {
                notif.setTargetY(y);
                y += notif.getHeight() + SPACING;
            }
        } else {
            y = 30;
            for (Notification notif : active) {
                notif.setTargetY(y);
                y += notif.getHeight() + SPACING;
            }
        }
    }

    public void render(GuiGraphics guiGraphics) {
        long currentTime = System.currentTimeMillis();
        active.removeIf(notif -> {
            if (currentTime - notif.getCreationTime() >= 2000 && !notif.isDisappearing()) {
                notif.startDisappearing();
                return false;
            }
            return notif.isDisappeared();
        });

        while (active.size() < MAX_ACTIVE && !queue.isEmpty()) {
            addToActive(queue.poll());
        }

        int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        for (Notification notif : active) {
            notif.update();
            int x;
            if (renderPos.isMode("Center of screen")) {
                x = (screenWidth - notif.getWidth()) / 2;
            } else if (renderPos.isMode("Right corner")) {
                x = screenWidth - notif.getWidth() - 5;
            } else {
                x = (screenWidth - notif.getWidth()) / 2;
            }
            notif.render(guiGraphics, x, notif.currentY);
        }
    }
}