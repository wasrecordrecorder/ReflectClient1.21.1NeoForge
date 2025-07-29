package com.dsp.main.Functions.Render;

import com.dsp.main.Core.Event.OnUpdate;
import com.dsp.main.Module;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.bus.api.SubscribeEvent;

import static com.dsp.main.Api.mc;

public class Fullbright extends Module {

    private static final int INFINITE = 1000000;

    public Fullbright() {
        super("Fullbright", 0, Category.RENDER, "A Real Fullbright (Night Vision)");
    }

    @Override
    public void onEnable() {
        giveEffect();
    }

    @Override
    public void onDisable() {
        removeEffect();
    }

    @SubscribeEvent
    public void onUpdate(OnUpdate e) {
        if (mc.player == null) return;
        if (!mc.player.hasEffect(MobEffects.NIGHT_VISION)) {
            giveEffect();
        }
    }

    private void giveEffect() {
        if (mc.player == null) return;
        mc.player.addEffect(new MobEffectInstance(
                MobEffects.NIGHT_VISION,
                INFINITE,
                0,
                false,
                false,
                false
        ));
    }

    private void removeEffect() {
        if (mc.player == null) return;
        mc.player.removeEffect(MobEffects.NIGHT_VISION);
    }
}