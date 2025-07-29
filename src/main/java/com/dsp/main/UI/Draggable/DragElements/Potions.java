package com.dsp.main.UI.Draggable.DragElements;

import com.dsp.main.Api;
import com.dsp.main.UI.Draggable.DraggableElement;
import com.dsp.main.Utils.Font.builders.Builder;
import com.dsp.main.Utils.Font.renderers.impl.BuiltText;
import com.dsp.main.Utils.Render.Blur.DrawShader;
import com.dsp.main.Utils.Render.DrawHelper;
import com.dsp.main.Utils.TimerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
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

public class Potions extends DraggableElement {
    private static final int TEXT_HEIGHT = 8;
    private static final int PADDING = 5;
    private static final int SPACING = 25;
    private static final int ROUND_RADIUS = 5;
    private static final int ICON_SIZE = 11;
    private static final long ANIMATION_DURATION_MS = 300;
    private static final MobEffect FAKE_EFFECT = new MobEffect(MobEffectCategory.BENEFICIAL, 0xFFFFFF) {
        @Override
        public Component getDisplayName() {
            return Component.literal("ExampleEffect");
        }

        @Override
        public String getDescriptionId() {
            return "effect.dsp.speed";
        }
    };
    private static final MobEffectInstance FAKE_EFFECT_INSTANCE = new MobEffectInstance(Holder.direct(FAKE_EFFECT), 1800, 0);

    private float opacity = 0.0f;
    private float targetOpacity = 0.0f;
    private float currentWidth = 0.0f;
    private float currentHeight = 0.0f;
    private float targetWidth = 0.0f;
    private float targetHeight = 0.0f;
    private long animationStartTime = 0;
    private List<MobEffectInstance> lastEffects = new ArrayList<>();
    private final TimerUtil timer = new TimerUtil();

    public Potions(String name, float initialX, float initialY, boolean canBeDragged) {
        super(name, initialX, initialY, canBeDragged);
    }

    @Override
    public float getWidth() {
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
        List<MobEffectInstance> effects = getActiveEffects();
        if (effects.isEmpty()) {
            return 0;
        }
        return effects.size() * (TEXT_HEIGHT + 4) + 3;
    }

    @Override
    public void render(GuiGraphics guiGraphics) {
        if (mc.player == null || !HudElements.isOptionEnabled("Potions") || !Api.isEnabled("Hud")) {
            targetOpacity = 0.0f;
        } else {
            List<MobEffectInstance> effects = getActiveEffects();
            if (effects.isEmpty()) {
                targetOpacity = 0.0f;
            } else {
                targetOpacity = 1.0f;
            }
            if (!effects.equals(lastEffects)) {
                targetWidth = getWidth();
                targetHeight = getHeight();
                animationStartTime = System.currentTimeMillis();
                lastEffects = new ArrayList<>(effects);
            }
            long elapsed = System.currentTimeMillis() - animationStartTime;
            float t = Math.min((float) elapsed / ANIMATION_DURATION_MS, 1.0f);
            opacity = lerp(opacity, targetOpacity, t);
            currentWidth = lerp(currentWidth, targetWidth, t);
            currentHeight = lerp(currentHeight, targetHeight, t);

            if (opacity <= 0.01f || currentWidth <= 0.01f || currentHeight <= 0.01f) {
                return;
            }
            int alpha = (int) (opacity * 255);
            DrawShader.drawRoundBlur(guiGraphics.pose(), xPos, yPos, currentWidth, currentHeight, ROUND_RADIUS,
                    new Color(23, 29, 35, alpha).getRGB(), 120, 0.4f);
            guiGraphics.enableScissor((int) xPos, (int) yPos, (int) (xPos + currentWidth), (int) (yPos + currentHeight));

            float currentY = yPos + PADDING - 1;
            for (MobEffectInstance effect : effects) {
                MobEffect mobEffect = effect.getEffect().value();
                String effectName = mobEffect.getDisplayName().getString();
                String amplifier = String.valueOf(effect.getAmplifier() + 1);
                String durationText = effect.getDuration() >= 32000 ? "Inf." : formatDuration(effect.getDuration());

                String effectId = mobEffect.getDescriptionId().replace("effect.minecraft.", "").replace("effect.dsp.", "");
                ResourceLocation texture = ResourceLocation.fromNamespaceAndPath("dsp", "effects/" + effectId + ".png");
                DrawHelper.drawTexture(texture, new Matrix4f(), xPos + PADDING - 3, currentY - 2.5f, ICON_SIZE, ICON_SIZE);

                Color nameColor = mobEffect.getCategory() == MobEffectCategory.BENEFICIAL ?
                        new Color(255, 255, 255, alpha) : new Color(255, 100, 100, alpha);
                BuiltText effectText = Builder.text()
                        .font(RUS.get())
                        .text(effectName + " " + amplifier)
                        .color(nameColor)
                        .size(TEXT_HEIGHT)
                        .thickness(0.1f)
                        .build();
                effectText.render(new Matrix4f(), xPos + ICON_SIZE + 2, currentY - 2);

                BuiltText durationTextRender = Builder.text()
                        .font(RUS.get())
                        .text(durationText)
                        .color(new Color(255, 255, 255, alpha))
                        .size(TEXT_HEIGHT)
                        .thickness(0.1f)
                        .build();
                float durationX = xPos + currentWidth - PADDING - RUS.get().getWidth(durationText, TEXT_HEIGHT);
                durationTextRender.render(new Matrix4f(), durationX, currentY - 2);

                currentY += TEXT_HEIGHT + 4;
            }
            guiGraphics.disableScissor();
        }
    }

    private List<MobEffectInstance> getActiveEffects() {
        List<MobEffectInstance> effects = new ArrayList<>();
        if (mc.player != null) {
            effects.addAll(mc.player.getActiveEffects());
        }
        if (effects.isEmpty() && isChatOpen()) {
            effects.add(FAKE_EFFECT_INSTANCE);
        }
        return effects;
    }

    private String formatDuration(int ticks) {
        int seconds = ticks / 20;
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    private float lerp(float start, float end, float t) {
        return start + (end - start) * t;
    }
}