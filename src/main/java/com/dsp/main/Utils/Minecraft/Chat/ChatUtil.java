package com.dsp.main.Utils.Minecraft.Chat;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.ChatFormatting;

public class ChatUtil {

    public static void sendMessage(String message) {
        if (Minecraft.getInstance().player != null) {
            MutableComponent prefix = createGradientText("[Reflect Client]", ChatFormatting.WHITE, ChatFormatting.WHITE, ChatFormatting.BOLD);
            MutableComponent formattedMessage = Component.literal(message)
                    .withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC);

            Minecraft.getInstance().player.sendSystemMessage(
                    prefix.append(Component.literal(" > ")).append(formattedMessage)
            );
        }
    }

    private static MutableComponent createGradientText(String text, ChatFormatting startColor, ChatFormatting endColor, ChatFormatting bold) {
        MutableComponent gradientText = Component.literal("");
        int length = text.length();
        for (int i = 0; i < length; i++) {
            float ratio = (float) i / (length - 1);
            ChatFormatting color = blendColors(startColor, endColor, ratio);
            gradientText.append(Component.literal(String.valueOf(text.charAt(i)))
                    .withStyle(Style.EMPTY.withColor(TextColor.fromLegacyFormat(color)).withBold(bold == ChatFormatting.BOLD)));
        }
        return gradientText;
    }

    private static ChatFormatting blendColors(ChatFormatting startColor, ChatFormatting endColor, float ratio) {
        return ratio < 0.5 ? startColor : endColor;
    }
}