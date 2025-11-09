package com.dsp.main.Functions.Combat;

import com.dsp.main.Core.Event.ClientPacketSendEvent;
import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.Slider;
import com.dsp.main.Utils.Minecraft.Chat.ChatUtil;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;

import static com.dsp.main.Api.mc;

public class SuperBow extends Module {

    private final Slider power = new Slider("Power", 10, 50, 20, 1);

    public SuperBow() {
        super("SuperBow", 0, Category.COMBAT, "Increases bow power");
        addSettings(power);
    }

    @SubscribeEvent
    public void onPacketSend(ClientPacketSendEvent event) {
        if (mc.player == null) return;

        if (event.getPacket() instanceof ServerboundPlayerActionPacket packet) {
            if (packet.getAction() == ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM &&
                    mc.player.getUseItem().getItem() == Items.BOW) {

                mc.player.connection.send(new ServerboundPlayerCommandPacket(
                        mc.player,
                        ServerboundPlayerCommandPacket.Action.START_SPRINTING
                ));

                for (int i = 0; i < power.getValueInt(); i++) {
                    sendPosRot(mc.player.getY() + 1e-10, false);
                    sendPosRot(mc.player.getY() - 1e-10, true);
                }
            }
        }
    }

    private void sendPosRot(double y, boolean onGround) {
        if (mc.player == null) return;

        mc.player.connection.send(new ServerboundMovePlayerPacket.PosRot(
                mc.player.getX(),
                y,
                mc.player.getZ(),
                mc.player.getYRot(),
                mc.player.getXRot(),
                onGround,
                false
        ));
    }
}