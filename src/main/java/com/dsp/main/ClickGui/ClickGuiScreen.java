package com.dsp.main.ClickGui;

import com.dsp.main.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.dsp.main.Api.mc;

public class ClickGuiScreen extends Screen {
    public static List<Frame> categoryFrames = new ArrayList<>();
    private float animationProgress = 0;
    private final float animationDuration = 300;
    private long animationStartTime;

    public ClickGuiScreen() {
        super(Component.literal("ClickGUI"));
        categoryFrames.clear();
        int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int frameWidth = 130;
        int frameHeight = 16;
        int gap = 20;

        int count = Module.Category.values().length;
        int totalWidth = count * frameWidth + (count - 1) * gap;
        int startX = (screenWidth - totalWidth) - 50;
        int startY = 30;

        for (int i = 0; i < count; i++) {
            Module.Category category = Module.Category.values()[i];
            int x = startX + i * (frameWidth + gap);
            categoryFrames.add(new Frame(x, startY, frameWidth, frameHeight, category));
        }
    }

    @Override
    protected void init() {
        super.init();
        animationStartTime = System.currentTimeMillis();
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBlurredBackground(partialTicks);
        long elapsed = System.currentTimeMillis() - animationStartTime;
        animationProgress = Math.min(elapsed / animationDuration, 1.0f);
        float easedProgress = (float) (1 - Math.pow(1 - animationProgress, 3));

        for (Frame frame : categoryFrames) {
            frame.render(guiGraphics, mouseX, mouseY, partialTicks, easedProgress);
        }
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        renderBackground(guiGraphics,mouseX,mouseY,partialTicks);
    }
    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

        //super.renderBackground(guiGraphics, mouseX,mouseY,partialTick);
    }

    @Override
    public void onClose() {
        super.onClose();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        for (Frame frame : categoryFrames) {
            frame.mouseScrolled(mouseX, mouseY, scrollY);
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (Frame frame : categoryFrames) {
            frame.mouseClicked(mouseX, mouseY, button);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (Frame frame : categoryFrames) {
            frame.mouseReleased(mouseX, mouseY, button);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (Frame frame : categoryFrames) {
            frame.keyPressed(keyCode);
        }
        if (keyCode == 344) {
            this.onClose();
            Minecraft.getInstance().setScreen(null);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
