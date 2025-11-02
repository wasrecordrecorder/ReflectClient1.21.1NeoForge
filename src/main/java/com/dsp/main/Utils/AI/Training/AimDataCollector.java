package com.dsp.main.Utils.AI.Training;

import com.dsp.main.Utils.AI.AimPredictor;
import com.dsp.main.Utils.AI.RotationData;
import com.dsp.main.Utils.Minecraft.Chat.ChatUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector2f;
import org.joml.Vector3d;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static com.dsp.main.Api.*;

public class AimDataCollector {
    private static BufferedWriter writer;
    private static boolean isCollecting = false;
    private static Path dataFile;
    private static Vector2f lastRotation = new Vector2f(0, 0);
    private static Vector2f prevRotation = new Vector2f(0, 0);
    private static long lastAttackTime = 0;
    private static int samplesCollected = 0;
    private static RotationData previousData = null;
    private static int validSamples = 0;
    private static int invalidSamples = 0;

    public static void startCollection() {
        if (isCollecting) {
            ChatUtil.sendMessage("§cСбор данных уже запущен!");
            return;
        }

        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            dataFile = getCustomConfigDir().resolve("aim_training_" + timestamp + ".csv");

            Files.createDirectories(dataFile.getParent());
            writer = new BufferedWriter(new FileWriter(dataFile.toFile()));

            writer.write("yaw_delta,pitch_delta,target_yaw,target_pitch,since_attack,distance,on_ground,mini_hitbox,current_yaw,current_pitch,next_yaw,next_pitch\n");

            isCollecting = true;
            samplesCollected = 0;
            validSamples = 0;
            invalidSamples = 0;
            previousData = null;
            lastRotation = new Vector2f(mc.player.getYRot(), mc.player.getXRot());
            prevRotation = new Vector2f(lastRotation);
            lastAttackTime = System.currentTimeMillis();

            ChatUtil.sendMessage("§aНачат сбор данных: " + dataFile.getFileName());
            ChatUtil.sendMessage("§eБейтесь с противником, данные собираются автоматически");
        } catch (IOException e) {
            ChatUtil.sendMessage("§cОшибка создания файла: " + e.getMessage());
        }
    }

    public static void stopCollection() {
        if (!isCollecting) {
            ChatUtil.sendMessage("§cСбор данных не запущен!");
            return;
        }

        try {
            if (writer != null) {
                writer.close();
            }
            isCollecting = false;
            previousData = null;

            ChatUtil.sendMessage("§aСбор завершен!");
            ChatUtil.sendMessage("§eВалидных: " + validSamples + " | Отброшено: " + invalidSamples);
            ChatUtil.sendMessage("§eФайл: " + dataFile.getFileName());
            ChatUtil.sendMessage("§6Запустите train_model.py для обучения модели");
        } catch (IOException e) {
            ChatUtil.sendMessage("§cОшибка закрытия файла: " + e.getMessage());
        }
    }

    public static void collectData(LivingEntity target) {
        if (!isCollecting || target == null || mc.player == null || writer == null) {
            return;
        }

        try {
            Vector2f currentRot = new Vector2f(mc.player.getYRot(), mc.player.getXRot());

            Vector3d targetPos = new Vector3d(
                    target.getX(),
                    target.getY() + target.getEyeHeight() / 2.0,
                    target.getZ()
            );

            Vector2f targetRot = AimPredictor.get(targetPos);

            float yawDelta = Math.abs(Mth.wrapDegrees(currentRot.x - prevRotation.x));
            float pitchDelta = Math.abs(currentRot.y - prevRotation.y);
            float targetYaw = Mth.wrapDegrees(targetRot.x);
            float targetPitch = Mth.clamp(targetRot.y, -90, 90);

            float sinceAttackSec = Math.min((System.currentTimeMillis() - lastAttackTime) / 1000.0f, 30.0f);

            float distance = mc.player.distanceTo(target);
            int onGround = mc.player.onGround() ? 1 : 0;
            int miniHitbox = (mc.player.isFallFlying() || mc.player.isSwimming()) ? 1 : 0;
            float currentYaw = Mth.wrapDegrees(currentRot.x);
            float currentPitch = Mth.clamp(currentRot.y, -90, 90);

            if (distance < 0.1f || distance > 50.0f) {
                invalidSamples++;
                return;
            }

            if (yawDelta > 180.0f || pitchDelta > 90.0f) {
                invalidSamples++;
                return;
            }

            if (sinceAttackSec < 0.0f) {
                sinceAttackSec = 0.0f;
            }

            if (previousData != null) {
                if (isValidData(previousData)) {
                    writer.write(String.format(Locale.US, "%.4f,%.4f,%.4f,%.4f,%.2f,%.4f,%d,%d,%.4f,%.4f,%.4f,%.4f\n",
                            previousData.yawDelta,
                            previousData.pitchDelta,
                            previousData.targetYaw,
                            previousData.targetPitch,
                            previousData.sinceAttack,
                            previousData.distance,
                            (int)previousData.onGround,
                            (int)previousData.miniHitbox,
                            previousData.yaw,
                            previousData.pitch,
                            currentYaw,
                            currentPitch
                    ));
                    validSamples++;
                    samplesCollected++;
                } else {
                    invalidSamples++;
                }
            }

            previousData = new RotationData(
                    yawDelta,
                    pitchDelta,
                    targetYaw,
                    targetPitch,
                    sinceAttackSec,
                    distance,
                    onGround,
                    miniHitbox,
                    currentYaw,
                    currentPitch
            );

            prevRotation.set(lastRotation);
            lastRotation.set(currentRot);

            if (samplesCollected % 100 == 0 && samplesCollected > 0) {
                ChatUtil.sendMessage(String.format("§7Собрано: %d | Валидных: %d", samplesCollected, validSamples));
            }

        } catch (IOException e) {
            ChatUtil.sendMessage("§cОшибка записи: " + e.getMessage());
            stopCollection();
        }
    }

    private static boolean isValidData(RotationData data) {
        return data.distance > 0.1f && data.distance < 50.0f &&
                data.yawDelta >= 0 && data.yawDelta < 180 &&
                data.pitchDelta >= 0 && data.pitchDelta < 90 &&
                data.sinceAttack >= 0 && data.sinceAttack < 30;
    }

    public static void onAttack() {
        lastAttackTime = System.currentTimeMillis();
    }

    public static boolean isCollecting() {
        return isCollecting;
    }

    public static int getSamplesCollected() {
        return samplesCollected;
    }
}