package com.dsp.main.Mixin;

import com.dsp.main.Api;
import com.dsp.main.Functions.Player.StreamerMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.ClientHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.Instant;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
@Mixin(ChatListener.class)
public abstract class ChatListenerMixin {
    @Shadow protected abstract UUID guessChatUUID(Component message);
    @Shadow protected abstract void logSystemMessage(Component message, Instant timestamp);

    @Inject(method = "handleSystemMessage", at = @At("HEAD"), cancellable = true)
    public void onHandleSystemMessage(Component message, boolean isOverlay, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (!mc.options.hideMatchedNames().get() || !mc.isBlocked(this.guessChatUUID(message))) {
            if (Api.isEnabled("StreamerMode")) {
                String localPlayerName = mc.player != null ? mc.player.getName().getString() : "";
                MutableComponent newComponent = Component.literal("");
                boolean replaced = false;
                for (Component child : message.getSiblings()) {
                    String text = child.getString();
                    if (!localPlayerName.isEmpty() && text.contains(localPlayerName)) {
                        String newText = text.replace(localPlayerName, StreamerMode.ProtectedName);
                        newComponent = newComponent.append(Component.literal(newText).withStyle(child.getStyle()));
                        replaced = true;
                    } else if (StreamerMode.FuntimePr.isEnabled() && text.toLowerCase().contains("funtime")) {
                        String newText = text.replace("funtime", "xuitime");
                        newComponent = newComponent.append(Component.literal(newText).withStyle(child.getStyle()));
                        replaced = true;
                    } else {
                        newComponent = newComponent.append(child);
                    }
                }
                message = replaced ? newComponent : message;
            }

            message = ClientHooks.onClientSystemChat(message, isOverlay);
            if (message == null) {
                ci.cancel();
                return;
            }

            if (isOverlay) {
                mc.gui.setOverlayMessage(message, false);
            } else {
                mc.gui.getChat().addMessage(message);
                this.logSystemMessage(message, Instant.now());
            }

            mc.getNarrator().say(message);
            ci.cancel();
        }
    }
}