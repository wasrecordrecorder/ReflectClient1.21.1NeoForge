package com.dsp.main.Functions.Misc;

import com.dsp.main.Module;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import static com.dsp.main.Api.mc;

public class DeathCoordinates extends Module {

    public DeathCoordinates() {
        super("DeathCoordinates", 0, Category.MISC, "Sends death coordinates to chat and copies to clipboard");
    }

    @SubscribeEvent
    public void onDeath(LivingDeathEvent event) {
        if (mc.player == null || mc.level == null) return;
        if (event.getEntity() != mc.player) return;

        BlockPos deathPos = mc.player.blockPosition();
        int x = deathPos.getX();
        int y = deathPos.getY();
        int z = deathPos.getZ();

        String coords = x + " " + y + " " + z;

        if (mc.keyboardHandler != null) {
            mc.keyboardHandler.setClipboard(coords);
        }

        Component message = Component.literal("")
                .append(Component.literal("Координаты смерти: ").withStyle(ChatFormatting.WHITE))
                .append(Component.literal(x + ", " + y + ", " + z).withStyle(ChatFormatting.YELLOW));

        if (mc.gui != null && mc.gui.getChat() != null) {
            mc.gui.getChat().addMessage(message);
        }
    }
}