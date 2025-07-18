package com.dsp.main.Functions.Player;

import com.dsp.main.Managers.Event.OnUpdate;
import com.dsp.main.Managers.FreeLook;
import com.dsp.main.Module;
import net.neoforged.bus.api.SubscribeEvent;

import static com.dsp.main.Api.mc;

public class FreelookModule extends Module {
    public FreelookModule() {
        super("FreeLook", 0, Category.PLAYER, "See all around with no rotation");
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (!(mc.player == null || mc.level == null) && !FreeLook.isFreeLookEnabled) FreeLook.enableFreeLook();
    }
    @Override
    public void onDisable() {
        super.onDisable();
        if (!(mc.player == null || mc.level == null) && FreeLook.isFreeLookEnabled) FreeLook.disableFreeLook();
    }

    @SubscribeEvent
    public void ondo(OnUpdate e) {
    }
}
