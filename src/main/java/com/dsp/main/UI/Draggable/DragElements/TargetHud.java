package com.dsp.main.UI.Draggable.DragElements;

import com.dsp.main.Api;
import com.dsp.main.Functions.Combat.Aura.Aura;
import com.dsp.main.Functions.Render.HudElement;
import com.dsp.main.UI.Draggable.DraggableElement;
import com.dsp.main.UI.Themes.ThemesUtil;
import com.dsp.main.Utils.Color.ColorHelper;
import com.dsp.main.Utils.Font.builders.Builder;
import com.dsp.main.Utils.Font.builders.states.QuadColorState;
import com.dsp.main.Utils.Font.builders.states.QuadRadiusState;
import com.dsp.main.Utils.Font.builders.states.SizeState;
import com.dsp.main.Utils.Font.renderers.impl.BuiltBorder;
import com.dsp.main.Utils.Font.renderers.impl.BuiltText;
import com.dsp.main.Utils.Render.Blur.DrawShader;
import com.dsp.main.Utils.Render.DrawHelper;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.scores.*;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static com.dsp.main.Functions.Combat.Aura.Aura.Target;
import static com.dsp.main.Main.RUS;
import static com.dsp.main.Api.mc;
import static com.dsp.main.Utils.Minecraft.Client.ClientPlayerUtil.getHealthFromScoreboard;

public class TargetHud extends DraggableElement {
    private static final int HEIGHT = 32;
    private static final int PADDING = 2;
    private static final int MODEL_SIZE = 24;
    private static final int MODEL_PADDING = 2;
    private static final int TEXT_HEIGHT = 8;
    private static final int BAR_HEIGHT = 2;
    private static final float ANIMATION_SPEED = 0.05f;
    private static final float SIZE_ANIMATION_SPEED = 0.1f;
    private static final float HEALTH_ANIMATION_SPEED = 0.2f;
    private static final float MIN_WIDTH = 70.0f;
    private static final long TARGET_TIMEOUT_MS = 3000;

    private static LivingEntity currentTarget = null;
    private static float nicknameOffset = 0.0f;
    private static boolean nicknameDirection = true;
    private static float sizeScale = 0.0f;
    private static boolean isVisible = false;
    private static float displayedHealthPercent = 0.0f;
    private static LivingEntity lastTarget = null;
    private static long lastTargetTime = 0;

    public TargetHud(String name, float initialX, float initialY, boolean canBeDragged) {
        super(name, initialX, initialY, canBeDragged);
    }

    public static void setCurrentTarget(LivingEntity target) {
        if (target != lastTarget) {
            sizeScale = 0.0f;
            nicknameOffset = 0.0f;
            nicknameDirection = true;
            displayedHealthPercent = target != null ? getHealthFromScoreboard(target)[0] / 20 : 0.0f;
        }
        currentTarget = target;
        lastTarget = target;
        if (target != null) {
            isVisible = true;
            lastTargetTime = System.currentTimeMillis();
        } else {
            isVisible = false;
        }
    }

    public void tick() {
        if (currentTarget != null && isVisible) {
            if (currentTarget == mc.player || !currentTarget.isAlive()) {
                isVisible = false;
                currentTarget = null;
                sizeScale = 0.0f;
                return;
            }
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastTargetTime > TARGET_TIMEOUT_MS) {
                isVisible = false;
                currentTarget = null;
                sizeScale = 0.0f;
            }
        }
    }

    @Override
    public float getWidth() {
        if (currentTarget == null) return MIN_WIDTH;
        String nickname = currentTarget.getName().getString();
        float nicknameWidth = RUS.get().getWidth(nickname, TEXT_HEIGHT);
        if (nicknameWidth < MIN_WIDTH) {
            nicknameWidth = MIN_WIDTH;
        }
        float hpTextWidth = RUS.get().getWidth("HP: " + getHealthFromScoreboard(currentTarget)[0], TEXT_HEIGHT);
        float maxTextWidth = Math.max(nicknameWidth, hpTextWidth);
        return Math.max(MIN_WIDTH, MODEL_SIZE + MODEL_PADDING + maxTextWidth + 2 * PADDING);
    }

    @Override
    public float getHeight() {
        return HEIGHT;
    }

    @Override
    public void render(GuiGraphics guiGraphics) {
        if (isChatOpen()) {
            setCurrentTarget(mc.player);
        } else if (Target != null) {
            setCurrentTarget((LivingEntity) Target);
        } else {
            HitResult hitResult = mc.hitResult;
            if (hitResult != null && hitResult.getType() == HitResult.Type.ENTITY) {
                EntityHitResult entityHitResult = (EntityHitResult) hitResult;
                Entity entity = entityHitResult.getEntity();
                if (entity instanceof LivingEntity && entity.isAlive()) {
                    setCurrentTarget((LivingEntity) entity);
                } else {
                    tick();
                }
            } else {
                tick();
            }

        }

        if (currentTarget == null || !Api.isEnabled("Hud") || !HudElement.HudElements.isOptionEnabled("Target Hud")) {
            isVisible = false;
        }

        if (isVisible) {
            sizeScale = Math.min(sizeScale + SIZE_ANIMATION_SPEED, 1.0f);
        } else {
            sizeScale = Math.max(sizeScale - SIZE_ANIMATION_SPEED, 0.0f);
        }

        if (sizeScale <= 0.0f) return;

        String nickname = currentTarget.getName().getString();
        String hpText = "HP: " + getHealthFromScoreboard(currentTarget)[0];
        float maxHealth = 20;
        if (getHealthFromScoreboard(currentTarget)[0] > 20) {
            maxHealth = getHealthFromScoreboard(currentTarget)[0];
        }
        float targetHealthPercent = getHealthFromScoreboard(currentTarget)[0] / maxHealth;
        displayedHealthPercent += (targetHealthPercent - displayedHealthPercent) * HEALTH_ANIMATION_SPEED;
        guiGraphics.pose().pushPose();
        float centerX = xPos + getWidth() / 2;
        float centerY = yPos + HEIGHT / 2;
        guiGraphics.pose().translate(centerX, centerY, 0);
        guiGraphics.pose().scale(sizeScale, sizeScale, 1.0f);
        guiGraphics.pose().translate(-centerX, -centerY, 0);

        Color borderColor = new Color(50, 66, 83, 120);
        Color textColor = new Color(255, 255, 255, 255);
        Color barBgColor = new Color(64, 64, 64, 255);
        Color healthBarColor = new Color(0, 243, 205, 255);
        DrawShader.drawRoundBlur(guiGraphics.pose(), xPos, yPos, getWidth(), HEIGHT, 4.0f, new Color(23, 29, 35, 255).getRGB(), 120, 0.4f);

        BuiltBorder border = Builder.border()
                .size(new SizeState(26.5f * sizeScale, 26.5f * sizeScale))
                .color(new QuadColorState(borderColor.getRGB(), borderColor.getRGB(), borderColor.getRGB(), borderColor.getRGB()))
                .radius(new QuadRadiusState(1f, 1f, 1f, 1f))
                .thickness(0.1f)
                .smoothness(0.9f, 0.9f)
                .build();
        border.render(new Matrix4f(guiGraphics.pose().last().pose()), xPos + PADDING - 1.5f, yPos + PADDING - 1.5f);
        renderEntity(guiGraphics, xPos + PADDING, yPos + PADDING - 1.5f, (int)(MODEL_SIZE * sizeScale), currentTarget);

        float textX = xPos + PADDING + MODEL_SIZE + MODEL_PADDING;
        float textY = yPos + PADDING - 1.5f;
        renderScrollingText(guiGraphics, nickname, textX, textY, getWidth() - PADDING - MODEL_SIZE - MODEL_PADDING - PADDING - 2, TEXT_HEIGHT, textColor);

        BuiltText hpBuiltText = Builder.text()
                .font(RUS.get())
                .text(hpText)
                .color(textColor.getRGB())
                .size(TEXT_HEIGHT - 1.5f)
                .thickness(0.1f)
                .build();
        hpBuiltText.render(guiGraphics.pose().last().pose(), textX, textY + TEXT_HEIGHT + 2);

        List<ItemStack> armorItems = new ArrayList<>();
        for (ItemStack armor : currentTarget.getArmorSlots()) {
            if (!armor.isEmpty()) armorItems.add(armor);
        }
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(0.65f, 0.65f, 1.0f);
        float scaleFactor = 0.65f;
        for (int i = 0; i < Math.min(armorItems.size(), 4); i++) {
            float itemX = (xPos + MODEL_SIZE + MODEL_PADDING - PADDING + (i % 4) * (16 - 6) + 4) / scaleFactor;
            float itemY = (yPos + 17) / scaleFactor;
            guiGraphics.renderItem(armorItems.get(i), (int)itemX, (int)itemY);
            guiGraphics.renderItemDecorations(mc.font, armorItems.get(i), (int)itemX, (int)itemY);
        }
        ItemStack mainHand = currentTarget.getMainHandItem();
        ItemStack offHand = currentTarget.getOffhandItem();
        if (!mainHand.isEmpty()) {
            guiGraphics.renderItem(mainHand, (int)((xPos + getWidth() - 40 - PADDING + 18) / scaleFactor), (int)((yPos + 16) / scaleFactor));
            guiGraphics.renderItemDecorations(mc.font, mainHand, (int)((xPos + getWidth() - 40 - PADDING + 18) / scaleFactor), (int)((yPos + 16) / scaleFactor));
        }
        if (!offHand.isEmpty()) {
            guiGraphics.renderItem(offHand, (int)((xPos + getWidth() - 40 - PADDING + 16 + 14) / scaleFactor), (int)((yPos + 16) / scaleFactor));
            guiGraphics.renderItemDecorations(mc.font, mainHand, (int)((xPos + getWidth() - 40 - PADDING + 18) / scaleFactor), (int)((yPos + 16) / scaleFactor));
        }
        guiGraphics.pose().popPose();

        float barWidth = getWidth() - 2 * PADDING;
        float barX = xPos + PADDING;
        float barY = yPos + HEIGHT - PADDING - 2;

        guiGraphics.enableScissor((int) barX, (int) barY, (int) (barX + barWidth), (int) (barY + BAR_HEIGHT));

        DrawHelper.rectangle(guiGraphics.pose(), barX, barY, barWidth, BAR_HEIGHT, 1, barBgColor.getRGB());
        DrawHelper.rectangle(guiGraphics.pose(), barX, barY, barWidth * displayedHealthPercent, BAR_HEIGHT, 1, ColorHelper.gradient(ThemesUtil.getCurrentStyle().getColorLowSpeed(1), ThemesUtil.getCurrentStyle().getColorLowSpeed(2), 20, 10));

        guiGraphics.disableScissor();

        guiGraphics.pose().popPose();
    }

    private void renderEntity(GuiGraphics guiGraphics, float x, float y, int size, LivingEntity entity) {
        int x1 = (int)x;
        int y1 = (int)y;
        int x2 = x1 + size;
        int y2 = y1 + size;
        float centerX = Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2.0f;
        float centerY = Minecraft.getInstance().getWindow().getGuiScaledHeight() / 2.0f;
        float angleXComponent = -((centerX - (x1 + x2) / 2.0f) / 100.0f);
        float angleYComponent = -((centerY - (y1 + y2) / 2.0f) / 100.0f);
        renderEntityInInventoryFollowsAngle(guiGraphics, x1, y1, x2, y2, size / 2, 0.0f, angleXComponent, angleYComponent, entity);
    }


    private void renderScrollingText(GuiGraphics guiGraphics, String text, float x, float y, float maxWidth, float textHeight, Color textColor) {
        float textWidth = RUS.get().getWidth(text, textHeight) + 8;
        if (textWidth > maxWidth) {
            float offset = nicknameOffset;
            if (nicknameDirection) {
                offset += ANIMATION_SPEED;
                if (offset > textWidth - maxWidth) {
                    nicknameDirection = false;
                }
            } else {
                offset -= ANIMATION_SPEED;
                if (offset < 0) {
                    nicknameDirection = true;
                }
            }
            nicknameOffset = offset;
            guiGraphics.enableScissor((int)x, (int)y, (int)(x + maxWidth), (int)(y + textHeight + 3));
            BuiltText nicknameText = Builder.text()
                    .font(RUS.get())
                    .text(text)
                    .color(textColor.getRGB())
                    .size(textHeight)
                    .thickness(0.1f)
                    .build();
            nicknameText.render(guiGraphics.pose().last().pose(), x - offset, y);
            guiGraphics.disableScissor();
        } else {
            float centeredX = x + (maxWidth - textWidth) / 2;
            BuiltText nicknameText = Builder.text()
                    .font(RUS.get())
                    .text(text)
                    .color(textColor.getRGB())
                    .size(textHeight)
                    .thickness(0.1f)
                    .build();
            nicknameText.render(guiGraphics.pose().last().pose(), centeredX, y);
            nicknameOffset = 0.0f;
        }
    }

    public static void renderEntityInInventoryFollowsAngle(
            GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int scale, float yOffset, float angleXComponent, float angleYComponent, LivingEntity entity
    ) {
        float f = (float)(x1 + x2) / 2.0f;
        float f1 = (float)(y1 + y2) / 2.0f;
        guiGraphics.enableScissor(x1, y1, x2, y2);
        float f2 = angleXComponent;
        float f3 = angleYComponent;
        Quaternionf quaternionf = new Quaternionf().rotateZ((float)Math.PI);
        Quaternionf quaternionf1 = new Quaternionf().rotateX(f3 * 20.0f * (float)(Math.PI / 180.0));
        quaternionf.mul(quaternionf1);
        float f4 = entity.yBodyRot;
        float f5 = entity.getYRot();
        float f6 = entity.getXRot();
        float f7 = entity.yHeadRotO;
        float f8 = entity.yHeadRot;
        entity.yBodyRot = 180.0f + f2 * 20.0f;
        entity.setYRot(180.0f + f2 * 40.0f);
        entity.setXRot(-f3 * 20.0f);
        entity.yHeadRot = entity.getYRot();
        entity.yHeadRotO = entity.getYRot();
        float f9 = entity.getScale();
        Vector3f vector3f = new Vector3f(0.0f, entity.getBbHeight() / 2.0f + yOffset * f9, 0.0f);
        float f10 = (float)scale / f9;
        renderEntityInInventory(guiGraphics, f, f1 + 14, f10 + 10, vector3f, quaternionf, quaternionf1, entity);
        entity.yBodyRot = f4;
        entity.setYRot(f5);
        entity.setXRot(f6);
        entity.yHeadRotO = f7;
        entity.yHeadRot = f8;
        entity.setInvisible(false);
        guiGraphics.disableScissor();
    }

    public static void renderEntityInInventory(
            GuiGraphics guiGraphics, float x, float y, float scale, Vector3f translate, Quaternionf pose, Quaternionf cameraOrientation, LivingEntity entity
    ) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate((double)x, (double)y, 50.0);
        guiGraphics.pose().scale(scale, scale, -scale);
        guiGraphics.pose().translate(translate.x, translate.y, translate.z);
        guiGraphics.pose().mulPose(pose);
        Lighting.setupForEntityInInventory();
        EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        if (cameraOrientation != null) {
            entityRenderDispatcher.overrideCameraOrientation(cameraOrientation.conjugate(new Quaternionf()).rotateY((float)Math.PI));
        }
        entityRenderDispatcher.setRenderShadow(false);
        RenderSystem.runAsFancy(() -> entityRenderDispatcher.render(entity, 0.0, 0.0, 0.0, 0.0f, 1.0f, guiGraphics.pose(), guiGraphics.bufferSource(), 15728880));
        guiGraphics.flush();
        entityRenderDispatcher.setRenderShadow(true);
        guiGraphics.pose().popPose();
        Lighting.setupFor3DItems();
    }
}