package com.dsp.main.Functions.Misc;

import com.dsp.main.Core.Event.OnUpdate;
import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.BindCheckBox;
import com.dsp.main.Utils.Minecraft.Client.InvUtil;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;

import static com.dsp.main.Api.isSlowBypass;
import static com.dsp.main.Api.mc;

public class HolyworldHelper extends Module {
    private static InvUtil invUtil = new InvUtil();
    public static BindCheckBox Disorent = new BindCheckBox("Explosive stuff", 0,() -> useItem(Items.FIRE_CHARGE));
    public static BindCheckBox Trapka = new BindCheckBox("Explosive trap", 0, () -> useItem(Items.PRISMARINE_SHARD));
    public static BindCheckBox plast = new BindCheckBox("The farewell hum", 0, () -> useItem(Items.FIREWORK_STAR));
    public static BindCheckBox YavnayaPil = new BindCheckBox("Stun", 0, () -> useItem(Items.NETHER_STAR));
    public HolyworldHelper() {
        super("HolyWorldHelper", 0, Category.MISC, "Assist you with playing on HolyWorld!");
        addSettings(Disorent, Trapka, plast, YavnayaPil);
    }
    protected static void useItem(Item item) {
        for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
            ItemStack CItem = mc.player.getInventory().getItem(i);
            if (!ValidateItemHw(CItem)) return;
            if (CItem.getItem() == item) {
                if (ClientSetting.slowBypass.isEnabled()) isSlowBypass = true;
                Slot slot = invUtil.getSlot(CItem.getItem());
                invUtil.findItemAndThrow(slot, mc.player.getYRot(), mc.player.getXRot());
            }
        }
    }
    private static boolean ValidateItemHw(ItemStack item) {
        if (item.getItem() == Items.FIRE_CHARGE) return item.getHoverName().getString().toLowerCase().contains("взрывная штучка");
        if (item.getItem() == Items.PRISMARINE_SHARD) return item.getHoverName().getString().toLowerCase().contains("взрывная трапка");
        if (item.getItem() == Items.FIREWORK_STAR) return item.getHoverName().getString().toLowerCase().contains("прощальный гул");
        if (item.getItem() == Items.NETHER_STAR) return item.getHoverName().getString().toLowerCase().contains("стан");
        return true;
    }
    @SubscribeEvent
    public void onUpdate(OnUpdate eve) {

    }
}
