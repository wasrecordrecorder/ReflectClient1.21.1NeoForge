package com.dsp.main.UI.Draggable.DragElements;

import com.dsp.main.Api;
import com.dsp.main.UI.Draggable.DraggableElement;
import com.dsp.main.Utils.Font.builders.Builder;
import com.dsp.main.Utils.Font.renderers.impl.BuiltText;
import com.dsp.main.Utils.Render.Blur.DrawShader;
import com.dsp.main.Utils.Render.DrawHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import org.joml.Matrix4f;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.dsp.main.Api.mc;
import static com.dsp.main.Functions.Render.HudElement.HudElements;
import static com.dsp.main.Functions.Render.HudElement.IconColor;
import static com.dsp.main.Main.*;

public class Cooldowns extends DraggableElement {
    private static final int TEXT_HEIGHT = 8;
    private static final int PADDING = 5;
    private static final int SPACING = 25;
    private static final int ROUND_RADIUS = 6;
    private static final int ICON_SIZE = 11;
    private static final long ANIMATION_DURATION_MS = 300;
    private static final float BAR_HEIGHT = 1.5f;
    private static final float BAR_OFFSET_Y = 1.0f; // Отступ полоски от текста
    private static final float BAR_ANIMATION_SPEED = 0.2f; // Скорость анимации полоски

    private float opacity = 0.0f;
    private float targetOpacity = 0.0f;
    private float currentWidth = 0.0f;
    private float targetWidth = 0.0f;
    private float currentHeight = 0.0f;
    private float targetHeight = 0.0f;
    private long animationStartTime = 0;
    private List<Item> lastItems = new ArrayList<>();
    private Map<Item, Float> currentBarWidths = new HashMap<>();

    public Cooldowns(String name, float initialX, float initialY, boolean canBeDragged) {
        super(name, initialX, initialY, canBeDragged);
    }

    private static String getFormattedName(ItemStack item) {
        if (item.getItem() == Items.ENCHANTED_GOLDEN_APPLE) return "Чарка";
        if (item.getItem() == Items.CROSSBOW) return "Арбалет";
        if (item.getItem() == Items.GOLDEN_APPLE) return "Гэпл";
        if (item.getItem() == Items.TRIDENT) return "Трезубец";
        if (item.getItem() == Items.NETHERITE_SCRAP) return "Трапка";
        if (item.getItem() == Items.ENDER_EYE) return "Дизорентация";
        if (item.getItem() == Items.SNOWBALL) return "Снежок";
        if (item.getItem() == Items.TOTEM_OF_UNDYING) return "Талисман";
        if (item.getItem() == Items.POTION) return "Хилка / Баф";
        if (item.getItem() == Items.FIRE_CHARGE) return "Огненый Смерч";
        if (item.getItem() == Items.PHANTOM_MEMBRANE) return "Божья Аура";
        if (item.getItem() == Items.SUGAR) return "Явная Пыль";
        if (item.getItem() == Items.DRIED_KELP) return "Пласт";
        if (item.getItem() == Items.ENDER_PEARL) return "Эндер-Перл";
        if (item.getItem() == Items.FIREWORK_ROCKET) return "Феерверк";
        if (item.getItem() == Items.CHORUS_FRUIT) return "Хорус";
        if (item.getItem() == Items.EXPERIENCE_BOTTLE) return "Пузырек опыта";
        return item.getItem().getDescription().getString();
    }

    @Override
    public float getWidth() {
        List<Item> itemsOnCooldown = getItemsOnCooldown();
        if (itemsOnCooldown.isEmpty()) {
            return 0;
        }
        float maxItemNameWidth = itemsOnCooldown.stream()
                .map(item -> RUS.get().getWidth(getFormattedName(new ItemStack(item)), TEXT_HEIGHT))
                .max(Float::compare)
                .orElse(0f);
        return ICON_SIZE + maxItemNameWidth + SPACING + 2 * PADDING;
    }

    @Override
    public float getHeight() {
        List<Item> itemsOnCooldown = getItemsOnCooldown();
        if (itemsOnCooldown.isEmpty()) {
            return 0;
        }
        return itemsOnCooldown.size() * (TEXT_HEIGHT + BAR_HEIGHT + BAR_OFFSET_Y + 4) + 3;
    }

    @Override
    public void render(GuiGraphics guiGraphics) {
        if (mc.player == null || !HudElements.isOptionEnabled("Cooldowns") || !Api.isEnabled("Hud")) {
            targetOpacity = 0.0f;
        } else {
            List<Item> itemsOnCooldown = getItemsOnCooldown();
            if (itemsOnCooldown.isEmpty() && !isChatOpen()) {
                targetOpacity = 0.0f;
            } else {
                targetOpacity = 1.0f;
            }
            if (!itemsOnCooldown.equals(lastItems)) {
                targetWidth = getWidth();
                targetHeight = getHeight();
                currentWidth = targetWidth;
                currentHeight = targetHeight;
                animationStartTime = System.currentTimeMillis();
                lastItems = new ArrayList<>(itemsOnCooldown);
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
            float currentY = yPos + PADDING - 1;
            for (Item item : itemsOnCooldown) {
                Color textColorWithAlpha = new Color(255, 255, 255, alpha);

                // Render item texture
                if (item == Items.ENCHANTED_GOLDEN_APPLE) {
                    ResourceLocation itemTexture = ResourceLocation.fromNamespaceAndPath("dsp", "textures/item/golden_apple.png");
                    DrawHelper.drawTexture(itemTexture, guiGraphics.pose().last().pose(), xPos + PADDING - 3, currentY - 2.5f, ICON_SIZE, ICON_SIZE);
                } else if (item == Items.PLAYER_HEAD) {
                    ResourceLocation itemTexture = ResourceLocation.fromNamespaceAndPath("dsp", "textures/item/barrier.png");
                    DrawHelper.drawTexture(itemTexture, guiGraphics.pose().last().pose(), xPos + PADDING - 3, currentY - 2.5f, ICON_SIZE, ICON_SIZE);
                } else if (item == Items.AIR) {
                    ResourceLocation itemTexture = ResourceLocation.fromNamespaceAndPath("dsp", "textures/item/barrier.png");
                    DrawHelper.drawTexture(itemTexture, guiGraphics.pose().last().pose(), xPos + PADDING - 3, currentY - 2.5f, ICON_SIZE, ICON_SIZE);
                } else {
                    ResourceLocation itemTexture = ResourceLocation.fromNamespaceAndPath("dsp", "textures/item/" + item.getDescriptionId().replace("item.minecraft.", "") + ".png");
                    DrawHelper.drawTexture(itemTexture, guiGraphics.pose().last().pose(), xPos + PADDING - 3, currentY - 2.5f, ICON_SIZE, ICON_SIZE);
                }

                // Render item name at the top
                String itemName = getFormattedName(new ItemStack(item));
                BuiltText itemText = Builder.text()
                        .font(RUS.get())
                        .text(itemName)
                        .color(textColorWithAlpha)
                        .size(TEXT_HEIGHT)
                        .thickness(0.1f)
                        .build();
                itemText.render(new Matrix4f(), xPos + PADDING + 9, currentY - 3.5f);

                // Render cooldown bar background
                float maxBarWidth = currentWidth - ICON_SIZE - 2 * PADDING - 10; // Adjusted for icon and padding
                DrawHelper.rectangle(
                        guiGraphics.pose(),
                        xPos + PADDING + ICON_SIZE, // Start after icon and padding
                        currentY + TEXT_HEIGHT + BAR_OFFSET_Y - 1,
                        maxBarWidth,
                        BAR_HEIGHT,
                        1.0f,
                        new Color(64, 64, 64, alpha).getRGB() // Gray background
                );

                // Render cooldown bar
                float cooldownPercent = mc.player.getCooldowns().getCooldownPercent(item, 0);
                float targetBarWidth = maxBarWidth * cooldownPercent; // Decreases from full to zero
                float currentBarWidth = currentBarWidths.getOrDefault(item, targetBarWidth);
                currentBarWidth = lerp(currentBarWidth, targetBarWidth, BAR_ANIMATION_SPEED);
                currentBarWidths.put(item, currentBarWidth);
                Color lineColor = new Color(0, 243, 205, alpha);
                if (cooldownPercent > 0) {
                    DrawHelper.rectangle(
                            guiGraphics.pose(),
                            xPos + PADDING + ICON_SIZE,
                            currentY + TEXT_HEIGHT + BAR_OFFSET_Y - 1,
                            currentBarWidth,
                            BAR_HEIGHT,
                            1.0f,
                            lineColor.getRGB()
                    );
                }

                currentY += TEXT_HEIGHT + BAR_HEIGHT + BAR_OFFSET_Y + 4;
            }
            // Clean up bar widths for items no longer on cooldown
            currentBarWidths.keySet().removeIf(item -> !itemsOnCooldown.contains(item));
        }
    }

    private List<Item> getItemsOnCooldown() {
        List<Item> itemsOnCooldown = new ArrayList<>();
        if (mc.player != null) {
            Map<Item, Float> itemCooldowns = new HashMap<>();
            for (ItemStack stack : mc.player.getInventory().items) {
                Item item = stack.getItem();
                float cooldownPercent = mc.player.getCooldowns().getCooldownPercent(item, 0);
                if (cooldownPercent > 0 && !itemCooldowns.containsKey(item)) {
                    itemCooldowns.put(item, cooldownPercent);
                    itemsOnCooldown.add(item);
                }
            }
            if (itemsOnCooldown.isEmpty() && isChatOpen()) {
                Item fakeItem = Items.GOLDEN_HELMET;
                itemsOnCooldown.add(fakeItem);
                itemCooldowns.put(fakeItem, 0.5f); // Full cooldown for fake item
            }
        }
        return itemsOnCooldown;
    }

    private float lerp(float start, float end, float t) {
        return start + (end - start) * t;
    }
}