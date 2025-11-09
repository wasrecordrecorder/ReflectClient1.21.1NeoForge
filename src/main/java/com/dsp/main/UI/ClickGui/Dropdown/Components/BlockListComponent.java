package com.dsp.main.UI.ClickGui.Dropdown.Components;

import com.dsp.main.UI.ClickGui.Dropdown.Button;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.BlockListSetting;
import com.dsp.main.Utils.Font.builders.Builder;
import com.dsp.main.Utils.Font.renderers.impl.BuiltBorder;
import com.dsp.main.Utils.Font.renderers.impl.BuiltText;
import com.dsp.main.Utils.Render.Blur.DrawShader;
import com.dsp.main.Utils.Render.DrawHelper;
import com.dsp.main.UI.Themes.ThemesUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.dsp.main.Api.mc;
import static com.dsp.main.Main.RUS;
import static com.dsp.main.Main.ICONS;

public class BlockListComponent extends Component {
    private static final float ROUNDING = 4.0f;
    private static final float PANEL_HEIGHT = 120.0f;
    private static final float SEARCH_HEIGHT = 18.0f;
    private static final float PADDING = 2.0f;
    private static final float ITEM_PADDING = 2.0f;
    private static final float MIN_ICON_SIZE = 14.0f;
    private static final float MAX_ICON_SIZE = 16.0f;
    private static final float SCROLL_SPEED = 15.0f;
    private static final float SCROLL_LERP = 0.25f;

    private final BlockListSetting blockListSetting;
    private String searchQuery = "";
    private boolean searchFocused = false;
    private float scrollOffset = 0.0f;
    private float targetScrollOffset = 0.0f;
    private float iconSize = 16.0f;
    private int columns = 1;
    private long lastFrameTime = System.currentTimeMillis();

    public BlockListComponent(BlockListSetting setting, Button parent, float scaleFactor) {
        super(setting, parent, scaleFactor);
        this.blockListSetting = setting;
        calculateLayout();
    }

    private void calculateLayout() {
        float availableWidth = parent.getWidth() - (PADDING * 4 * scaleFactor);

        for (int cols = 5; cols >= 1; cols--) {
            float testIconSize = (availableWidth - ((cols - 1) * ITEM_PADDING * scaleFactor)) / cols;
            if (testIconSize >= MIN_ICON_SIZE * scaleFactor && testIconSize <= MAX_ICON_SIZE * scaleFactor) {
                columns = cols;
                iconSize = testIconSize;
                return;
            }
        }

        columns = Math.max(1, (int) (availableWidth / (MIN_ICON_SIZE * scaleFactor + ITEM_PADDING * scaleFactor)));
        iconSize = (availableWidth - ((columns - 1) * ITEM_PADDING * scaleFactor)) / columns;
    }

    @Override
    public float getHeight() {
        return (SEARCH_HEIGHT + PANEL_HEIGHT + PADDING * 3) * scaleFactor;
    }

    @Override
    public void draw(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!isVisible()) return;

        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastFrameTime) / 1000.0f;
        lastFrameTime = currentTime;

        PoseStack poseStack = graphics.pose();
        Matrix4f matrix = poseStack.last().pose();

        float startY = (float) (y + PADDING * scaleFactor);

        drawSearchField(poseStack, matrix, startY, mouseX, mouseY);
        drawBlockPanel(graphics, poseStack, matrix, startY + SEARCH_HEIGHT * scaleFactor + PADDING * scaleFactor, mouseX, mouseY, deltaTime);
    }

    private void drawSearchField(PoseStack poseStack, Matrix4f matrix, float startY, int mouseX, int mouseY) {
        float searchX = (float) (x - 2 + PADDING * scaleFactor);
        float searchWidth = parent.getWidth() - (PADDING * 2 * scaleFactor);

        DrawShader.drawRoundBlur(poseStack, searchX, startY, searchWidth, SEARCH_HEIGHT * scaleFactor,
                ROUNDING * scaleFactor, new Color(15, 20, 30, 200).hashCode(), 90, 0.5f);

        float iconSize = 10.0f * scaleFactor;
        BuiltText searchIcon = Builder.text()
                .font(ICONS.get())
                .text("k")
                .color(new Color(150, 150, 150))
                .size(iconSize)
                .thickness(0.05f)
                .build();
        searchIcon.render(matrix, searchX + 4 * scaleFactor, startY + (SEARCH_HEIGHT * scaleFactor - iconSize) / 2);

        if (searchQuery.isEmpty()) {
            BuiltText placeholderText = Builder.text()
                    .font(RUS.get())
                    .text("Search blocks...")
                    .color(new Color(100, 100, 100))
                    .size(8.0f * scaleFactor)
                    .thickness(0.05f)
                    .build();
            placeholderText.render(matrix, searchX + 18 * scaleFactor, startY + (SEARCH_HEIGHT * scaleFactor - 8.0f * scaleFactor) / 2);
        } else {
            BuiltText searchText = Builder.text()
                    .font(RUS.get())
                    .text(searchQuery)
                    .color(Color.WHITE)
                    .size(8.0f * scaleFactor)
                    .thickness(0.05f)
                    .build();
            searchText.render(matrix, searchX + 18 * scaleFactor, startY + (SEARCH_HEIGHT * scaleFactor - 8.0f * scaleFactor) / 2);
        }

        if (searchFocused && System.currentTimeMillis() % 1000 < 500 && !searchQuery.isEmpty()) {
            float cursorX = searchX + 18 * scaleFactor + RUS.get().getWidth(searchQuery, 8.0f * scaleFactor);
            DrawHelper.rectangle(poseStack, cursorX, startY + 3 * scaleFactor,
                    1 * scaleFactor, SEARCH_HEIGHT * scaleFactor - 6 * scaleFactor,
                    0, Color.WHITE.getRGB());
        }
    }

    private void drawBlockPanel(GuiGraphics graphics, PoseStack poseStack, Matrix4f matrix, float startY, int mouseX, int mouseY, float deltaTime) {
        float panelX = (float) (x - 2 + PADDING * scaleFactor);
        float panelWidth = parent.getWidth() - (PADDING * 2 * scaleFactor);
        float panelHeight = PANEL_HEIGHT * scaleFactor;

        Color basePanelColor = new Color(10, 15, 25, 220);
        Color darkerPanelColor = new Color(
                (int)(basePanelColor.getRed() * 0.8f),
                (int)(basePanelColor.getGreen() * 0.8f),
                (int)(basePanelColor.getBlue() * 0.8f),
                basePanelColor.getAlpha()
        );

        DrawShader.drawRoundBlur(poseStack, panelX, startY, panelWidth, panelHeight,
                ROUNDING * scaleFactor, darkerPanelColor.getRGB(), 90, 0.6f);

        List<Block> filteredBlocks = getFilteredBlocks();
        int rows = (int) Math.ceil((double) filteredBlocks.size() / columns);
        float contentHeight = rows * (iconSize + ITEM_PADDING * scaleFactor) - ITEM_PADDING * scaleFactor;
        float maxScroll = Math.max(0, contentHeight - panelHeight + PADDING * 2 * scaleFactor);

        targetScrollOffset = Math.max(0, Math.min(targetScrollOffset, maxScroll));
        scrollOffset += (targetScrollOffset - scrollOffset) * SCROLL_LERP;

        graphics.enableScissor((int) panelX, (int) startY + 3, (int) (panelX + panelWidth), (int) (startY + panelHeight));

        float blockX = panelX - 2 + PADDING * scaleFactor;
        float blockY = startY + PADDING * scaleFactor - scrollOffset;

        int index = 0;
        for (Block block : filteredBlocks) {
            int col = index % columns;
            int row = index / columns;

            float bx = blockX + col * (iconSize + ITEM_PADDING * scaleFactor);
            float by = blockY + row * (iconSize + ITEM_PADDING * scaleFactor);

            if (by + iconSize < startY || by > startY + panelHeight) {
                index++;
                continue;
            }

            boolean selected = blockListSetting.isBlockSelected(block);
            boolean hovered = mouseX >= bx && mouseX <= bx + iconSize &&
                    mouseY >= by && mouseY <= by + iconSize;

            Color bgColor = selected ? new Color(50, 70, 100, 200) : new Color(25, 30, 40, 180);
            if (hovered && !selected) {
                bgColor = new Color(40, 50, 70, 200);
            }

            DrawHelper.rectangle(poseStack, bx, by, iconSize, iconSize,
                    ROUNDING * scaleFactor * 0.5f, bgColor.getRGB());

            ResourceLocation texture = blockListSetting.getBlockTexture(block);
            if (texture != null) {
                float texturePadding = 1 * scaleFactor;
                DrawHelper.drawTexture(texture, matrix,
                        bx + texturePadding, by + texturePadding,
                        iconSize - texturePadding * 2, iconSize - texturePadding * 2);
            }

            if (selected) {
                BuiltBorder border = Builder.border()
                        .size(new com.dsp.main.Utils.Font.builders.states.SizeState(iconSize, iconSize))
                        .color(new com.dsp.main.Utils.Font.builders.states.QuadColorState(
                                ThemesUtil.getCurrentStyle().getColor(1),
                                ThemesUtil.getCurrentStyle().getColor(2),
                                ThemesUtil.getCurrentStyle().getColor(1),
                                ThemesUtil.getCurrentStyle().getColor(2)
                        ))
                        .radius(new com.dsp.main.Utils.Font.builders.states.QuadRadiusState(
                                ROUNDING * scaleFactor * 0.5f,
                                ROUNDING * scaleFactor * 0.5f,
                                ROUNDING * scaleFactor * 0.5f,
                                ROUNDING * scaleFactor * 0.5f
                        ))
                        .thickness(0.5f)
                        .smoothness(0.5f, 0.9f)
                        .build();
                border.render(matrix, bx, by);
            }

            index++;
        }

        graphics.disableScissor();

        if (contentHeight > panelHeight) {
            drawScrollbar(poseStack, panelX + 2, startY, panelWidth, panelHeight, contentHeight, maxScroll);
        }
    }

    private void drawScrollbar(PoseStack poseStack, float panelX, float panelY, float panelWidth, float panelHeight, float contentHeight, float maxScroll) {
        float scrollbarWidth = 2.0f * scaleFactor;
        float scrollbarHeight = (panelHeight / contentHeight) * panelHeight;
        float scrollbarY = panelY + (scrollOffset / maxScroll) * (panelHeight - scrollbarHeight);
        float scrollbarX = panelX + panelWidth - scrollbarWidth + 2 * scaleFactor;  // +2px вправо

        DrawShader.drawRoundBlur(poseStack, scrollbarX, scrollbarY, scrollbarWidth, scrollbarHeight,
                1.5f * scaleFactor, new Color(100, 100, 100, 150).hashCode(), 90, 0.7f);
    }

    private List<Block> getFilteredBlocks() {
        List<Block> filtered = new ArrayList<>();
        String query = searchQuery.toLowerCase();

        for (Block block : blockListSetting.getAvailableBlocks()) {
            ResourceLocation rl = BuiltInRegistries.BLOCK.getKey(block);
            if (rl != null) {
                String name = rl.getPath().replace("_", " ");
                if (query.isEmpty() || name.contains(query)) {
                    filtered.add(block);
                }
            }
        }

        filtered.sort(new Comparator<Block>() {
            @Override
            public int compare(Block b1, Block b2) {
                boolean selected1 = blockListSetting.isBlockSelected(b1);
                boolean selected2 = blockListSetting.isBlockSelected(b2);

                if (selected1 && !selected2) return -1;
                if (!selected1 && selected2) return 1;

                ResourceLocation rl1 = BuiltInRegistries.BLOCK.getKey(b1);
                ResourceLocation rl2 = BuiltInRegistries.BLOCK.getKey(b2);

                if (rl1 != null && rl2 != null) {
                    return rl1.getPath().compareTo(rl2.getPath());
                }

                return 0;
            }
        });

        return filtered;
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (!isVisible() || button != 0) return;

        float searchX = (float) (x + PADDING * scaleFactor);
        float searchY = (float) (y + PADDING * scaleFactor);
        float searchWidth = parent.getWidth() - (PADDING * 2 * scaleFactor);

        if (mouseX >= searchX && mouseX <= searchX + searchWidth &&
                mouseY >= searchY && mouseY <= searchY + SEARCH_HEIGHT * scaleFactor) {
            searchFocused = true;
            return;
        } else {
            searchFocused = false;
        }

        float panelX = (float) (x + PADDING * scaleFactor);
        float panelY = (float) (y + PADDING * scaleFactor + SEARCH_HEIGHT * scaleFactor + PADDING * scaleFactor);
        float panelHeight = PANEL_HEIGHT * scaleFactor;

        if (mouseX < panelX || mouseX > panelX + parent.getWidth() - (PADDING * 2 * scaleFactor) ||
                mouseY < panelY || mouseY > panelY + panelHeight) {
            return;
        }

        float blockX = panelX + PADDING * scaleFactor;
        float blockY = panelY + PADDING * scaleFactor - scrollOffset;

        List<Block> filteredBlocks = getFilteredBlocks();
        int index = 0;
        for (Block block : filteredBlocks) {
            int col = index % columns;
            int row = index / columns;

            float bx = blockX + col * (iconSize + ITEM_PADDING * scaleFactor);
            float by = blockY + row * (iconSize + ITEM_PADDING * scaleFactor);

            if (mouseX >= bx && mouseX <= bx + iconSize &&
                    mouseY >= by && mouseY <= by + iconSize) {
                blockListSetting.toggleBlock(block);
                return;
            }

            index++;
        }
    }

    public void keyPressed(int keyCode) {
        if (!searchFocused) return;

        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (!searchQuery.isEmpty()) {
                searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
                targetScrollOffset = 0;
            }
        } else if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_ENTER) {
            searchFocused = false;
        }
    }

    public void charTyped(char chr) {
        if (!searchFocused) return;

        if (Character.isLetterOrDigit(chr) || chr == ' ' || chr == '_') {
            searchQuery += chr;
            targetScrollOffset = 0;
        }
    }

    public void mouseScrolled(double mouseX, double mouseY, double delta) {
        if (!isVisible()) return;

        float panelX = (float) (x + PADDING * scaleFactor);
        float panelY = (float) (y + PADDING * scaleFactor + SEARCH_HEIGHT * scaleFactor + PADDING * scaleFactor);
        float panelWidth = parent.getWidth() - (PADDING * 2 * scaleFactor);
        float panelHeight = PANEL_HEIGHT * scaleFactor;

        if (mouseX >= panelX && mouseX <= panelX + panelWidth &&
                mouseY >= panelY && mouseY <= panelY + panelHeight) {
            targetScrollOffset -= (float) (delta * SCROLL_SPEED * scaleFactor);
        }
    }

    public boolean isScrolling(double mouseX, double mouseY) {
        float panelX = (float) (x + PADDING * scaleFactor);
        float panelY = (float) (y + PADDING * scaleFactor + SEARCH_HEIGHT * scaleFactor + PADDING * scaleFactor);
        float panelWidth = parent.getWidth() - (PADDING * 2 * scaleFactor);
        float panelHeight = PANEL_HEIGHT * scaleFactor;

        return mouseX >= panelX && mouseX <= panelX + panelWidth &&
                mouseY >= panelY && mouseY <= panelY + panelHeight;
    }

    @Override
    public boolean isInputActive() {
        return searchFocused;
    }

    @Override
    public boolean isHovered(double mouseX, double mouseY) {
        if (!isVisible()) return false;
        return mouseX >= x && mouseX <= x + parent.getWidth() &&
                mouseY >= y && mouseY <= y + getHeight();
    }
}