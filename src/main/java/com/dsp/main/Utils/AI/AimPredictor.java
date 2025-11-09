package com.dsp.main.Utils.AI;

import ai.catboost.CatBoostModel;
import ai.catboost.CatBoostPredictions;
import com.dsp.main.Api;
import com.dsp.main.Utils.Minecraft.Chat.ChatUtil;
import com.dsp.main.Utils.Minecraft.Client.ClientFallDistance;
import com.dsp.main.Utils.Render.AnimFromRockstarClient.RotationAnimation;
import com.dsp.main.Utils.Render.Other.Vec2Vector;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector2f;
import org.joml.Vector3d;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.dsp.main.Api.*;

public final class AimPredictor {
    public static RotationAnimation interp = new RotationAnimation();
    private static CatBoostModel yawModel;
    private static CatBoostModel pitchModel;

    private static String currentModelName = "default";
    private static List<String> availableModels = new ArrayList<>();

    private static boolean modelsAvailable = false;
    private static boolean checkedAvailability = false;
    private static boolean loadAttempted = false;

    private static Path rotationDir() {
        return Api.getCustomConfigDir().resolve("rotation");
    }

    private static Path getModelDir(String modelName) {
        return rotationDir().resolve(modelName);
    }

    private static boolean isCatBoostAvailable() {
        if (!checkedAvailability) {
            try {
                Class.forName("ai.catboost.CatBoostModel");
                modelsAvailable = true;
            } catch (ClassNotFoundException e) {
                modelsAvailable = false;
                ChatUtil.sendMessage("§c[AimPredictor] CatBoost недоступен");
            }
            checkedAvailability = true;
        }
        return modelsAvailable;
    }

    public static List<String> scanAvailableModels() {
        List<String> models = new ArrayList<>();

        try {
            Path rotDir = rotationDir();
            if (!Files.exists(rotDir)) {
                Files.createDirectories(rotDir);
                return models;
            }

            File[] folders = rotDir.toFile().listFiles(File::isDirectory);
            if (folders == null) return models;

            for (File folder : folders) {
                String modelName = folder.getName();
                Path yawPath = folder.toPath().resolve("yaw.cbm");
                Path pitchPath = folder.toPath().resolve("pitch.cbm");

                if (Files.exists(yawPath) && Files.exists(pitchPath)) {
                    models.add(modelName);
                }
            }

            if (models.isEmpty()) {
                models.add("default");
            }

        } catch (Exception e) {
            e.printStackTrace();
            ChatUtil.sendMessage("§c[AimPredictor] Ошибка сканирования моделей");
        }

        return models;
    }

    public static void rescanModels() {
        availableModels = scanAvailableModels();
        ChatUtil.sendMessage("§a[AimPredictor] Найдено моделей: " + availableModels.size());
        for (String model : availableModels) {
            ChatUtil.sendMessage("§7- " + model);
        }
    }

    public static boolean loadModel(String modelName) {
        if (!isCatBoostAvailable()) {
            return false;
        }

        try {
            Path modelDir = getModelDir(modelName);
            Path yawPath = modelDir.resolve("yaw.cbm");
            Path pitchPath = modelDir.resolve("pitch.cbm");

            if (!Files.exists(yawPath) || !Files.exists(pitchPath)) {
                ChatUtil.sendMessage("§c[AimPredictor] Модель '" + modelName + "' не найдена");

                if (!modelName.equals("default")) {
                    ChatUtil.sendMessage("§e[AimPredictor] Попытка загрузить default...");
                    return loadModel("default");
                }
                return false;
            }

            if (yawModel != null) {
                yawModel = null;
            }
            if (pitchModel != null) {
                pitchModel = null;
            }

            yawModel = CatBoostModel.loadModel(yawPath.toString());
            pitchModel = CatBoostModel.loadModel(pitchPath.toString());

            currentModelName = modelName;
            modelsAvailable = true;

            ChatUtil.sendMessage("§a[AimPredictor] Модель '" + modelName + "' загружена");

            return true;

        } catch (Exception e) {
            modelsAvailable = false;
            ChatUtil.sendMessage("§c[AimPredictor] Ошибка загрузки '" + modelName + "': " + e.getMessage());

            if (!modelName.equals("default")) {
                return loadModel("default");
            }
            return false;
        }
    }

    public static void reloadModel(String newModelName) {
        if (newModelName == null || newModelName.isEmpty()) {
            ChatUtil.sendMessage("§c[AimPredictor] Неверное имя модели");
            return;
        }

        if (newModelName.equals(currentModelName) && modelsAvailable) {
            return;
        }

        ChatUtil.sendMessage("§e[AimPredictor] Переключение на '" + newModelName + "'...");

        yawModel = null;
        pitchModel = null;
        modelsAvailable = false;

        loadModel(newModelName);
    }

    public static void load() {
        if (loadAttempted) {
            return;
        }

        loadAttempted = true;

        availableModels = scanAvailableModels();

        if (availableModels.isEmpty()) {
            ChatUtil.sendMessage("§e[AimPredictor] Модели не найдены. AI prediction отключен");
            modelsAvailable = false;
            return;
        }

        String modelToLoad = availableModels.contains("default") ? "default" : availableModels.get(0);
        loadModel(modelToLoad);
    }

    public static boolean isAiAvailable() {
        if (!loadAttempted) {
            load();
        }
        return modelsAvailable && yawModel != null && pitchModel != null;
    }

    public static String getCurrentModelName() {
        return currentModelName;
    }

    public static String[] getAvailableModelNames() {
        if (availableModels.isEmpty()) {
            availableModels = scanAvailableModels();
        }
        return availableModels.toArray(new String[0]);
    }

    public static float[] predict(float[] features) {
        if (!isAiAvailable()) {
            return null;
        }

        try {
            String[] categoricals = {
                    String.valueOf(mc.player.onGround()),
                    String.valueOf(ClientFallDistance.get() > 0)
            };

            CatBoostPredictions predYaw = yawModel.predict(features, categoricals);
            float yaw = (float) predYaw.get(0, 0);
            CatBoostPredictions predPitch = pitchModel.predict(features, categoricals);
            float pitch = (float) predPitch.get(0, 0);
            return new float[]{yaw, pitch};
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Vector2f predict(LivingEntity target, Vector2f current, Vector2f prev, Vector2f speed) {
        if (target == null || mc.player == null) {
            return current;
        }

        if (!isAiAvailable()) {
            return null;
        }

        try {
            Vector3d targetPos = Vec2Vector.convert2(target.getPosition(partialTickAp));
            float yawDiff = Mth.wrapDegrees(get(targetPos).x + 5);
            float pitchDiff = Mth.clamp(get(targetPos).y, -90, 90);

            RotationData data = new RotationData(
                    Math.abs(current.x - prev.x),
                    Math.abs(current.y - prev.y),
                    0,
                    pitchDiff,
                    2000,
                    (float) mc.player.distanceTo(target),
                    mc.player.onGround() ? 1 : 0,
                    mc.player.isFallFlying() || mc.player.isSwimming() ? 1 : 0,
                    Mth.wrapDegrees(current.x) - yawDiff,
                    current.y
            );

            float[] features = getCurrentFeatures(data);

            String[] categoricals = {
                    String.valueOf(data.onGround),
                    String.valueOf(data.miniHitbox)
            };

            CatBoostPredictions predYaw = yawModel.predict(features, categoricals);
            CatBoostPredictions predPitch = pitchModel.predict(features, categoricals);

            float rawYaw = (float) predYaw.get(0, 0);
            float rawPitch = (float) predPitch.get(0, 0);

            float shortestYawPath = (float) (((((rawYaw + yawDiff - interp.getYaw()) % 360) + 540) % 360) - 180);
            float targetYaw = interp.getYaw() + shortestYawPath;
            float targetPitch = rawPitch;

            if (mc.player.getY() > target.getY() + target.getBbHeight()) {
                targetPitch = get(Vec2Vector.convert2(target.getPosition(partialTickAp))
                        .add(0, target.getBbHeight(), 0)).y;
            }

            if (mc.player.getY() + 1 < target.getY()) {
                targetPitch = get(Vec2Vector.convert2(target.getPosition(partialTickAp))
                        .add(0, 0.5f, 0)).y;
            }

            if (mc.player.isSwimming()) {
                targetPitch = get(Vec2Vector.convert2(target.getPosition(partialTickAp))
                        .add(0, target.getBbHeight() / 2F, 0)).y;
                speed = new Vector2f(random(350, 200), random(350, 200));
            }

            interp.animate(new Vector2f(targetYaw, targetPitch), (int) speed.x, (int) speed.y);
            Vector2f rot = correctRotation(interp.getYaw(), interp.getPitch());
            rot.y = Mth.clamp(rot.y, -90, 90);
            return rot;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static float[] getCurrentFeatures(RotationData data) {
        return new float[]{
                data.yawDelta,
                data.pitchDelta,
                data.targetYaw,
                data.targetPitch,
                data.sinceAttack,
                data.distance,
                data.onGround,
                data.miniHitbox
        };
    }

    public static Vector2f get(Vector3d from, Vector3d target) {
        double posX = target.x() - from.x();
        double posY = target.y() - from.y();
        double posZ = target.z() - from.z();
        double sqrt = Mth.sqrt((float) (posX * posX + posZ * posZ));
        float yaw = (float) (Math.atan2(posZ, posX) * 180.0 / Math.PI) - 90.0f;
        float pitch = (float) (-(Math.atan2(posY, sqrt) * 180.0 / Math.PI));
        float sens = (float) (Math.pow(mc.options.sensitivity().get(), 1.5) * 0.05f + 0.1f);
        float pow = sens * sens * sens * 1.2F;
        yaw -= yaw % pow;
        pitch -= pitch % (pow * sens);
        return new Vector2f(yaw, pitch);
    }

    public static Vector2f get(Vector3d target) {
        Vector3d vec = target;
        double posX = vec.x() - mc.player.getX();
        double posY = vec.y() - (mc.player.getY() + (double) mc.player.getEyeHeight());
        double posZ = vec.z() - mc.player.getZ();
        double sqrt = Mth.sqrt((float) (posX * posX + posZ * posZ));
        float yaw = (float) (Math.atan2(posZ, posX) * 180.0 / Math.PI) - 90.0f;
        float pitch = (float) (-(Math.atan2(posY, sqrt) * 180.0 / Math.PI));
        float sens = (float) (Math.pow(mc.options.sensitivity().get(), 1.5) * 0.05f + 0.1f);
        float pow = sens * sens * sens * 1.2F;
        yaw -= yaw % pow;
        pitch -= pitch % (pow * sens);
        return new Vector2f(yaw, pitch);
    }

    public static float random(double min, double max) {
        return (float) (min + (max - min) * Math.random());
    }

    public static float getGCDValue() {
        double realGcd = mc.options.sensitivity().get();
        double d4 = realGcd * (double) 0.6F + (double) 0.2F;
        return (float) (d4 * d4 * d4 * 8.0D * 0.15);
    }

    public static Vector2f correctRotation(float yaw, float pitch) {
        float gcd = getGCDValue();
        yaw -= yaw % gcd;
        pitch -= pitch % gcd;
        return new Vector2f(yaw, pitch);
    }
}