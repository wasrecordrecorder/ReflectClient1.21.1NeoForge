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

public class FuntimeHelper extends Module {
    private static InvUtil invUtil = new InvUtil();
    public static BindCheckBox Disorent = new BindCheckBox("Disorientation", 0,() -> useItem(Items.ENDER_EYE));
    public static BindCheckBox Trapka = new BindCheckBox("Trap", 0, () -> useItem(Items.NETHERITE_SCRAP));
    public static BindCheckBox plast = new BindCheckBox("Plast", 0, () -> useItem(Items.DRIED_KELP));
    public static BindCheckBox YavnayaPil = new BindCheckBox("Obvious Dust", 0, () -> useItem(Items.SUGAR));
    public static BindCheckBox BojuaAura = new BindCheckBox("God's Aura", 0, () -> useItem(Items.PHANTOM_MEMBRANE));
    public static BindCheckBox OngeniSmerch = new BindCheckBox("The fiery tornado", 0, () -> useItem(Items.FIRE_CHARGE));

    public FuntimeHelper() {
        super("FTHelper", 0, Category.MISC, "Assist you with playing on FunTime");
        addSettings(Disorent, Trapka, plast, YavnayaPil, BojuaAura, OngeniSmerch);
    }
    protected static void useItem(Item item) {
        for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
            ItemStack CItem = mc.player.getInventory().getItem(i);
            if (!InvUtil.ValidateItem(CItem)) return;
            if (CItem.getItem() == item) {
                if (ClientSetting.slowBypass.isEnabled()) isSlowBypass = true;
                Slot slot = invUtil.getSlot(CItem.getItem());
                invUtil.findItemAndThrow(slot, mc.player.getYRot(), mc.player.getXRot());
            }
        }
    }
    @SubscribeEvent
    public void onUpdate(OnUpdate event) {

    }
}
