package com.dsp.main.Functions.Player;

import com.dsp.main.Core.Event.ClientPacketReceiveEvent;
import com.dsp.main.Core.Event.ClientPacketSendEvent;
import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.Slider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.SubscribeEvent;

import static com.dsp.main.Api.mc;

public class FreeCamera extends Module {

    public static Slider speed = new Slider("Speed", 1, 10, 5, 1);

    // Ссылка на фейкового игрока
    private LocalPlayer fakePlayer;

    // Запоминаем старый гейм-мод
    private GameType prevGameMode;

    public FreeCamera() {
        super("FreeCamera", 0, Category.PLAYER, "Создаёт клона и переводит вас в спектатора");
        addSetting(speed);
    }

    @Override
    public void onEnable() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        LocalPlayer real = mc.player;
        prevGameMode = mc.gameMode.getPlayerMode();
        fakePlayer = new LocalPlayer(
                mc,
                mc.level,
                mc.getConnection(),
                null,
                mc.player.getRecipeBook(),
                false,
                false
        ) {};
        fakePlayer.setPos(real.getX(), real.getY(), real.getZ());
        fakePlayer.setYRot(real.getYRot());
        fakePlayer.setXRot(real.getXRot());
        fakePlayer.setDeltaMovement(real.getDeltaMovement());
        fakePlayer.setHealth(real.getHealth());
        fakePlayer.getInventory().replaceWith(real.getInventory());
        fakePlayer.input = mc.player.input;
        ((ClientLevel) mc.level).addEntity(
                fakePlayer
        );
        if (mc.gameMode != null)
            mc.gameMode.setLocalMode(GameType.SPECTATOR);
        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (mc.level == null || mc.player == null) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && fakePlayer != null) {
            ((ClientLevel) mc.level).removeEntity(
                    fakePlayer.getId(),
                    net.minecraft.world.entity.Entity.RemovalReason.DISCARDED
            );
            fakePlayer = null;
        }
        if (mc.gameMode != null && prevGameMode != null)
            mc.gameMode.setLocalMode(prevGameMode);

        super.onDisable();
    }

    @SubscribeEvent
    public void onPacket(ClientPacketSendEvent e) {
        var p = e.getPacket();
        if (p instanceof ServerboundMovePlayerPacket ||
                p instanceof ServerboundMovePlayerPacket.Pos ||
                p instanceof ServerboundMovePlayerPacket.Rot ||
                p instanceof ServerboundMovePlayerPacket.PosRot ||
                p instanceof ServerboundPlayerInputPacket ||
                p instanceof ServerboundPlayerCommandPacket ||
                p instanceof ServerboundMoveVehiclePacket ||
                p instanceof ServerboundAcceptTeleportationPacket) {
            e.setCanceled(true);
        }
    }
    @SubscribeEvent
    public void onPacket(ClientPacketReceiveEvent e) {
        var p = e.getPacket();
        if (p instanceof ServerboundMovePlayerPacket ||
                p instanceof ServerboundMovePlayerPacket.Pos ||
                p instanceof ServerboundMovePlayerPacket.Rot ||
                p instanceof ServerboundMovePlayerPacket.PosRot ||
                p instanceof ServerboundPlayerInputPacket ||
                p instanceof ServerboundPlayerCommandPacket ||
                p instanceof ServerboundMoveVehiclePacket ||
                p instanceof ServerboundAcceptTeleportationPacket) {
            e.setCanceled(true);
        }
    }
}