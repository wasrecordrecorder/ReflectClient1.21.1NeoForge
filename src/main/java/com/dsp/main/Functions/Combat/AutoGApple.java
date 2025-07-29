package com.dsp.main.Functions.Combat;

import com.dsp.main.Core.Event.OnUpdate;
import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.Slider;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;

import static com.dsp.main.Api.mc;

public class AutoGApple extends Module {
    private static Slider TargetHp = new Slider("Health", 1, 20, 14, 1);
    public AutoGApple() {
        super("AutoGApple", 0, Category.COMBAT, "Automatically eating Golden apples");
        addSetting(TargetHp);
    }
    @SubscribeEvent
    public void onUpdate(OnUpdate e) {
        if (mc.player == null) return;
        if (mc.player.getHealth() <= TargetHp.getValueInt()) {
            if (!mc.player.isUsingItem() && mc.player.getOffhandItem().getItem() == Items.GOLDEN_APPLE) {
                mc.options.keyUse.setDown(true);
            }
        }
    }
}
