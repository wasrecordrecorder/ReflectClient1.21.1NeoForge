package com.dsp.main.Functions.Movement;

import com.dsp.main.Core.Event.OnUpdate;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.Mode;
import com.dsp.main.Module;
import net.neoforged.bus.api.SubscribeEvent;

import static com.dsp.main.Api.mc;

public class AutoSprint extends Module {
    private static Mode SprintType = new Mode("Sprint Mode", "Legit", "Force");
    public AutoSprint() {
        super("Auto Sprint", 0, Category.MOVEMENT, "Make you automatically running");
        addSetting(SprintType);
    }
    @SubscribeEvent
    public void onTick(OnUpdate event) {
        if (!this.isEnabled()) return;
        if (!(mc.player == null) && mc.player.getFoodData().getFoodLevel() > 6 && SprintType.isMode("Legit")) {
            mc.options.keySprint.setDown(true);
        } else if (!(mc.player == null) && mc.player.getFoodData().getFoodLevel() > 6 && mc.player.zza > 0 && !mc.player.isSprinting()) {
            mc.player.setSprinting(true);
        }
    }
}
