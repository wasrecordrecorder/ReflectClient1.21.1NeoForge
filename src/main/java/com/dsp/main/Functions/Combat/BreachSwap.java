package com.dsp.main.Functions.Combat;

import com.dsp.main.Core.Event.OnUpdate;
import com.dsp.main.Module;
import com.dsp.main.Utils.Minecraft.Client.InvUtil;
import com.dsp.main.Utils.TimerUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.SwordItem;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;

import static com.dsp.main.Api.isSlowBypass;
import static com.dsp.main.Api.mc;
import static com.dsp.main.Functions.Misc.ClientSetting.slowBypass;

public class BreachSwap extends Module {
    public static InvUtil invUtilForBreach = new InvUtil();

    public BreachSwap() {
        super("BreachSwap", 0, Category.COMBAT,
                "If sword in hand before hit: swap to mace for the strike, then return to sword");
    }
    public static boolean hasEverything() {
        if (mc.player == null) return false;
        return findMaceSlot(mc.player) != -1 && findSwordSlot(mc.player) != -1;
    }


    private static int findMaceSlot(Player p) {
        for (int i = 0; i < p.getInventory().getContainerSize(); i++) {
            ItemStack s = p.getInventory().getItem(i);
            if (!s.isEmpty() && s.getItem() instanceof MaceItem) return i;
        }
        return -1;
    }
    private static int findSwordSlot(Player p) {
        for (int i = 0; i < p.getInventory().getContainerSize(); i++) {
            ItemStack s = p.getInventory().getItem(i);
            if (!s.isEmpty() && s.getItem() instanceof SwordItem) return i;
        }
        return -1;
    }
    @SubscribeEvent
    public void OnUpdate(OnUpdate e) {

    }
}