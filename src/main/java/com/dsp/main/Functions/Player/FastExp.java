package com.dsp.main.Functions.Player;

import com.dsp.main.Managers.Event.OnUpdate;
import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Settings.Slider;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

public class FastExp extends Module {
    public FastExp() {
        super("FastExp", 0, Category.PLAYER, "You can use exp very fast");
    }
    @SubscribeEvent
    public void dwoaod(OnUpdate event) {

    }
}
