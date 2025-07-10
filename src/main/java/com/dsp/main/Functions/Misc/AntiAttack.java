package com.dsp.main.Functions.Misc;

import com.dsp.main.Api;
import com.dsp.main.Managers.Event.ClientPacketSendEvent;
import com.dsp.main.Managers.FrndSys.FriendManager;
import com.dsp.main.Managers.Hooks.ServerboundInteractPacketAccessor;
import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Settings.CheckBox;
import com.dsp.main.UI.ClickGui.Settings.MultiCheckBox;
import com.dsp.main.UI.ClickGui.Settings.Slider;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.npc.Villager;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.Arrays;

public class AntiAttack extends Module {
    public static MultiCheckBox Options = new MultiCheckBox("Options", Arrays.asList(
            new CheckBox("Friends", false),
            new CheckBox("Villager", false),
            new CheckBox("Zoglin", false),
            new CheckBox("Low Hp", false)
    ));
    public static Slider HpVal = new Slider("Hp", 1,20,4,1).setVisible(() -> Options.isOptionEnabled("Low Hp"));
    public AntiAttack() {
        super("Anti Attack", 0, Category.MISC, "Prevents you from attacking selected entities");
        addSettings(Options, HpVal);
    }
    @SubscribeEvent
    public void onPacketSend(ClientPacketSendEvent e) {
        if (e.getPacket() instanceof ServerboundInteractPacket pac) {
            ServerboundInteractPacketAccessor accessor = (ServerboundInteractPacketAccessor) pac;
            Entity entity = Minecraft.getInstance().level.getEntity(accessor.getEntityId());
            if (entity == null || !Api.isEnabled("Anti Attack")) return;
            if (FriendManager.isFriend(entity.getName().getString()) && Options.isOptionEnabled("Friends"))
                e.setCanceled(true);
            if (entity instanceof ZombifiedPiglin && Options.isOptionEnabled("Zoglin"))
                e.setCanceled(true);
            if (entity instanceof Villager && Options.isOptionEnabled("Villager")) {
                e.setCanceled(true);
            } else if (Options.isOptionEnabled("Low Hp") && entity instanceof LivingEntity lent) {
                if (lent.getHealth() <= HpVal.getValueInt()) {
                    e.setCanceled(true);
                }
            }
        }
    }
}
