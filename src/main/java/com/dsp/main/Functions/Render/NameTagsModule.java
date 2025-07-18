package com.dsp.main.Functions.Render;

import com.dsp.main.Managers.FrndSys.FriendManager;
import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Settings.CheckBox;
import com.dsp.main.UI.ClickGui.Settings.MultiCheckBox;
import com.dsp.main.UI.Themes.ThemesUtil;
import com.dsp.main.Utils.Color.ColorHelper;
import com.dsp.main.Utils.Font.builders.Builder;
import com.dsp.main.Utils.Font.renderers.impl.BuiltText;
import com.dsp.main.Utils.Minecraft.Chat.ChatUtil;
import com.dsp.main.Utils.Render.DrawHelper;
import com.dsp.main.Utils.Render.Other.ESPUtils;
import com.dsp.main.Utils.Render.Other.EntityPos;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AirItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
        addSettings(Modi,pltItems,drawDurab);
    }

    @SubscribeEvent
    public void onRenderLevelStage(RenderGuiEvent.Post event) {
        if (mc.level == null || mc.player == null) {
            System.out.println("World or player is null, skipping render");
            return;
        }
        if (Modi.isOptionEnabled("Players")) {
            renderplayers(event);
        }
        if (Modi.isOptionEnabled("Items")) {
            renderitems(event);
        }

    }

    public void renderplayers(RenderGuiEvent event) {
        for (Player ent : mc.level.players()) {
            if (mc.player.equals(ent)) continue;
            boolean isFriend = FriendManager.isFriend(ent.getName().getString());
            Vector3d entPos = EntityPos.get(ent, 2.5f, event.getPartialTick().getRealtimeDeltaTicks());
            Vector3d vec = ESPUtils.toScreen(entPos);
            if (vec.z == 0) continue;
            String name = ent.getScoreboardName();

            float width = BIKO_FONT.get().getWidth(name, 8f);
            int healthBarColor = isFriend
                    ? ColorHelper.gradient(new Color(0, 255, 0).getRGB(), new Color(0, 100, 0).getRGB(), 20, 10)
                    : ColorHelper.gradient(ThemesUtil.getCurrentStyle().getColorLowSpeed(1), ThemesUtil.getCurrentStyle().getColorLowSpeed(2), 20, 10);
            DrawHelper.rectangle(
                    event.getGuiGraphics().pose(),
                    (float) vec.x - (width / 2),
                    (float) vec.y,
                    width,
                    BIKO_FONT.get().getMetrics().baselineHeight() * 7f + 10,
                    3f,
                    new Color(30, 30, 30, 220).getRGB()
            );
            // ----
            float maxHealth = 20;
            if (getHealthFromScoreboard(ent)[0] > 20) {
                maxHealth = getHealthFromScoreboard(ent)[0];
            }
            float targetHealthPercent = getHealthFromScoreboard(ent)[0] / maxHealth;
            displayedHealthPercent += (targetHealthPercent - displayedHealthPercent) * 0.2f;
            DrawHelper.rectangle(event.getGuiGraphics().pose(), (float) vec.x - (width / 2) + 2, (float) (vec.y + BIKO_FONT.get().getMetrics().baselineHeight() * 7f + 5), width - 4, 2, 1, new Color(64, 64, 64, 255).getRGB());
            DrawHelper.rectangle(event.getGuiGraphics().pose(), (float) vec.x - (width / 2) + 2, (float) (vec.y + BIKO_FONT.get().getMetrics().baselineHeight() * 7f + 5), (width * displayedHealthPercent) - 4, 2, 1, healthBarColor);
            //----
            BuiltText text = Builder.text()
                    .font(BIKO_FONT.get())
                    .text(name)
                    .color(Color.WHITE)
                    .size(7f)
                    .thickness(0.05f)
                    .build();
            text.render(new Matrix4f(event.getGuiGraphics().pose().last().pose()), vec.x - (width / 2) + 1, vec.y + 2);
            BuiltText hpText = Builder.text()
                    .font(BIKO_FONT.get())
                    .text(String.valueOf(getHealthFromScoreboard(ent)[0]))
                    .color(Color.GRAY)
                    .size(6f)
                    .thickness(0.05f)
                    .build();
            hpText.render(new Matrix4f(event.getGuiGraphics().pose().last().pose()), (vec.x - width /2) + width, vec.y + BIKO_FONT.get().getMetrics().baselineHeight() * 7f);

            if (isFriend) {
                BuiltText friendText = Builder.text()
                        .font(BIKO_FONT.get())
                        .text("F ")
                        .color(new Color(0, 255, 0))
                        .size(8f)
                        .thickness(0.05f)
                        .build();
                friendText.render(new Matrix4f(event.getGuiGraphics().pose().last().pose()), (vec.x + width / 2) - (width + 10), vec.y + BIKO_FONT.get().getMetrics().baselineHeight() * 5f);
            }
            //----
            float tagCenterX = (float) vec.x;
            float tagY       = (float) vec.y;
            if (pltItems.isEnabled()) {
                renderPlayerItems(event, tagCenterX, tagY, ent);
            }
        }
    }

    public void renderPlayerItems(RenderGuiEvent event, float tagCenterX, float tagY, Player player) {
        List<ItemStack> stacks = new ArrayList<>();
        stacks.add(player.getMainHandItem());
        player.getArmorSlots().forEach(stacks::add);
        stacks.add(player.getOffhandItem());
        stacks.removeIf(s -> s.getItem() instanceof AirItem);

        if (stacks.isEmpty()) return;

        final float scale = 0.65f;
        final float iconSize = 16f;
        final float padding = 2f;

        float totalWidth = stacks.size() * (iconSize * scale)
                + (stacks.size() - 1) * padding;
        float startX = tagCenterX - totalWidth / 2f;
        float startY = tagY - (iconSize * scale) - 4f;
        DrawHelper.rectangle(
                event.getGuiGraphics().pose(),
                startX - 2f,
                startY - 2f,
                totalWidth + 4f,
                iconSize * scale + 4f,
                3f,
                new Color(30, 30, 30, 180).getRGB()
        );
        event.getGuiGraphics().pose().pushPose();
        event.getGuiGraphics().pose().translate(startX, startY, 0);
        event.getGuiGraphics().pose().scale(scale, scale, 1f);

        float offset = 0;
        for (ItemStack stack : stacks) {
            event.getGuiGraphics().renderItem(stack, (int)(offset / scale), 0);
            if (drawDurab.isEnabled()) event.getGuiGraphics().renderItemDecorations(mc.font, stack, (int)(offset / scale), 1);
            offset += iconSize * scale + padding;
        }
        event.getGuiGraphics().pose().popPose();
    }


    public void renderitems(RenderGuiEvent event) {
        assert mc.level != null;
        for (Entity ent : mc.level.getEntities((Entity) null, mc.player.getBoundingBox().inflate(128.0), e -> true)) {
            if (ent instanceof ItemEntity) {
                Vector3d vec = ESPUtils.toScreen(EntityPos.get(ent, 0.9f, event.getPartialTick().getRealtimeDeltaTicks()));
                if (vec.z == 0) continue;
                String name = ent.getName().getString();
                assert mc.player != null;
                String full = " " + name + " [" + (int) ent.distanceTo(mc.player) + "M] ";
                float width = BIKO_FONT.get().getWidth(full, 8f);
                float height = BIKO_FONT.get().getMetrics().baselineHeight() * 8f + 5;

                event.getGuiGraphics().pose().pushPose();
                event.getGuiGraphics().pose().translate(vec.x - (width / 2), vec.y, 0);
                DrawHelper.rectangle(event.getGuiGraphics().pose(), 0, 0, width, height, 0.5f, new Color(30, 30, 30, 150).getRGB());
                BuiltText text = Builder.text()
                        .font(BIKO_FONT.get())
                        .text(full)
                        .color(new Color(143, 83, 83).getRGB())
                        .size(8f)
                        .thickness(0.05f)
                        .build();
                text.render(new Matrix4f(event.getGuiGraphics().pose().last().pose()), 0, 2f);
                event.getGuiGraphics().pose().popPose();
            }
        }
    }
}