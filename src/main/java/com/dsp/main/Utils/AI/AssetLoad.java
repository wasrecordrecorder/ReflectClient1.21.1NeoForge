package com.dsp.main.Utils.AI;

import com.dsp.main.Api;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class AssetLoad {
    private static final String YAW_URL = "https://github.com/wasrecordrecorder/LittleEye/releases/download/cbm/yaw.cbm";
    private static final String PITCH_URL = "https://github.com/wasrecordrecorder/LittleEye/releases/download/cbm/pitch.cbm";

    private static Path rotationDir() {
        return Api.getCustomConfigDir().resolve("rotation");
    }

    private static Path yawPath() {
        return rotationDir().resolve("yaw.cbm");
    }

    private static Path pitchPath() {
        return rotationDir().resolve("pitch.cbm");
    }

    public static void LoadAsset() {
        try {
            Path dir = rotationDir();
            Files.createDirectories(dir);
            Path yaw = yawPath();
            if (Files.notExists(yaw)) {
                System.out.println("yaw.cbm file not found, downloading...");
                downloadFile(YAW_URL, yaw);
                System.out.println("yaw.cbm downloaded successfully");
            }
            Path pitch = pitchPath();
            if (Files.notExists(pitch)) {
                System.out.println("pitch.cbm file not found, downloading...");
                downloadFile(PITCH_URL, pitch);
                System.out.println("pitch.cbm downloaded successfully");
            }
        } catch (Exception e) {
            System.err.println("Error in LoadAsset: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void downloadFile(String urlStr, Path target) throws IOException {
        try (InputStream in = new URL(urlStr).openStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}