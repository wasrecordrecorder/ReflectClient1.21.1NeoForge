package com.dsp.main.Functions.Misc;

import com.dsp.main.Managers.Event.OnUpdate;
import com.dsp.main.Module;
import net.neoforged.bus.api.SubscribeEvent;

import static com.dsp.main.Api.mc;

public class AutoRespawn extends Module {
    public AutoRespawn() {
        super("Auto Respawn", 0, Category.MISC, "Automatically respawning you after death");
    }
    @SubscribeEvent
    public void onTick(OnUpdate e) {
        if (mc.player != null && mc.level != null) {
            if (mc.player.isDeadOrDying()) {
                mc.player.respawn();
            }
        }
    }
}
