package com.dsp.main.UI.ClickGui.Dropdown.Settings;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BooleanSupplier;

public class BlockListSetting extends Setting {
    private final Set<Block> selectedBlocks = ConcurrentHashMap.newKeySet();
    private final List<Block> availableBlocks = new ArrayList<>();
    private final Map<Block, ResourceLocation> blockTextures = new ConcurrentHashMap<>();

    public BlockListSetting(String name) {
        super(name);
        initializeBlocks();
    }

    public BlockListSetting(String name, BooleanSupplier visibleSupplier) {
        this(name);
        setVisible(visibleSupplier);
    }

    private void initializeBlocks() {
        for (Block block : BuiltInRegistries.BLOCK) {
            if (block == Blocks.AIR || block == Blocks.CAVE_AIR || block == Blocks.VOID_AIR) continue;

            ResourceLocation registryName = BuiltInRegistries.BLOCK.getKey(block);
            if (registryName == null) continue;

            String texturePath = registryName.getPath();
            ResourceLocation textureLocation = ResourceLocation.fromNamespaceAndPath("dsp", "textures/block/" + texturePath + ".png");

            if (textureExists(textureLocation)) {
                availableBlocks.add(block);
                blockTextures.put(block, textureLocation);
            }
        }
        availableBlocks.sort(Comparator.comparing(block ->
                BuiltInRegistries.BLOCK.getKey(block).getPath()
        ));
    }

    private boolean textureExists(ResourceLocation location) {
        try {
            return Thread.currentThread().getContextClassLoader()
                    .getResource("assets/" + location.getNamespace() + "/" + location.getPath()) != null;
        } catch (Exception e) {
            return false;
        }
    }

    public void toggleBlock(Block block) {
        if (selectedBlocks.contains(block)) {
            selectedBlocks.remove(block);
        } else {
            selectedBlocks.add(block);
        }
    }

    public boolean isBlockSelected(Block block) {
        return selectedBlocks.contains(block);
    }

    public Set<Block> getSelectedBlocks() {
        return selectedBlocks;
    }

    public List<Block> getAvailableBlocks() {
        return availableBlocks;
    }

    public ResourceLocation getBlockTexture(Block block) {
        return blockTextures.get(block);
    }

    public void setSelectedBlocks(Set<String> blockNames) {
        selectedBlocks.clear();
        for (String name : blockNames) {
            ResourceLocation rl = ResourceLocation.tryParse(name);
            if (rl != null) {
                Optional<Holder.Reference<Block>> blockOpt = BuiltInRegistries.BLOCK.get(rl);
                if (blockOpt.isPresent()) {
                    Block block = blockOpt.get().value();
                    if (block != Blocks.AIR) {
                        selectedBlocks.add(block);
                    }
                }
            }
        }
    }

    public Set<String> getSelectedBlockNames() {
        Set<String> names = new HashSet<>();
        for (Block block : selectedBlocks) {
            ResourceLocation rl = BuiltInRegistries.BLOCK.getKey(block);
            if (rl != null) {
                names.add(rl.toString());
            }
        }
        return names;
    }
}