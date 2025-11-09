package com.dsp.main.UI.Draggable.DragElements;

import com.dsp.main.Api;
import com.dsp.main.UI.Draggable.DraggableElement;
import com.dsp.main.UI.Themes.ThemesUtil;
import com.dsp.main.Utils.Color.ColorHelper;
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
    private static final long FADE_DURATION_MS = 250;
    private static final float BAR_HEIGHT = 1.5f;
    private static final float BAR_OFFSET_Y = 1.0f;

    private float opacity = 0.0f;
    private float targetOpacity = 0.0f;
    private float currentWidth = 0.0f;
    private float targetWidth = 0.0f;
    private float currentHeight = 0.0f;
    private float targetHeight = 0.0f;
    private long animationStartTime = 0;
    private List<Item> lastItems = new ArrayList<>();
    private Map<Item, Float> currentBarWidths = new HashMap<>();
    private Map<Item, Long> itemAppearTime = new HashMap<>();
    private Map<Item, Long> itemRemoveTime = new HashMap<>();
    private Map<Item, Float> itemOpacity = new HashMap<>();

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
        return item.getItem().getName().getString();
    }

    @Override
    public float getWidth() {
        List<Item> itemsOnCooldown = getItemsOnCooldown();
        if (itemsOnCooldown.isEmpty()) return 0;
        float maxItemNameWidth = itemsOnCooldown.stream()
                .map(item -> RUS.get().getWidth(getFormattedName(new ItemStack(item)), TEXT_HEIGHT))
                .max(Float::compare).orElse(0f);
        return ICON_SIZE + maxItemNameWidth + SPACING + 2 * PADDING;
    }

    @Override
    public float getHeight() {
        List<Item> itemsOnCooldown = getItemsOnCooldown();
        if (itemsOnCooldown.isEmpty()) return 0;
        float total = 0f;
        for (Item item : itemsOnCooldown) {
            float io = itemOpacity.getOrDefault(item, 0f);
            if (io > 0.01f) {
                total += (TEXT_HEIGHT + BAR_HEIGHT + BAR_OFFSET_Y + 4) * io;
            }
        }
        return total + 3;
    }

    @Override
    public void render(GuiGraphics guiGraphics) {
        if (mc.player == null || !HudElements.isOptionEnabled("Cooldowns") || !Api.isEnabled("Hud")) {
            targetOpacity = 0f;
        } else {
            List<Item> itemsOnCooldown = getItemsOnCooldown();
            long now = System.currentTimeMillis();
            if (itemsOnCooldown.isEmpty() && !isChatOpen()) {
                targetOpacity = 0f;
            } else {
                targetOpacity = 1f;
            }
            if (!itemsOnCooldown.equals(lastItems)) {
                targetWidth = getWidth();
                targetHeight = getHeight();
                currentWidth = targetWidth;
                currentHeight = targetHeight;
                animationStartTime = now;
                lastItems = new ArrayList<>(itemsOnCooldown);
            }
            long elapsed = now - animationStartTime;
            float t = Math.min((float) elapsed / ANIMATION_DURATION_MS, 1f);
            opacity = lerp(opacity, targetOpacity, t);
            currentWidth = lerp(currentWidth, targetWidth, t);
            currentHeight = lerp(currentHeight, targetHeight, t);
            if (opacity <= 0.01f || currentWidth <= 0.01f || currentHeight <= 0.01f) return;
            int alpha = (int) (opacity * 255);
            DrawShader.drawRoundBlur(guiGraphics.pose(), xPos, yPos, currentWidth, currentHeight,
                    ROUND_RADIUS, new Color(23, 29, 35, alpha).getRGB(), 120, 0.4f);
            guiGraphics.enableScissor((int) xPos, (int) yPos,
                    (int) (xPos + currentWidth), (int) (yPos + currentHeight));
            float currentY = yPos + PADDING - 1;
            for (Item item : itemsOnCooldown) {
                long appear = itemAppearTime.getOrDefault(item, 0L);
                long remove = itemRemoveTime.getOrDefault(item, 0L);
                float io = itemOpacity.getOrDefault(item, 0f);
                if (itemsOnCooldown.contains(item)) {
                    if (remove != 0) {
                        remove = 0L;
                        itemRemoveTime.remove(item);
                    }
                    if (appear == 0) {
                        appear = now;
                        itemAppearTime.put(item, appear);
                    }
                    long fadeElapsed = now - appear;
                    io = Math.min((float) fadeElapsed / FADE_DURATION_MS, 1f);
                } else {
                    if (remove == 0) {
                        remove = now;
                        itemRemoveTime.put(item, remove);
                    }
                    long fadeElapsed = now - remove;
                    io = 1f - Math.min((float) fadeElapsed / FADE_DURATION_MS, 1f);
                }
                itemOpacity.put(item, io);
                if (io <= 0.01f) continue;
                int itemAlpha = (int) (io * alpha);
                Color textColorWithAlpha = new Color(255, 255, 255, itemAlpha);
                if (item == Items.ENCHANTED_GOLDEN_APPLE) {
                    ResourceLocation itemTexture = ResourceLocation.fromNamespaceAndPath("dsp", "textures/item/golden_apple.png");
                    DrawHelper.drawTexture(itemTexture, guiGraphics.pose().last().pose(),
                            xPos + PADDING - 3, currentY - 2.5f, ICON_SIZE, ICON_SIZE);
                } else if (item == Items.PLAYER_HEAD || item == Items.AIR) {
                    ResourceLocation itemTexture = ResourceLocation.fromNamespaceAndPath("dsp", "textures/item/barrier.png");
                    DrawHelper.drawTexture(itemTexture, guiGraphics.pose().last().pose(),
                            xPos + PADDING - 3, currentY - 2.5f, ICON_SIZE, ICON_SIZE);
                } else {
                    ResourceLocation itemTexture = ResourceLocation.fromNamespaceAndPath("dsp",
                            "textures/item/" + item.getDescriptionId().replace("item.minecraft.", "") + ".png");
                    DrawHelper.drawTexture(itemTexture, guiGraphics.pose().last().pose(),
                            xPos + PADDING - 3, currentY - 2.5f, ICON_SIZE, ICON_SIZE);
                }
                String itemName = getFormattedName(new ItemStack(item));
                BuiltText itemText = Builder.text().font(RUS.get()).text(itemName)
                        .color(textColorWithAlpha).size(TEXT_HEIGHT).thickness(0.1f).build();
                itemText.render(new Matrix4f(), xPos + PADDING + 9, currentY - 3.5f);
                float maxBarWidth = currentWidth - ICON_SIZE - 2 * PADDING - 10;
                DrawHelper.rectangle(guiGraphics.pose(), xPos + PADDING + ICON_SIZE,
                        currentY + TEXT_HEIGHT + BAR_OFFSET_Y - 1, maxBarWidth, BAR_HEIGHT, 1f,
                        new Color(64, 64, 64, itemAlpha).getRGB());
                float cooldownPercent = mc.player.getCooldowns().getCooldownPercent(new ItemStack(item), 0);
                float targetBarWidth = maxBarWidth * cooldownPercent;
                float currentBarWidth = currentBarWidths.getOrDefault(item, targetBarWidth);
                currentBarWidth = lerp(currentBarWidth, targetBarWidth, 0.2f);
                currentBarWidths.put(item, currentBarWidth);
                if (cooldownPercent > 0) {
                    DrawHelper.rectangle(guiGraphics.pose(), xPos + PADDING + ICON_SIZE,
                            currentY + TEXT_HEIGHT + BAR_OFFSET_Y - 1, currentBarWidth, BAR_HEIGHT, 1f,
                            ColorHelper.gradient(ThemesUtil.getCurrentStyle().getColorLowSpeed(1),
                                    ThemesUtil.getCurrentStyle().getColorLowSpeed(2), 20, 10));
                }
                currentY += ((TEXT_HEIGHT + BAR_HEIGHT + BAR_OFFSET_Y + 4) * io);
            }
            guiGraphics.disableScissor();
            itemAppearTime.entrySet().removeIf(e -> !itemsOnCooldown.contains(e.getKey()));
            itemRemoveTime.entrySet().removeIf(e -> !itemsOnCooldown.contains(e.getKey()));
            itemOpacity.entrySet().removeIf(e -> !itemsOnCooldown.contains(e.getKey()) && e.getValue() <= 0.01f);
        }
    }

    private List<Item> getItemsOnCooldown() {
        List<Item> itemsOnCooldown = new ArrayList<>();
        if (mc.player != null) {
            Map<Item, Float> itemCooldowns = new HashMap<>();
            for (ItemStack stack : mc.player.getInventory().items) {
                Item item = stack.getItem();
                float cooldownPercent = mc.player.getCooldowns().getCooldownPercent(new ItemStack(item), 0);
                if (cooldownPercent > 0 && !itemCooldowns.containsKey(item)) {
                    itemCooldowns.put(item, cooldownPercent);
                    itemsOnCooldown.add(item);
                }
            }
            if (itemsOnCooldown.isEmpty() && isChatOpen()) {
                itemsOnCooldown.add(Items.GOLDEN_HELMET);
            }
        }
        return itemsOnCooldown;
    }

    private float lerp(float start, float end, float t) {
        return start + (end - start) * t;
    }
}