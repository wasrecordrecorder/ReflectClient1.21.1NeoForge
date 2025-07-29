package com.dsp.main.UI.ClickGui.Dropdown;

import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Dropdown.Theme.ThemesFrame;
import com.dsp.main.Utils.Font.builders.Builder;
import com.dsp.main.Utils.Font.renderers.impl.BuiltText;
import com.dsp.main.Utils.Minecraft.Chat.ChatUtil;
import com.dsp.main.Utils.Render.Blur.DrawShader;
import com.dsp.main.Utils.Render.DrawHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import static com.dsp.main.Api.mc;
import static com.dsp.main.Main.BIKO_FONT;
import static com.dsp.main.Main.ICONS;

public class ClickGuiScreen extends Screen {
    private static final List<Frame> categoryFrames = new ArrayList<>();
    private static float scaleFactor = 1f;
    private static String searchQuery = "";
    private boolean isSearchActive = true;
    private ThemesFrame themesFrame = null;
    private boolean isThemesFrameVisible = false;

    // --- closing animation ---
    private long closeStart = -1;
    private float closePartialTicks = 0f;

    public ClickGuiScreen() {
        super(Component.literal("ClickGUI"));
        categoryFrames.clear();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int frameWidth = (int) (117 * scaleFactor);
        int frameHeight = (int) (18 * scaleFactor);
        int gap = (int) (18 * scaleFactor);

        int count = Module.Category.values().length;
        int totalWidth = count * frameWidth + (count - 1) * gap;
        int startX = (screenWidth - totalWidth) / 2;

        int headerHeight = frameHeight;
        int footerHeight = (int) (frameHeight / 2 * scaleFactor);
        int maxVisibleHeight = (int) (screenHeight * 0.65 * scaleFactor);
        int totalFrameHeight = headerHeight + maxVisibleHeight + footerHeight;
        int startY = (screenHeight - totalFrameHeight) / 2;

        for (int i = 0; i < count; i++) {
            Module.Category category = Module.Category.values()[i];
            int x = startX + i * (frameWidth + gap);
            categoryFrames.add(new Frame(x, startY, frameHeight, category, i, scaleFactor));
        }
        themesFrame = new ThemesFrame((int) (screenWidth * 0.1), (int) (screenHeight * 0.3), scaleFactor, this);
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        return;
    }

    @Override
    public void onClose() {
        if (closeStart == -1) {
            closeStart = System.currentTimeMillis();
        }
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        // ----- closing animation bookkeeping -----
        if (closeStart != -1) {
            closePartialTicks = (System.currentTimeMillis() - closeStart + partialTicks * 50f) / 200f;
            if (closePartialTicks >= 1f) {
                closeStart = -1;
                closePartialTicks = 0f;
                super.onClose();
                mc.setScreen(null);
                return;
            }
        }

        // ----- apply transform -----
        guiGraphics.pose().pushPose();
        float scale = closeStart == -1 ? 1f : 1f - closePartialTicks;

        int cx = mc.getWindow().getGuiScaledWidth() / 2;
        int cy = mc.getWindow().getGuiScaledHeight() / 2;

        guiGraphics.pose().translate(cx, cy, 0);
        guiGraphics.pose().scale(scale, scale, 1f);
        guiGraphics.pose().translate(-cx, -cy, 0);
        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();

        int clipW = (int) (screenW * scale);
        int clipH = (int) (screenH * scale);
        int clipX = (screenW - clipW) / 2;
        int clipY = (screenH - clipH) / 2;

        guiGraphics.enableScissor(clipX, clipY, clipX + clipW, clipY + clipH);

        // ----- existing rendering -----
        this.renderBlurredBackground(partialTicks);
        int shadowColor = 0x80000000;
        DrawHelper.rectangle(guiGraphics.pose(), 0, 0, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight(), 0, shadowColor);
        renderThemesButton(guiGraphics, mouseX, mouseY);
        renderSearchField(guiGraphics);

        for (Frame frame : categoryFrames) {
            frame.setSearchQuery(searchQuery);
            frame.render(guiGraphics, mouseX, mouseY, partialTicks);
        }
        if (isThemesFrameVisible) {
            themesFrame.render(guiGraphics, mouseX, mouseY, partialTicks);
        }
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.disableScissor();
        guiGraphics.pose().popPose();
    }

    private void renderThemesButton(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int buttonSize = (int) (20 * scaleFactor);
        int buttonX = (int) (5 * scaleFactor);
        int buttonY = (int) (screenHeight - buttonSize - 5 * scaleFactor);
        DrawShader.drawRoundBlur(guiGraphics.pose(), buttonX, buttonY, buttonSize, buttonSize, 4 * scaleFactor, new Color(15, 20, 30, 200).hashCode(), 90, 0.2f);
        BuiltText icon = Builder.text()
                .font(ICONS.get())
                .text("g")
                .color(Color.WHITE.getRGB())
                .size(16f * scaleFactor)
                .thickness(0.05f)
                .build();
        int iconX = buttonX + (int) ((buttonSize - BIKO_FONT.get().getWidth("g", 14f * scaleFactor)) / 2);
        int iconY = buttonY + (int) ((buttonSize - BIKO_FONT.get().getMetrics().lineHeight() * 14f * scaleFactor) / 2);
        icon.render(new Matrix4f(), iconX - 5, iconY - 1);
    }

    private void renderSearchField(GuiGraphics guiGraphics) {
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int searchFieldWidth = (int) (200 * scaleFactor);
        int searchFieldHeight = (int) (20 * scaleFactor);
        int searchFieldX = (screenWidth - searchFieldWidth) / 2;
        int searchFieldY = (int) (30 * scaleFactor);
        DrawShader.drawRoundBlur(guiGraphics.pose(), searchFieldX, searchFieldY, searchFieldWidth, searchFieldHeight, 6 * scaleFactor, new Color(5, 15, 45).hashCode(), 90, 0.6f);
        if (searchQuery.isEmpty()) {
            BuiltText searchText = Builder.text()
                    .font(BIKO_FONT.get())
                    .text("Search")
                    .color(new Color(150, 150, 150).getRGB())
                    .size(9f * scaleFactor)
                    .thickness(0.05f)
                    .build();
            searchText.render(new Matrix4f(), searchFieldX + (int) (5 * scaleFactor), searchFieldY + 1 + (int) (5 * scaleFactor));
        } else {
            BuiltText queryText = Builder.text()
                    .font(BIKO_FONT.get())
                    .text(searchQuery)
                    .color(Color.WHITE.getRGB())
                    .size(9f * scaleFactor)
                    .thickness(0.05f)
                    .build();
            queryText.render(new Matrix4f(), searchFieldX + (int) (5 * scaleFactor), searchFieldY + 1 + (int) (5 * scaleFactor));
        }

        BuiltText icon = Builder.text()
                .font(ICONS.get())
                .text("k")
                .color(Color.WHITE.getRGB())
                .size(10f * scaleFactor)
                .thickness(0.05f)
                .build();
        int iconX = searchFieldX + searchFieldWidth - (int) (15 * scaleFactor);
        int iconY = searchFieldY + (int) (5 * scaleFactor);
        icon.render(new Matrix4f(), iconX, iconY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (closeStart != -1) return true;
        if (isThemesFrameVisible) {
            themesFrame.mouseScrolled(mouseX, mouseY, scrollY);
        }
        for (Frame frame : categoryFrames) {
            frame.mouseScrolled(mouseX, mouseY, scrollY);
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (closeStart != -1) return true;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int buttonSize = (int) (20 * scaleFactor);
        int buttonX = (int) (5 * scaleFactor);
        int buttonY = (int) (screenHeight - buttonSize - 5 * scaleFactor);
        if (mouseX >= buttonX && mouseX <= buttonX + buttonSize && mouseY >= buttonY && mouseY <= buttonY + buttonSize && button == 0) {
            isThemesFrameVisible = !isThemesFrameVisible;
            return true;
        }
        if (isThemesFrameVisible && themesFrame.isHovered(mouseX, mouseY)) {
            themesFrame.mouseClicked(mouseX, mouseY, button);
            return true;
        }
        for (Frame frame : categoryFrames) {
            if (frame.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (closeStart != -1) return true;
        if (isThemesFrameVisible) {
            themesFrame.mouseDragged(mouseX, mouseY);
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (closeStart != -1) return true;
        if (isThemesFrameVisible) {
            themesFrame.mouseReleased(mouseX, mouseY, button);
        }
        for (Frame frame : categoryFrames) {
            frame.mouseReleased(mouseX, mouseY, button);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (closeStart != -1) return true;

        boolean anyBindingActive = categoryFrames.stream()
                .flatMap(f -> f.getButtons().stream())
                .anyMatch(Button::isBinding);
        boolean anyInputActive = categoryFrames.stream()
                .flatMap(f -> f.getButtons().stream())
                .flatMap(b -> b.getComponents().stream())
                .anyMatch(com.dsp.main.UI.ClickGui.Dropdown.Components.Component::isInputActive);
        isSearchActive = !anyBindingActive && !anyInputActive;

        if (isSearchActive) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (!searchQuery.isEmpty()) {
                    searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
                }
            } else {
                char typedChar = getCharFromKey(keyCode);
                if (typedChar != 0) {
                    searchQuery += typedChar;
                }
            }
        }

        for (Frame frame : categoryFrames) {
            frame.keyPressed(keyCode);
        }
        if (keyCode == 344) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private char getCharFromKey(int keyCode) {
        if (keyCode >= GLFW.GLFW_KEY_A && keyCode <= GLFW.GLFW_KEY_Z) {
            return (char) ('a' + (keyCode - GLFW.GLFW_KEY_A));
        } else if (keyCode == GLFW.GLFW_KEY_SPACE) {
            return ' ';
        }
        return 0;
    }

    public static String getSearchQuery() {
        return searchQuery;
    }

    public void setThemesFrameVisible(boolean visible) {
        isThemesFrameVisible = visible;
    }
}