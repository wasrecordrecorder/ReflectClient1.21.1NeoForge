package com.dsp.main.Functions.Player;

import com.dsp.main.Core.Event.OnUpdate;
import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.Slider;
import com.dsp.main.Utils.Minecraft.Client.InvUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.KnowledgeBookItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.BowItem;
import net.neoforged.bus.api.SubscribeEvent;

import static com.dsp.main.Api.mc;

public class AutoSoup extends Module {
    private static InvUtil invUtil = new InvUtil();
    private static Slider health = new Slider("Health", 1, 20, 8, 1);

    public AutoSoup() {
        super("Auto Soup", 0, Category.PLAYER, "Automatically using soups");
        addSettings(health);
    }

    @SubscribeEvent
    public void OnUpdate(OnUpdate event) {
        if (mc.player == null || mc.level == null) return;
        if (mc.player.getMainHandItem().getItem() == Items.BOWL) {
            mc.player.drop(true);
        }
        if ((mc.player.getHealth() + mc.player.getAbsorptionAmount()) <= health.getValueInt()) {
            for (int i = 0; i < 9; i++) {
                if (mc.player.getInventory().getItem(i).getItem() == Items.MUSHROOM_STEW) {
                    mc.player.getInventory().selected = i;
                    mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
                    mc.player.swing(InteractionHand.MAIN_HAND);
                    return;
                }
            }
            for (int i = 9; i < mc.player.getInventory().getContainerSize(); i++) {
                if (mc.player.getInventory().getItem(i).getItem() == Items.MUSHROOM_STEW) {
                    for (int hotbarSlot = 0; hotbarSlot < 9; hotbarSlot++) {
                        if (!(mc.player.getInventory().getItem(hotbarSlot).getItem() instanceof SwordItem) &&
                                !(mc.player.getInventory().getItem(hotbarSlot).getItem() instanceof BowItem) &&
                                !(mc.player.getInventory().getItem(hotbarSlot).getItem() instanceof KnowledgeBookItem)) {
                            invUtil.moveItem(i, hotbarSlot + 36);
                            mc.player.getInventory().selected = hotbarSlot;
                            mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
                            mc.player.swing(InteractionHand.MAIN_HAND);
                            return;
                        }
                    }
                }
            }
        }
    }
}