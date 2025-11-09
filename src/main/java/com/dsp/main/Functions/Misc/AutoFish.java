package com.dsp.main.Functions.Misc;

import com.dsp.main.Core.Event.OnUpdate;
import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Dropdown.ClickGuiScreen;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.CheckBox;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.Slider;
import com.dsp.main.Utils.Minecraft.Client.InvUtil;
import com.dsp.main.Utils.TimerUtil;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;

import static com.dsp.main.Api.mc;

public class AutoFish extends Module {

    private final CheckBox rodSave = new CheckBox("Rod Save", true);
    private final CheckBox changeRod = new CheckBox("Change Rod", false);
    private final Slider reelDelay = new Slider("Reel Delay", 0, 500, 150, 10);
    private final Slider castDelay = new Slider("Cast Delay", 100, 1500, 500, 50);

    private static final TimerUtil reelTimer = new TimerUtil();
    private final TimerUtil castTimer = new TimerUtil();
    private final InvUtil invUtil = new InvUtil();

    private static boolean waitingToReel = false;
    private static boolean waitingToCast = false;

    public AutoFish() {
        super("AutoFish", 0, Category.MISC, "Automatically catches fish");
        addSettings(rodSave, changeRod, reelDelay, castDelay);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (mc.player == null || mc.level == null) {
            toggle();
            return;
        }

        waitingToReel = false;
        waitingToCast = false;
        reelTimer.reset();
        castTimer.reset();

        if (!equipRod()) {
            toggle();
            return;
        }


        if (mc.player.fishing == null) {
            castTimer.reset();
            waitingToCast = true;
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        waitingToReel = false;
        waitingToCast = false;
    }

    @SubscribeEvent
    public void onUpdate(OnUpdate e) {
        if (mc.player == null || mc.level == null) return;
        if (!isHoldingRod()) {
            if (!equipRod()) {
                toggle();
                return;
            }
        }

        if (waitingToReel && reelTimer.hasReached((long) reelDelay.getValue())) {
            waitingToReel = false;
            useRod();

            if (changeRod.isEnabled()) {
                equipRod();
            }

            castTimer.reset();
            waitingToCast = true;
        }

        if (waitingToCast && castTimer.hasReached((long) castDelay.getValue())) {
            if (mc.player.fishing == null) {
                if (rodSave.isEnabled() && getCurrentRodDurability() < 10) {
                    if (!changeRod.isEnabled()) {
                        toggle();
                        return;
                    }
                    if (!equipRod()) {
                        toggle();
                        return;
                    }
                }

                waitingToCast = false;
                useRod();
            }
        }
    }

    public static void handleSoundPacket(ClientboundSoundPacket packet) {
        if (mc.player == null || mc.player.fishing == null) return;
        if (packet.getSound().value().equals(SoundEvents.FISHING_BOBBER_SPLASH)) {
            double distance = mc.player.fishing.distanceToSqr(packet.getX(), packet.getY(), packet.getZ());

            if (distance < 25.0) {
                reelTimer.reset();
                waitingToReel = true;
                waitingToCast = false;
            }
        }
    }

    private boolean equipRod() {
        if (isHoldingRod()) return true;

        int bestRodSlot = -1;
        int bestDurability = 0;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.getItem() == Items.FISHING_ROD) {
                int durability = stack.getMaxDamage() - stack.getDamageValue();

                if (!rodSave.isEnabled() || durability >= 10) {
                    if (durability > bestDurability) {
                        bestDurability = durability;
                        bestRodSlot = i;
                    }
                }
            }
        }

        if (bestRodSlot == -1) {
            int invRodSlot = invUtil.getSlotInInventory(Items.FISHING_ROD);
            if (invRodSlot != -1) {
                int hotbarSlot = findEmptyHotbarSlot();
                if (hotbarSlot != -1) {
                    invUtil.moveItem(invRodSlot, hotbarSlot);
                    mc.player.getInventory().selected = hotbarSlot;
                    return true;
                } else {
                    invUtil.moveItem(invRodSlot, 0);
                    mc.player.getInventory().selected = 0;
                    return true;
                }
            }
            return false;
        }

        mc.player.getInventory().selected = bestRodSlot;
        return true;
    }

    private int findEmptyHotbarSlot() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getItem(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    private void useRod() {
        if (mc.player == null || mc.gameMode == null) return;
        //mc.getConnection().send(new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, 1, mc.player.getYRot(), mc.player.getXRot()));
        mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
        mc.player.swing(InteractionHand.MAIN_HAND);
    }

    private boolean isHoldingRod() {
        if (mc.player == null) return false;
        return mc.player.getMainHandItem().getItem() instanceof FishingRodItem;
    }

    private int getCurrentRodDurability() {
        if (mc.player == null) return 0;
        ItemStack rod = mc.player.getMainHandItem();
        if (!(rod.getItem() instanceof FishingRodItem)) return 0;
        return rod.getMaxDamage() - rod.getDamageValue();
    }
}