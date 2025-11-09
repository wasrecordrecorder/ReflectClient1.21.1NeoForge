package com.dsp.main.UI.ClickGui.Dropdown;

import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Dropdown.Theme.ThemesFrame;
import com.dsp.main.Utils.Font.builders.Builder;
import com.dsp.main.Utils.Font.renderers.impl.BuiltText;
import com.dsp.main.Utils.Render.Blur.DrawShader;
import com.dsp.main.Utils.Render.DrawHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;
import java.util.*;
import java.util.stream.Collectors;

import static com.dsp.main.Api.mc;
import static com.dsp.main.Main.BIKO_FONT;
import static com.dsp.main.Main.ICONS;

public class ClickGuiScreen extends Screen {
    private static final List<Frame> categoryFrames = new ArrayList<>();
    private static float scaleFactor = 1f;
    private static float userScaleMultiplier = 1f;
    private static String searchQuery = "";
    private boolean searchEnabled = false;
    private ThemesFrame themesFrame = null;
    private boolean isThemesFrameVisible = false;

    private long closeStart = -1;
    private boolean closing = false;
    private List<Integer> framesCloseOrder = new ArrayList<>();
    private Map<Integer, Long> frameCloseStart = new HashMap<>();
    private static final long FRAME_CLOSE_INTERVAL = 80L;
    private static final long FRAME_ANIM_DURATION = 150L;
    private float searchAnimProgress = 0f;
    private boolean searchVisible = false;
    private static final float SEARCH_ANIM_SPEED = 0.15f;

    private static final float MIN_SCALE = 0.5f;
    private static final float MAX_SCALE = 2.0f;
    private static final float SCALE_STEP = 0.05f;

    public ClickGuiScreen() {
        super(Component.literal("ClickGUI"));
        initFrames();
    }

    private void initFrames() {
        categoryFrames.clear();

        scaleFactor = calculateScaleFactor() * userScaleMultiplier;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int frameWidth = (int) (140 * scaleFactor);
        int frameHeight = (int) (22 * scaleFactor);
        int gap = (int) (20 * scaleFactor);

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

        if (themesFrame == null) {
            themesFrame = new ThemesFrame((int) (screenWidth * 0.1), (int) (screenHeight * 0.3), scaleFactor, this);
        } else {
            themesFrame.updateScale(scaleFactor);
        }
    }

    private static float calculateScaleFactor() {
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        float baseWidth = 1920f;
        float baseHeight = 1080f;

        float scaleX = screenWidth / baseWidth;
        float scaleY = screenHeight / baseHeight;

        float calculatedScale = Math.min(scaleX, scaleY);

        calculatedScale = Math.max(0.6f, Math.min(1.2f, calculatedScale));

        if (screenWidth < 800 || screenHeight < 600) {
            calculatedScale *= 1.3f;
        }

        return calculatedScale * 1.2f;
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
        if (!closing) {
            deactivateAllInputs();
            closing = true;
            framesCloseOrder.clear();
            for (int i = 0; i < categoryFrames.size(); i++) framesCloseOrder.add(i);
            Collections.shuffle(framesCloseOrder);
            long now = System.currentTimeMillis();
            for (int i = 0; i < framesCloseOrder.size(); i++) {
                frameCloseStart.put(framesCloseOrder.get(i), now + i * FRAME_CLOSE_INTERVAL);
            }
            searchVisible = false;
            searchEnabled = false;
            searchQuery = "";
        }
    }

    private void deactivateAllInputs() {
        for (Frame frame : categoryFrames) {
            for (com.dsp.main.UI.ClickGui.Dropdown.Button button : frame.getButtons()) {
                for (com.dsp.main.UI.ClickGui.Dropdown.Components.Component comp : button.getComponents()) {
                    if (comp instanceof com.dsp.main.UI.ClickGui.Dropdown.Components.InputComponent) {
                        ((com.dsp.main.UI.ClickGui.Dropdown.Components.InputComponent) comp).deactivate();
                    }
                    if (comp instanceof com.dsp.main.UI.ClickGui.Dropdown.Components.BindCheckBoxComponent) {
                        ((com.dsp.main.UI.ClickGui.Dropdown.Components.BindCheckBoxComponent) comp).deactivate();
                    }
                }
            }
        }
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (closing) {
            boolean allDone = true;
            long now = System.currentTimeMillis();
            for (int i = 0; i < categoryFrames.size(); i++) {
                Frame f = categoryFrames.get(i);
                Long start = frameCloseStart.get(i);
                if (start != null) {
                    float animProgress = (now - start) / (float) FRAME_ANIM_DURATION;
                    if (animProgress < 1f) {
                        allDone = false;
                        animProgress = Math.max(0, Math.min(animProgress, 1f));
                        f.setCloseOffsetY(-animProgress * 600f * scaleFactor);
                    } else {
                        f.setCloseOffsetY(-600f * scaleFactor);
                    }
                }
            }
            if (allDone) {
                super.onClose();
                mc.setScreen(null);
                return;
            }
        }

        this.renderBlurredBackground();
        DrawHelper.rectangle(guiGraphics.pose(), 0, 0,
                mc.getWindow().getGuiScaledWidth(),
                mc.getWindow().getGuiScaledHeight(), 0, 0x80000000);

        renderThemesButton(guiGraphics, mouseX, mouseY);
        if (searchVisible) {
            searchAnimProgress += SEARCH_ANIM_SPEED;
            if (searchAnimProgress > 1f) searchAnimProgress = 1f;
        } else {
            searchAnimProgress -= SEARCH_ANIM_SPEED;
            if (searchAnimProgress < 0f) searchAnimProgress = 0f;
        }
        if (searchAnimProgress > 0.01f) {
            renderSearchField(guiGraphics);
        }

        for (Frame frame : categoryFrames) {
            frame.setSearchQuery(searchQuery);
            frame.render(guiGraphics, mouseX, mouseY, partialTicks);
        }
        if (isThemesFrameVisible) themesFrame.render(guiGraphics, mouseX, mouseY, partialTicks);

        renderHints(guiGraphics);

        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    private void renderHints(GuiGraphics guiGraphics) {
        BuiltText text = Builder.text()
                .font(BIKO_FONT.get())
                .text("Press 'Ctrl + F' to enable search")
                .color(Color.WHITE)
                .size(7f * scaleFactor)
                .thickness(0.05f)
                .build();
        BuiltText text1 = Builder.text()
                .font(BIKO_FONT.get())
                .text("Press Enter to enable the module you are looking for")
                .color(Color.WHITE)
                .size(7f * scaleFactor)
                .thickness(0.05f)
                .build();
        BuiltText text2 = Builder.text()
                .font(BIKO_FONT.get())
                .text("Hold 'Ctrl + Scroll' to change GUI scale")
                .color(new Color(100, 200, 255))
                .size(7f * scaleFactor)
                .thickness(0.05f)
                .build();

        text.render(new Matrix4f(), 10 * scaleFactor, 10 * scaleFactor);
        text1.render(new Matrix4f(), 10 * scaleFactor, 20 * scaleFactor);
        text2.render(new Matrix4f(), 10 * scaleFactor, 30 * scaleFactor);

        String scaleText = String.format("Scale: %.0f%%", userScaleMultiplier * 100);
        BuiltText scaleInfo = Builder.text()
                .font(BIKO_FONT.get())
                .text(scaleText)
                .color(new Color(255, 200, 100))
                .size(8f * scaleFactor)
                .thickness(0.05f)
                .build();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        float textWidth = BIKO_FONT.get().getWidth(scaleText, 8f * scaleFactor);
        scaleInfo.render(new Matrix4f(), screenWidth - textWidth - 10 * scaleFactor, 10 * scaleFactor);
    }

    private void renderThemesButton(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int buttonSize = (int) (25 * scaleFactor);
        int buttonX = (int) (8 * scaleFactor);
        int buttonY = (int) (screenHeight - buttonSize - 8 * scaleFactor);
        DrawShader.drawRoundBlur(guiGraphics.pose(), buttonX, buttonY, buttonSize, buttonSize, 5 * scaleFactor, new Color(15, 20, 30, 200).hashCode(), 90, 0.2f);
        BuiltText icon = Builder.text()
                .font(ICONS.get())
                .text("g")
                .color(Color.WHITE.getRGB())
                .size(18f * scaleFactor)
                .thickness(0.05f)
                .build();
        int iconX = buttonX + (int) ((buttonSize - BIKO_FONT.get().getWidth("g", 16f * scaleFactor)) / 2);
        int iconY = buttonY + (int) ((buttonSize - BIKO_FONT.get().getMetrics().lineHeight() * 16f * scaleFactor) / 2);
        icon.render(new Matrix4f(), iconX - 5 * scaleFactor, iconY - 1 * scaleFactor);
    }

    private void renderSearchField(GuiGraphics guiGraphics) {
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int searchFieldWidth = (int) (250 * scaleFactor * searchAnimProgress);
        int searchFieldHeight = (int) (25 * scaleFactor * searchAnimProgress);
        int searchFieldX = (screenWidth - searchFieldWidth) / 2;
        int searchFieldY = (int) (35 * scaleFactor);
        int alpha = (int) (255 * searchAnimProgress);
        Color bgColor = new Color(5, 15, 45, alpha);

        DrawShader.drawRoundBlur(guiGraphics.pose(), searchFieldX, searchFieldY, searchFieldWidth, searchFieldHeight, 7 * scaleFactor, bgColor.getRGB(), 90, 0.6f);

        if (searchAnimProgress > 0.6f) {
            if (searchQuery.isEmpty()) {
                BuiltText searchText = Builder.text()
                        .font(BIKO_FONT.get())
                        .text("Search")
                        .color(new Color(150, 150, 150, alpha).getRGB())
                        .size(10f * scaleFactor)
                        .thickness(0.05f)
                        .build();
                searchText.render(new Matrix4f(), searchFieldX + (int) (8 * scaleFactor), searchFieldY + 2 + (int) (7 * scaleFactor));
            } else {
                BuiltText queryText = Builder.text()
                        .font(BIKO_FONT.get())
                        .text(searchQuery)
                        .color(new Color(255, 255, 255, alpha).getRGB())
                        .size(10f * scaleFactor)
                        .thickness(0.05f)
                        .build();
                queryText.render(new Matrix4f(), searchFieldX + (int) (8 * scaleFactor), searchFieldY + 2 + (int) (7 * scaleFactor));
            }

            BuiltText icon = Builder.text()
                    .font(ICONS.get())
                    .text("k")
                    .color(new Color(255, 255, 255, alpha).getRGB())
                    .size(12f * scaleFactor)
                    .thickness(0.05f)
                    .build();
            int iconX = searchFieldX + searchFieldWidth - (int) (18 * scaleFactor);
            int iconY = searchFieldY + (int) (6 * scaleFactor);
            icon.render(new Matrix4f(), iconX, iconY);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (closeStart != -1) return true;

        long window = mc.getWindow().getWindow();
        boolean ctrlPressed = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS ||
                GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;

        if (ctrlPressed) {
            float oldScale = userScaleMultiplier;
            userScaleMultiplier += (scrollY > 0 ? SCALE_STEP : -SCALE_STEP);
            userScaleMultiplier = Math.max(MIN_SCALE, Math.min(MAX_SCALE, userScaleMultiplier));

            if (oldScale != userScaleMultiplier) {
                initFrames();
            }
            return true;
        }

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
        int buttonSize = (int) (25 * scaleFactor);
        int buttonX = (int) (8 * scaleFactor);
        int buttonY = (int) (screenHeight - buttonSize - 8 * scaleFactor);
        if (mouseX >= buttonX && mouseX <= buttonX + buttonSize && mouseY >= buttonY && mouseY <= buttonY + buttonSize && button == 0) {
            isThemesFrameVisible = !isThemesFrameVisible;
            themesFrame.setVisible(isThemesFrameVisible);
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
                .anyMatch(com.dsp.main.UI.ClickGui.Dropdown.Button::isBinding);
        boolean anyInputActive = categoryFrames.stream()
                .flatMap(f -> f.getButtons().stream())
                .flatMap(b -> b.getComponents().stream())
                .anyMatch(com.dsp.main.UI.ClickGui.Dropdown.Components.Component::isInputActive);

        if (!searchEnabled && keyCode == GLFW.GLFW_KEY_F && (modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
            searchEnabled = true;
            searchVisible = true;
            searchQuery = "";
            return true;
        }

        if (searchEnabled && !anyInputActive) {
            if (keyCode == GLFW.GLFW_KEY_ENTER) {
                List<com.dsp.main.UI.ClickGui.Dropdown.Button> visible = categoryFrames.stream()
                        .flatMap(f -> f.getButtons().stream())
                        .filter(b -> b.getModule().getName().toLowerCase().contains(searchQuery.toLowerCase()))
                        .collect(Collectors.toList());
                if (visible.size() == 1) {
                    visible.get(0).getModule().toggle();
                }
                searchEnabled = false;
                searchVisible = false;
                searchQuery = "";
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                searchEnabled = false;
                searchVisible = false;
                searchQuery = "";
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (!searchQuery.isEmpty()) {
                    searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
                    if (searchQuery.isEmpty()) {
                        searchEnabled = false;
                        searchVisible = false;
                    }
                }
                return true;
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

    @Override
    public boolean charTyped(char chr, int modifiers) {
        for (Frame frame : categoryFrames) {
            frame.charTyped(chr);
        }
        return super.charTyped(chr, modifiers);
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

    public static float getUserScaleMultiplier() {
        return userScaleMultiplier;
    }

    public static void setUserScaleMultiplier(float scale) {
        userScaleMultiplier = Math.max(MIN_SCALE, Math.min(MAX_SCALE, scale));
    }
}