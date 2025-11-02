package com.dsp.main.Functions.Render;

import com.dsp.main.Core.FrndSys.FriendManager;
import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.CheckBox;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.MultiCheckBox;
import com.dsp.main.UI.Themes.ThemesUtil;
import com.dsp.main.Utils.Color.ColorHelper;
import com.dsp.main.Utils.Font.builders.Builder;
import com.dsp.main.Utils.Font.renderers.impl.BuiltText;
import com.dsp.main.Utils.Render.DrawHelper;
import com.dsp.main.Utils.Render.Other.ESPUtils;
import com.dsp.main.Utils.Render.Other.EntityPos;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AirItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.joml.Matrix4f;
import org.joml.Vector3d;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.dsp.main.Main.BIKO_FONT;
import static com.dsp.main.Utils.Minecraft.Client.ClientPlayerUtil.getHealthFromScoreboard;

public class NameTagsModule extends Module {
    private static final Minecraft mc = Minecraft.getInstance();
    private static MultiCheckBox Modi = new MultiCheckBox("Options", Arrays.asList(
            new CheckBox("Players", false),
            new CheckBox("Items", false)
    ));
    private static CheckBox pltItems = new CheckBox("Render Player Items", false).setVisible(() -> Modi.isOptionEnabled("Players"));
    private static CheckBox drawDurab = new CheckBox("Draw Item Decorations", false).setVisible(() -> pltItems.isEnabled());
    private static float displayedHealthPercent = 0.0f;

    public NameTagsModule() {
        super("NameTags", 0, Category.RENDER, "Renders custom name tags above players");
        addSettings(Modi, pltItems, drawDurab);
    }

    @SubscribeEvent
    public void onRenderLevelStage(RenderGuiEvent.Post event) {
        if (mc.level == null || mc.player == null) return;

        if (Modi.isOptionEnabled("Players")) {
            renderplayers(event);
        }
        if (Modi.isOptionEnabled("Items")) {
            renderitems(event);
        }
    }

    private float calculateDynamicYOffset(Entity entity, float baseHeight) {
        if (mc.player == null) return baseHeight + 0.35f;

        float distance = mc.player.distanceTo(entity);
        float baseOffset = 0.2f;

        float distanceFactor = (float) Math.pow(distance, 0.72) * 0.12f;

        return baseHeight + baseOffset + distanceFactor;
    }

    public void renderplayers(RenderGuiEvent event) {
        if (mc.level == null || mc.player == null) return;

        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);

        for (Player ent : mc.level.players()) {
            if (ent == null || mc.player.equals(ent)) continue;

            try {
                boolean isFriend = FriendManager.isFriend(ent.getName().getString());
                float dynamicYOffset = calculateDynamicYOffset(ent, ent.getBbHeight());
                Vector3d entPos = EntityPos.get(ent, dynamicYOffset, partialTick);
                if (entPos == null) continue;

                Vector3d vec = ESPUtils.toScreen(entPos);
                if (vec == null || vec.z == 0) continue;

                String name = ent.getScoreboardName();
                if (name == null) continue;

                float width = BIKO_FONT.get().getWidth(name, 5.5f);
                int healthBarColor = isFriend
                        ? ColorHelper.gradient(new Color(0, 255, 0).getRGB(), new Color(0, 100, 0).getRGB(), 20, 10)
                        : ColorHelper.gradient(ThemesUtil.getCurrentStyle().getColorLowSpeed(1), ThemesUtil.getCurrentStyle().getColorLowSpeed(2), 20, 10);

                DrawHelper.rectangle(
                        event.getGuiGraphics().pose(),
                        (float) vec.x - (width / 2),
                        (float) vec.y,
                        width,
                        BIKO_FONT.get().getMetrics().baselineHeight() * 5f + 6,
                        2f,
                        new Color(30, 30, 30, 220).getRGB()
                );

                float[] healthData = getHealthFromScoreboard(ent);
                if (healthData == null || healthData.length == 0) continue;

                float maxHealth = healthData[0] > 20 ? healthData[0] : 20;
                float targetHealthPercent = healthData[0] / maxHealth;
                displayedHealthPercent += (targetHealthPercent - displayedHealthPercent) * 0.2f;

                DrawHelper.rectangle(
                        event.getGuiGraphics().pose(),
                        (float) vec.x - (width / 2) + 1.5f,
                        (float) (vec.y + BIKO_FONT.get().getMetrics().baselineHeight() * 5f + 3),
                        width - 3,
                        1.5f,
                        0.5f,
                        new Color(64, 64, 64, 255).getRGB()
                );

                DrawHelper.rectangle(
                        event.getGuiGraphics().pose(),
                        (float) vec.x - (width / 2) + 1.5f,
                        (float) (vec.y + BIKO_FONT.get().getMetrics().baselineHeight() * 5f + 3),
                        (width * displayedHealthPercent) - 3,
                        1.5f,
                        0.5f,
                        healthBarColor
                );

                BuiltText text = Builder.text()
                        .font(BIKO_FONT.get())
                        .text(name)
                        .color(Color.WHITE)
                        .size(5.5f)
                        .thickness(0.05f)
                        .build();
                text.render(new Matrix4f(event.getGuiGraphics().pose().last().pose()), vec.x - (width / 2) + 1, vec.y + 1.5f);

                BuiltText hpText = Builder.text()
                        .font(BIKO_FONT.get())
                        .text(String.valueOf((int)healthData[0]))
                        .color(Color.GRAY)
                        .size(4.5f)
                        .thickness(0.05f)
                        .build();
                hpText.render(new Matrix4f(event.getGuiGraphics().pose().last().pose()), (vec.x - width / 2) + width - 1, vec.y + BIKO_FONT.get().getMetrics().baselineHeight() * 5f - 1);

                if (isFriend) {
                    BuiltText friendText = Builder.text()
                            .font(BIKO_FONT.get())
                            .text("F")
                            .color(new Color(0, 255, 0))
                            .size(5.5f)
                            .thickness(0.05f)
                            .build();
                    friendText.render(new Matrix4f(event.getGuiGraphics().pose().last().pose()), (vec.x - width / 2) -4, vec.y + BIKO_FONT.get().getMetrics().baselineHeight() * 3.5f);
                }

                float tagCenterX = (float) vec.x;
                float tagY = (float) vec.y;
                if (pltItems.isEnabled()) {
                    renderPlayerItems(event, tagCenterX, tagY, ent);
                }
            } catch (Exception ignored) {}
        }
    }

    public void renderPlayerItems(RenderGuiEvent event, float tagCenterX, float tagY, Player player) {
        if (player == null) return;

        try {
            List<ItemStack> stacks = new ArrayList<>();
            ItemStack mainHand = player.getMainHandItem();
            ItemStack offHand = player.getOffhandItem();

            if (mainHand != null) stacks.add(mainHand);
            player.getArmorSlots().forEach(s -> {if (s != null) stacks.add(s);});
            if (offHand != null) stacks.add(offHand);
            stacks.removeIf(s -> s == null || s.getItem() instanceof AirItem);

            if (stacks.isEmpty()) return;

            final float scale = 0.5f;
            final float iconSize = 16f;
            final float padding = 1.5f;

            float totalWidth = stacks.size() * (iconSize * scale) + (stacks.size() - 1) * padding;
            float startX = tagCenterX - totalWidth / 2f;
            float startY = tagY - (iconSize * scale) - 3f;

            DrawHelper.rectangle(
                    event.getGuiGraphics().pose(),
                    startX - 1.5f,
                    startY - 1.5f,
                    totalWidth + 3f,
                    iconSize * scale + 3f,
                    2f,
                    new Color(30, 30, 30, 180).getRGB()
            );

            event.getGuiGraphics().pose().pushPose();
            event.getGuiGraphics().pose().translate(startX, startY, 0);
            event.getGuiGraphics().pose().scale(scale, scale, 1f);

            float offset = 0;
            for (ItemStack stack : stacks) {
                event.getGuiGraphics().renderItem(stack, (int)(offset / scale), 0);
                if (drawDurab.isEnabled()) {
                    event.getGuiGraphics().renderItemDecorations(mc.font, stack, (int)(offset / scale), 0);
                }
                offset += iconSize * scale + padding;
            }
            event.getGuiGraphics().pose().popPose();
        } catch (Exception ignored) {}
    }

    public void renderitems(RenderGuiEvent event) {
        if (mc.level == null || mc.player == null) return;

        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);

        try {
            for (Entity ent : mc.level.getEntities((Entity) null, mc.player.getBoundingBox().inflate(128.0), e -> e instanceof ItemEntity)) {
                if (ent == null) continue;

                try {
                    float dynamicYOffset = calculateDynamicYOffset(ent, ent.getBbHeight());
                    Vector3d entPos = EntityPos.get(ent, dynamicYOffset, partialTick);
                    if (entPos == null) continue;

                    Vector3d vec = ESPUtils.toScreen(entPos);
                    if (vec == null || vec.z == 0) continue;

                    String name = ent.getName().getString();
                    if (name == null) continue;

                    String full = " " + name + " [" + (int) ent.distanceTo(mc.player) + "M] ";
                    float width = BIKO_FONT.get().getWidth(full, 5.5f);
                    float height = BIKO_FONT.get().getMetrics().baselineHeight() * 5.5f + 3;

                    event.getGuiGraphics().pose().pushPose();
                    event.getGuiGraphics().pose().translate(vec.x - (width / 2), vec.y, 0);
                    DrawHelper.rectangle(event.getGuiGraphics().pose(), 0, 0, width, height, 1.5f, new Color(30, 30, 30, 150).getRGB());

                    BuiltText text = Builder.text()
                            .font(BIKO_FONT.get())
                            .text(full)
                            .color(new Color(143, 83, 83).getRGB())
                            .size(5.5f)
                            .thickness(0.05f)
                            .build();
                    text.render(new Matrix4f(event.getGuiGraphics().pose().last().pose()), 0, 1.5f);
                    event.getGuiGraphics().pose().popPose();
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
    }
}