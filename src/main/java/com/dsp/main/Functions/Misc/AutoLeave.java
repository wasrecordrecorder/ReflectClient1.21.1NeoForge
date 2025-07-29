package com.dsp.main.Functions.Misc;

import com.dsp.main.Core.Event.OnUpdate;
import com.dsp.main.Core.FrndSys.FriendManager;
import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.CheckBox;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.Mode;
import com.dsp.main.Utils.Minecraft.Server.isPvP;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.List;

import static com.dsp.main.Api.mc;

public class AutoLeave extends Module {
    private static final Mode Mode = new Mode("Leave Mode", "Closest Player");
    private static final Mode LeaveMode = new Mode("Leave Type", "/hub", "/spawn", "/darena", "kick");
    private static final CheckBox PvPCheck = new CheckBox("Check PvP Mode", false);
    private static final CheckBox FriendCheck = new CheckBox("Check is Friend", false);
    private static long lastDarenaTime = 0;
    private static final long DARENA_COOLDOWN = 2000;
    private static final long DARENA_SCREEN_CHECK_DELAY = 600;

    public AutoLeave() {
        super("Auto Leave", 0, Category.MISC, "Автоматически ливает при выбранных условиях");
        addSettings(Mode, LeaveMode, PvPCheck, FriendCheck);
    }

    @SubscribeEvent
    public void onTick(OnUpdate event) {
        if (mc.level == null || mc.player == null || (isPvP.isPvPMode() && PvPCheck.isEnabled())) {
            return;
        }

        if (Mode.isMode("Closest Player")) {
            List<AbstractClientPlayer> players = mc.level.players();
            for (Player player : players) {
                if (player.equals(mc.player)) {
                    continue;
                }
                if (FriendCheck.isEnabled() && FriendManager.isFriend(player.getName().getString())) {
                    continue;
                }
                double distance = mc.player.distanceTo(player);
                if (distance <= 200) {
                    if (LeaveMode.isMode("kick")) {
                        mc.player.connection.disconnect(
                                Component.translatable("PlayerEntity " + player.getDisplayName().getString() + " detected within " + distance + " blocks!")
                        );
                        mc.level.disconnect();
                        this.toggle();
                    } else if (LeaveMode.isMode("/spawn")) {
                        mc.player.connection.sendCommand("spawn");
                        this.toggle();
                    } else if (LeaveMode.isMode("/hub")) {
                        mc.player.connection.sendCommand("hub");
                        this.toggle();
                    } else if (LeaveMode.isMode("/darena")) {
                        long currentTime = System.currentTimeMillis();
                        if (currentTime - lastDarenaTime >= DARENA_COOLDOWN) {
                            mc.player.connection.sendCommand("darena");
                            lastDarenaTime = currentTime;
                        }
                        if (currentTime - lastDarenaTime >= DARENA_SCREEN_CHECK_DELAY && mc.screen instanceof ContainerScreen containerScreen) {
                            for (Slot slot : containerScreen.getMenu().slots) {
                                if (slot.getItem().getDisplayName().getString().contains("На Смотровую")) {
                                    mc.gameMode.handleInventoryMouseClick(
                                            containerScreen.getMenu().containerId,
                                            slot.index,
                                            0,
                                            net.minecraft.world.inventory.ClickType.PICKUP,
                                            mc.player
                                    );
                                    this.toggle();
                                    break;
                                }
                            }
                        }
                    }
                    break;
                }
            }
        }
    }
}