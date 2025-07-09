package com.dsp.main.UI.Draggable.DragElements;

import com.dsp.main.Api;
import com.dsp.main.UI.Draggable.DraggableElement;
import com.dsp.main.Utils.Font.builders.Builder;
import com.dsp.main.Utils.Font.renderers.impl.BuiltText;
import com.dsp.main.Utils.Render.Blur.DrawShader;
import com.dsp.main.Utils.Render.DrawHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import org.joml.Matrix4f;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import static com.dsp.main.Api.mc;
import static com.dsp.main.Functions.Render.HudElement.HudElements;
import static com.dsp.main.Main.RUS;
import static com.dsp.main.Main.RUS;

public class Potions extends DraggableElement {
    private static final int TEXT_HEIGHT = 8;
    private static final int PADDING = 5;
    private static final int SPACING = 25;
    private static final int ROUND_RADIUS = 5;
    private static final int ICON_SIZE = 12;

    public Potions(String name, float initialX, float initialY, boolean canBeDragged) {
        super(name, initialX, initialY, canBeDragged);
    }

    @Override
    public float getWidth() {
        if (mc.player == null ) return 60;
        List<MobEffectInstance> effects = getActiveEffects();
        if (effects.isEmpty()) {
            return 0;
        }
        float maxEffectNameWidth = effects.stream()
                .map(effect -> {
                    MobEffect mobEffect = effect.getEffect().value();
                    String name = mobEffect.getDisplayName().getString();
                    String amplifier = String.valueOf(effect.getAmplifier() + 1);
                    return RUS.get().getWidth(name + " " + amplifier, TEXT_HEIGHT);
                })
                .max(Float::compare)
                .orElse(0f);
        float maxDurationWidth = effects.stream()
                .map(effect -> {
                    String duration = effect.getDuration() >= 32000 ? "Inf." : formatDuration(effect.getDuration());
                    return RUS.get().getWidth(duration, TEXT_HEIGHT);
                })
                .max(Float::compare)
                .orElse(0f);
        return ICON_SIZE + maxEffectNameWidth + SPACING + maxDurationWidth + 2 * PADDING;
    }

    @Override
    public float getHeight() {
        if (mc.player == null) return (TEXT_HEIGHT + 4) + 3;
        List<MobEffectInstance> effects = getActiveEffects();
        if (effects.isEmpty()) {
            return 0;
        }
        return effects.size() * (TEXT_HEIGHT + 4) + 3;
    }

    @Override
    public void render(GuiGraphics guiGraphics) {
        if (mc.player != null && HudElements.isOptionEnabled("Potions") && Api.isEnabled("Hud")) {
            List<MobEffectInstance> effects = getActiveEffects();
            if (effects.isEmpty()) {
                return;
            }

            float width = getWidth();
            float height = getHeight();
            DrawShader.drawRoundBlur(guiGraphics.pose(), xPos, yPos, width, height, ROUND_RADIUS,
                    new Color(23, 29, 35, 255).getRGB(), 120, 0.4f);

            float currentY = yPos + PADDING - 1;
            for (MobEffectInstance effect : effects) {
                MobEffect mobEffect = effect.getEffect().value();
                String effectName = mobEffect.getDisplayName().getString();
                String amplifier = String.valueOf(effect.getAmplifier() + 1);
                String durationText = effect.getDuration() >= 32000 ? "Inf." : formatDuration(effect.getDuration());
                String effectId = mobEffect.getDescriptionId().replace("effect.minecraft.", "");
                ResourceLocation texture = ResourceLocation.fromNamespaceAndPath("dsp", "effects/" + effectId + ".png");
                DrawHelper.drawTexture(texture, new Matrix4f(),xPos + PADDING - 3, currentY - 2.5f, ICON_SIZE -1, ICON_SIZE -1);
                Color nameColor = mobEffect.getCategory() == MobEffectCategory.BENEFICIAL ? Color.WHITE : new Color(255, 100, 100);
                BuiltText effectText = Builder.text()
                        .font(RUS.get())
                        .text(effectName + " " + amplifier)
                        .color(nameColor)
                        .size(TEXT_HEIGHT)
                        .thickness(0.1f)
                        .build();
                effectText.render(new Matrix4f(), xPos  + ICON_SIZE + 2, currentY - 2);
                BuiltText durationTextRender = Builder.text()
                        .font(RUS.get())
                        .text(durationText)
                        .color(Color.WHITE)
                        .size(TEXT_HEIGHT)
                        .thickness(0.1f)
                        .build();
                float durationX = xPos + width - PADDING - RUS.get().getWidth(durationText, TEXT_HEIGHT);
                durationTextRender.render(new Matrix4f(), durationX, currentY - 2);

                currentY += TEXT_HEIGHT + 4;
            }
        }
    }

    private List<MobEffectInstance> getActiveEffects() {
        if (!(mc.player == null)) {
            return new ArrayList<>(mc.player.getActiveEffects());
        }
        return null;
    }

    private String formatDuration(int ticks) {
        int seconds = ticks / 20;
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}