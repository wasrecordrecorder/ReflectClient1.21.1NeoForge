package com.dsp.main.Functions.Combat;

import com.dsp.main.Module;
import com.dsp.main.Utils.AI.Training.AimDataCollector;
import com.dsp.main.Utils.Minecraft.Chat.ChatUtil;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import static com.dsp.main.Api.mc;

public class AimTrainer extends Module {
    private LivingEntity lastTarget;

    public AimTrainer() {
        super("AimTrainer", 0, Category.COMBAT, "Collects data for AI training");
    }

    @Override
    public void onEnable() {
        super.onEnable();
        AimDataCollector.startCollection();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        AimDataCollector.stopCollection();
        ChatUtil.sendMessage("§aСобрано " + AimDataCollector.getSamplesCollected() + " образцов");
    }

    @SubscribeEvent
    public void onTick(PlayerTickEvent.Post event) {
        if (mc.player == null || mc.level == null) return;

        LivingEntity target = findTarget();

        if (target != null) {
            AimDataCollector.collectData(target);
            lastTarget = target;
        }
    }

    private LivingEntity findTarget() {
        if (mc.crosshairPickEntity instanceof LivingEntity living) {
            return living;
        }

        if (lastTarget != null && !lastTarget.isRemoved() && mc.player.distanceTo(lastTarget) < 6.0f) {
            return lastTarget;
        }

        return mc.level.getEntitiesOfClass(LivingEntity.class,
                        mc.player.getBoundingBox().inflate(6.0),
                        e -> e != mc.player && e.isAlive() && !e.isSpectator())
                .stream()
                .min((e1, e2) -> Float.compare(mc.player.distanceTo(e1), mc.player.distanceTo(e2)))
                .orElse(null);
    }
}