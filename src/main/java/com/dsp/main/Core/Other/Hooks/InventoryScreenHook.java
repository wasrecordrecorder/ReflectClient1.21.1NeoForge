package com.dsp.main.Core.Other.Hooks;

import com.dsp.main.Utils.Minecraft.Client.InvUtil;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@OnlyIn(Dist.CLIENT)
public class InventoryScreenHook extends EffectRenderingInventoryScreen<InventoryMenu> implements RecipeUpdateListener {
    private float xMouse;
    private float yMouse;
    private final RecipeBookComponent recipeBookComponent = new RecipeBookComponent();
    private boolean widthTooNarrow;
    private boolean buttonClicked;
    private int ticksSinceOpened = 0;
    private boolean isClosing = false;
    private int closingTicks = 0;

    public InventoryScreenHook(Player player) {
        super(player.inventoryMenu, player.getInventory(), Component.translatable("container.crafting"));
        this.titleLabelX = 97;
    }

    @Override
    protected void init() {
        this.ticksSinceOpened = 0;
        this.isClosing = false;
        this.closingTicks = 0;
        if (this.minecraft.gameMode.hasInfiniteItems()) {
            this.minecraft.setScreen(new CreativeModeInventoryScreen(
                    this.minecraft.player,
                    this.minecraft.player.connection.enabledFeatures(),
                    this.minecraft.options.operatorItemsTab().get()
            ));
        } else {
            super.init();
            this.widthTooNarrow = this.width < 379;
            this.recipeBookComponent.init(this.width, this.height, this.minecraft, this.widthTooNarrow, this.menu);
            this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
            this.addRenderableWidget(new Button.Builder(
                    Component.literal("Drop All"),
                    button -> dropAllItems()
            ).pos(this.leftPos + this.imageWidth / 2 - 50, this.topPos - 30)
                    .size(100, 20)
                    .build());
            this.addRenderableWidget(new ImageButton(
                    this.leftPos + 104, this.height / 2 - 22, 20, 18, RecipeBookComponent.RECIPE_BUTTON_SPRITES,
                    button -> {
                        this.recipeBookComponent.toggleVisibility();
                        this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
                        button.setPosition(this.leftPos + 104, this.height / 2 - 22);
                        this.buttonClicked = true;
                    }
            ));
            this.addWidget(this.recipeBookComponent);
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
    public void containerTick() {
        if (this.minecraft.gameMode.hasInfiniteItems()) {
            this.minecraft.setScreen(new CreativeModeInventoryScreen(
                    this.minecraft.player,
                    this.minecraft.player.connection.enabledFeatures(),
                    this.minecraft.options.operatorItemsTab().get()
            ));
        } else {
            this.recipeBookComponent.tick();
            if (isClosing) {
                this.closingTicks++;
                if (this.closingTicks >= 4) {
                    super.onClose();
                }
            } else {
                this.ticksSinceOpened++;
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        float animationDuration = 4.0f;
        float progress;
        if (isClosing) {
            float time = this.closingTicks + partialTick;
            float rawProgress = Math.min(time / animationDuration, 1.0f);
            progress = 1.0f - easeInOutCubic(rawProgress);
        } else {
            float time = this.ticksSinceOpened + partialTick;
            float rawProgress = Math.min(time / animationDuration, 1.0f);
            progress = easeInOutCubic(rawProgress);
        }
        int alphaBg = (int)(progress * 128);
        int colorBg = (alphaBg << 24) | 0x000000;
        guiGraphics.fill(0, 0, this.width, this.height, colorBg);
        guiGraphics.pose().pushPose();
        float centerX = this.leftPos + this.imageWidth / 2.0f;
        float centerY = this.topPos + this.imageHeight / 2.0f;
        guiGraphics.pose().translate(centerX, centerY, 0);
        guiGraphics.pose().scale(progress, progress, 1.0f);
        guiGraphics.pose().translate(-centerX, -centerY, 0);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, progress);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        guiGraphics.pose().popPose();

        if (this.recipeBookComponent.isVisible() && this.widthTooNarrow) {
            this.recipeBookComponent.render(guiGraphics, mouseX, mouseY, partialTick);
        } else {
            this.recipeBookComponent.render(guiGraphics, mouseX, mouseY, partialTick);
            this.recipeBookComponent.renderGhostRecipe(guiGraphics, this.leftPos, this.topPos, false, partialTick);
        }
        this.renderTooltip(guiGraphics, mouseX, mouseY);
        this.recipeBookComponent.renderTooltip(guiGraphics, this.leftPos, this.topPos, mouseX, mouseY);
        this.xMouse = (float)mouseX;
        this.yMouse = (float)mouseY;
    }

    private float easeInOutCubic(float t) {
        if (t < 0.5f) {
            return 4.0f * t * t * t;
        } else {
            float f = (t - 1.0f);
            return 1.0f + 4.0f * f * f * f;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        float time = isClosing ? this.closingTicks + this.minecraft.getFrameTimeNs() : this.ticksSinceOpened + this.minecraft.getFrameTimeNs();
        float animationDuration = 4.0f;
        float rawProgress = Math.min(time / animationDuration, 1.0f);
        float easedProgress = isClosing ? 1.0f - easeInOutCubic(rawProgress) : easeInOutCubic(rawProgress);

        if (easedProgress < 1.0f && easedProgress > 0.0f) {
            float centerX = this.leftPos + this.imageWidth / 2.0f;
            float centerY = this.topPos + this.imageHeight / 2.0f;
            double adjustedMouseX = centerX + (mouseX - centerX) / easedProgress;
            double adjustedMouseY = centerY + (mouseY - centerY) / easedProgress;
            mouseX = adjustedMouseX;
            mouseY = adjustedMouseY;
        }

        if (this.recipeBookComponent.mouseClicked(mouseX, mouseY, button)) {
            this.setFocused(this.recipeBookComponent);
            return true;
        }
        return this.widthTooNarrow && this.recipeBookComponent.isVisible() ? false : super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onClose() {
        if (!isClosing) {
            this.isClosing = true;
            this.closingTicks = 0;
        }
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBg(guiGraphics, partialTick, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int i = this.leftPos;
        int j = this.topPos;
        guiGraphics.blit(INVENTORY_LOCATION, i, j, 0, 0, this.imageWidth, this.imageHeight);
        renderEntityInInventoryFollowsMouse(guiGraphics, i + 26, j + 8, i + 75, j + 78, 30, 0.0625f, this.xMouse, this.yMouse, this.minecraft.player);
    }

    public static void renderEntityInInventoryFollowsMouse(
            GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int scale, float yOffset, float mouseX, float mouseY, LivingEntity entity
    ) {
        float f = (float)(x1 + x2) / 2.0f;
        float f1 = (float)(y1 + y2) / 2.0f;
        float f2 = (float)Math.atan((double)((f - mouseX) / 40.0f));
        float f3 = (float)Math.atan((double)((f1 - mouseY) / 40.0f));
        renderEntityInInventoryFollowsAngle(guiGraphics, x1, y1, x2, y2, scale, yOffset, f2, f3, entity);
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
        renderEntityInInventory(guiGraphics, f, f1, f10, vector3f, quaternionf, quaternionf1, entity);
        entity.yBodyRot = f4;
        entity.setYRot(f5);
        entity.setXRot(f6);
        entity.yHeadRotO = f7;
        entity.yHeadRot = f8;
        guiGraphics.disableScissor();
    }

    public static void renderEntityInInventory(
            GuiGraphics guiGraphics, float x, float y, float scale, Vector3f translate, Quaternionf pose, @Nullable Quaternionf cameraOrientation, LivingEntity entity
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

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return this.recipeBookComponent.keyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return this.recipeBookComponent.charTyped(codePoint, modifiers) || super.charTyped(codePoint, modifiers);
    }

    @Override
    protected boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
        return (!this.widthTooNarrow || !this.recipeBookComponent.isVisible()) && super.isHovering(x, y, width, height, mouseX, mouseY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.buttonClicked) {
            this.buttonClicked = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeft, int guiTop, int mouseButton) {
        boolean outsideGui = mouseX < guiLeft || mouseY < guiTop || mouseX >= (guiLeft + this.imageWidth) || mouseY >= (guiTop + this.imageHeight);
        return this.recipeBookComponent.hasClickedOutside(mouseX, mouseY, this.leftPos, this.topPos, this.imageWidth, this.imageHeight, mouseButton) && outsideGui;
    }

    @Override
    protected void slotClicked(Slot slot, int slotId, int mouseButton, ClickType type) {
        super.slotClicked(slot, slotId, mouseButton, type);
        this.recipeBookComponent.slotClicked(slot);
    }

    @Override
    public void recipesUpdated() {
        this.recipeBookComponent.recipesUpdated();
    }

    @Override
    public RecipeBookComponent getRecipeBookComponent() {
        return this.recipeBookComponent;
    }
}