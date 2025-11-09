package com.dsp.main.UI.ClickGui.Dropdown.Settings;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BooleanSupplier;

public class ItemListSetting extends Setting {
    private final Set<Item> selectedItems = ConcurrentHashMap.newKeySet();
    private final List<Item> availableItems = new ArrayList<>();
    private final Map<Item, ResourceLocation> itemTextures = new ConcurrentHashMap<>();

    public ItemListSetting(String name) {
        super(name);
        initializeItems();
    }

    public ItemListSetting(String name, BooleanSupplier visibleSupplier) {
        this(name);
        setVisible(visibleSupplier);
    }

    private void initializeItems() {
        for (Item item : BuiltInRegistries.ITEM) {
            if (item == Items.AIR) continue;

            ResourceLocation registryName = BuiltInRegistries.ITEM.getKey(item);
            if (registryName == null) continue;

            String texturePath = registryName.getPath();
            ResourceLocation itemTexture = ResourceLocation.fromNamespaceAndPath("dsp", "textures/item/" + texturePath + ".png");
            ResourceLocation blockTexture = ResourceLocation.fromNamespaceAndPath("dsp", "textures/block/" + texturePath + ".png");

            if (textureExists(itemTexture)) {
                availableItems.add(item);
                itemTextures.put(item, itemTexture);
            } else if (textureExists(blockTexture)) {
                availableItems.add(item);
                itemTextures.put(item, blockTexture);
            }
        }
        availableItems.sort(Comparator.comparing(item ->
                BuiltInRegistries.ITEM.getKey(item).getPath()
        ));
    }

    private boolean textureExists(ResourceLocation location) {
        try {
            String path = "assets/" + location.getNamespace() + "/" + location.getPath();
            InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
            if (stream != null) {
                stream.close();
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public void toggleItem(Item item) {
        if (selectedItems.contains(item)) {
            selectedItems.remove(item);
        } else {
            selectedItems.add(item);
        }
    }

    public boolean isItemSelected(Item item) {
        return selectedItems.contains(item);
    }

    public Set<Item> getSelectedItems() {
        return selectedItems;
    }

    public List<Item> getAvailableItems() {
        return availableItems;
    }

    public ResourceLocation getItemTexture(Item item) {
        return itemTextures.get(item);
    }

    public void setSelectedItems(Set<String> itemNames) {
        selectedItems.clear();
        for (String name : itemNames) {
            ResourceLocation rl = ResourceLocation.tryParse(name);
            if (rl != null) {
                Optional<Holder.Reference<Item>> itemOpt = BuiltInRegistries.ITEM.get(rl);
                if (itemOpt.isPresent()) {
                    Item item = itemOpt.get().value();
                    if (item != Items.AIR) {
                        selectedItems.add(item);
                    }
                }
            }
        }
    }

    public Set<String> getSelectedItemNames() {
        Set<String> names = new HashSet<>();
        for (Item item : selectedItems) {
            ResourceLocation rl = BuiltInRegistries.ITEM.getKey(item);
            if (rl != null) {
                names.add(rl.toString());
            }
        }
        return names;
    }
}