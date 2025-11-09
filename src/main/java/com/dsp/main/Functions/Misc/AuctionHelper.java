package com.dsp.main.Functions.Misc;

import com.dsp.main.Core.Event.OnUpdate;
import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.*;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.*;

import static com.dsp.main.Api.mc;

public class AuctionHelper extends Module {

    private static AuctionHelper instance;

    private final CheckBox filterEnabled = new CheckBox("Enable Filter", true);
    private final CheckBox countStacks = new CheckBox("Count Stacks", false);

    private final MultiCheckBox enchantFilter = new MultiCheckBox("Enchantments", List.of(
            new CheckBox("Protection", false),
            new CheckBox("Fire Protection", false),
            new CheckBox("Blast Protection", false),
            new CheckBox("Projectile Protection", false),
            new CheckBox("Feather Falling", false),
            new CheckBox("Thorns", false),
            new CheckBox("Respiration", false),
            new CheckBox("Aqua Affinity", false),
            new CheckBox("Depth Strider", false),
            new CheckBox("Frost Walker", false),
            new CheckBox("Soul Speed", false),
            new CheckBox("Swift Sneak", false),
            new CheckBox("Sharpness", false),
            new CheckBox("Smite", false),
            new CheckBox("Bane of Arthropods", false),
            new CheckBox("Knockback", false),
            new CheckBox("Fire Aspect", false),
            new CheckBox("Looting", false),
            new CheckBox("Sweeping Edge", false),
            new CheckBox("Efficiency", false),
            new CheckBox("Silk Touch", false),
            new CheckBox("Fortune", false),
            new CheckBox("Power", false),
            new CheckBox("Punch", false),
            new CheckBox("Flame", false),
            new CheckBox("Infinity", false),
            new CheckBox("Luck of the Sea", false),
            new CheckBox("Lure", false),
            new CheckBox("Loyalty", false),
            new CheckBox("Impaling", false),
            new CheckBox("Riptide", false),
            new CheckBox("Channeling", false),
            new CheckBox("Multishot", false),
            new CheckBox("Quick Charge", false),
            new CheckBox("Piercing", false),
            new CheckBox("Unbreaking", false),
            new CheckBox("Mending", false),
            new CheckBox("Curse of Binding", false),
            new CheckBox("Curse of Vanishing", false)
    ));

    private final MultiCheckBox customEnchantFilter = new MultiCheckBox("Custom Enchants (Funtime)", List.of(
            new CheckBox("Магнит", false),
            new CheckBox("Пингер", false),
            new CheckBox("Бульдозер", false),
            new CheckBox("Мега-бульдозер", false),
            new CheckBox("Паутина", false),
            new CheckBox("Авто-Плавка", false),
            new CheckBox("Опытный", false),
            new CheckBox("Яд", false),
            new CheckBox("Детекция", false),
            new CheckBox("Окисление", false),
            new CheckBox("Вампиризм", false),
            new CheckBox("Лаваход", false)
    ));

    private final MultiCheckBox itemStateFilter = new MultiCheckBox("Item State", List.of(
            new CheckBox("Full Durability Only", false),
            new CheckBox("Damaged Only", false),
            new CheckBox("Stackable Only", false),
            new CheckBox("Enchanted Only", false),
            new CheckBox("Non-Enchanted Only", false)
    ));

    private final Map<Slot, Integer> highlightedSlots = new HashMap<>();
    private int cheapestSingle = -1;
    private int cheapestStack = -1;

    public AuctionHelper() {
        super("AuctionHelper", 0, Category.MISC, "Helps find cheapest items on auction");
        addSettings(filterEnabled, countStacks, enchantFilter, customEnchantFilter, itemStateFilter);
        instance = this;
    }

    public static AuctionHelper getInstance() {
        return instance;
    }

    @SubscribeEvent
    public void onUpdate(OnUpdate e) {
        highlightedSlots.clear();

        if (mc.player == null || mc.level == null) return;
        if (!(mc.screen instanceof AbstractContainerScreen<?> screen)) return;

        String title = screen.getTitle().getString();
        if (!title.contains("Аукцион") && !title.contains("Поиск:")) return;

        cheapestSingle = -1;
        cheapestStack = -1;

        Map<Slot, Integer> tempPrices = new HashMap<>();

        for (Slot slot : screen.getMenu().slots) {
            if (slot.container == mc.player.getInventory()) continue;
            if (slot.getItem().isEmpty()) continue;

            ItemStack stack = slot.getItem();
            if (!filterEnabled.isEnabled() || (matchesEnchantFilter(stack) && matchesCustomEnchantFilter(stack) && matchesStateFilter(stack))) {
                int price = extractPrice(stack);
                if (price > 0) {
                    tempPrices.put(slot, price);
                }
            }
        }

        for (Map.Entry<Slot, Integer> entry : tempPrices.entrySet()) {
            Slot slot = entry.getKey();
            int price = entry.getValue();
            ItemStack stack = slot.getItem();

            boolean isStack = countStacks.isEnabled() && stack.getCount() > 1;

            if (isStack) {
                if (cheapestStack == -1 || price < cheapestStack) {
                    cheapestStack = price;
                }
            } else {
                if (cheapestSingle == -1 || price < cheapestSingle) {
                    cheapestSingle = price;
                }
            }
        }

        for (Map.Entry<Slot, Integer> entry : tempPrices.entrySet()) {
            Slot slot = entry.getKey();
            int price = entry.getValue();
            ItemStack stack = slot.getItem();

            boolean isStack = countStacks.isEnabled() && stack.getCount() > 1;

            if (isStack && price == cheapestStack) {
                highlightedSlots.put(slot, 1);
            } else if (!isStack && price == cheapestSingle) {
                highlightedSlots.put(slot, 0);
            }
        }
    }

    public Integer getSlotHighlight(Slot slot) {
        return highlightedSlots.get(slot);
    }

    private boolean isFuntimeServer() {
        ServerData serverData = mc.getCurrentServer();
        if (serverData == null) return false;
        return serverData.ip.toLowerCase().contains("funtime");
    }

    private boolean matchesEnchantFilter(ItemStack stack) {
        if (mc.level == null) return false;

        ItemEnchantments enchants = stack.getEnchantments();
        HolderLookup.Provider lookupProvider = mc.level.registryAccess();
        HolderLookup<Enchantment> enchantLookup = lookupProvider.lookupOrThrow(Registries.ENCHANTMENT);

        for (CheckBox cb : enchantFilter.getOptions()) {
            if (!cb.isEnabled()) continue;

            Optional<Holder.Reference<Enchantment>> holderOpt = switch (cb.getName()) {
                case "Protection" -> enchantLookup.get(Enchantments.PROTECTION);
                case "Fire Protection" -> enchantLookup.get(Enchantments.FIRE_PROTECTION);
                case "Blast Protection" -> enchantLookup.get(Enchantments.BLAST_PROTECTION);
                case "Projectile Protection" -> enchantLookup.get(Enchantments.PROJECTILE_PROTECTION);
                case "Feather Falling" -> enchantLookup.get(Enchantments.FEATHER_FALLING);
                case "Thorns" -> enchantLookup.get(Enchantments.THORNS);
                case "Respiration" -> enchantLookup.get(Enchantments.RESPIRATION);
                case "Aqua Affinity" -> enchantLookup.get(Enchantments.AQUA_AFFINITY);
                case "Depth Strider" -> enchantLookup.get(Enchantments.DEPTH_STRIDER);
                case "Frost Walker" -> enchantLookup.get(Enchantments.FROST_WALKER);
                case "Soul Speed" -> enchantLookup.get(Enchantments.SOUL_SPEED);
                case "Swift Sneak" -> enchantLookup.get(Enchantments.SWIFT_SNEAK);
                case "Sharpness" -> enchantLookup.get(Enchantments.SHARPNESS);
                case "Smite" -> enchantLookup.get(Enchantments.SMITE);
                case "Bane of Arthropods" -> enchantLookup.get(Enchantments.BANE_OF_ARTHROPODS);
                case "Knockback" -> enchantLookup.get(Enchantments.KNOCKBACK);
                case "Fire Aspect" -> enchantLookup.get(Enchantments.FIRE_ASPECT);
                case "Looting" -> enchantLookup.get(Enchantments.LOOTING);
                case "Sweeping Edge" -> enchantLookup.get(Enchantments.SWEEPING_EDGE);
                case "Efficiency" -> enchantLookup.get(Enchantments.EFFICIENCY);
                case "Silk Touch" -> enchantLookup.get(Enchantments.SILK_TOUCH);
                case "Fortune" -> enchantLookup.get(Enchantments.FORTUNE);
                case "Power" -> enchantLookup.get(Enchantments.POWER);
                case "Punch" -> enchantLookup.get(Enchantments.PUNCH);
                case "Flame" -> enchantLookup.get(Enchantments.FLAME);
                case "Infinity" -> enchantLookup.get(Enchantments.INFINITY);
                case "Luck of the Sea" -> enchantLookup.get(Enchantments.LUCK_OF_THE_SEA);
                case "Lure" -> enchantLookup.get(Enchantments.LURE);
                case "Loyalty" -> enchantLookup.get(Enchantments.LOYALTY);
                case "Impaling" -> enchantLookup.get(Enchantments.IMPALING);
                case "Riptide" -> enchantLookup.get(Enchantments.RIPTIDE);
                case "Channeling" -> enchantLookup.get(Enchantments.CHANNELING);
                case "Multishot" -> enchantLookup.get(Enchantments.MULTISHOT);
                case "Quick Charge" -> enchantLookup.get(Enchantments.QUICK_CHARGE);
                case "Piercing" -> enchantLookup.get(Enchantments.PIERCING);
                case "Unbreaking" -> enchantLookup.get(Enchantments.UNBREAKING);
                case "Mending" -> enchantLookup.get(Enchantments.MENDING);
                case "Curse of Binding" -> enchantLookup.get(Enchantments.BINDING_CURSE);
                case "Curse of Vanishing" -> enchantLookup.get(Enchantments.VANISHING_CURSE);
                default -> Optional.empty();
            };

            if (holderOpt.isPresent() && enchants.getLevel(holderOpt.get()) <= 0) {
                return false;
            }
        }
        return true;
    }

    private boolean matchesCustomEnchantFilter(ItemStack stack) {
        if (!isFuntimeServer()) return true;

        if (mc.level == null || mc.player == null) return false;

        boolean anyCustomEnchantEnabled = customEnchantFilter.getOptions().stream()
                .anyMatch(CheckBox::isEnabled);

        if (!anyCustomEnchantEnabled) return true;

        var ctx = Item.TooltipContext.of(mc.level);
        List<net.minecraft.network.chat.Component> tooltip = stack.getTooltipLines(ctx, mc.player, TooltipFlag.NORMAL);

        StringBuilder fullTooltip = new StringBuilder();
        for (net.minecraft.network.chat.Component line : tooltip) {
            fullTooltip.append(line.getString()).append("\n");
        }
        String tooltipText = fullTooltip.toString();

        for (CheckBox cb : customEnchantFilter.getOptions()) {
            if (!cb.isEnabled()) continue;

            String enchantName = cb.getName();
            if (!tooltipText.contains(enchantName)) {
                return false;
            }
        }

        return true;
    }

    private boolean matchesStateFilter(ItemStack stack) {
        for (CheckBox cb : itemStateFilter.getOptions()) {
            if (!cb.isEnabled()) continue;

            boolean matches = switch (cb.getName()) {
                case "Full Durability Only" -> {
                    if (!stack.isDamageableItem()) yield true;
                    yield stack.getDamageValue() == 0;
                }
                case "Damaged Only" -> {
                    if (!stack.isDamageableItem()) yield false;
                    yield stack.getDamageValue() > 0;
                }
                case "Stackable Only" -> stack.getMaxStackSize() > 1;
                case "Enchanted Only" -> !stack.getEnchantments().isEmpty();
                case "Non-Enchanted Only" -> stack.getEnchantments().isEmpty();
                default -> true;
            };

            if (!matches) return false;
        }
        return true;
    }

    private int extractPrice(ItemStack stack) {
        if (mc.level == null || mc.player == null) return -1;

        var ctx = Item.TooltipContext.of(mc.level);
        List<net.minecraft.network.chat.Component> tooltip = stack.getTooltipLines(ctx, mc.player, TooltipFlag.NORMAL);

        for (net.minecraft.network.chat.Component line : tooltip) {
            String text = line.getString();
            if (text.contains("Цена:") || text.contains("Price:") || text.contains("Ценa")) {
                String digits = text.replaceAll("[^0-9]", "");
                if (!digits.isEmpty()) {
                    try {
                        return Integer.parseInt(digits);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        return -1;
    }
}