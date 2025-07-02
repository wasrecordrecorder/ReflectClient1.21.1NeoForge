package com.dsp.main.Functions.Movement;

import com.dsp.main.ClickGui.Settings.Mode;
import com.dsp.main.Module;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static com.dsp.main.Api.mc;

public class AutoSprint extends Module {
    private static Mode SprintType = new Mode("Режим Спринта", "Legit", "Force");
    public AutoSprint() {
        super("Auto Sprint", 0, Category.MOVEMENT, "Make you automatically running");
        addSetting(SprintType);
    }
    @SubscribeEvent
    public void onTick(TickEvent event) {
        System.out.println(SprintType.getMode());
        if (!(mc.player == null) && mc.player.getFoodData().getFoodLevel() > 6 && SprintType.isMode("Legit")) {
            mc.options.keySprint.setDown(true);
        } else if (!(mc.player == null) && mc.player.getFoodData().getFoodLevel() > 6 && mc.player.xxa > 0 && !mc.player.isSprinting()) {
            mc.player.setSprinting(true);
        }
    }
}
