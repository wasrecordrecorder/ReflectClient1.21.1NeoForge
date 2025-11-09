package com.dsp.main.Functions.Render;

import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.CheckBox;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.MultiCheckBox;
import com.dsp.main.UI.Themes.ThemesUtil;
import com.dsp.main.Utils.Font.builders.Builder;
import com.dsp.main.Utils.Font.renderers.impl.BuiltText;
import com.dsp.main.Utils.Render.ColorUtil;
import com.dsp.main.Utils.Render.DrawHelper;
import com.dsp.main.Utils.Render.Other.ESPUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Vector3d;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.dsp.main.Main.BIKO_FONT;

public class Predictions extends Module {
    private static final Minecraft mc = Minecraft.getInstance();
    private static MultiCheckBox Modi = new MultiCheckBox("Options", Arrays.asList(
            new CheckBox("Ender Pearls", false),
            new CheckBox("Trident", false),
            new CheckBox("Arrows", false),
            new CheckBox("Splash Potions", false)
    ));
    private static CheckBox DrawTrajectory = new CheckBox("Render Trajectory", false).setVisible(() -> Modi.hasAnyEnabled());
    private static CheckBox Preview = new CheckBox("Preview", false).setVisible(() -> Modi.hasAnyEnabled());
    private static CheckBox DrawRadius = new CheckBox("Render Radius", true).setVisible(() -> Modi.isOptionEnabled("Splash Potions"));

    public Predictions() {
        super("Predictions", 0, Category.RENDER, "Rendering predicted pos to Pearls and other");
        addSettings(Modi, DrawTrajectory, Preview, DrawRadius);
    }

    @SubscribeEvent
    public void onRenderLevelStage(RenderGuiEvent.Post event) {
        if (mc.level == null || mc.player == null) {
            return;
        }
        if (Modi.isOptionEnabled("Ender Pearls")) {
            renderEnderPearls(event);
        }
        if (Modi.isOptionEnabled("Trident")) {
            renderTridents(event);
        }
        if (Modi.isOptionEnabled("Arrows")) {
            renderArrows(event);
        }
        if (Modi.isOptionEnabled("Splash Potions")) {
            renderSplashPotions(event);
        }
    }

    public void renderSplashPotions(RenderGuiEvent event) {
        if (mc.level == null || mc.player == null) return;

        for (Entity ent : mc.level.getEntities((Entity) null, mc.player.getBoundingBox().inflate(128.0), e -> e instanceof ThrownPotion)) {
            ThrownPotion potion = (ThrownPotion) ent;

            Vec3 landingPos = predictLandingPosition(potion);
            double timeToLand = predictTimeToLand(potion);
            if (timeToLand == 0) continue;

            Vector3d screenPos = ESPUtils.toScreen(landingPos);
            if (screenPos.z == 0) continue;
            if (potion.getOwner() == null) continue;

            boolean isHarmful = isPotionHarmful(potion);
            String effectType = isHarmful ? "Harmful" : "Beneficial";
            String text = String.format("Potion %s (%.1f)", effectType, timeToLand);

            float width = BIKO_FONT.get().getWidth(text, 8f);
            int X = (int) (screenPos.x - 5);
            float height = BIKO_FONT.get().getMetrics().baselineHeight() * 8f + 5;

            Color boxColor = isHarmful ? new Color(50, 30, 30, 150) : new Color(30, 50, 30, 150);
            DrawHelper.rectangle(event.getGuiGraphics().pose(), (float) (X - width / 2) - 5, (float) screenPos.y - 2, width + 19, height + 4, 3f, boxColor.getRGB());

            ResourceLocation itemTexture = ResourceLocation.fromNamespaceAndPath("dsp", "textures/item/splash_potion.png");
            DrawHelper.drawTexture(itemTexture, event.getGuiGraphics().pose().last().pose(), (float) (X - width / 2) - 5, (float) screenPos.y - 1, 12, 12);

            Color textColor = isHarmful ? new Color(255, 100, 100) : new Color(100, 255, 100);
            BuiltText builtText = Builder.text()
                    .font(BIKO_FONT.get())
                    .text(text)
                    .color(textColor)
                    .size(8f)
                    .thickness(0.05f)
                    .build();
            builtText.render(new Matrix4f(event.getGuiGraphics().pose().last().pose()), (float) (X - width / 2) + 9, (float) screenPos.y + 2);

            if (!(potion.getOwner() instanceof Player owner)) continue;

            String OwnerName = owner.getName().getString().equals(mc.player.getName().getString()) ? "From You" : owner.getName().getString();
            float ownerWidth = BIKO_FONT.get().getWidth(OwnerName, 6f);
            float ownerBoxWidth = ownerWidth + 14 + 5;
            float ownerStartX = (float) (X - ownerBoxWidth / 2) + 3.5f;
            float ownerY = (float) (screenPos.y + height + 0.7f);
            DrawHelper.drawSemiRoundRect(event.getGuiGraphics().pose(), ownerStartX + 1, ownerY + 1, ownerBoxWidth, height + 1, 3f, 0, 3f, 0, boxColor.getRGB());
            DrawHelper.drawHead(event.getGuiGraphics().pose(), owner, ownerStartX + 3, ownerY + 1, 10, 10);
            BuiltText builtText1 = Builder.text()
                    .font(BIKO_FONT.get())
                    .text(OwnerName)
                    .color(Color.WHITE)
                    .size(6f)
                    .thickness(0.05f)
                    .build();
            builtText1.render(new Matrix4f(event.getGuiGraphics().pose().last().pose()), ownerStartX + 16, ownerY + 4);
        }
    }

    public void renderArrows(RenderGuiEvent event) {
        if (mc.level == null || mc.player == null) return;

        for (Entity ent : mc.level.getEntities((Entity) null, mc.player.getBoundingBox().inflate(128.0), e -> e instanceof Arrow)) {
            Arrow arrow = (Arrow) ent;
            if (arrow.onGround()) continue;

            Vec3 landingPos = predictLandingPosition(arrow);
            double timeToLand = predictTimeToLand(arrow);
            if (timeToLand == 0) continue;

            Vector3d screenPos = ESPUtils.toScreen(landingPos);
            if (screenPos.z == 0) continue;

            String text = String.format("Arrow (%.1f)", timeToLand);
            float width = BIKO_FONT.get().getWidth(text, 8f);
            int X = (int) (screenPos.x - 5);
            float height = BIKO_FONT.get().getMetrics().baselineHeight() * 8f + 5;
            DrawHelper.rectangle(event.getGuiGraphics().pose(), (float) (X - width / 2) - 5, (float) screenPos.y - 2, width + 19, height + 4, 3f, new Color(30, 30, 30, 150).getRGB());
            ResourceLocation itemTexture = ResourceLocation.fromNamespaceAndPath("dsp", "textures/item/arrow.png");
            DrawHelper.drawTexture(itemTexture, event.getGuiGraphics().pose().last().pose(), (float) (X - width / 2) - 5, (float) screenPos.y - 1, 12, 12);
            BuiltText builtText = Builder.text()
                    .font(BIKO_FONT.get())
                    .text(text)
                    .color(Color.WHITE)
                    .size(8f)
                    .thickness(0.05f)
                    .build();
            builtText.render(new Matrix4f(event.getGuiGraphics().pose().last().pose()), (float) (X - width / 2) + 9, (float) screenPos.y + 2);
        }
    }

    public void renderTridents(RenderGuiEvent event) {
        if (mc.level == null || mc.player == null) return;

        for (Entity ent : mc.level.getEntities((Entity) null, mc.player.getBoundingBox().inflate(128.0), e -> e instanceof ThrownTrident)) {
            ThrownTrident trident = (ThrownTrident) ent;
            if (trident.onGround()) continue;

            Vec3 landingPos = predictLandingPosition(trident);
            double timeToLand = predictTimeToLand(trident);
            if (timeToLand == 0 || trident.getOwner() == null) continue;

            Vector3d screenPos = ESPUtils.toScreen(landingPos);
            if (screenPos.z == 0) continue;

            String text = String.format("Trident (%.1f)", timeToLand);
            float width = BIKO_FONT.get().getWidth(text, 8f);
            int X = (int) (screenPos.x - 5);
            float height = BIKO_FONT.get().getMetrics().baselineHeight() * 8f + 5;
            DrawHelper.rectangle(event.getGuiGraphics().pose(), (float) (X - width / 2) - 5, (float) screenPos.y - 2, width + 19, height + 4, 3f, new Color(30, 30, 30, 150).getRGB());
            ResourceLocation itemTexture = ResourceLocation.fromNamespaceAndPath("dsp", "textures/item/trident.png");
            DrawHelper.drawTexture(itemTexture, event.getGuiGraphics().pose().last().pose(), (float) (X - width / 2) - 5, (float) screenPos.y - 1, 12, 12);
            BuiltText builtText = Builder.text()
                    .font(BIKO_FONT.get())
                    .text(text)
                    .color(Color.WHITE)
                    .size(8f)
                    .thickness(0.05f)
                    .build();
            builtText.render(new Matrix4f(event.getGuiGraphics().pose().last().pose()), (float) (X - width / 2) + 9, (float) screenPos.y + 2);

            if (!(trident.getOwner() instanceof Player owner)) continue;

            String OwnerName = owner.getName().getString().equals(mc.player.getName().getString()) ? "From You" : owner.getName().getString();
            float ownerWidth = BIKO_FONT.get().getWidth(OwnerName, 6f);
            float ownerBoxWidth = ownerWidth + 14 + 5;
            float ownerStartX = (float) (X - ownerBoxWidth / 2) + 3.5f;
            float ownerY = (float) (screenPos.y + height + 0.7f);
            DrawHelper.drawSemiRoundRect(event.getGuiGraphics().pose(), ownerStartX + 1, ownerY + 1, ownerBoxWidth, height + 1, 3f, 0, 3f, 0, new Color(30, 30, 30, 150).getRGB());
            DrawHelper.drawHead(event.getGuiGraphics().pose(), owner, ownerStartX + 3, ownerY + 1, 10, 10);
            BuiltText builtText1 = Builder.text()
                    .font(BIKO_FONT.get())
                    .text(OwnerName)
                    .color(Color.WHITE)
                    .size(6f)
                    .thickness(0.05f)
                    .build();
            builtText1.render(new Matrix4f(event.getGuiGraphics().pose().last().pose()), ownerStartX + 16, ownerY + 4);
        }
    }

    public void renderEnderPearls(RenderGuiEvent event) {
        if (mc.level == null || mc.player == null) return;

        for (Entity ent : mc.level.getEntities((Entity) null, mc.player.getBoundingBox().inflate(128.0), e -> e instanceof ThrownEnderpearl)) {
            ThrownEnderpearl pearl = (ThrownEnderpearl) ent;
            Vec3 landingPos = predictLandingPosition(pearl);
            double timeToLand = predictTimeToLand(pearl);
            Vector3d screenPos = ESPUtils.toScreen(landingPos);
            if (screenPos.z == 0) continue;
            if (pearl.getOwner() == null) continue;
            String text = String.format("Ender Pearl (%.1f)", timeToLand);
            float width = BIKO_FONT.get().getWidth(text, 8f);
            int X = (int) (screenPos.x - 5);
            float height = BIKO_FONT.get().getMetrics().baselineHeight() * 8f + 5;
            DrawHelper.rectangle(event.getGuiGraphics().pose(), (float) (X - width / 2) - 5, (float) screenPos.y - 2, width + 19, height + 4, 3f, new Color(30, 30, 30, 150).getRGB());
            ResourceLocation itemTexture = ResourceLocation.fromNamespaceAndPath("dsp", "textures/item/ender_pearl.png");
            DrawHelper.drawTexture(itemTexture, event.getGuiGraphics().pose().last().pose(), (float) (X - width / 2) - 5, (float) screenPos.y - 1, 12, 12);
            BuiltText builtText = Builder.text()
                    .font(BIKO_FONT.get())
                    .text(text)
                    .color(Color.WHITE)
                    .size(8f)
                    .thickness(0.05f)
                    .build();
            builtText.render(new Matrix4f(event.getGuiGraphics().pose().last().pose()), (float) (X - width / 2) + 9, (float) screenPos.y + 2);

            if (!(pearl.getOwner() instanceof Player owner)) continue;

            String OwnerName = owner.getName().getString().equals(mc.player.getName().getString()) ? "From You" : owner.getName().getString();
            float ownerWidth = BIKO_FONT.get().getWidth(OwnerName, 6f);
            float ownerBoxWidth = ownerWidth + 14 + 5;
            float ownerStartX = (float) (X - ownerBoxWidth / 2) + 3.5f;
            float ownerY = (float) (screenPos.y + height + 0.7f);
            DrawHelper.drawSemiRoundRect(event.getGuiGraphics().pose(), ownerStartX + 1, ownerY + 1, ownerBoxWidth, height + 1, 3f, 0, 3f, 0, new Color(30, 30, 30, 150).getRGB());
            DrawHelper.drawHead(event.getGuiGraphics().pose(), owner, ownerStartX + 3, ownerY + 1, 10, 10);
            BuiltText builtText1 = Builder.text()
                    .font(BIKO_FONT.get())
                    .text(OwnerName)
                    .color(Color.WHITE)
                    .size(6f)
                    .thickness(0.05f)
                    .build();
            builtText1.render(new Matrix4f(event.getGuiGraphics().pose().last().pose()), ownerStartX + 16, ownerY + 4);
        }
    }

    private boolean isPotionHarmful(ThrownPotion potion) {
        PotionContents contents = potion.getItem().get(DataComponents.POTION_CONTENTS);
        if (contents == null) return false;

        for (MobEffectInstance inst : contents.getAllEffects()) {
            var holder = inst.getEffect();
            if (holder == null) continue;
            var effect = holder.value();
            if (effect.getCategory() == MobEffectCategory.HARMFUL) {
                return true;
            }
        }
        return false;
    }

    private Vec3 predictLandingPosition(ThrownEnderpearl pearl) {
        Vec3 pos = pearl.position();
        Vec3 vel = pearl.getDeltaMovement();
        Level level = pearl.level();

        for (int i = 0; i < 300; i++) {
            Vec3 nextPos = pos.add(vel);
            HitResult hitResult = level.clip(new ClipContext(pos, nextPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, pearl));
            if (hitResult.getType() != HitResult.Type.MISS) {
                return hitResult.getLocation();
            }
            pos = nextPos;
            vel = vel.scale(0.99);
            if (!pearl.isNoGravity()) {
                vel = vel.add(0, -0.03, 0);
            }
        }
        return pos;
    }

    private Vec3 predictLandingPosition(ThrownPotion potion) {
        Vec3 pos = potion.position();
        Vec3 vel = potion.getDeltaMovement();
        Level level = potion.level();
        boolean inWater = potion.isInWater();

        for (int i = 0; i < 300; i++) {
            Vec3 nextPos = pos.add(vel);
            HitResult hitResult = level.clip(new ClipContext(pos, nextPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, potion));
            if (hitResult.getType() != HitResult.Type.MISS) {
                return hitResult.getLocation();
            }
            pos = nextPos;

            if (inWater) {
                vel = vel.scale(0.8);
            } else {
                vel = vel.scale(0.99);
            }

            if (!potion.isNoGravity()) {
                vel = vel.add(0, -0.05, 0);
            }
        }
        return pos;
    }

    private double predictTimeToLand(ThrownEnderpearl pearl) {
        Vec3 pos = pearl.position();
        Vec3 vel = pearl.getDeltaMovement();
        Level level = pearl.level();
        int ticks = 0;

        for (int i = 0; i < 300; i++) {
            Vec3 nextPos = pos.add(vel);
            HitResult hitResult = level.clip(new ClipContext(pos, nextPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, pearl));
            if (hitResult.getType() != HitResult.Type.MISS) {
                return ticks / 20.0;
            }
            pos = nextPos;
            vel = vel.scale(0.99);
            if (!pearl.isNoGravity()) {
                vel = vel.add(0, -0.03, 0);
            }
            ticks++;
        }
        return ticks / 20.0;
    }

    private double predictTimeToLand(ThrownPotion potion) {
        Vec3 pos = potion.position();
        Vec3 vel = potion.getDeltaMovement();
        Level level = potion.level();
        int ticks = 0;
        boolean inWater = potion.isInWater();

        for (int i = 0; i < 300; i++) {
            Vec3 nextPos = pos.add(vel);
            HitResult hitResult = level.clip(new ClipContext(pos, nextPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, potion));
            if (hitResult.getType() != HitResult.Type.MISS) {
                return ticks / 20.0;
            }
            pos = nextPos;

            if (inWater) {
                vel = vel.scale(0.8);
            } else {
                vel = vel.scale(0.99);
            }

            if (!potion.isNoGravity()) {
                vel = vel.add(0, -0.05, 0);
            }
            ticks++;
        }
        return ticks / 20.0;
    }

    private Vec3 predictLandingPosition(ThrownTrident trident) {
        Vec3 pos = trident.position();
        Vec3 vel = trident.getDeltaMovement();
        Level level = trident.level();

        for (int i = 0; i < 300; i++) {
            Vec3 nextPos = pos.add(vel);
            HitResult hitResult = level.clip(new ClipContext(pos, nextPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, trident));
            if (hitResult.getType() != HitResult.Type.MISS) {
                return hitResult.getLocation();
            }
            pos = nextPos;
            vel = vel.scale(0.99);
            if (!trident.isNoGravity()) {
                vel = vel.add(0, -0.05, 0);
            }
        }
        return pos;
    }

    private Vec3 predictLandingPosition(Arrow arrow) {
        Vec3 pos = arrow.position();
        Vec3 vel = arrow.getDeltaMovement();
        Level level = arrow.level();
        boolean inWater = arrow.isInWater();

        for (int i = 0; i < 300; i++) {
            Vec3 nextPos = pos.add(vel);
            HitResult hitResult = level.clip(new ClipContext(pos, nextPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, arrow));
            if (hitResult.getType() != HitResult.Type.MISS) {
                return hitResult.getLocation();
            }
            pos = nextPos;

            if (inWater) {
                vel = vel.scale(0.6);
            } else {
                vel = vel.scale(0.99);
            }

            if (!arrow.isNoGravity()) {
                vel = vel.add(0, -0.05, 0);
            }
        }
        return pos;
    }

    private double predictTimeToLand(ThrownTrident trident) {
        Vec3 pos = trident.position();
        Vec3 vel = trident.getDeltaMovement();
        Level level = trident.level();
        int ticks = 0;

        for (int i = 0; i < 300; i++) {
            Vec3 nextPos = pos.add(vel);
            HitResult hitResult = level.clip(new ClipContext(pos, nextPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, trident));
            if (hitResult.getType() != HitResult.Type.MISS) {
                return ticks / 20.0;
            }
            pos = nextPos;
            vel = vel.scale(0.99);
            if (!trident.isNoGravity()) {
                vel = vel.add(0, -0.05, 0);
            }
            ticks++;
        }
        return ticks / 20.0;
    }

    private double predictTimeToLand(Arrow arrow) {
        Vec3 pos = arrow.position();
        Vec3 vel = arrow.getDeltaMovement();
        Level level = arrow.level();
        int ticks = 0;
        boolean inWater = arrow.isInWater();

        for (int i = 0; i < 300; i++) {
            Vec3 nextPos = pos.add(vel);
            HitResult hitResult = level.clip(new ClipContext(pos, nextPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, arrow));
            if (hitResult.getType() != HitResult.Type.MISS) {
                return ticks / 20.0;
            }
            pos = nextPos;

            if (inWater) {
                vel = vel.scale(0.6);
            } else {
                vel = vel.scale(0.99);
            }

            if (!arrow.isNoGravity()) {
                vel = vel.add(0, -0.05, 0);
            }
            ticks++;
        }
        return ticks / 20.0;
    }

    @SubscribeEvent
    public void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS) return;
        if (mc.level == null || mc.player == null) return;
        renderAllTrajectories(event.getPoseStack(), mc.renderBuffers().bufferSource());
    }

    private void renderAllTrajectories(PoseStack poseStack, MultiBufferSource.BufferSource bufSource) {
        Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();

        if (Modi.isOptionEnabled("Ender Pearls") && DrawTrajectory.isEnabled()) {
            for (Entity ent : mc.level.getEntities((Entity) null, mc.player.getBoundingBox().inflate(128.0), e -> e instanceof ThrownEnderpearl)) {
                ThrownEnderpearl pearl = (ThrownEnderpearl) ent;
                renderTrajectory(pearl, poseStack, bufSource, cam);
            }
        }

        if (Modi.isOptionEnabled("Trident") && DrawTrajectory.isEnabled()) {
            for (Entity ent : mc.level.getEntities((Entity) null, mc.player.getBoundingBox().inflate(128.0), e -> e instanceof ThrownTrident)) {
                ThrownTrident trident = (ThrownTrident) ent;
                if (!trident.onGround()) {
                    renderTrajectory(trident, poseStack, bufSource, cam);
                }
            }
        }

        if (Modi.isOptionEnabled("Arrows") && DrawTrajectory.isEnabled()) {
            for (Entity ent : mc.level.getEntities((Entity) null, mc.player.getBoundingBox().inflate(128.0), e -> e instanceof Arrow)) {
                Arrow arrow = (Arrow) ent;
                if (!arrow.onGround()) {
                    renderTrajectory(arrow, poseStack, bufSource, cam);
                }
            }
        }

        if (Modi.isOptionEnabled("Splash Potions")) {
            for (Entity ent : mc.level.getEntities((Entity) null, mc.player.getBoundingBox().inflate(128.0), e -> e instanceof ThrownPotion)) {
                ThrownPotion potion = (ThrownPotion) ent;
                if (DrawTrajectory.isEnabled()) {
                    renderTrajectory(potion, poseStack, bufSource, cam);
                }
                if (DrawRadius.isEnabled()) {
                    Vec3 landingPos = predictLandingPosition(potion);
                    boolean isHarmful = isPotionHarmful(potion);
                    renderPotionRadius(landingPos, poseStack, bufSource, cam, isHarmful);
                }
            }
        }

        if (Preview.isEnabled()) {
            renderPreviewTrajectories(poseStack, bufSource, cam);
        }

        bufSource.endBatch();
    }

    private void renderPreviewTrajectories(PoseStack poseStack, MultiBufferSource.BufferSource bufSource, Vec3 camPos) {
        if (mc.player == null || mc.level == null) return;

        ItemStack mainHand = mc.player.getMainHandItem();
        ItemStack offHand = mc.player.getOffhandItem();

        if (Modi.isOptionEnabled("Ender Pearls")) {
            if (mainHand.getItem() instanceof EnderpearlItem) {
                renderPreviewEnderPearl(poseStack, bufSource, camPos);
            } else if (offHand.getItem() instanceof EnderpearlItem) {
                renderPreviewEnderPearl(poseStack, bufSource, camPos);
            }
        }

        if (Modi.isOptionEnabled("Splash Potions")) {
            if (mainHand.getItem() instanceof SplashPotionItem || mainHand.getItem() instanceof LingeringPotionItem) {
                renderPreviewPotion(poseStack, bufSource, camPos, mainHand);
            } else if (offHand.getItem() instanceof SplashPotionItem || offHand.getItem() instanceof LingeringPotionItem) {
                renderPreviewPotion(poseStack, bufSource, camPos, offHand);
            }
        }

        if (Modi.isOptionEnabled("Trident")) {
            if (mc.player.isUsingItem()) {
                ItemStack usingItem = mc.player.getUseItem();
                if (usingItem.getItem() instanceof TridentItem) {
                    renderPreviewTrident(poseStack, bufSource, camPos);
                }
            }
        }

        if (Modi.isOptionEnabled("Arrows")) {
            if (mc.player.isUsingItem()) {
                ItemStack usingItem = mc.player.getUseItem();
                if (usingItem.getItem() instanceof BowItem) {
                    renderPreviewBow(poseStack, bufSource, camPos);
                } else if (mainHand.getItem() instanceof CrossbowItem && CrossbowItem.isCharged(mainHand)) {
                    renderPreviewCrossbow(poseStack, bufSource, camPos);
                } else if (offHand.getItem() instanceof CrossbowItem && CrossbowItem.isCharged(offHand)) {
                    renderPreviewCrossbow(poseStack, bufSource, camPos);
                }
            } else {
                if (mainHand.getItem() instanceof CrossbowItem && CrossbowItem.isCharged(mainHand)) {
                    renderPreviewCrossbow(poseStack, bufSource, camPos);
                } else if (offHand.getItem() instanceof CrossbowItem && CrossbowItem.isCharged(offHand)) {
                    renderPreviewCrossbow(poseStack, bufSource, camPos);
                }
            }
        }
    }

    private void renderPreviewEnderPearl(PoseStack poseStack, MultiBufferSource.BufferSource bufSource, Vec3 camPos) {
        Vec3 eyePos = mc.player.getEyePosition(1.0f);
        Vec3 lookVec = mc.player.getViewVector(1.0f);
        float velocity = 1.5f;
        Vec3 vel = lookVec.scale(velocity);

        List<Vec3> points = simulateProjectile(eyePos, vel, 0.03, 0.99, false);
        renderTrajectoryLine(points, poseStack, bufSource, camPos, getThemeColor());
    }

    private void renderPreviewPotion(PoseStack poseStack, MultiBufferSource.BufferSource bufSource, Vec3 camPos, ItemStack stack) {
        Vec3 eyePos = mc.player.getEyePosition(1.0f);
        Vec3 lookVec = mc.player.getViewVector(1.0f);
        float velocity = 0.5f;
        Vec3 vel = lookVec.scale(velocity);
        vel = vel.add(0, 0.2, 0);

        List<Vec3> points = simulateProjectile(eyePos, vel, 0.05, 0.99, false);

        boolean isHarmful = isPotionHarmfulStack(stack);
        Color color = isHarmful ? new Color(255, 100, 100, 200) : new Color(100, 255, 100, 200);
        renderTrajectoryLine(points, poseStack, bufSource, camPos, color);

        if (DrawRadius.isEnabled() && !points.isEmpty()) {
            Vec3 landingPos = points.get(points.size() - 1);
            renderPotionRadius(landingPos, poseStack, bufSource, camPos, isHarmful);
        }
    }

    private void renderPreviewTrident(PoseStack poseStack, MultiBufferSource.BufferSource bufSource, Vec3 camPos) {
        Vec3 eyePos = mc.player.getEyePosition(1.0f);
        Vec3 lookVec = mc.player.getViewVector(1.0f);
        float velocity = 2.5f;
        Vec3 vel = lookVec.scale(velocity);

        List<Vec3> points = simulateProjectile(eyePos, vel, 0.05, 0.99, false);
        renderTrajectoryLine(points, poseStack, bufSource, camPos, getThemeColor());
    }

    private void renderPreviewBow(PoseStack poseStack, MultiBufferSource.BufferSource bufSource, Vec3 camPos) {
        int useDuration = mc.player.getTicksUsingItem();
        float power = BowItem.getPowerForTime(useDuration);
        if (power < 0.1f) return;

        Vec3 eyePos = mc.player.getEyePosition(1.0f);
        Vec3 lookVec = mc.player.getViewVector(1.0f);
        float velocity = power * 3.0f;
        Vec3 vel = lookVec.scale(velocity);

        List<Vec3> points = simulateProjectile(eyePos, vel, 0.05, 0.99, false);
        renderTrajectoryLine(points, poseStack, bufSource, camPos, getThemeColor());
    }

    private void renderPreviewCrossbow(PoseStack poseStack, MultiBufferSource.BufferSource bufSource, Vec3 camPos) {
        Vec3 eyePos = mc.player.getEyePosition(1.0f);
        Vec3 lookVec = mc.player.getViewVector(1.0f);
        float velocity = 3.15f;
        Vec3 vel = lookVec.scale(velocity);

        List<Vec3> points = simulateProjectile(eyePos, vel, 0.05, 0.99, false);
        renderTrajectoryLine(points, poseStack, bufSource, camPos, getThemeColor());
    }

    private List<Vec3> simulateProjectile(Vec3 startPos, Vec3 startVel, double gravity, double drag, boolean inWater) {
        List<Vec3> points = new ArrayList<>();
        Vec3 pos = startPos;
        Vec3 vel = startVel;
        points.add(pos);

        for (int i = 0; i < 300; i++) {
            Vec3 nextPos = pos.add(vel);

            if (mc.level == null) break;
            HitResult hitResult = mc.level.clip(new ClipContext(pos, nextPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player));

            if (hitResult.getType() != HitResult.Type.MISS) {
                points.add(hitResult.getLocation());
                break;
            }

            points.add(nextPos);
            pos = nextPos;

            if (inWater) {
                vel = vel.scale(0.6);
            } else {
                vel = vel.scale(drag);
            }

            vel = vel.add(0, -gravity, 0);
        }

        return points;
    }

    private boolean isPotionHarmfulStack(ItemStack stack) {
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        if (contents == null) return false;

        for (MobEffectInstance inst : contents.getAllEffects()) {
            var holder = inst.getEffect();
            if (holder == null) continue;
            var effect = holder.value();
            if (effect.getCategory() == MobEffectCategory.HARMFUL) {
                return true;
            }
        }
        return false;
    }

    private Color getThemeColor() {
        return new Color(ColorUtil.gradient(
                ThemesUtil.getCurrentStyle().getColor(1),
                ThemesUtil.getCurrentStyle().getColor(2),
                10, 10
        ), true);
    }

    private void renderPotionRadius(Vec3 center, PoseStack poseStack, MultiBufferSource.BufferSource bufSource, Vec3 camPos, boolean isHarmful) {
        VertexConsumer vc = bufSource.getBuffer(RenderType.lines());
        Matrix4f m = poseStack.last().pose();
        RenderSystem.lineWidth(3);

        Color color = isHarmful ? new Color(255, 50, 50, 200) : new Color(50, 255, 50, 200);

        float radius = 4.0f;
        int segments = 64;

        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (2 * Math.PI * i / segments);
            float angle2 = (float) (2 * Math.PI * (i + 1) / segments);

            float x1 = (float) (center.x + radius * Mth.cos(angle1));
            float z1 = (float) (center.z + radius * Mth.sin(angle1));
            float x2 = (float) (center.x + radius * Mth.cos(angle2));
            float z2 = (float) (center.z + radius * Mth.sin(angle2));

            Vec3 a = new Vec3(x1, center.y + 0.01, z1).subtract(camPos);
            Vec3 b = new Vec3(x2, center.y + 0.01, z2).subtract(camPos);

            vc.addVertex(m, (float)a.x, (float)a.y, (float)a.z)
                    .setColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha())
                    .setNormal(poseStack.last(), 0, 1, 0);
            vc.addVertex(m, (float)b.x, (float)b.y, (float)b.z)
                    .setColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha())
                    .setNormal(poseStack.last(), 0, 1, 0);
        }

        RenderSystem.lineWidth(2);
    }

    private List<Vec3> generateTrajectoryPoints(ThrownEnderpearl pearl, Vec3 landingPos) {
        List<Vec3> points = new ArrayList<>();
        Vec3 pos = pearl.position();
        Vec3 vel = pearl.getDeltaMovement();
        Level level = pearl.level();
        points.add(pos);

        for (int i = 0; i < 300; i++) {
            Vec3 nextPos = pos.add(vel);
            points.add(nextPos);

            if (nextPos.distanceToSqr(landingPos) < 0.01) {
                break;
            }

            HitResult hitResult = level.clip(new ClipContext(pos, nextPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, pearl));
            if (hitResult.getType() != HitResult.Type.MISS) {
                points.add(landingPos);
                break;
            }

            pos = nextPos;
            vel = vel.scale(0.99);
            if (!pearl.isNoGravity()) {
                vel = vel.add(0, -0.03, 0);
            }
        }

        return points;
    }

    private List<Vec3> generateTrajectoryPoints(ThrownPotion potion, Vec3 landingPos) {
        List<Vec3> points = new ArrayList<>();
        Vec3 pos = potion.position();
        Vec3 vel = potion.getDeltaMovement();
        Level level = potion.level();
        boolean inWater = potion.isInWater();
        points.add(pos);

        for (int i = 0; i < 300; i++) {
            Vec3 nextPos = pos.add(vel);
            points.add(nextPos);

            if (nextPos.distanceToSqr(landingPos) < 0.01) {
                break;
            }

            HitResult hitResult = level.clip(new ClipContext(pos, nextPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, potion));
            if (hitResult.getType() != HitResult.Type.MISS) {
                points.add(landingPos);
                break;
            }

            pos = nextPos;

            if (inWater) {
                vel = vel.scale(0.8);
            } else {
                vel = vel.scale(0.99);
            }

            if (!potion.isNoGravity()) {
                vel = vel.add(0, -0.05, 0);
            }
        }

        return points;
    }

    private List<Vec3> generateTrajectoryPoints(ThrownTrident trident, Vec3 landingPos) {
        List<Vec3> points = new ArrayList<>();
        Vec3 pos = trident.position();
        Vec3 vel = trident.getDeltaMovement();
        Level level = trident.level();
        points.add(pos);

        for (int i = 0; i < 300; i++) {
            Vec3 nextPos = pos.add(vel);
            points.add(nextPos);

            if (nextPos.distanceToSqr(landingPos) < 0.01) {
                break;
            }

            HitResult hitResult = level.clip(new ClipContext(pos, nextPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, trident));
            if (hitResult.getType() != HitResult.Type.MISS) {
                points.add(landingPos);
                break;
            }

            pos = nextPos;
            vel = vel.scale(0.99);
            if (!trident.isNoGravity()) {
                vel = vel.add(0, -0.05, 0);
            }
        }

        return points;
    }

    private List<Vec3> generateTrajectoryPoints(Arrow arrow, Vec3 landingPos) {
        List<Vec3> points = new ArrayList<>();
        Vec3 pos = arrow.position();
        Vec3 vel = arrow.getDeltaMovement();
        Level level = arrow.level();
        boolean inWater = arrow.isInWater();
        points.add(pos);

        for (int i = 0; i < 300; i++) {
            Vec3 nextPos = pos.add(vel);
            points.add(nextPos);

            if (nextPos.distanceToSqr(landingPos) < 0.01) {
                break;
            }

            HitResult hitResult = level.clip(new ClipContext(pos, nextPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, arrow));
            if (hitResult.getType() != HitResult.Type.MISS) {
                points.add(landingPos);
                break;
            }

            pos = nextPos;

            if (inWater) {
                vel = vel.scale(0.6);
            } else {
                vel = vel.scale(0.99);
            }

            if (!arrow.isNoGravity()) {
                vel = vel.add(0, -0.05, 0);
            }
        }

        return points;
    }

    private void renderTrajectory(ThrownEnderpearl pearl, PoseStack poseStack, MultiBufferSource.BufferSource bufSource, Vec3 camPos) {
        Vec3 landingPos = predictLandingPosition(pearl);
        List<Vec3> trajectoryPoints = generateTrajectoryPoints(pearl, landingPos);
        renderTrajectoryLine(trajectoryPoints, poseStack, bufSource, camPos, getThemeColor());
    }

    private void renderTrajectory(ThrownPotion potion, PoseStack poseStack, MultiBufferSource.BufferSource bufSource, Vec3 camPos) {
        Vec3 landingPos = predictLandingPosition(potion);
        List<Vec3> trajectoryPoints = generateTrajectoryPoints(potion, landingPos);

        boolean isHarmful = isPotionHarmful(potion);
        Color lineColor = isHarmful ? new Color(255, 100, 100, 200) : new Color(100, 255, 100, 200);
        renderTrajectoryLine(trajectoryPoints, poseStack, bufSource, camPos, lineColor);
    }

    private void renderTrajectory(ThrownTrident trident, PoseStack poseStack, MultiBufferSource.BufferSource bufSource, Vec3 camPos) {
        Vec3 landingPos = predictLandingPosition(trident);
        List<Vec3> trajectoryPoints = generateTrajectoryPoints(trident, landingPos);
        renderTrajectoryLine(trajectoryPoints, poseStack, bufSource, camPos, getThemeColor());
    }

    private void renderTrajectory(Arrow arrow, PoseStack poseStack, MultiBufferSource.BufferSource bufSource, Vec3 camPos) {
        Vec3 landingPos = predictLandingPosition(arrow);
        List<Vec3> trajectoryPoints = generateTrajectoryPoints(arrow, landingPos);
        renderTrajectoryLine(trajectoryPoints, poseStack, bufSource, camPos, getThemeColor());
    }

    private void renderTrajectoryLine(List<Vec3> points, PoseStack poseStack, MultiBufferSource.BufferSource bufSource, Vec3 camPos, Color color) {
        if (points.size() < 2) return;

        VertexConsumer vc = bufSource.getBuffer(RenderType.lines());
        Matrix4f m = poseStack.last().pose();
        RenderSystem.lineWidth(5);

        for (int i = 0; i < points.size() - 1; i++) {
            Vec3 a = points.get(i).subtract(camPos);
            Vec3 b = points.get(i + 1).subtract(camPos);

            vc.addVertex(m, (float)a.x, (float)a.y, (float)a.z)
                    .setColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha())
                    .setNormal(poseStack.last(), 0, 1, 0);
            vc.addVertex(m, (float)b.x, (float)b.y, (float)b.z)
                    .setColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha())
                    .setNormal(poseStack.last(), 0, 1, 0);
        }
        RenderSystem.lineWidth(1);
    }
}