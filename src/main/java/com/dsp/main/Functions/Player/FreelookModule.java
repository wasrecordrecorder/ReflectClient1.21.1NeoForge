package com.dsp.main.Functions.Player;

import com.dsp.main.Core.Event.OnUpdate;
import com.dsp.main.Core.Other.FreeLook;
import com.dsp.main.Module;
import net.minecraft.client.CameraType;
import net.neoforged.bus.api.SubscribeEvent;

import static com.dsp.main.Api.mc;

public class FreelookModule extends Module {
    public FreelookModule() {
        super("FreeLook", 0, Category.PLAYER, "See all around with no rotation");
    }

    @Override
    public void onDisable() {
        super.onDisable();
        mc.options.setCameraType(CameraType.FIRST_PERSON);
        FreeLook.releaseFreeLook("FreeLook");  // ← НОВЫЙ КОД
    }
    @Override
    public void onEnable() {
        super.onEnable();
        mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        if (mc.player != null && mc.level != null) {
            FreeLook.requestFreeLook("FreeLook");  // ← НОВЫЙ КОД
        }
    }

    @SubscribeEvent
    public void ondo(OnUpdate e) {
    }
}
