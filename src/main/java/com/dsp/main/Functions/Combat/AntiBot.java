package com.dsp.main.Functions.Combat;

import com.dsp.main.Core.Event.OnUpdate;
import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.CheckBox;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.MultiCheckBox;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.dsp.main.Api.mc;

public class AntiBot extends Module {

    private static final Minecraft minecraft = Minecraft.getInstance();
    private final Set<UUID> bots = new HashSet<>();
    private final Map<UUID, Integer> joinTicks = new HashMap<>(); // Tracks ticks since player joined
    private final Map<UUID, Integer> botSuspicionCount = new HashMap<>(); // Tracks consecutive bot detections
    private static final int PING_CHECK_DELAY = 1200;
    private static final int SUSPICION_THRESHOLD = 4; // Require 3 consecutive detections to confirm bot
    private static final double MOTION_THRESHOLD = 9.0; // Adjusted for sprinting (~0.1 blocks/tick squared)

    private final MultiCheckBox options;
    private static AntiBot INSTANCE;
    public AntiBot() {
        super("AntiBot", 0, Category.COMBAT, "Detect and filter fake players");

        List<CheckBox> checks = new ArrayList<>();
        checks.add(new CheckBox("UUID Check", true));
        checks.add(new CheckBox("Zero Ping", true));
        checks.add(new CheckBox("Motion Check", false));
        checks.add(new CheckBox("Auto Remove", false));
        this.options = new MultiCheckBox("Bot Checks", checks);
        INSTANCE = this;
        addSettings(options);
    }
    public static boolean isBot(UUID uuid) {
        return INSTANCE != null && INSTANCE.bots.contains(uuid);
    }

    @SubscribeEvent
    public void onTick(OnUpdate event) {
        if (mc.level == null || mc.player == null || mc.getConnection() == null) return;

        List<Player> toRemove = new ArrayList<>();
        for (Player player : mc.level.players()) {
            if (player == mc.player) continue;
            joinTicks.put(player.getUUID(), joinTicks.getOrDefault(player.getUUID(), 0) + 1);
        }

        for (Player player : mc.level.players()) {
            if (player == mc.player || bots.contains(player.getUUID())) continue;

            String reason = checkBot(player);
            if (reason != null) {
                int suspicionCount = botSuspicionCount.getOrDefault(player.getUUID(), 0) + 1;
                botSuspicionCount.put(player.getUUID(), suspicionCount);

                //ChatUtil.sendMessage("[AntiBot] Suspected bot: " + player.getName().getString() +
                //        " | Reason: " + reason + " | Suspicion Count: " + suspicionCount);

                if (suspicionCount >= SUSPICION_THRESHOLD) {
                    bots.add(player.getUUID());
                    //ChatUtil.sendMessage("[AntiBot] Confirmed bot: " + player.getName().getString());

                    if (isEnabled("Auto Remove")) {
                        toRemove.add(player);
                    }
                }
            } else {
                botSuspicionCount.remove(player.getUUID());
            }
        }
        for (Player bot : toRemove) {
            mc.level.removeEntity(bot.getId(), Entity.RemovalReason.UNLOADED_WITH_PLAYER);
            //ChatUtil.sendMessage("[AntiBot] Removed bot: " + bot.getName().getString());
        }
        joinTicks.keySet().removeIf(uuid -> mc.level.getPlayerByUUID(uuid) == null);
    }

    private String checkBot(Player p) {
        if (isEnabled("UUID Check")) {
            UUID offline = UUID.nameUUIDFromBytes(("OfflinePlayer:" + p.getName().getString())
                    .getBytes(StandardCharsets.UTF_8));
            if (!p.getUUID().equals(offline)) {
                return "UUID Check failed";
            }
        }

        if (isEnabled("Zero Ping")) {
            int ticksSinceJoin = joinTicks.getOrDefault(p.getUUID(), 0);
            if (ticksSinceJoin < PING_CHECK_DELAY) {
                return null;
            }
            int ping = Optional.ofNullable(mc.getConnection().getPlayerInfo(p.getUUID()))
                    .map(info -> info.getLatency())
                    .orElse(-999);
            if (ping <= 0) {
                return "Zero Ping (" + ping + ")";
            }
        }

        if (isEnabled("Motion Check")) {
            if (p.isFallFlying() || p.isPassenger() || p.isInWater()) {
                return null;
            }
            double dx = p.getX() - p.xOld;
            double dz = p.getZ() - p.zOld;
            double motionSquared = dx * dx + dz * dz;
            if (motionSquared > MOTION_THRESHOLD && motionSquared < 100) {
                return String.format("Motion suspicious (Δ²=%.2f)", motionSquared);
            }
        }

        return null;
    }

    private boolean isEnabled(String name) {
        return options.getOptions().stream().anyMatch(box -> box.getName().equals(name) && box.isEnabled());
    }
}