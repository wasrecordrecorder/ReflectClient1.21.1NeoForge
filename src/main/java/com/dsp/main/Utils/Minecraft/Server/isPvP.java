package com.dsp.main.Utils.Minecraft.Server;

import com.dsp.main.Mixin.Accesors.BossHealthOverlayAccessor;
import com.dsp.main.Utils.Render.Mine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.world.BossEvent;

public class isPvP {
    public static boolean isPvPMode() {
        if (Minecraft.getInstance().player == null) return false;
        return BossBarsContains("PvP-режим") || BossBarsContains("PVP режима") || BossBarsContains("PvP") || BossBarsContains("PVP") || BossBarsContains("ПвП") || BossBarsContains("ПВП");
    }
    public static boolean BossBarsContains(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }

        Minecraft minecraft = Minecraft.getInstance();

        BossHealthOverlay bossHealthOverlay = minecraft.gui.getBossOverlay();
        for (BossEvent bossEvent : ((BossHealthOverlayAccessor) bossHealthOverlay).getBossEvents().values()) {
            String bossBarName = bossEvent.getName().getString();
            if (bossBarName.toLowerCase().contains(value.toLowerCase())) {
                return true;
            }
        }

        return false;
    }
}
