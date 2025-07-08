package com.dsp.main.UI.Draggable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class DragManager {
    public static HashMap<String, DraggableElement> draggables = new HashMap<>();

    private static final Path CONFIG_DIR = Paths.get(System.getenv("APPDATA"), "Some");
    private static final Path DRAG_DATA = CONFIG_DIR.resolve("draggables.json");
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .excludeFieldsWithoutExposeAnnotation()
            .create();

    public static void save() {
        try {
            Files.createDirectories(CONFIG_DIR);
            Files.writeString(DRAG_DATA, GSON.toJson(draggables.values()));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void init() {
        if (!Files.exists(DRAG_DATA)) {
            return;
        }
        try {
            DraggableElement[] elements = GSON.fromJson(Files.readString(DRAG_DATA), DraggableElement[].class);
            for (DraggableElement element : elements) {
                if (element == null) continue;
                DraggableElement currentElement = draggables.get(element.getName());
                if (currentElement != null) {
                    currentElement.setX(element.getX());
                    currentElement.setY(element.getY());
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void addDraggable(DraggableElement element) {
        draggables.put(element.getName(), element);
    }
}