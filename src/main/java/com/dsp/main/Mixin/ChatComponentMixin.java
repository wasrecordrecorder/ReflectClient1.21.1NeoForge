package com.dsp.main.Mixin;

import com.dsp.main.Api;
import com.dsp.main.Functions.Player.StreamerMode;
import com.dsp.main.Utils.Minecraft.Chat.ChatUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import static com.dsp.main.Functions.Player.StreamerMode.FuntimePr;

@OnlyIn(net.neoforged.api.distmarker.Dist.CLIENT)
@Mixin(ChatComponent.class)
public class ChatComponentMixin {
    @ModifyVariable(
            method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V",
            at = @At("HEAD"),
            index = 1,
            argsOnly = true)
    private Component replaceStreamerName(Component original) {
        if (!Api.isEnabled("StreamerMode")) {
            return original;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return original;
        }

        String playerName = mc.player.getName().getString();
        MutableComponent newComponent = Component.literal("");
        boolean replaced = false;
        for (Component child : original.getSiblings()) {
            String text = child.getString();
            if (StreamerMode.FuntimePr.isEnabled() && text.toLowerCase().contains("funtime")) {
                String newText = text.replace("funtime", "xuitime");
                newComponent = newComponent.append(Component.literal(newText).withStyle(child.getStyle()));
                replaced = true;
            } else if (text.contains(playerName)) {
                String newText = text.replace(playerName, StreamerMode.ProtectedName);
                newComponent = newComponent.append(Component.literal(newText).withStyle(child.getStyle()));
                replaced = true;
            } else {
                newComponent = newComponent.append(child);
            }
        }
        return replaced ? newComponent : original;
    }
}