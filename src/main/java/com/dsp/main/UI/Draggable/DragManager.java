package com.dsp.main.UI.Draggable;

import com.dsp.main.UI.Draggable.DragElements.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.gui.GuiGraphics;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import static com.dsp.main.Functions.Render.HudElement.snapGride;

public class DragManager {
    public static HashMap<String, DraggableElement> draggables = new HashMap<>();
    private static final float GRID_SIZE = 20.0f;
    private static final float SNAP_THRESHOLD = 5.0f;
    private static final int GRID_COLOR = 0xCCFFFFFF;

    private static final Path CONFIG_DIR = Paths.get(System.getenv("APPDATA"), "Some");
    private static final Path DRAG_DATA = CONFIG_DIR.resolve("dragg.rfcl");
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .excludeFieldsWithoutExposeAnnotation()
            .registerTypeAdapter(DraggableElement.class, new DraggableElementTypeAdapter())
            .create();

    public static void save() {
        try {
            Files.createDirectories(CONFIG_DIR);
            if (!draggables.isEmpty()) {
                String json = GSON.toJson(draggables.values());
                Files.writeString(DRAG_DATA, json);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void init() {
        if (!Files.exists(DRAG_DATA)) {
            try {
                Files.createDirectories(CONFIG_DIR);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return;
        }
        try {
            String jsonContent = Files.readString(DRAG_DATA);
            if (jsonContent.trim().isEmpty()) {
                return;
            }
            DraggableElement[] elements = GSON.fromJson(jsonContent, DraggableElement[].class);
            if (elements == null || elements.length == 0) {
                return;
            }
            for (DraggableElement element : elements) {
                if (element == null) {
                    continue;
                }
                DraggableElement currentElement = draggables.get(element.getName());
                if (currentElement != null) {
                    currentElement.setX(element.getX());
                    currentElement.setY(element.getY());
                    currentElement.setCanBeDragged(element.canBeDragged());
                } else {
                    draggables.put(element.getName(), element);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void addDraggable(DraggableElement element) {
        draggables.put(element.getName(), element);
    }

    public static boolean isAnyDragging() {
        for (DraggableElement element : draggables.values()) {
            if (element.isDragging() && element.isChatOpen()) {
                return true;
            }
        }
        return false;
    }

    public static void renderGrid(GuiGraphics guiGraphics, Window window) {
        if (!isAnyDragging() || !snapGride.isEnabled()) {
            return;
        }

        int width = window.getGuiScaledWidth();
        int height = window.getGuiScaledHeight();
        for (float x = 0; x <= width; x += GRID_SIZE) {
            guiGraphics.vLine((int)x, 0, height, GRID_COLOR);
        }
        for (float y = 0; y <= height; y += GRID_SIZE) {
            guiGraphics.hLine(0, width, (int)y, GRID_COLOR);
        }
    }

    public static float[] snapToGrid(float x, float y, float width, float height, float maxX, float maxY) {
        if (!snapGride.isEnabled()) {
            return new float[]{x, y};
        }
        float snappedX = x;
        float snappedY = y;
        float leftEdge = x;
        float rightEdge = x + width;
        float centerX = x + width / 2;
        float nearestGridXLeft = Math.round(leftEdge / GRID_SIZE) * GRID_SIZE;
        float nearestGridXRight = Math.round(rightEdge / GRID_SIZE) * GRID_SIZE;
        float nearestGridXCenter = Math.round(centerX / GRID_SIZE) * GRID_SIZE;
        float distLeft = Math.abs(leftEdge - nearestGridXLeft);
        float distRight = Math.abs(rightEdge - nearestGridXRight);
        float distCenterX = Math.abs(centerX - nearestGridXCenter);

        if (distLeft <= SNAP_THRESHOLD && distLeft <= distRight && distLeft <= distCenterX) {
            snappedX = nearestGridXLeft;
        } else if (distRight <= SNAP_THRESHOLD && distRight <= distCenterX) {
            snappedX = nearestGridXRight - width;
        } else if (distCenterX <= SNAP_THRESHOLD) {
            snappedX = nearestGridXCenter - width / 2;
        }
        float topEdge = y;
        float bottomEdge = y + height;
        float centerY = y + height / 2;
        float nearestGridYTop = Math.round(topEdge / GRID_SIZE) * GRID_SIZE;
        float nearestGridYBottom = Math.round(bottomEdge / GRID_SIZE) * GRID_SIZE;
        float nearestGridYCenter = Math.round(centerY / GRID_SIZE) * GRID_SIZE;
        float distTop = Math.abs(topEdge - nearestGridYTop);
        float distBottom = Math.abs(bottomEdge - nearestGridYBottom);
        float distCenterY = Math.abs(centerY - nearestGridYCenter);

        if (distTop <= SNAP_THRESHOLD && distTop <= distBottom && distTop <= distCenterY) {
            snappedY = nearestGridYTop;
        } else if (distBottom <= SNAP_THRESHOLD && distBottom <= distCenterY) {
            snappedY = nearestGridYBottom - height;
        } else if (distCenterY <= SNAP_THRESHOLD) {
            snappedY = nearestGridYCenter - height / 2;
        }

        return new float[]{snappedX, snappedY};
    }

    private static class DraggableElementTypeAdapter extends TypeAdapter<DraggableElement> {
        @Override
        public void write(JsonWriter out, DraggableElement value) throws IOException {
            out.beginObject();
            out.name("type").value(value.getClass().getSimpleName());
            out.name("name").value(value.getName());
            out.name("xPos").value(value.getX());
            out.name("yPos").value(value.getY());
            out.name("canBeDragged").value(value.canBeDragged());
            out.endObject();
        }

        @Override
        public DraggableElement read(JsonReader in) throws IOException {
            in.beginObject();
            String type = null;
            String name = null;
            float xPos = 0, yPos = 0;
            boolean canBeDragged = true;

            while (in.hasNext()) {
                String fieldName = in.nextName();
                switch (fieldName) {
                    case "type":
                        type = in.nextString();
                        break;
                    case "name":
                        name = in.nextString();
                        break;
                    case "xPos":
                        xPos = (float) in.nextDouble();
                        break;
                    case "yPos":
                        yPos = (float) in.nextDouble();
                        break;
                    case "canBeDragged":
                        canBeDragged = in.nextBoolean();
                        break;
                    default:
                        in.skipValue();
                        break;
                }
            }
            in.endObject();
            if (name == null) {
                return null;
            }
            if (type == null) {
                type = name;
            }
            return switch (type) {
                case "WaterMark" -> new WaterMark(name, xPos, yPos, canBeDragged);
                case "PlayerInfo" -> new PlayerInfo(name, xPos, yPos, canBeDragged);
                case "Keybinds" -> new Keybinds(name, xPos, yPos, canBeDragged);
                case "Potions" -> new Potions(name, xPos, yPos, canBeDragged);
                case "Cooldowns" -> new Cooldowns(name, xPos, yPos, canBeDragged);
                case "TargetHud" -> new TargetHud(name, xPos, yPos, canBeDragged);
                case "StaffList" -> new StaffList(name, xPos, yPos, canBeDragged);
                default -> null;
            };
        }
    }
}