package com.dsp.main.UI.ClickGui;

import com.dsp.main.Module;
import com.dsp.main.Utils.Render.DrawHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.dsp.main.Api.mc;

public class ClickGuiScreen extends Screen {
    private static final List<Frame> categoryFrames = new ArrayList<>();
    private final float animationDuration = 300; // Длительность анимации в мс
    private long animationStartTime;

    public ClickGuiScreen() {
        super(Component.literal("ClickGUI"));
        categoryFrames.clear();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int frameWidth = 130;
        int frameHeight = 18;
        int gap = 18;

        int count = Module.Category.values().length;
        int totalWidth = count * frameWidth + (count - 1) * gap;
        int startX = (screenWidth - totalWidth) / 2;

        int headerHeight = frameHeight;
        int footerHeight = frameHeight / 2;
        int maxVisibleHeight = (int)(screenHeight * 0.8);
        int totalFrameHeight = headerHeight + maxVisibleHeight + footerHeight;
        int startY = (screenHeight - totalFrameHeight) / 2;

        for (int i = 0; i < count; i++) {
            Module.Category category = Module.Category.values()[i];
            int x = startX + i * (frameWidth + gap);
            categoryFrames.add(new Frame(x, startY, frameHeight, category, i));
        }
    }

    @Override
    protected void init() {
        super.init();
        animationStartTime = System.currentTimeMillis();
    }

    @Override
    public void tick() {
        long elapsed = System.currentTimeMillis() - animationStartTime;
        for (int i = 0; i < categoryFrames.size(); i++) {
            categoryFrames.get(i).updatePosition(elapsed);
        }
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        return;
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBlurredBackground(partialTicks);
        int shadowColor = 0x80000000;
        DrawHelper.rectangle(guiGraphics.pose(), 0, 0, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight(), 0, shadowColor);

        for (Frame frame : categoryFrames) {
            frame.render(guiGraphics, mouseX, mouseY, partialTicks);
        }
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
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
        if (keyCode == 344) { // Код клавиши RSHIFT
            this.onClose();
            mc.setScreen(null);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}