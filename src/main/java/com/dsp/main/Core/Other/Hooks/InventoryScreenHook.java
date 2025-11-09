package com.dsp.main.Core.Other.Hooks;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.EffectsInInventory;
import net.minecraft.client.gui.screens.recipebook.CraftingRecipeBookComponent;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class InventoryScreenHook extends AbstractRecipeBookScreen<InventoryMenu> {
    private float xMouse;
    private float yMouse;
    private boolean buttonClicked;
    private long openTime = 0;
    private boolean isClosing = false;
    private long closeTime = 0;
    private final EffectsInInventory effects;
    private static final long ANIMATION_DURATION = 150; // 150 мс

    public InventoryScreenHook(Player player) {
        super(
                player.inventoryMenu,
                new CraftingRecipeBookComponent(player.inventoryMenu),
                player.getInventory(),
                Component.translatable("container.crafting")
        );
        this.titleLabelX = 97;
        this.effects = new EffectsInInventory(this);
    }

    @Override
    protected void init() {
        this.openTime = System.currentTimeMillis();
        this.isClosing = false;

        if (this.minecraft.gameMode.hasInfiniteItems()) {
            this.minecraft.setScreen(new CreativeModeInventoryScreen(
                    this.minecraft.player,
                    this.minecraft.player.connection.enabledFeatures(),
                    this.minecraft.options.operatorItemsTab().get()
            ));
        } else {
            super.init();

            this.addRenderableWidget(new Button.Builder(
                    Component.literal("Drop All"),
                    button -> dropAllItems()
            ).pos(this.leftPos + this.imageWidth / 2 - 50, this.topPos - 30)
                    .size(100, 20)
                    .build());
        }
    }

    private void dropAllItems() {
        if (this.minecraft == null || this.minecraft.gameMode == null || this.minecraft.player == null) return;
        for (Slot slot : this.menu.slots) {
            if (slot.hasItem() && slot.mayPickup(this.minecraft.player)) {
                this.minecraft.gameMode.handleInventoryMouseClick(
                        this.menu.containerId,
                        slot.index,
                        1,
                        ClickType.THROW,
                        this.minecraft.player
                );
            }
        }
    }

    @Override
    protected ScreenPosition getRecipeBookButtonPosition() {
        return new ScreenPosition(this.leftPos + 104, this.height / 2 - 22);
    }

    @Override
    protected void onRecipeBookButtonClick() {
        this.buttonClicked = true;
    }

    @Override
    public void containerTick() {
        if (this.minecraft.gameMode.hasInfiniteItems()) {
            this.minecraft.setScreen(new CreativeModeInventoryScreen(
                    this.minecraft.player,
                    this.minecraft.player.connection.enabledFeatures(),
                    this.minecraft.options.operatorItemsTab().get()
            ));
        } else {
            if (isClosing) {
                long elapsed = System.currentTimeMillis() - closeTime;
                if (elapsed >= ANIMATION_DURATION) {
                    super.onClose();
                }
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        long currentTime = System.currentTimeMillis();
        float progress;

        if (isClosing) {
            long elapsed = currentTime - closeTime;
            progress = 1.0f - Math.min((float)elapsed / ANIMATION_DURATION, 1.0f);
        } else {
            long elapsed = currentTime - openTime;
            progress = Math.min((float)elapsed / ANIMATION_DURATION, 1.0f);
        }

        // Фон с анимацией прозрачности от 0 до 0.5
        int alpha = (int)(progress * 128); // 128 = 0.5 прозрачности
        guiGraphics.fill(0, 0, this.width, this.height, (alpha << 24) | 0x000000);

        // Анимация расширения инвентаря
        guiGraphics.pose().pushPose();
        float centerX = this.leftPos + this.imageWidth / 2.0f;
        float centerY = this.topPos + this.imageHeight / 2.0f;
        guiGraphics.pose().translate(centerX, centerY, 0);
        guiGraphics.pose().scale(progress, progress, 1.0f);
        guiGraphics.pose().translate(-centerX, -centerY, 0);

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, progress);

        // Вызываем super.render() для отрисовки всего инвентаря (фон, слоты, предметы)
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderBg(guiGraphics, partialTick, mouseX, mouseY);

        // Рендерим эффекты
        this.effects.render(guiGraphics, mouseX, mouseY, partialTick);

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        guiGraphics.pose().popPose();

        this.xMouse = (float)mouseX;
        this.yMouse = (float)mouseY;
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Ничего не рендерим
    }

    @Override
    public boolean showsActiveEffects() {
        return this.effects.canSeeEffects();
    }

    @Override
    protected boolean isBiggerResultSlot() {
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onClose() {
        if (!isClosing) {
            this.isClosing = true;
            this.closeTime = System.currentTimeMillis();
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int i = this.leftPos;
        int j = this.topPos;
        guiGraphics.blit(RenderType::guiTextured, INVENTORY_LOCATION, i, j, 0.0f, 0.0f, this.imageWidth, this.imageHeight, 256, 256);
        renderEntityInInventoryFollowsMouse(guiGraphics, i + 26, j + 8, i + 75, j + 78, 30, 0.0625f, this.xMouse, this.yMouse, this.minecraft.player);
    }

    public static void renderEntityInInventoryFollowsMouse(
            GuiGraphics guiGraphics,
            int x1,
            int y1,
            int x2,
            int y2,
            int scale,
            float yOffset,
            float mouseX,
            float mouseY,
            LivingEntity entity
    ) {
        float f = (float)(x1 + x2) / 2.0f;
        float f1 = (float)(y1 + y2) / 2.0f;
        float f2 = (float)Math.atan((double)((f - mouseX) / 40.0f));
        float f3 = (float)Math.atan((double)((f1 - mouseY) / 40.0f));
        renderEntityInInventoryFollowsAngle(guiGraphics, x1, y1, x2, y2, scale, yOffset, f2, f3, entity);
    }

    public static void renderEntityInInventoryFollowsAngle(
            GuiGraphics guiGraphics,
            int x1,
            int y1,
            int x2,
            int y2,
            int scale,
            float yOffset,
            float angleXComponent,
            float angleYComponent,
            LivingEntity entity
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
        renderEntityInInventory(guiGraphics, f, f1, f10, vector3f, quaternionf, quaternionf1, entity);
        entity.yBodyRot = f4;
        entity.setYRot(f5);
        entity.setXRot(f6);
        entity.yHeadRotO = f7;
        entity.yHeadRot = f8;
        guiGraphics.disableScissor();
    }

    public static void renderEntityInInventory(
            GuiGraphics guiGraphics,
            float x,
            float y,
            float scale,
            Vector3f translate,
            Quaternionf pose,
            @Nullable Quaternionf cameraOrientation,
            LivingEntity entity
    ) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate((double)x, (double)y, 50.0);
        guiGraphics.pose().scale(scale, scale, -scale);
        guiGraphics.pose().translate(translate.x, translate.y, translate.z);
        guiGraphics.pose().mulPose(pose);
        guiGraphics.flush();
        Lighting.setupForEntityInInventory();
        EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        if (cameraOrientation != null) {
            entityRenderDispatcher.overrideCameraOrientation(cameraOrientation.conjugate(new Quaternionf()).rotateY((float)Math.PI));
        }
        entityRenderDispatcher.setRenderShadow(false);
        guiGraphics.drawSpecial(bufferSource -> entityRenderDispatcher.render(entity, 0.0, 0.0, 0.0, 1.0f, guiGraphics.pose(), bufferSource, 15728880));
        guiGraphics.flush();
        entityRenderDispatcher.setRenderShadow(true);
        guiGraphics.pose().popPose();
        Lighting.setupFor3DItems();
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.buttonClicked) {
            this.buttonClicked = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }
}