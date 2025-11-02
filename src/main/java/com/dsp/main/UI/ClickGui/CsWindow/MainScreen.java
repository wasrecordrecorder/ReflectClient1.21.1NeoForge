package com.dsp.main.UI.ClickGui.CsWindow;

import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.*;
import com.dsp.main.UI.Themes.ThemesUtil;
import com.dsp.main.Utils.Color.ColorHelper;
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

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.dsp.main.Api.mc;
import static com.dsp.main.Main.BIKO_FONT;
import static com.dsp.main.Main.ICONS;

public class MainScreen extends Screen {
    private int windowX, windowY, windowWidth, windowHeight;
    private boolean dragging = false;
    private int dragOffsetX, dragOffsetY;

    private Module.Category selectedCategory = Module.Category.COMBAT;
    private Module selectedModule = null;
    private String searchQuery = "";
    private boolean searchFocused = false;

    private float categoryScroll = 0;
    private float moduleScroll = 0;
    private float settingsScroll = 0;
    private float targetCategoryScroll = 0;
    private float targetModuleScroll = 0;
    private float targetSettingsScroll = 0;

    private final List<SettingComponent> settingComponents = new ArrayList<>();

    private static final int HEADER_HEIGHT = 35;
    private static final int CATEGORY_WIDTH = 120;
    private static final int MODULE_WIDTH = 180;
    private static final int SETTINGS_WIDTH = 220;
    private static final float SCROLL_SPEED = 0.3f;

    // Modern Dark Blue Theme
    private static final Color BASE = new Color(15, 20, 35);           // Темно-синий фон
    private static final Color MANTLE = new Color(12, 16, 28);         // Еще темнее
    private static final Color SURFACE0 = new Color(25, 35, 55);       // Панели с синевой
    private static final Color SURFACE1 = new Color(35, 45, 70);       // Элементы светлее
    private static final Color SURFACE2 = new Color(45, 60, 90);       // Hover состояния
    private static final Color OVERLAY0 = new Color(60, 80, 120);      // Borders синие
    private static final Color BLUE_ACCENT = new Color(88, 166, 255);  // Яркий синий акцент
    private static final Color BLUE_DIM = new Color(65, 120, 190);     // Тусклый синий
    private static final Color TEXT = new Color(225, 235, 255);        // Белый с синевой
    private static final Color SUBTEXT0 = new Color(180, 195, 220);    // Серо-синий
    private static final Color SUBTEXT1 = new Color(140, 160, 190);    // Тусклый текст

    public MainScreen() {
        super(Component.literal("ClickGUI"));
    }

    @Override
    protected void init() {
        super.init();
        windowWidth = (int) (this.width * 0.65f);
        windowHeight = (int) (this.height * 0.7f);
        windowX = (this.width - windowWidth) / 2;
        windowY = (this.height - windowHeight) / 2;

        if (selectedModule != null) {
            updateSettingComponents();
        }
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        return;
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBlurredBackground(partialTicks);
        DrawHelper.rectangle(guiGraphics.pose(), 0, 0, width, height, 0, 0x95000510);

        categoryScroll = lerp(categoryScroll, targetCategoryScroll, SCROLL_SPEED);
        moduleScroll = lerp(moduleScroll, targetModuleScroll, SCROLL_SPEED);
        settingsScroll = lerp(settingsScroll, targetSettingsScroll, SCROLL_SPEED);

        renderMainWindow(guiGraphics, mouseX, mouseY);
        renderHeader(guiGraphics, mouseX, mouseY);
        renderCategoryPanel(guiGraphics, mouseX, mouseY);
        renderModulePanel(guiGraphics, mouseX, mouseY);
        renderSettingsPanel(guiGraphics, mouseX, mouseY);
        renderPanelDividers(guiGraphics);

        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    private void renderMainWindow(GuiGraphics graphics, int mouseX, int mouseY) {
        // Основной фон окна с синим градиентом
        DrawHelper.rectRGB(graphics.pose(), windowX, windowY, windowWidth, windowHeight, 10,
                withAlpha(BASE, 250), withAlpha(MANTLE, 250),
                withAlpha(BASE, 250), withAlpha(MANTLE, 250));

        // Blur эффект
        DrawShader.drawRoundBlur(graphics.pose(), windowX, windowY, windowWidth, windowHeight, 10,
                withAlpha(BASE, 230), 6, 0.55f);

        // Синяя тонкая обводка

    }

    private void renderHeader(GuiGraphics graphics, int mouseX, int mouseY) {
        int headerY = windowY;

        // Фон хедера с синим оттенком
        DrawShader.drawRoundBlur(graphics.pose(), windowX, headerY, windowWidth, HEADER_HEIGHT, 10,
                withAlpha(SURFACE0, 220), 90, 0.45f);

        // Нижняя граница хедера синяя
        DrawHelper.rectangle(graphics.pose(), windowX, headerY + HEADER_HEIGHT - 1, windowWidth, 1, 0,
                withAlpha(BLUE_DIM, 120));

        // Иконка и название
        int gradientColor = ColorHelper.gradient(
                ThemesUtil.getCurrentStyle().getColor(1),
                ThemesUtil.getCurrentStyle().getColor(2), 20, 10);

        BuiltText icon = Builder.text()
                .font(ICONS.get())
                .text("X")
                .color(BLUE_ACCENT.getRGB())
                .size(18f)
                .thickness(0.06f)
                .build();

        BuiltText text = Builder.text()
                .font(BIKO_FONT.get())
                .text("Reflect")
                .color(TEXT.getRGB())
                .size(11f)
                .thickness(0.08f)
                .build();

        icon.render(new Matrix4f(), windowX + 10, headerY + 12);
        text.render(new Matrix4f(), windowX + 20 + ICONS.get().getWidth("X", 18f), headerY + 15);

        renderSearchBar(graphics, mouseX, mouseY);
    }

    private void renderSearchBar(GuiGraphics graphics, int mouseX, int mouseY) {
        int searchWidth = 150;
        int searchHeight = 22;
        int searchX = windowX + windowWidth - searchWidth - 15;
        int searchY = windowY + (HEADER_HEIGHT - searchHeight) / 2;

        boolean searchHovered = mouseX >= searchX && mouseX <= searchX + searchWidth &&
                mouseY >= searchY && mouseY <= searchY + searchHeight;

        // Фон поиска
        DrawShader.drawRoundBlur(graphics.pose(), searchX, searchY, searchWidth, searchHeight, 6,
                searchFocused ? withAlpha(SURFACE2, 230) :
                        searchHovered ? withAlpha(SURFACE1, 210) :
                                withAlpha(SURFACE0, 190), 90, 0.4f);

        // Синяя обводка при фокусе
        if (searchFocused) {
            DrawHelper.rectangle(graphics.pose(), searchX - 1, searchY - 1, searchWidth + 2, searchHeight + 2, 6,
                    withAlpha(BLUE_ACCENT, 150));
        }

        String displayText = searchQuery.isEmpty() && !searchFocused ? "Search..." : searchQuery;
        int textColor = searchQuery.isEmpty() && !searchFocused ? SUBTEXT1.getRGB() : TEXT.getRGB();

        BuiltText searchText = Builder.text()
                .font(BIKO_FONT.get())
                .text(displayText)
                .color(textColor)
                .size(8.5f)
                .thickness(0.05f)
                .build();

        searchText.render(new Matrix4f(), searchX + 8, searchY + 7);

        BuiltText searchIcon = Builder.text()
                .font(ICONS.get())
                .text("k")
                .color(withAlpha(BLUE_DIM, 200))
                .size(11f)
                .thickness(0.05f)
                .build();

        searchIcon.render(new Matrix4f(), searchX + searchWidth - 22, searchY + 6);
    }

    private void renderCategoryPanel(GuiGraphics graphics, int mouseX, int mouseY) {
        int panelX = windowX;
        int panelY = windowY + HEADER_HEIGHT;
        int panelHeight = windowHeight - HEADER_HEIGHT;

        // Фон панели категорий
        DrawHelper.rectangle(graphics.pose(), panelX, panelY, CATEGORY_WIDTH, panelHeight, 0,
                withAlpha(MANTLE, 220));

        int contentY = panelY + 10;
        int itemHeight = 36;

        graphics.enableScissor(panelX, panelY, panelX + CATEGORY_WIDTH, panelY + panelHeight);

        int offsetY = (int) (contentY - categoryScroll);

        for (Module.Category category : Module.Category.values()) {
            boolean selected = category == selectedCategory;
            boolean hovered = mouseX >= panelX && mouseX <= panelX + CATEGORY_WIDTH &&
                    mouseY >= offsetY && mouseY <= offsetY + itemHeight;

            // Фон элемента
            if (selected) {
                DrawShader.drawRoundBlur(graphics.pose(), panelX + 6, offsetY, CATEGORY_WIDTH - 12,
                        itemHeight, 6, withAlpha(SURFACE2, 220), 90, 0.4f);

                // Синяя акцентная полоска слева
                DrawHelper.rectangle(graphics.pose(), panelX + 6, offsetY, 3, itemHeight, 6,
                        BLUE_ACCENT.getRGB());
            } else if (hovered) {
                DrawHelper.rectangle(graphics.pose(), panelX + 6, offsetY, CATEGORY_WIDTH - 12,
                        itemHeight, 6, withAlpha(SURFACE1, 170));
            }

            String iconChar = getCategoryIcon(category);
            int iconColor = selected ? BLUE_ACCENT.getRGB() : SUBTEXT0.getRGB();

            BuiltText icon = Builder.text()
                    .font(ICONS.get())
                    .text(iconChar)
                    .color(iconColor)
                    .size(15f)
                    .thickness(0.05f)
                    .build();

            icon.render(new Matrix4f(), panelX + 16, offsetY + 11);

            BuiltText name = Builder.text()
                    .font(BIKO_FONT.get())
                    .text(category.name())
                    .color(selected ? TEXT.getRGB() : SUBTEXT0.getRGB())
                    .size(9f)
                    .thickness(selected ? 0.07f : 0.05f)
                    .build();

            name.render(new Matrix4f(), panelX + 40, offsetY + 14);

            offsetY += itemHeight + 4;
        }

        graphics.disableScissor();
    }

    private void renderModulePanel(GuiGraphics graphics, int mouseX, int mouseY) {
        int panelX = windowX + CATEGORY_WIDTH;
        int panelY = windowY + HEADER_HEIGHT;
        int panelHeight = windowHeight - HEADER_HEIGHT;

        // Фон панели модулей с контрастом
        DrawHelper.rectangle(graphics.pose(), panelX, panelY, MODULE_WIDTH, panelHeight, 0,
                withAlpha(BASE, 220));

        List<Module> modules = Module.getModulesByCategory(selectedCategory).stream()
                .filter(m -> searchQuery.isEmpty() ||
                        m.getName().toLowerCase().contains(searchQuery.toLowerCase()))
                .collect(Collectors.toList());

        graphics.enableScissor(panelX, panelY, panelX + MODULE_WIDTH, panelY + panelHeight);

        int offsetY = (int) (panelY + 10 - moduleScroll);

        for (Module module : modules) {
            boolean selected = module == selectedModule;
            boolean hovered = mouseX >= panelX && mouseX <= panelX + MODULE_WIDTH &&
                    mouseY >= offsetY && mouseY <= offsetY + getModuleItemHeight(module);

            int itemHeight = getModuleItemHeight(module);

            // Фон модуля - ВСЕГДА видимый
            if (selected) {
                DrawShader.drawRoundBlur(graphics.pose(), panelX + 6, offsetY, MODULE_WIDTH - 12,
                        itemHeight, 6, withAlpha(SURFACE2, 230), 90, 0.45f);

                // Синяя полоска для выбранного
                DrawHelper.rectangle(graphics.pose(), panelX + 6, offsetY, 3, itemHeight, 6,
                        BLUE_ACCENT.getRGB());
            } else if (hovered) {
                DrawShader.drawRoundBlur(graphics.pose(), panelX + 6, offsetY, MODULE_WIDTH - 12,
                        itemHeight, 6, withAlpha(SURFACE1, 200), 90, 0.35f);
            } else {
                // ВАЖНО: видимый фон для неактивных модулей
                DrawShader.drawRoundBlur(graphics.pose(), panelX + 6, offsetY, MODULE_WIDTH - 12,
                        itemHeight, 6, withAlpha(SURFACE0, 180), 90, 0.3f);
            }

            // Название модуля
            int moduleNameColor = module.isEnabled() ?
                    ColorHelper.gradient(ThemesUtil.getCurrentStyle().getColor(1),
                            ThemesUtil.getCurrentStyle().getColor(2), 20, 10) :
                    TEXT.getRGB();

            BuiltText moduleName = Builder.text()
                    .font(BIKO_FONT.get())
                    .text(module.getName())
                    .color(moduleNameColor)
                    .size(8.5f)
                    .thickness(module.isEnabled() ? 0.07f : 0.05f)
                    .build();

            moduleName.render(new Matrix4f(), panelX + 16, offsetY + 8);

            // Иконка переключателя
            int toggleColor = module.isEnabled() ?
                    ColorHelper.gradient(ThemesUtil.getCurrentStyle().getColor(1),
                            ThemesUtil.getCurrentStyle().getColor(2), 20, 10) :
                    SUBTEXT1.getRGB();

            BuiltText toggleIcon = Builder.text()
                    .font(ICONS.get())
                    .text(module.isEnabled() ? "R" : "S")
                    .color(toggleColor)
                    .size(12f)
                    .thickness(0.05f)
                    .build();

            toggleIcon.render(new Matrix4f(), panelX + MODULE_WIDTH - 28, offsetY + 7);

            // Описание модуля под названием
            if (!module.getDescription().isEmpty()) {
                String description = module.getDescription();
                List<String> lines = wrapText(description, MODULE_WIDTH - 24, 6.5f);

                int descY = offsetY + 24;
                for (int i = 0; i < Math.min(lines.size(), 2); i++) {
                    BuiltText descText = Builder.text()
                            .font(BIKO_FONT.get())
                            .text(lines.get(i))
                            .color(SUBTEXT1.getRGB())
                            .size(6.5f)
                            .thickness(0.04f)
                            .build();

                    descText.render(new Matrix4f(), panelX + 16, descY);
                    descY += 9;
                }
            }

            offsetY += itemHeight + 4;
        }

        graphics.disableScissor();
    }

    private int getModuleItemHeight(Module module) {
        if (module.getDescription().isEmpty()) {
            return 28;
        }

        String description = module.getDescription();
        List<String> lines = wrapText(description, MODULE_WIDTH - 24, 6.5f);
        int lineCount = Math.min(lines.size(), 2);

        return 28 + (lineCount * 9);
    }

    private void renderSettingsPanel(GuiGraphics graphics, int mouseX, int mouseY) {
        int panelX = windowX + CATEGORY_WIDTH + MODULE_WIDTH;
        int panelY = windowY + HEADER_HEIGHT;
        int panelHeight = windowHeight - HEADER_HEIGHT;

        // Фон панели настроек
        DrawHelper.rectangle(graphics.pose(), panelX, panelY, SETTINGS_WIDTH, panelHeight, 0,
                withAlpha(MANTLE, 220));

        if (selectedModule == null) {
            BuiltText noModule = Builder.text()
                    .font(BIKO_FONT.get())
                    .text("Select a module")
                    .color(SUBTEXT1.getRGB())
                    .size(9f)
                    .thickness(0.05f)
                    .build();

            noModule.render(new Matrix4f(),
                    panelX + (SETTINGS_WIDTH - BIKO_FONT.get().getWidth("Select a module", 9f)) / 2,
                    panelY + panelHeight / 2);
            return;
        }

        graphics.enableScissor(panelX, panelY, panelX + SETTINGS_WIDTH, panelY + panelHeight);

        int offsetY = (int) (panelY + 10 - settingsScroll);

        for (SettingComponent component : settingComponents) {
            if (!component.setting.isVisible()) continue;

            component.setPosition(panelX + 8, offsetY);
            component.render(graphics, mouseX, mouseY);

            offsetY += component.getHeight() + 6;
        }

        graphics.disableScissor();
    }

    private void renderPanelDividers(GuiGraphics graphics) {
        int dividerY = windowY + HEADER_HEIGHT;
        int dividerHeight = windowHeight - HEADER_HEIGHT;

        // Синие разделители между панелями
        DrawHelper.rectangle(graphics.pose(), windowX + CATEGORY_WIDTH - 1, dividerY, 1, dividerHeight, 0,
                withAlpha(BLUE_DIM, 120));

        DrawHelper.rectangle(graphics.pose(), windowX + CATEGORY_WIDTH + MODULE_WIDTH - 1, dividerY, 1, dividerHeight, 0,
                withAlpha(BLUE_DIM, 120));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int panelY = windowY + HEADER_HEIGHT;
        int panelHeight = windowHeight - HEADER_HEIGHT;

        if (mouseX >= windowX && mouseX <= windowX + CATEGORY_WIDTH &&
                mouseY >= panelY && mouseY <= panelY + panelHeight) {
            targetCategoryScroll -= scrollY * 15;
            targetCategoryScroll = Math.max(0, targetCategoryScroll);
            return true;
        }

        if (mouseX >= windowX + CATEGORY_WIDTH && mouseX <= windowX + CATEGORY_WIDTH + MODULE_WIDTH &&
                mouseY >= panelY && mouseY <= panelY + panelHeight) {
            targetModuleScroll -= scrollY * 15;
            targetModuleScroll = Math.max(0, targetModuleScroll);
            return true;
        }

        if (mouseX >= windowX + CATEGORY_WIDTH + MODULE_WIDTH &&
                mouseX <= windowX + CATEGORY_WIDTH + MODULE_WIDTH + SETTINGS_WIDTH &&
                mouseY >= panelY && mouseY <= panelY + panelHeight) {
            targetSettingsScroll -= scrollY * 15;
            targetSettingsScroll = Math.max(0, targetSettingsScroll);
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (mouseY >= windowY && mouseY <= windowY + HEADER_HEIGHT &&
                    mouseX >= windowX && mouseX <= windowX + windowWidth) {
                int searchWidth = 150;
                int searchX = windowX + windowWidth - searchWidth - 15;
                int searchY = windowY + (HEADER_HEIGHT - 22) / 2;

                if (!(mouseX >= searchX && mouseX <= searchX + searchWidth &&
                        mouseY >= searchY && mouseY <= searchY + 22)) {
                    dragging = true;
                    dragOffsetX = (int) (mouseX - windowX);
                    dragOffsetY = (int) (mouseY - windowY);
                    return true;
                }
            }

            int searchWidth = 150;
            int searchHeight = 22;
            int searchX = windowX + windowWidth - searchWidth - 15;
            int searchY = windowY + (HEADER_HEIGHT - searchHeight) / 2;

            searchFocused = mouseX >= searchX && mouseX <= searchX + searchWidth &&
                    mouseY >= searchY && mouseY <= searchY + searchHeight;

            int categoryPanelX = windowX;
            int panelY = windowY + HEADER_HEIGHT;
            int panelHeight = windowHeight - HEADER_HEIGHT;
            int itemHeight = 36;

            if (mouseX >= categoryPanelX && mouseX <= categoryPanelX + CATEGORY_WIDTH &&
                    mouseY >= panelY && mouseY <= panelY + panelHeight) {
                int offsetY = (int) (panelY + 10 - categoryScroll);
                for (Module.Category category : Module.Category.values()) {
                    if (mouseY >= offsetY && mouseY <= offsetY + itemHeight) {
                        selectedCategory = category;
                        selectedModule = null;
                        updateSettingComponents();
                        return true;
                    }
                    offsetY += itemHeight + 4;
                }
            }

            int modulePanelX = windowX + CATEGORY_WIDTH;

            if (mouseX >= modulePanelX && mouseX <= modulePanelX + MODULE_WIDTH &&
                    mouseY >= panelY && mouseY <= panelY + panelHeight) {
                List<Module> modules = Module.getModulesByCategory(selectedCategory).stream()
                        .filter(m -> searchQuery.isEmpty() ||
                                m.getName().toLowerCase().contains(searchQuery.toLowerCase()))
                        .collect(Collectors.toList());

                int offsetY = (int) (panelY + 10 - moduleScroll);
                for (Module module : modules) {
                    int moduleItemHeight = getModuleItemHeight(module);
                    if (mouseY >= offsetY && mouseY <= offsetY + moduleItemHeight) {
                        selectedModule = module;
                        updateSettingComponents();
                        return true;
                    }
                    offsetY += moduleItemHeight + 4;
                }
            }

            for (SettingComponent component : settingComponents) {
                if (component.mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
            }
        } else if (button == 1) {
            int modulePanelX = windowX + CATEGORY_WIDTH;
            int panelY = windowY + HEADER_HEIGHT;
            int panelHeight = windowHeight - HEADER_HEIGHT;

            if (mouseX >= modulePanelX && mouseX <= modulePanelX + MODULE_WIDTH &&
                    mouseY >= panelY && mouseY <= panelY + panelHeight) {
                List<Module> modules = Module.getModulesByCategory(selectedCategory).stream()
                        .filter(m -> searchQuery.isEmpty() ||
                                m.getName().toLowerCase().contains(searchQuery.toLowerCase()))
                        .collect(Collectors.toList());

                int offsetY = (int) (panelY + 10 - moduleScroll);
                for (Module module : modules) {
                    int moduleItemHeight = getModuleItemHeight(module);
                    if (mouseY >= offsetY && mouseY <= offsetY + moduleItemHeight) {
                        module.toggle();
                        return true;
                    }
                    offsetY += moduleItemHeight + 4;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging && button == 0) {
            windowX = (int) (mouseX - dragOffsetX);
            windowY = (int) (mouseY - dragOffsetY);
            return true;
        }

        for (SettingComponent component : settingComponents) {
            component.mouseDragged(mouseX, mouseY, button);
        }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;

        for (SettingComponent component : settingComponents) {
            component.mouseReleased(mouseX, mouseY, button);
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (searchFocused) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (!searchQuery.isEmpty()) {
                    searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
                }
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                searchFocused = false;
                searchQuery = "";
                return true;
            } else {
                char c = getCharFromKey(keyCode);
                if (c != 0) {
                    searchQuery += c;
                    return true;
                }
            }
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.onClose();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void updateSettingComponents() {
        settingComponents.clear();
        targetSettingsScroll = 0;
        settingsScroll = 0;

        if (selectedModule == null) return;

        for (Setting setting : selectedModule.getSettings()) {
            if (setting instanceof CheckBox) {
                settingComponents.add(new CheckBoxSettingComponent((CheckBox) setting));
            } else if (setting instanceof Mode) {
                settingComponents.add(new ModeSettingComponent((Mode) setting));
            } else if (setting instanceof Slider) {
                settingComponents.add(new SliderSettingComponent((Slider) setting));
            } else if (setting instanceof MultiCheckBox) {
                settingComponents.add(new MultiCheckBoxSettingComponent((MultiCheckBox) setting));
            }
        }
    }

    private String getCategoryIcon(Module.Category category) {
        return switch (category) {
            case COMBAT -> "a";
            case MOVEMENT -> "K";
            case RENDER -> "c";
            case PLAYER -> "B";
            case MISC -> "e";
        };
    }

    private float lerp(float start, float end, float t) {
        return start + t * (end - start);
    }

    private char getCharFromKey(int keyCode) {
        if (keyCode >= GLFW.GLFW_KEY_A && keyCode <= GLFW.GLFW_KEY_Z) {
            return (char) ('a' + (keyCode - GLFW.GLFW_KEY_A));
        } else if (keyCode == GLFW.GLFW_KEY_SPACE) {
            return ' ';
        }
        return 0;
    }

    private int withAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha).getRGB();
    }

    private List<String> wrapText(String text, float maxWidth, float fontSize) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;
            float width = BIKO_FONT.get().getWidth(testLine, fontSize);

            if (width > maxWidth && currentLine.length() > 0) {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            } else {
                currentLine = new StringBuilder(testLine);
            }
        }

        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        return lines;
    }

    abstract static class SettingComponent {
        protected Setting setting;
        protected int x, y;

        public SettingComponent(Setting setting) {
            this.setting = setting;
        }

        public void setPosition(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public abstract void render(GuiGraphics graphics, int mouseX, int mouseY);
        public abstract int getHeight();
        public boolean mouseClicked(double mouseX, double mouseY, int button) { return false; }
        public void mouseDragged(double mouseX, double mouseY, int button) {}
        public void mouseReleased(double mouseX, double mouseY, int button) {}
    }

    static class CheckBoxSettingComponent extends SettingComponent {
        private final CheckBox checkBox;
        private static final Color SURFACE0 = new Color(25, 35, 55);
        private static final Color SURFACE1 = new Color(35, 45, 70);
        private static final Color TEXT = new Color(225, 235, 255);
        private static final Color SUBTEXT0 = new Color(180, 195, 220);
        private static final Color BLUE_ACCENT = new Color(88, 166, 255);

        public CheckBoxSettingComponent(CheckBox checkBox) {
            super(checkBox);
            this.checkBox = checkBox;
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY) {
            boolean hovered = mouseX >= x && mouseX <= x + 204 &&
                    mouseY >= y && mouseY <= y + 28;

            DrawShader.drawRoundBlur(graphics.pose(), x, y, 204, 28, 5,
                    new Color(SURFACE0.getRed(), SURFACE0.getGreen(), SURFACE0.getBlue(),
                            hovered ? 220 : 180).getRGB(), 90, 0.35f);

            BuiltText name = Builder.text()
                    .font(BIKO_FONT.get())
                    .text(checkBox.getName())
                    .color(TEXT.getRGB())
                    .size(8.5f)
                    .thickness(0.05f)
                    .build();

            name.render(new Matrix4f(), x + 8, y + 10);

            int iconColor = checkBox.isEnabled() ?
                    ColorHelper.gradient(ThemesUtil.getCurrentStyle().getColor(1),
                            ThemesUtil.getCurrentStyle().getColor(2), 20, 10) :
                    SUBTEXT0.getRGB();

            BuiltText icon = Builder.text()
                    .font(ICONS.get())
                    .text(checkBox.isEnabled() ? "R" : "S")
                    .color(iconColor)
                    .size(12f)
                    .thickness(0.05f)
                    .build();

            icon.render(new Matrix4f(), x + 182, y + 8);
        }

        @Override
        public int getHeight() {
            return 28;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0 && mouseX >= x && mouseX <= x + 204 &&
                    mouseY >= y && mouseY <= y + 28) {
                checkBox.toggle();
                return true;
            }
            return false;
        }
    }

    static class ModeSettingComponent extends SettingComponent {
        private final Mode mode;
        private boolean expanded = false;
        private static final Color SURFACE0 = new Color(25, 35, 55);
        private static final Color SURFACE1 = new Color(35, 45, 70);
        private static final Color SURFACE2 = new Color(45, 60, 90);
        private static final Color TEXT = new Color(225, 235, 255);
        private static final Color SUBTEXT0 = new Color(180, 195, 220);
        private static final Color BLUE_ACCENT = new Color(88, 166, 255);

        public ModeSettingComponent(Mode mode) {
            super(mode);
            this.mode = mode;
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY) {
            BuiltText name = Builder.text()
                    .font(BIKO_FONT.get())
                    .text(mode.getName())
                    .color(SUBTEXT0.getRGB())
                    .size(7.5f)
                    .thickness(0.05f)
                    .build();

            name.render(new Matrix4f(), x + 2, y + 2);

            int modeY = y + 16;
            boolean modeHovered = mouseX >= x && mouseX <= x + 204 &&
                    mouseY >= modeY && mouseY <= modeY + 24;

            DrawShader.drawRoundBlur(graphics.pose(), x, modeY, 204, 24, 5,
                    new Color(SURFACE1.getRed(), SURFACE1.getGreen(), SURFACE1.getBlue(),
                            modeHovered ? 230 : 190).getRGB(), 90, 0.4f);

            BuiltText currentMode = Builder.text()
                    .font(BIKO_FONT.get())
                    .text(mode.getMode())
                    .color(TEXT.getRGB())
                    .size(8f)
                    .thickness(0.06f)
                    .build();

            currentMode.render(new Matrix4f(), x + 8, modeY + 8);

            BuiltText arrow = Builder.text()
                    .font(BIKO_FONT.get())
                    .text(expanded ? "▲" : "▼")
                    .color(SUBTEXT0.getRGB())
                    .size(7f)
                    .thickness(0.05f)
                    .build();

            arrow.render(new Matrix4f(), x + 188, modeY + 9);

            if (expanded) {
                int optionY = modeY + 28;
                for (String modeOption : mode.getModes()) {
                    boolean selected = modeOption.equals(mode.getMode());
                    boolean hovered = mouseX >= x && mouseX <= x + 204 &&
                            mouseY >= optionY && mouseY <= optionY + 22;

                    DrawHelper.rectangle(graphics.pose(), x, optionY, 204, 22, 4,
                            selected ? new Color(SURFACE2.getRed(), SURFACE2.getGreen(), SURFACE2.getBlue(), 220).getRGB() :
                                    hovered ? new Color(SURFACE1.getRed(), SURFACE1.getGreen(), SURFACE1.getBlue(), 200).getRGB() :
                                            new Color(SURFACE0.getRed(), SURFACE0.getGreen(), SURFACE0.getBlue(), 170).getRGB());

                    BuiltText option = Builder.text()
                            .font(BIKO_FONT.get())
                            .text(modeOption)
                            .color(selected ? BLUE_ACCENT.getRGB() : TEXT.getRGB())
                            .size(7.5f)
                            .thickness(selected ? 0.06f : 0.05f)
                            .build();

                    option.render(new Matrix4f(), x + 8, optionY + 7);

                    optionY += 22;
                }
            }
        }

        @Override
        public int getHeight() {
            return expanded ? 40 + (mode.getModes().size() * 22) : 40;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
                int modeY = y + 16;
                if (mouseX >= x && mouseX <= x + 204 &&
                        mouseY >= modeY && mouseY <= modeY + 24) {
                    expanded = !expanded;
                    return true;
                }

                if (expanded) {
                    int optionY = modeY + 28;
                    for (String modeOption : mode.getModes()) {
                        if (mouseX >= x && mouseX <= x + 204 &&
                                mouseY >= optionY && mouseY <= optionY + 22) {
                            mode.setMode(modeOption);
                            expanded = false;
                            return true;
                        }
                        optionY += 22;
                    }
                }
            }
            return false;
        }
    }

    static class SliderSettingComponent extends SettingComponent {
        private final Slider slider;
        private boolean dragging = false;
        private static final Color SURFACE1 = new Color(35, 45, 70);
        private static final Color TEXT = new Color(225, 235, 255);
        private static final Color SUBTEXT0 = new Color(180, 195, 220);
        private static final Color BLUE_ACCENT = new Color(88, 166, 255);

        public SliderSettingComponent(Slider slider) {
            super(slider);
            this.slider = slider;
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY) {
            BuiltText name = Builder.text()
                    .font(BIKO_FONT.get())
                    .text(slider.getName())
                    .color(SUBTEXT0.getRGB())
                    .size(8f)
                    .thickness(0.05f)
                    .build();

            name.render(new Matrix4f(), x + 2, y + 2);

            String valueText = String.format("%.2f", slider.getValue());
            BuiltText value = Builder.text()
                    .font(BIKO_FONT.get())
                    .text(valueText)
                    .color(TEXT.getRGB())
                    .size(8f)
                    .thickness(0.06f)
                    .build();

            value.render(new Matrix4f(), x + 180, y + 2);

            int sliderY = y + 18;
            int sliderWidth = 204;

            DrawHelper.rectangle(graphics.pose(), x, sliderY, sliderWidth, 4, 2,
                    new Color(SURFACE1.getRed(), SURFACE1.getGreen(), SURFACE1.getBlue(), 220).getRGB());

            float progress = (float) ((slider.getValue() - slider.getMin()) /
                    (slider.getMax() - slider.getMin()));
            int fillWidth = (int) (sliderWidth * progress);

            int gradientColor = ColorHelper.gradient(
                    ThemesUtil.getCurrentStyle().getColor(1),
                    ThemesUtil.getCurrentStyle().getColor(2), 20, 10);

            DrawHelper.rectangle(graphics.pose(), x, sliderY, fillWidth, 4, 2, BLUE_ACCENT.getRGB());

            // Ручка слайдера
            int knobX = x + fillWidth - 4;
            DrawShader.drawRoundBlur(graphics.pose(), knobX, sliderY - 3, 8, 10, 4,
                    BLUE_ACCENT.getRGB(), 90, 0.5f);
        }

        @Override
        public int getHeight() {
            return 26;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
                int sliderY = y + 18;
                if (mouseX >= x && mouseX <= x + 204 &&
                        mouseY >= sliderY - 5 && mouseY <= sliderY + 9) {
                    dragging = true;
                    updateSliderValue(mouseX);
                    return true;
                }
            }
            return false;
        }

        @Override
        public void mouseDragged(double mouseX, double mouseY, int button) {
            if (dragging) {
                updateSliderValue(mouseX);
            }
        }

        @Override
        public void mouseReleased(double mouseX, double mouseY, int button) {
            dragging = false;
        }

        private void updateSliderValue(double mouseX) {
            float progress = (float) Math.max(0, Math.min(1, (mouseX - x) / 204.0));
            double newValue = slider.getMin() + progress * (slider.getMax() - slider.getMin());
            slider.setValue(newValue);
        }
    }

    static class MultiCheckBoxSettingComponent extends SettingComponent {
        private final MultiCheckBox multiCheckBox;
        private boolean expanded = false;
        private static final Color SURFACE0 = new Color(25, 35, 55);
        private static final Color SURFACE1 = new Color(35, 45, 70);
        private static final Color TEXT = new Color(225, 235, 255);
        private static final Color SUBTEXT0 = new Color(180, 195, 220);
        private static final Color SUBTEXT1 = new Color(140, 160, 190);
        private static final Color BLUE_ACCENT = new Color(88, 166, 255);

        public MultiCheckBoxSettingComponent(MultiCheckBox multiCheckBox) {
            super(multiCheckBox);
            this.multiCheckBox = multiCheckBox;
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY) {
            boolean hovered = mouseX >= x && mouseX <= x + 204 &&
                    mouseY >= y && mouseY <= y + 28;

            DrawShader.drawRoundBlur(graphics.pose(), x, y, 204, 28, 5,
                    new Color(SURFACE0.getRed(), SURFACE0.getGreen(), SURFACE0.getBlue(),
                            hovered ? 220 : 180).getRGB(), 90, 0.35f);

            BuiltText name = Builder.text()
                    .font(BIKO_FONT.get())
                    .text(multiCheckBox.getName())
                    .color(TEXT.getRGB())
                    .size(8.5f)
                    .thickness(0.05f)
                    .build();

            name.render(new Matrix4f(), x + 8, y + 10);

            long enabled = multiCheckBox.getOptions().stream().filter(CheckBox::isEnabled).count();
            String count = enabled + "/" + multiCheckBox.getOptions().size();

            BuiltText countText = Builder.text()
                    .font(BIKO_FONT.get())
                    .text(count)
                    .color(SUBTEXT0.getRGB())
                    .size(7.5f)
                    .thickness(0.05f)
                    .build();

            countText.render(new Matrix4f(), x + 168, y + 11);

            BuiltText arrow = Builder.text()
                    .font(BIKO_FONT.get())
                    .text(expanded ? "▲" : "▼")
                    .color(SUBTEXT1.getRGB())
                    .size(7f)
                    .thickness(0.05f)
                    .build();

            arrow.render(new Matrix4f(), x + 190, y + 11);

            if (expanded) {
                int optionY = y + 32;
                for (CheckBox option : multiCheckBox.getOptions()) {
                    boolean optHovered = mouseX >= x + 4 && mouseX <= x + 200 &&
                            mouseY >= optionY && mouseY <= optionY + 22;

                    DrawHelper.rectangle(graphics.pose(), x + 4, optionY, 200, 22, 4,
                            new Color(SURFACE1.getRed(), SURFACE1.getGreen(), SURFACE1.getBlue(),
                                    optHovered ? 200 : 160).getRGB());

                    BuiltText optName = Builder.text()
                            .font(BIKO_FONT.get())
                            .text(option.getName())
                            .color(TEXT.getRGB())
                            .size(8f)
                            .thickness(0.05f)
                            .build();

                    optName.render(new Matrix4f(), x + 10, optionY + 7);

                    int iconColor = option.isEnabled() ?
                            ColorHelper.gradient(ThemesUtil.getCurrentStyle().getColor(1),
                                    ThemesUtil.getCurrentStyle().getColor(2), 20, 10) :
                            SUBTEXT1.getRGB();

                    BuiltText icon = Builder.text()
                            .font(ICONS.get())
                            .text(option.isEnabled() ? "R" : "S")
                            .color(iconColor)
                            .size(11f)
                            .thickness(0.05f)
                            .build();

                    icon.render(new Matrix4f(), x + 182, optionY + 6);

                    optionY += 24;
                }
            }
        }

        @Override
        public int getHeight() {
            return expanded ? 32 + (multiCheckBox.getOptions().size() * 24) : 28;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
                if (mouseX >= x && mouseX <= x + 204 &&
                        mouseY >= y && mouseY <= y + 28) {
                    expanded = !expanded;
                    return true;
                }

                if (expanded) {
                    int optionY = y + 32;
                    for (CheckBox option : multiCheckBox.getOptions()) {
                        if (mouseX >= x + 4 && mouseX <= x + 200 &&
                                mouseY >= optionY && mouseY <= optionY + 22) {
                            option.toggle();
                            return true;
                        }
                        optionY += 24;
                    }
                }
            }
            return false;
        }
    }
}