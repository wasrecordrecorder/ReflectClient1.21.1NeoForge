package com.dsp.main.UI.Draggable;

import com.dsp.main.UI.Draggable.DragElements.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class DragManager {
    public static HashMap<String, DraggableElement> draggables = new HashMap<>();

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
                Files.writeString(DRAG_DATA, GSON.toJson(draggables.values()));
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
            DraggableElement[] elements = GSON.fromJson(Files.readString(DRAG_DATA), DraggableElement[].class);
            for (DraggableElement element : elements) {
                if (element == null) continue;
                DraggableElement currentElement = draggables.get(element.getName());
                if (currentElement != null) {
                    // Update existing element
                    currentElement.setX(element.getX());
                    currentElement.setY(element.getY());
                    currentElement.setCanBeDragged(element.canBeDragged());
                } else {
                    // Add new element if not already registered
                    draggables.put(element.getName(), element);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void addDraggable(DraggableElement element) {
        draggables.put(element.getName(), element);
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
                switch (in.nextName()) {
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
                }
            }
            in.endObject();
            if (type == null) return null;
            return switch (type) {
                case "WaterMark" -> new WaterMark(name, xPos, yPos, canBeDragged);
                case "PlayerInfo" -> new PlayerInfo(name, xPos, yPos, canBeDragged);
                case "Keybinds" -> new Keybinds(name, xPos, yPos, canBeDragged);
                case "Potions" -> new Potions(name, xPos, yPos, canBeDragged);
                case "Cooldowns" -> new Cooldowns(name, xPos, yPos, canBeDragged);
                default -> null;
            };
        }
    }
}