package com.dsp.main.UI.Draggable.DragElements;

import com.dsp.main.Api;
import com.dsp.main.Module;
import com.dsp.main.UI.Draggable.DraggableElement;
import com.dsp.main.UI.Themes.ThemesUtil;
import com.dsp.main.Utils.Color.ColorHelper;
import com.dsp.main.Utils.Font.builders.Builder;
import com.dsp.main.Utils.Font.renderers.impl.BuiltText;
import com.dsp.main.Utils.Render.Blur.DrawShader;
import com.dsp.main.Utils.Render.ColorUtil;
import com.dsp.main.Utils.TimerUtil;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix4f;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import static com.dsp.main.Api.mc;
import static com.dsp.main.Functions.Render.HudElement.HudElements;
import static com.dsp.main.Main.BIKO_FONT;
import static com.dsp.main.Main.ICONS;
import static com.dsp.main.Utils.KeyName.getKeyName;

public class Keybinds extends DraggableElement {
    private static final int TEXT_HEIGHT = 8;
    private static final int PADDING = 5;
    private static final int SPACING = 25;
    private static final int ROUND_RADIUS = 5;
    private static final int ICON_SIZE = 11;
    private static final List<Module> modules = Api.Functions;
    private static final long ANIMATION_DURATION_MS = 300;

    private static final Module FAKE_MODULE = new Module("Testik", 0, Module.Category.MISC, "Test") {
        @Override
        public String getName() {
            return "ExampleModule";
        }

        @Override
        public int getKeyCode() {
            return 45;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    };

    private float opacity = 0.0f;
    private float targetOpacity = 0.0f;
    private float currentWidth = 0.0f;
    private float currentHeight = 0.0f;
    private float targetWidth = 0.0f;
    private float targetHeight = 0.0f;
    private long animationStartTime = 0;
    private List<Module> lastModules = new ArrayList<>();
    private final TimerUtil timer = new TimerUtil();

    public Keybinds(String name, float initialX, float initialY, boolean canBeDragged) {
        super(name, initialX, initialY, canBeDragged);
    }

    @Override
    public float getWidth() {
        List<Module> activeModulesWithBind = getActiveModulesWithBind();
        if (activeModulesWithBind.isEmpty()) {
            return 0;
        }
        float maxModuleNameWidth = activeModulesWithBind.stream()
                .map(module -> BIKO_FONT.get().getWidth(module.getName(), TEXT_HEIGHT))
                .max(Float::compare)
                .orElse(0f);
        float maxKeybindWidth = activeModulesWithBind.stream()
                .map(module -> BIKO_FONT.get().getWidth(getKeyName(module.getKeyCode()).toUpperCase(), TEXT_HEIGHT))
                .max(Float::compare)
                .orElse(0f);
        return ICON_SIZE + maxModuleNameWidth + SPACING + maxKeybindWidth + 2 * PADDING;
    }

    @Override
    public float getHeight() {
        List<Module> activeModulesWithBind = getActiveModulesWithBind();
        if (activeModulesWithBind.isEmpty()) {
            return 0;
        }
        return activeModulesWithBind.size() * (TEXT_HEIGHT + 4) + 3;
    }

    @Override
    public void render(GuiGraphics guiGraphics) {
        if (mc.player == null || !HudElements.isOptionEnabled("Keybinds") || !Api.isEnabled("Hud")) {
            targetOpacity = 0.0f;
        } else {
            List<Module> activeModulesWithBind = getActiveModulesWithBind();
            if (activeModulesWithBind.isEmpty() && !isChatOpen()) {
                targetOpacity = 0.0f;
            } else {
                targetOpacity = 1.0f;
            }
            if (!activeModulesWithBind.equals(lastModules)) {
                targetWidth = getWidth();
                targetHeight = getHeight();
                animationStartTime = System.currentTimeMillis();
                lastModules = new ArrayList<>(activeModulesWithBind);
            }
            long elapsed = System.currentTimeMillis() - animationStartTime;
            float t = Math.min((float) elapsed / ANIMATION_DURATION_MS, 1.0f);
            opacity = lerp(opacity, targetOpacity, t);
            currentWidth = lerp(currentWidth, targetWidth, t);
            currentHeight = lerp(currentHeight, targetHeight, t);
            if (opacity <= 0.01f || currentWidth <= 0.01f || currentHeight <= 0.01f) {
                return;
            }
            int alpha = (int) (opacity * 255);
            DrawShader.drawRoundBlur(guiGraphics.pose(), xPos, yPos, currentWidth, currentHeight, ROUND_RADIUS,
                    new Color(23, 29, 35, alpha).getRGB(), 120, 0.4f);
            float currentY = yPos + PADDING - 1;
            for (Module module : activeModulesWithBind) {
                Color textColorWithAlpha = new Color(255, 255, 255, alpha);

                switch (module.getCategory().toString().toLowerCase()) {
                    case "combat":
                        BuiltText combatIcon = Builder.text()
                                .font(ICONS.get())
                                .text("a")
                                .color(ColorHelper.gradient(ThemesUtil.getCurrentStyle().getColor(1), ThemesUtil.getCurrentStyle().getColor(2), 20, 10))
                                .size(ICON_SIZE)
                                .thickness(0.05f)
                                .build();
                        combatIcon.render(new Matrix4f(), xPos + 1, currentY - 2.3f);
                        break;
                    case "movement":
                        BuiltText movementIcon = Builder.text()
                                .font(ICONS.get())
                                .text("K")
                                .color(ColorHelper.gradient(ThemesUtil.getCurrentStyle().getColor(1), ThemesUtil.getCurrentStyle().getColor(2), 20, 10))
                                .size(ICON_SIZE)
                                .thickness(0.05f)
                                .build();
                        movementIcon.render(new Matrix4f(), xPos + 1, currentY - 1.6f);
                        break;
                    case "render":
                        BuiltText renderIcon = Builder.text()
                                .font(ICONS.get())
                                .text("c")
                                .color(ColorHelper.gradient(ThemesUtil.getCurrentStyle().getColor(1), ThemesUtil.getCurrentStyle().getColor(2), 20, 10))
                                .size(ICON_SIZE)
                                .thickness(0.05f)
                                .build();
                        renderIcon.render(new Matrix4f(), xPos + 1, currentY - 2.3f);
                        break;
                    case "player":
                        BuiltText playerIcon = Builder.text()
                                .font(ICONS.get())
                                .text("B")
                                .color(ColorHelper.gradient(ThemesUtil.getCurrentStyle().getColor(1), ThemesUtil.getCurrentStyle().getColor(2), 20, 10))
                                .size(ICON_SIZE)
                                .thickness(0.05f)
                                .build();
                        playerIcon.render(new Matrix4f(), xPos + 1, currentY - 2.3f);
                        break;
                    case "misc":
                        BuiltText miscIcon = Builder.text()
                                .font(ICONS.get())
                                .text("e")
                                .color(ColorHelper.gradient(ThemesUtil.getCurrentStyle().getColor(1), ThemesUtil.getCurrentStyle().getColor(2), 20, 10))
                                .size(ICON_SIZE)
                                .thickness(0.05f)
                                .build();
                        miscIcon.render(new Matrix4f(), xPos + 1, currentY - 2.3f);
                        break;
                    default:
                        BuiltText testIcon = Builder.text()
                                .font(ICONS.get())
                                .text("f")
                                .color(ColorHelper.gradient(ThemesUtil.getCurrentStyle().getColor(1), ThemesUtil.getCurrentStyle().getColor(2), 20, 10))
                                .size(ICON_SIZE)
                                .thickness(0.05f)
                                .build();
                        testIcon.render(new Matrix4f(), xPos + 1, currentY - 2.3f);
                        break;
                }
                String moduleName = module.getName();
                String keybindName = getKeyName(module.getKeyCode()).toUpperCase();
                BuiltText moduleText = Builder.text()
                        .font(BIKO_FONT.get())
                        .text(moduleName)
                        .color(textColorWithAlpha)
                        .size(TEXT_HEIGHT)
                        .thickness(0.05f)
                        .build();
                moduleText.render(new Matrix4f(), xPos + PADDING + 9, currentY);
                BuiltText keybindText = Builder.text()
                        .font(BIKO_FONT.get())
                        .text(keybindName)
                        .color(textColorWithAlpha)
                        .size(TEXT_HEIGHT)
                        .thickness(0.05f)
                        .build();
                float keybindX = xPos + currentWidth - PADDING - BIKO_FONT.get().getWidth(keybindName, TEXT_HEIGHT);
                keybindText.render(new Matrix4f(), keybindX, currentY);

                currentY += TEXT_HEIGHT + 4;
            }
        }
    }

    private List<Module> getActiveModulesWithBind() {
        List<Module> activeModulesWithBind = new ArrayList<>();
        if (mc.player != null) {
            for (Module module : modules) {
                if (module.isEnabled() && module.getKeyCode() != 0) {
                    activeModulesWithBind.add(module);
                }
            }
        }
        if (activeModulesWithBind.isEmpty() && isChatOpen()) {
            activeModulesWithBind.add(FAKE_MODULE);
        }
        return activeModulesWithBind;
    }
    private float lerp(float start, float end, float t) {
        return start + (end - start) * t;
    }
}