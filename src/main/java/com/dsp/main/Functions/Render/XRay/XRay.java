package com.dsp.main.Functions.Render.XRay;

import com.dsp.main.Core.Event.OnUpdate;
import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.BlockListSetting;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.Slider;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

public class XRay extends Module {
    private final BlockListSetting blocks;
    private final Slider radius;

    public XRay() {
        super("XRay", 0, Category.RENDER, "See selected blocks through walls");

        blocks = new BlockListSetting("Blocks");
        radius = new Slider("Radius", 16, 256, 64, 1);

        addSettings(blocks, radius);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        XRayRenderer.init();
        updateRenderer();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        XRayRenderer.shutdown();
    }

    @SubscribeEvent
    public void onUpdate(OnUpdate event) {
        int newRadius = (int) radius.getValue();
        XRayRenderer.setRadius(newRadius);
        XRayRenderer.setTargetBlocks(blocks.getSelectedBlocks());
    }

    @SubscribeEvent
    public void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        XRayRenderer.render(event.getPoseStack(), event.getCamera());
    }
    private void updateRenderer() {
        XRayRenderer.setRadius((int) radius.getValue());
        XRayRenderer.setTargetBlocks(blocks.getSelectedBlocks());
        XRayRenderer.markDirty();
    }

    public BlockListSetting getBlocksSetting() {
        return blocks;
    }
}