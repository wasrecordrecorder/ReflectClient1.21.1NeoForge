package com.dsp.main.Functions.Movement;

import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.CheckBox;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.Slider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import static com.dsp.main.Api.mc;

public class ElytraRecast extends Module {
    public final CheckBox allowBroken  = new CheckBox("Allow Broken", false);

    public ElytraRecast() {
        super("ElytraRecast", 0, Category.MOVEMENT, "Automatically re-deploys the elytra when falling");
        addSettings(allowBroken);
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post e) {
        if (mc.player == null) return;

        if (!mc.player.isFallFlying()
                && checkElytra()
                && !mc.player.isFallFlying()) {

            castElytra();
        }
    }

    private boolean castElytra() {
        if (checkElytra() && check()) {
            mc.player.connection.send(
                    new ServerboundPlayerCommandPacket(
                            mc.player,
                            ServerboundPlayerCommandPacket.Action.START_FALL_FLYING
                    )
            );
            mc.player.startFallFlying();
            return true;
        }
        return false;
    }

    private boolean checkElytra() {
        if (mc.options.keyJump.isDown()
                && !mc.player.getAbilities().flying
                && mc.player.getVehicle() == null
                && !mc.player.onClimbable()) {

            ItemStack chest = mc.player.getItemBySlot(EquipmentSlot.CHEST);
            return chest.getItem() == Items.ELYTRA
                    && (ElytraItem.isFlyEnabled(chest) || allowBroken.isEnabled());
        }
        return false;
    }

    private boolean check() {
        return mc.player != null
                && mc.level != null
                && !mc.player.isCreative()
                && !mc.player.isSpectator()
                && !mc.player.hasEffect(net.minecraft.world.effect.MobEffects.LEVITATION);
    }

    @Override
    public void onDisable() {
        if (!mc.options.keyUp.isDown()) mc.options.keyUp.setDown(false);
        if (!mc.options.keyJump.isDown()) mc.options.keyJump.setDown(false);
    }
}