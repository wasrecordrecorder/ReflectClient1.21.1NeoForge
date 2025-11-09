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

    private static Path defaultModelDir() {
        return rotationDir().resolve("default");
    }

    private static Path yawPath() {
        return defaultModelDir().resolve("yaw.cbm");
    }

    private static Path pitchPath() {
        return defaultModelDir().resolve("pitch.cbm");
    }

    public static void LoadAsset() {
        try {
            Path dir = defaultModelDir();
            Files.createDirectories(dir);

            Path yaw = yawPath();
            if (Files.notExists(yaw)) {
                System.out.println("[AssetLoad] yaw.cbm not found in default/, downloading...");
                downloadFile(YAW_URL, yaw);
                System.out.println("[AssetLoad] yaw.cbm downloaded successfully");
            }

            Path pitch = pitchPath();
            if (Files.notExists(pitch)) {
                System.out.println("[AssetLoad] pitch.cbm not found in default/, downloading...");
                downloadFile(PITCH_URL, pitch);
                System.out.println("[AssetLoad] pitch.cbm downloaded successfully");
            }

            System.out.println("[AssetLoad] Default AI model ready at: " + dir);
        } catch (Exception e) {
            System.err.println("[AssetLoad] Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void downloadFile(String urlStr, Path target) throws IOException {
        try (InputStream in = new URL(urlStr).openStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}