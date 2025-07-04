package com.dsp.main.UI.MainMenu;

import com.dsp.main.Utils.AltConfig;
import com.dsp.main.Utils.Minecraft.UserSession.UserSessionUtil;
import com.dsp.main.Utils.Render.DrawHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.systems.RenderSystem;
import net.neoforged.neoforge.client.gui.ModListScreen;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainMenuScreen extends Screen {
    private static final float MAX_HEIGHT = 200.0F;
    private static final int MAX_VISIBLE_ACCOUNTS = 10;
    private static final float ITEM_HEIGHT = 17.0F;
    private static final float WIDTH = 145.0F;
    private static final float SCROLLBAR_WIDTH = 3.0F;
    private static final ResourceLocation ICON_TEXTURE = ResourceLocation.fromNamespaceAndPath("dsp", "textures/1.png");
    private static final ResourceLocation CROSS_TEXTURE = ResourceLocation.fromNamespaceAndPath("dsp", "textures/cross.png");
    private static final ResourceLocation ARROW_DOWN = ResourceLocation.fromNamespaceAndPath("dsp", "textures/arrow_down.png");
    private static final ResourceLocation ARROW_UP = ResourceLocation.fromNamespaceAndPath("dsp", "textures/arrow_up.png");
    private static final ResourceLocation ENTER = ResourceLocation.fromNamespaceAndPath("dsp", "textures/enter.png");
    private static final ResourceLocation RANDOM = ResourceLocation.fromNamespaceAndPath("dsp", "textures/random.png");
    private static final ResourceLocation BACKGROUND_TEXTURE = ResourceLocation.fromNamespaceAndPath("dsp", "textures/backmenu.png");
    private static final ResourceLocation FAVORITED = ResourceLocation.fromNamespaceAndPath("dsp", "textures/favorited.png");
    private static final ResourceLocation UNFAVORITED = ResourceLocation.fromNamespaceAndPath("dsp", "textures/unfavorited.png");
    private static final ResourceLocation BUTTON_TEXTURE = ResourceLocation.fromNamespaceAndPath("dsp", "textures/button.png");

    private final List<Account> alts = new ArrayList<>();
    private final Map<String, Float> deletingAccounts = new HashMap<>();
    private final Map<String, Float> visualYPositions = new HashMap<>();
    private final Map<String, Float> accountOpacities = new HashMap<>();
    private final List<Button> buttons = new ArrayList<>();
    private final Map<Button, Float> buttonOpacities = new HashMap<>();
    private float x = 0.0F;
    private final float y = 10.0F;
    private boolean open = false;
    private float currentHeight = 20.0F;
    private float targetHeight = 20.0F;
    private float scroll = 0.0F;
    private float scrollTarget = 0.0F;
    private EditBox inputField;
    private boolean typing = false;
    private String altName = "";
    private int hoveredIndex = -1;
    private double lastMouseX = 0.0;
    private double lastMouseY = 0.0;
    private float inputOpacity = 1.0F;
    private float currentOpenProgress = 0.0F;

    public static class Account {
        private String username;
        private boolean isFavorite;

        public Account(String username, boolean isFavorite) {
            this.username = username;
            this.isFavorite = isFavorite;
        }

        public String getUsername() {
            return username;
        }

        public boolean isFavorite() {
            return isFavorite;
        }

        public void setFavorite(boolean favorite) {
            this.isFavorite = favorite;
        }
    }

    private class Button {
        private final float x, y, width, height;
        private final String text;
        private final Runnable action;
        private float hoverProgress;

        public Button(float x, float y, float width, float height, String text, Runnable action) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.text = text;
            this.action = action;
            this.hoverProgress = 0.0F;
        }

        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            boolean isHovered = isMouseOver(mouseX, mouseY, x, y, width, height);
            hoverProgress = lerp(hoverProgress, isHovered ? 1.0F : 0.0F, partialTick * 0.4F);
            float opacity = buttonOpacities.getOrDefault(this, 0.0F);
            opacity = lerp(opacity, 1.0F, partialTick * 0.3F);
            buttonOpacities.put(this, opacity);
            float scale = 1.0F + 0.05F * hoverProgress;
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(x + width / 2, y + height / 2, 0);
            guiGraphics.pose().scale(scale, scale, 1.0F);
            guiGraphics.pose().translate(-(x + width / 2), -(y + height / 2), 0);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, opacity);
            DrawHelper.drawTexture(BUTTON_TEXTURE, guiGraphics.pose().last().pose(), x, y, width, height);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            int textColor = new Color(
                    (int)lerp(161, 255, hoverProgress),
                    (int)lerp(164, 255, hoverProgress),
                    (int)lerp(177, 255, hoverProgress),
                    (int)(255 * opacity)
            ).getRGB();
            guiGraphics.drawCenteredString(Minecraft.getInstance().font, text, (int)(x + width / 2), (int)(y + height / 2 - 4), textColor);

            guiGraphics.pose().popPose();
        }

        public void click(int mouseX, int mouseY, int button) {
            if (button == 0 && isMouseOver(mouseX, mouseY, x, y, width, height)) {
                action.run();
            }
        }
    }

    public MainMenuScreen() {
        super(Component.literal("Main Menu"));
    }

    @Override
    protected void init() {
        Font font = Minecraft.getInstance().font;
        this.inputField = new EditBox(font, 0, 0, 110, 15, Component.literal("Nickname"));
        this.inputField.setMaxLength(16);
        this.inputField.setValue("");
        AltConfig.loadAlts(alts);

        buttons.clear();
        float buttonWidth = 160.0F;
        float buttonHeight = 25.0F;
        float buttonSpacing = 5.0F;
        float centerX = (float)Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2 - buttonWidth / 2;
        float centerY = (float)Minecraft.getInstance().getWindow().getGuiScaledHeight() / 2 - (5 * buttonHeight + 4 * buttonSpacing) / 2;

        buttons.add(new Button(centerX, centerY, buttonWidth, buttonHeight, "Singleplayer", () -> {
            Minecraft.getInstance().setScreen(new SelectWorldScreen(this));
        }));
        buttons.add(new Button(centerX, centerY + buttonHeight + buttonSpacing, buttonWidth, buttonHeight, "Multiplayer", () -> {
            Minecraft.getInstance().setScreen(new JoinMultiplayerScreen(this));
        }));
        buttons.add(new Button(centerX, centerY + 2 * (buttonHeight + buttonSpacing), buttonWidth, buttonHeight, "Options", () -> {
            Minecraft.getInstance().setScreen(new OptionsScreen(this, Minecraft.getInstance().options));
        }));
        buttons.add(new Button(centerX, centerY + 3 * (buttonHeight + buttonSpacing), buttonWidth, buttonHeight, "Mods", () -> {
            Minecraft.getInstance().setScreen(new ModListScreen(this));
        }));
        buttons.add(new Button(centerX, centerY + 4 * (buttonHeight + buttonSpacing), buttonWidth, buttonHeight, "Exit", () -> {
            Minecraft.getInstance().stop();
        }));
        for (Button button : buttons) {
            buttonOpacities.put(button, 0.0F);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int height = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        guiGraphics.blit(BACKGROUND_TEXTURE, 0, 0, 0, 0, width, height, width, height);
        guiGraphics.fill(0, 0, width, height, 0xC0101010);
        drawButtons(guiGraphics, mouseX, mouseY, partialTick);
        drawAltWidget(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    }

    private void drawButtons(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        buttons.forEach(button -> button.render(guiGraphics, mouseX, mouseY, partialTick));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (handleMouseClick(mouseX, mouseY, button)) {
            return true;
        }
        for (Button b : buttons) {
            b.click((int)mouseX, (int)mouseY, button);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        handleKeyPressed(keyCode, scanCode, modifiers);
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        handleCharTyped(codePoint, modifiers);
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        handleMouseScrolled(mouseX, mouseY, horizontal, vertical);
        return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
    }

    public void handleKeyPressed(int keyCode, int scanCode, int modifiers) {
        if (typing) {
            if (keyCode == 259 && !altName.isEmpty()) {
                altName = altName.substring(0, altName.length() - 1);
            } else if (keyCode == 257 && altName.length() >= 3 && !alts.stream().anyMatch(a -> a.getUsername().equals(altName))) {
                Account newAccount = new Account(altName, false);
                alts.add(newAccount);
                accountOpacities.put(newAccount.getUsername(), 0.0F);
                UserSessionUtil.setNameSession(altName);
                AltConfig.saveAlts(alts);
                altName = "";
                inputField.setValue("");
                typing = false;
            }
            inputField.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    public void handleCharTyped(char codePoint, int modifiers) {
        if (typing) {
            if (Minecraft.getInstance().font.width(altName + codePoint) < WIDTH - 50) {
                altName += codePoint;
                inputField.charTyped(codePoint, modifiers);
            }
        }
    }

    public void handleMouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        if (open && isMouseOver(mouseX, mouseY, x, y, WIDTH, currentHeight)) {
            scrollTarget += vertical * 10;
            float maxScroll = Math.max(0, alts.size() * ITEM_HEIGHT - (MAX_HEIGHT - 20 - 2 * ITEM_HEIGHT));
            scrollTarget = Math.max(0, Math.min(maxScroll, scrollTarget));
        }
    }

    private void drawAltWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        x = Minecraft.getInstance().getWindow().getGuiScaledWidth() - WIDTH - 10;
        targetHeight = open ? Math.min(20 + 10 + alts.size() * ITEM_HEIGHT + 2 * ITEM_HEIGHT, MAX_HEIGHT) : 20.0F;
        currentHeight = lerp(currentHeight, targetHeight, partialTick * 0.3F);
        scroll = lerp(scroll, scrollTarget, partialTick * 0.3F);
        inputOpacity = lerp(inputOpacity, typing ? 1.0F : 0.7F, partialTick * 0.2F);

        float targetOpenProgress = open ? 1.0F : 0.0F;
        currentOpenProgress = lerp(currentOpenProgress, targetOpenProgress, partialTick * 0.3F);

        RenderSystem.enableBlend();
        DrawHelper.rectangle(guiGraphics.pose(), x, y, WIDTH, currentHeight, 6.0F, new Color(0, 0, 10, 160).getRGB());
        DrawHelper.rectangle(guiGraphics.pose(), x - 2, y - 2, WIDTH + 4, currentHeight + 4, 6.0F, new Color(0, 0, 10, 80).getRGB());

        String username = Minecraft.getInstance().getUser().getName();
        guiGraphics.drawString(Minecraft.getInstance().font, username, (int)(x + 6), (int)(y + 6), Color.WHITE.getRGB());
        DrawHelper.drawTexture(ICON_TEXTURE, guiGraphics.pose().last().pose(), x - 20, y, 20, 20);
        DrawHelper.drawTexture(open ? ARROW_UP : ARROW_DOWN, guiGraphics.pose().last().pose(), x + WIDTH - 16, y + 2, 12, 12);

        if (open) {
            DrawHelper.rectangle(guiGraphics.pose(), x, y + 20, WIDTH, 0.5F, 0.0F, new Color(64, 64, 64, 255).getRGB());
            float contentHeight = alts.size() * ITEM_HEIGHT;
            boolean needsScroll = contentHeight > (MAX_HEIGHT - 20 - 2 * ITEM_HEIGHT);
            float scrollableHeight = MAX_HEIGHT - 20 - 2 * ITEM_HEIGHT;

            RenderSystem.enableScissor((int)(x * Minecraft.getInstance().getWindow().getGuiScale()),
                    (int)((Minecraft.getInstance().getWindow().getGuiScaledHeight() - (y + currentHeight - 2 * ITEM_HEIGHT)) * Minecraft.getInstance().getWindow().getGuiScale()),
                    (int)((WIDTH - (needsScroll ? SCROLLBAR_WIDTH + 8 : 4)) * Minecraft.getInstance().getWindow().getGuiScale()),
                    (int)((currentHeight - 20 - 2 * ITEM_HEIGHT) * Minecraft.getInstance().getWindow().getGuiScale()));

            hoveredIndex = -1;
            List<Account> sortedAlts = new ArrayList<>(alts);
            sortedAlts.sort((a1, a2) -> Boolean.compare(a2.isFavorite(), a1.isFavorite()));

            for (int i = 0; i < sortedAlts.size(); i++) {
                Account account = sortedAlts.get(i);
                String accountUsername = account.getUsername();
                float targetY = y + 26 + i * ITEM_HEIGHT;
                if (!visualYPositions.containsKey(accountUsername)) {
                    visualYPositions.put(accountUsername, targetY);
                } else {
                    float currentVisualY = visualYPositions.get(accountUsername);
                    float newVisualY = lerp(currentVisualY, targetY, partialTick * 0.5F);
                    visualYPositions.put(accountUsername, newVisualY);
                }
                if (!accountOpacities.containsKey(accountUsername)) {
                    accountOpacities.put(accountUsername, 1.0F);
                } else {
                    float currentOpacity = accountOpacities.get(accountUsername);
                    float newOpacity = lerp(currentOpacity, 1.0F, partialTick * 0.3F);
                    accountOpacities.put(accountUsername, newOpacity);
                }
            }

            for (int i = 0; i < sortedAlts.size(); i++) {
                Account account = sortedAlts.get(i);
                String accountUsername = account.getUsername();
                float visualY = visualYPositions.get(accountUsername) - scroll;
                float opacity = accountOpacities.get(accountUsername);
                float fadeDistance = ITEM_HEIGHT;
                float topEdge = y + 20;
                float bottomEdge = y + currentHeight - 2 * ITEM_HEIGHT;
                if (visualY < topEdge) {
                    opacity *= Math.max(0, (visualY - (topEdge - fadeDistance)) / fadeDistance);
                } else if (visualY + ITEM_HEIGHT > bottomEdge) {
                    opacity *= Math.max(0, (bottomEdge - (visualY + ITEM_HEIGHT - fadeDistance)) / fadeDistance);
                }
                if (visualY >= topEdge - fadeDistance && visualY + ITEM_HEIGHT <= bottomEdge + fadeDistance) {
                    boolean isHovered = isMouseOver(mouseX, mouseY, x + 5, visualY, WIDTH - (needsScroll ? SCROLLBAR_WIDTH + 12 : 10), ITEM_HEIGHT);
                    if (isHovered) hoveredIndex = i;
                    boolean isCurrent = account.getUsername().equals(username);
                    float hoverOpacity = isHovered ? lerp(64, 128, partialTick * 0.4F) : 64;
                    int bgColor = isCurrent ? new Color(30, 30, 36, (int)(128 * opacity)).getRGB() : new Color(30, 30, 36, (int)(hoverOpacity * opacity)).getRGB();
                    DrawHelper.rectangle(guiGraphics.pose(), x + 5, visualY, WIDTH - (needsScroll ? SCROLLBAR_WIDTH + 12 : 10), ITEM_HEIGHT - 2, 3.0F, bgColor);
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, opacity);
                    DrawHelper.drawTexture(account.isFavorite() ? FAVORITED : UNFAVORITED, guiGraphics.pose().last().pose(),
                            x + 7, visualY + 3, 10, 10);
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                    int textColor = new Color(255, 255, 255, (int)(255 * opacity)).getRGB();
                    guiGraphics.drawString(Minecraft.getInstance().font, account.getUsername(), (int)(x + 18), (int)(visualY + 4), textColor);
                    if (isHovered && !isCurrent && !account.isFavorite()) {
                        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, opacity);
                        DrawHelper.drawTexture(CROSS_TEXTURE, guiGraphics.pose().last().pose(), x + WIDTH - (needsScroll ? SCROLLBAR_WIDTH + 22 : 20), visualY + 3, 10, 10);
                        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                    }
                }
            }

            List<String> toRemove = new ArrayList<>();
            for (Map.Entry<String, Float> entry : deletingAccounts.entrySet()) {
                String deletingUsername = entry.getKey();
                float opacity = entry.getValue();
                opacity -= partialTick * 0.1F;
                if (opacity <= 0) {
                    toRemove.add(deletingUsername);
                    continue;
                }
                deletingAccounts.put(deletingUsername, opacity);
                if (visualYPositions.containsKey(deletingUsername)) {
                    float visualY = visualYPositions.get(deletingUsername) - scroll;
                    if (visualY >= y + 20 && visualY + ITEM_HEIGHT <= y + currentHeight - 2 * ITEM_HEIGHT) {
                        int bgColor = new Color(30, 30, 36, (int)(64 * opacity)).getRGB();
                        DrawHelper.rectangle(guiGraphics.pose(), x + 5, visualY, WIDTH - (needsScroll ? SCROLLBAR_WIDTH + 12 : 10), ITEM_HEIGHT - 2, 3.0F, bgColor);
                        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, opacity);
                        DrawHelper.drawTexture(UNFAVORITED, guiGraphics.pose().last().pose(),
                                x + 7, visualY + 3, 10, 10);
                        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                        int textColor = new Color(255, 255, 255, (int)(255 * opacity)).getRGB();
                        guiGraphics.drawString(Minecraft.getInstance().font, deletingUsername, (int)(x + 18), (int)(visualY + 4), textColor);
                    }
                }
            }
            for (String usernameToRemove : toRemove) {
                deletingAccounts.remove(usernameToRemove);
                visualYPositions.remove(usernameToRemove);
            }

            RenderSystem.disableScissor();

            float inputY = y + currentHeight - 2 * ITEM_HEIGHT;
            float randomButtonY = y + currentHeight - ITEM_HEIGHT;

            inputField.setX((int)(x + 5));
            inputField.setY((int)inputY);
            inputField.setWidth((int)(WIDTH - 30));
            inputField.setHeight(15);
            DrawHelper.rectangle(guiGraphics.pose(), x + 5, inputY, WIDTH - 10, ITEM_HEIGHT - 2, 3.0F,
                    new Color(30, 30, 36, (int)(64 * currentOpenProgress)).getRGB());
            String displayText = typing ? altName + (System.currentTimeMillis() % 1000 > 500 ? "_" : "") :
                    (altName.isEmpty() ? "Enter nickname" : altName);
            int textAlpha = (int)(255 * inputOpacity * currentOpenProgress);
            guiGraphics.drawString(Minecraft.getInstance().font, displayText, (int)(x + 10), (int)(inputY + 4),
                    new Color(255, 255, 255, textAlpha).getRGB());
            inputField.render(guiGraphics, mouseX, mouseY, partialTick);
            if (altName.length() >= 3 && !alts.stream().anyMatch(a -> a.getUsername().equals(altName))) {
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, currentOpenProgress);
                DrawHelper.drawTexture(ENTER, guiGraphics.pose().last().pose(), x + WIDTH - 18, inputY + 3, 10, 10);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            }

            boolean randomHovered = isMouseOver(mouseX, mouseY, x + 5, randomButtonY, WIDTH - 10, ITEM_HEIGHT);
            float randomOpacity = randomHovered ? lerp(64, 128, partialTick * 0.4F) : 64;
            DrawHelper.rectangle(guiGraphics.pose(), x + 5, randomButtonY, WIDTH - 10, ITEM_HEIGHT - 2, 3.0F,
                    new Color(30, 30, 36, (int)(randomOpacity * currentOpenProgress)).getRGB());
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, currentOpenProgress);
            DrawHelper.drawTexture(RANDOM, guiGraphics.pose().last().pose(), x + WIDTH - 18, randomButtonY + 3, 10, 10);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            int randomTextAlpha = (int)(255 * currentOpenProgress);
            guiGraphics.drawString(Minecraft.getInstance().font, "Random", (int)(x + 10), (int)(randomButtonY + 4),
                    new Color(255, 255, 255, randomTextAlpha).getRGB());

            if (needsScroll) {
                float scrollbarHeight = Math.max(20, scrollableHeight * (scrollableHeight / contentHeight));
                float scrollProgress = scroll / (contentHeight - scrollableHeight);
                float scrollbarY = y + 20 + 3 + scrollProgress * (scrollableHeight - scrollbarHeight - 6);
                DrawHelper.rectangle(guiGraphics.pose(), x + WIDTH - SCROLLBAR_WIDTH - 3, scrollbarY,
                        SCROLLBAR_WIDTH, scrollbarHeight, 1.0F, new Color(128, 128, 128, 160).getRGB());
            }
        }
    }

    private boolean handleMouseClick(double mouseX, double mouseY, int button) {
        float inputY = y + currentHeight - 2 * ITEM_HEIGHT;
        float randomButtonY = y + currentHeight - ITEM_HEIGHT;
        boolean needsScroll = alts.size() * ITEM_HEIGHT > (MAX_HEIGHT - 20 - 2 * ITEM_HEIGHT);

        if (open && isMouseOver(mouseX, mouseY, x + 5, inputY, WIDTH - 10, ITEM_HEIGHT)) {
            typing = !typing;
            inputField.mouseClicked(mouseX, mouseY, button);
            return true;
        }

        if (isMouseOver(mouseX, mouseY, x, y, WIDTH, 20)) {
            open = !open;
            if (!open) {
                typing = false;
            }
            return true;
        }

        if (open) {
            List<Account> sortedAlts = new ArrayList<>(alts);
            sortedAlts.sort((a1, a2) -> Boolean.compare(a2.isFavorite(), a1.isFavorite()));
            for (int i = 0; i < sortedAlts.size(); i++) {
                float visualY = visualYPositions.get(sortedAlts.get(i).getUsername()) - scroll;
                if (visualY >= y + 20 && visualY + ITEM_HEIGHT <= y + currentHeight - 2 * ITEM_HEIGHT) {
                    if (isMouseOver(mouseX, mouseY, x + 5, visualY + 3, 10, 10) && button == 0) {
                        Account account = alts.get(alts.indexOf(sortedAlts.get(i)));
                        account.setFavorite(!account.isFavorite());
                        AltConfig.saveAlts(alts);
                        return true;
                    }
                    if (isMouseOver(mouseX, mouseY, x + 5, visualY, WIDTH - (needsScroll ? SCROLLBAR_WIDTH + 12 : 10), ITEM_HEIGHT)) {
                        if (button == 0) {
                            UserSessionUtil.setNameSession(sortedAlts.get(i).getUsername());
                            AltConfig.saveAlts(alts);
                        } else if (button == 1 && !sortedAlts.get(i).getUsername().equals(Minecraft.getInstance().getUser().getName()) && !sortedAlts.get(i).isFavorite()) {
                            String username = sortedAlts.get(i).getUsername();
                            deletingAccounts.put(username, 1.0F);
                            alts.remove(sortedAlts.get(i));
                            AltConfig.saveAlts(alts);
                        }
                        return true;
                    }
                }
            }

            if (isMouseOver(mouseX, mouseY, x + WIDTH - 18, inputY + 3, 10, 10) && altName.length() >= 3 && !alts.stream().anyMatch(a -> a.getUsername().equals(altName))) {
                alts.add(new Account(altName, false));
                UserSessionUtil.setNameSession(altName);
                AltConfig.saveAlts(alts);
                altName = "";
                inputField.setValue("");
                typing = false;
                return true;
            }

            if (isMouseOver(mouseX, mouseY, x + 5, randomButtonY, WIDTH - 10, ITEM_HEIGHT)) {
                String randomName = UserSessionUtil.generateRandomUsername();
                String finalRandomName = randomName;
                while (alts.stream().anyMatch(a -> a.getUsername().equals(finalRandomName))) {
                    randomName = UserSessionUtil.generateRandomUsername();
                }
                alts.add(new Account(randomName, false));
                UserSessionUtil.setNameSession(randomName);
                AltConfig.saveAlts(alts);
                return true;
            }
        }

        if (!isMouseOver(mouseX, mouseY, x, y, WIDTH, currentHeight)) {
            typing = false;
        }

        return false;
    }

    private boolean isMouseOver(double mouseX, double mouseY, float x, float y, float width, float height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private float lerp(float current, float target, float speed) {
        return current + (target - current) * Math.min(speed, 1.0F);
    }
}