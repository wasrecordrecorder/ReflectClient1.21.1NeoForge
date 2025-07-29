package com.dsp.main.Functions.Player;

import com.dsp.main.Core.Event.OnUpdate;
import com.dsp.main.Module;
import net.neoforged.bus.api.SubscribeEvent;

public class FastExp extends Module {
    public FastExp() {
        super("FastExp", 0, Category.PLAYER, "You can use exp very fast");
    }
    @SubscribeEvent
    public void dwoaod(OnUpdate event) {

    }
}
