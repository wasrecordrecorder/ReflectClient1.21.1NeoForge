package com.dsp.main.Functions.Render;

import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.Slider;
import com.dsp.main.UI.Themes.ThemesUtil;
import com.dsp.main.Utils.Engine.Particle.SinusoidEngine;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

import java.util.Random;

import static com.dsp.main.Api.mc;

public class Snow extends Module {
    private static final Slider RADIUS = new Slider("Radius", 2, 20, 8, 1);
    private static final Slider LIFETIME = new Slider("Lifetime", 100, 1000, 300, 50);
    private static final Slider MAX_PARTICLES = new Slider("Max Particles", 10, 200, 60, 5);

    private final Random random = new Random();

    public Snow() {
        super("Snow", 0, Category.RENDER, "Spawns snow particles around player");
        addSettings(RADIUS, MAX_PARTICLES, LIFETIME);
    }

    @SubscribeEvent
    public void onRenderFrame(RenderFrameEvent.Pre event) {
        if (mc.player == null || mc.level == null) return;
        int max = (int) MAX_PARTICLES.getValue();
        if (SinusoidEngine.getParticles().size() >= max) return;
        float radius = (float) RADIUS.getValue();
        Vec3 pPos = mc.player.position();
        double x = pPos.x + (random.nextDouble() - 0.5) * radius * 2;
        double y = pPos.y + random.nextDouble() * radius;
        double z = pPos.z + (random.nextDouble() - 0.5) * radius * 2;
        Vec3 pos = new Vec3(x, y, z);
        SinusoidEngine.spawnParticle(pos, LIFETIME.getValueInt(),0.4f,
                new Vec3(
                        ((ThemesUtil.getCurrentStyle().getColor(1) >> 16) & 0xFF) / 255.0,
                        ((ThemesUtil.getCurrentStyle().getColor(2) >> 8) & 0xFF) / 255.0,
                        ((ThemesUtil.getCurrentStyle().getColor(1)) & 0xFF) / 255.0
                ), true
        );
    }

    @Override
    public void onDisable() {
        super.onDisable();
        SinusoidEngine.clear();
    }
}