package com.dsp.main.Functions.Misc;

import com.dsp.main.Core.Event.OnUpdate;
import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.*;
import com.dsp.main.Utils.Font.builders.Builder;
import com.dsp.main.Utils.Font.builders.states.QuadColorState;
import com.dsp.main.Utils.Font.builders.states.QuadRadiusState;
import com.dsp.main.Utils.Font.builders.states.SizeState;
import com.dsp.main.Utils.Font.renderers.impl.BuiltBorder;
import com.dsp.main.Utils.Minecraft.Chat.ChatUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.List;
import java.util.*;

import static com.dsp.main.Api.mc;

public class AuctionHelper extends Module {

    private final CheckBox filterEnabled = new CheckBox("Enable Filter", true);
    private final CheckBox countStacks = new CheckBox("Count Stacks", false);
    private final MultiCheckBox enchantFilter = new MultiCheckBox("Enchantments", List.of(
            new CheckBox("Protection", false),
            new CheckBox("Sharpness", false),
            new CheckBox("Efficiency", false),
            new CheckBox("Unbreaking", false),
            new CheckBox("Mending", false),
            new CheckBox("Fortune", false),
            new CheckBox("Silk Touch", false),
            new CheckBox("Thorns", false),
            new CheckBox("Depth Strider", false)
    ));

    private int cheapestSingle = -1;
    private int cheapestStack = -1;

    public AuctionHelper() {
        super("AuctionHelper", 0, Category.MISC, "Helps find cheapest items on auction");
        addSettings(filterEnabled, countStacks, enchantFilter);
    }

    @SubscribeEvent
    public void onUpdate(OnUpdate e) {
        if (!(mc.screen instanceof AbstractContainerScreen<?> screen)) return;
        if (!screen.getTitle().getString().contains("Аукцион") &&
                !screen.getTitle().getString().contains("Поиск:")) return;

        cheapestSingle = -1;
        cheapestStack = -1;

        screen.getMenu().slots.forEach(slot -> {
            if (slot.container != mc.player.getInventory() && !slot.getItem().isEmpty()) {
                ItemStack stack = slot.getItem();
                if (!filterEnabled.isEnabled() || matchesFilter(stack)) {
                    int price = extractPrice(stack);
                    if (price > 0) {
                        if (countStacks.isEnabled() && stack.getCount() > 1) {
                            if (cheapestStack == -1 || price < cheapestStack) {
                                cheapestStack = price;
                            }
                        } else {
                            if (cheapestSingle == -1 || price < cheapestSingle) {
                                cheapestSingle = price;
                            }
                        }
                    }
                }
            }
        });
    }

    private boolean matchesFilter(ItemStack stack) {
        ItemEnchantments enchants = stack.getEnchantments();
        var registry = mc.level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);

        for (CheckBox cb : enchantFilter.getOptions()) {
            if (!cb.isEnabled()) continue;

            Holder<Enchantment> holder = switch (cb.getName()) {
                case "Protection"   -> registry.getHolderOrThrow(Enchantments.PROTECTION);
                case "Sharpness"    -> registry.getHolderOrThrow(Enchantments.SHARPNESS);
                case "Efficiency"   -> registry.getHolderOrThrow(Enchantments.EFFICIENCY);
                case "Unbreaking"   -> registry.getHolderOrThrow(Enchantments.UNBREAKING);
                case "Mending"      -> registry.getHolderOrThrow(Enchantments.MENDING);
                case "Fortune"      -> registry.getHolderOrThrow(Enchantments.FORTUNE);
                case "Silk Touch"   -> registry.getHolderOrThrow(Enchantments.SILK_TOUCH);
                case "Thorns"       -> registry.getHolderOrThrow(Enchantments.THORNS);
                case "Depth Strider"-> registry.getHolderOrThrow(Enchantments.DEPTH_STRIDER);
                default -> null;
            };
            if (holder != null && enchants.getLevel(holder) <= 0) return false;
        }
        return true;
    }

    private int extractPrice(ItemStack stack) {
        var ctx = Item.TooltipContext.of(Minecraft.getInstance().level);
        List<net.minecraft.network.chat.Component> tooltip = stack.getTooltipLines(ctx, Minecraft.getInstance().player, TooltipFlag.NORMAL);

        for (net.minecraft.network.chat.Component line : tooltip) {
            String text = line.getString();
            if (text.contains("Цена:") || text.contains("Price:") || text.contains("Ценa")) {
                String digits = text.replaceAll("[^0-9]", "");
                if (!digits.isEmpty()) {
                    try {
                        return Integer.parseInt(digits);
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        return -1;
    }

    public void renderBorders(RenderGuiEvent.Post e) {
        if (!(mc.screen instanceof AbstractContainerScreen<?> screen)) return;

        int guiLeft = screen.getGuiLeft();
        int guiTop  = screen.getGuiTop();

        screen.getMenu().slots.forEach(slot -> {
            if (slot.container == mc.player.getInventory() || slot.getItem().isEmpty()) return;

            ItemStack stack = slot.getItem();
            int price = extractPrice(stack);
            if (price <= 0) return;

            boolean isSingle = !countStacks.isEnabled() || stack.getCount() == 1;
            boolean isStack  = countStacks.isEnabled() && stack.getCount() > 1;

            if ((isSingle && price == cheapestSingle) || (isStack && price == cheapestStack)) {
                Color color = isStack ? Color.ORANGE : Color.GREEN;

                BuiltBorder border = Builder.border()
                        .size(new SizeState(16, 16))
                        .color(new QuadColorState(color.getRGB(), color.getRGB(), color.getRGB(), color.getRGB()))
                        .radius(new QuadRadiusState(2f, 2f, 2f, 2f))
                        .thickness(1f)
                        .smoothness(0.9f, 0.9f)
                        .build();

                // Координаты уже в экранном пространстве
                int x = slot.x;
                int y = slot.y;

                // Отрисовываем прямо в текущем PoseStack
                border.render(e.getGuiGraphics().pose().last().pose(),
                        x, y, 0);   // z-уровень можно оставить 0
                e.getGuiGraphics().fill(x, y, x + 16, y + 16, 0xFFFF0000);
            }
        });
    }
    @SubscribeEvent
    public void onRender(RenderGuiEvent.Post e) {
        if (mc.player == null) return;
        renderBorders(e);
    }
}