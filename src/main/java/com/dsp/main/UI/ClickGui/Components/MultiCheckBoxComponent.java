package com.dsp.main.UI.ClickGui.Components;

import com.dsp.main.UI.ClickGui.Button;
import com.dsp.main.UI.ClickGui.Settings.CheckBox;
import com.dsp.main.UI.ClickGui.Settings.MultiCheckBox;
import com.dsp.main.Utils.Font.builders.Builder;
import com.dsp.main.Utils.Font.renderers.impl.BuiltText;
import com.dsp.main.Utils.Render.Blur.DrawShader;
import com.dsp.main.Utils.Render.DrawHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix4f;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import static com.dsp.main.Main.BIKO_FONT;
import static com.dsp.main.Main.RUS;

public class MultiCheckBoxComponent extends Component {
    private static final float ROUNDING = 3.0f;
    private static final float ANIMATION_SPEED = 0.2f;
    private static final float TEXT_SIZE = 8.0f;
    private static final float PADDING = 4.0f;
    private static final float ELEMENT_PADDING = 1.0f;

    private final MultiCheckBox multiCheckBox;
    private boolean isOpen = true;
    private float openAnimationProgress = 0.0f;
    private float width = 0;
    private float heightPadding = 0;

    public MultiCheckBoxComponent(MultiCheckBox setting, Button parent, float scaleFactor) {
        super(setting, parent, scaleFactor);
        this.multiCheckBox = setting;
    }

    @Override
    public void draw(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!isVisible()) return;

        PoseStack poseStack = graphics.pose();
        float nameY = (float) (y + PADDING * scaleFactor);
        BuiltText nameText = Builder.text()
                .font(BIKO_FONT.get())
                .text(multiCheckBox.getName())
                .color(new Color(160, 163, 175))
                .size(TEXT_SIZE * scaleFactor)
                .thickness(0.05f)
                .build();
        nameText.render(new Matrix4f(), (int) (x + 2 * scaleFactor), (int) nameY);

        String enabledOptions = String.join(", ", multiCheckBox.getOptions().stream()
                .filter(CheckBox::isEnabled)
                .map(CheckBox::getName)
                .toList());
        enabledOptions = "Select";

        float modeTextWidth = BIKO_FONT.get().getWidth(enabledOptions, TEXT_SIZE * scaleFactor);
        float currentModeWidth = modeTextWidth + 5 * scaleFactor;
        float modeX = (float) (x + parent.getWidth() - currentModeWidth - 7 * scaleFactor);
        float modeY = (float) (y + PADDING * scaleFactor);
        float modeHeight = BIKO_FONT.get().getMetrics().lineHeight() * TEXT_SIZE * scaleFactor + 2 * scaleFactor;

        if (isOpen) {
            openAnimationProgress = lerp(openAnimationProgress, 1.0f, ANIMATION_SPEED);
            List<List<CheckBox>> rows = calculateRows();
            float dropdownWidth = width + 9 * scaleFactor;
            float dropdownHeight = 14 * scaleFactor + heightPadding;
            float dropdownX = (float) (x + 3 * scaleFactor);
            float dropdownY = (float) (y + modeHeight + PADDING * scaleFactor + ELEMENT_PADDING * scaleFactor);
            float animatedHeight = dropdownHeight * openAnimationProgress + 2 * scaleFactor;

            DrawShader.drawRoundBlur(poseStack, dropdownX - 2 * scaleFactor, dropdownY, dropdownWidth, animatedHeight, ROUNDING * scaleFactor, new Color(10, 10, 12, 200).hashCode(), 90, 0.7f);
            DrawHelper.rectangle(poseStack, dropdownX - 2 * scaleFactor, dropdownY, dropdownWidth, animatedHeight, ROUNDING * scaleFactor, new Color(20, 30, 40, 150).hashCode());

            float offset = 0;
            float heightOffset = 0;

            for (List<CheckBox> row : rows) {
                offset = 0;
                for (CheckBox option : row) {
                    float optWidth = BIKO_FONT.get().getWidth(option.getName(), TEXT_SIZE + 1.3f * scaleFactor) * scaleFactor;
                    float optX = dropdownX + 2 * scaleFactor + offset;
                    float optY = dropdownY + 3 * scaleFactor + heightOffset;
                    float optHeight = BIKO_FONT.get().getMetrics().lineHeight() * TEXT_SIZE * scaleFactor + 1 * scaleFactor;
                    boolean isEnabled = option.isEnabled();

                    DrawHelper.rectangle(poseStack, optX - 1 * scaleFactor, optY, optWidth, optHeight, ROUNDING * scaleFactor, new Color(40, 42, 50, 190).hashCode());
                    if (isEnabled) {
                        DrawHelper.rectangle(poseStack, optX - 1 * scaleFactor, optY, optWidth, optHeight, ROUNDING * scaleFactor, new Color(86, 111, 138, 150).hashCode());
                    }

                    BuiltText optionText = Builder.text()
                            .font(BIKO_FONT.get())
                            .text(option.getName())
                            .color(isEnabled ? new Color(200, 200, 255) : new Color(100, 100, 120))
                            .size(TEXT_SIZE * scaleFactor)
                            .thickness(isEnabled ? 0.07f : 0.05f)
                            .build();
                    optionText.render(new Matrix4f(), (int) (optX + 1 * scaleFactor), (int) (optY + 1 * scaleFactor));

                    offset += optWidth + ELEMENT_PADDING * scaleFactor;
                }
                heightOffset += 10 * scaleFactor + ELEMENT_PADDING * scaleFactor;
            }
        } else {
            openAnimationProgress = lerp(openAnimationProgress, 0.0f, ANIMATION_SPEED);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (!isVisible() || button != 0) return;
        String enabledOptions = String.join(", ", multiCheckBox.getOptions().stream()
                .filter(CheckBox::isEnabled)
                .map(CheckBox::getName)
                .toList());
        if (enabledOptions.isEmpty()) enabledOptions = "None";

        float modeTextWidth = BIKO_FONT.get().getWidth(enabledOptions, TEXT_SIZE * scaleFactor);
        float currentModeWidth = modeTextWidth + 8 * scaleFactor;
        float modeX = (float) (x + parent.getWidth() - currentModeWidth - 7 * scaleFactor);
        float modeY = (float) (y + PADDING * scaleFactor);
        float modeHeight = BIKO_FONT.get().getMetrics().lineHeight() * TEXT_SIZE * scaleFactor + 2 * scaleFactor;

        if (mouseX >= modeX && mouseX <= modeX + currentModeWidth && mouseY >= modeY && mouseY <= modeY + modeHeight) {
            isOpen = true;
            calculateRows();
            return;
        }
        if (isOpen) {
            float dropdownX = (float) (x + 3 * scaleFactor);
            float dropdownY = (float) (y + modeHeight + PADDING * scaleFactor + ELEMENT_PADDING * scaleFactor);
            float offset = 0;
            float heightOffset = 0;

            for (List<CheckBox> row : calculateRows()) {
                offset = 0;
                for (CheckBox option : row) {
                    float optWidth = BIKO_FONT.get().getWidth(option.getName(), TEXT_SIZE * scaleFactor) + 4 * scaleFactor;
                    float optX = dropdownX + 2 * scaleFactor + offset;
                    float optY = dropdownY + 1 * scaleFactor + heightOffset;
                    float optHeight = BIKO_FONT.get().getMetrics().lineHeight() * TEXT_SIZE * scaleFactor + 1 * scaleFactor;

                    if (mouseX >= optX && mouseX <= optX + optWidth && mouseY >= optY && mouseY <= optY + optHeight) {
                        option.toggle();
                        return;
                    }
                    offset += optWidth + ELEMENT_PADDING * scaleFactor;
                }
                heightOffset += 10 * scaleFactor + ELEMENT_PADDING * scaleFactor;
            }
        }
    }

    @Override
    public boolean isHovered(double mouseX, double mouseY) {
        if (!isVisible()) return false;

        String enabledOptions = String.join(", ", multiCheckBox.getOptions().stream()
                .filter(CheckBox::isEnabled)
                .map(CheckBox::getName)
                .toList());
        if (enabledOptions.isEmpty()) enabledOptions = "None";

        float modeTextWidth = BIKO_FONT.get().getWidth(enabledOptions, TEXT_SIZE * scaleFactor);
        float currentModeWidth = modeTextWidth + 8 * scaleFactor;
        float modeX = (float) (x + parent.getWidth() - currentModeWidth - 7 * scaleFactor);
        float modeY = (float) (y + PADDING * scaleFactor);
        float modeHeight = BIKO_FONT.get().getMetrics().lineHeight() * TEXT_SIZE * scaleFactor + 2 * scaleFactor;

        if (mouseX >= modeX && mouseX <= modeX + currentModeWidth && mouseY >= modeY && mouseY <= modeY + modeHeight) {
            return true;
        }

        if (isOpen) {
            float dropdownX = (float) (x + 3 * scaleFactor);
            float dropdownY = (float) (y + modeHeight + PADDING * scaleFactor + ELEMENT_PADDING * scaleFactor);
            float offset = 0;
            float heightOffset = 0;

            for (List<CheckBox> row : calculateRows()) {
                offset = 0;
                for (CheckBox option : row) {
                    float optWidth = BIKO_FONT.get().getWidth(option.getName(), TEXT_SIZE * scaleFactor) + 4 * scaleFactor;
                    float optX = dropdownX + 2 * scaleFactor + offset;
                    float optY = dropdownY + 1 * scaleFactor + heightOffset;
                    float optHeight = BIKO_FONT.get().getMetrics().lineHeight() * TEXT_SIZE * scaleFactor + 1 * scaleFactor;

                    if (mouseX >= optX && mouseX <= optX + optWidth && mouseY >= optY && mouseY <= optY + optHeight) {
                        return true;
                    }
                    offset += optWidth + ELEMENT_PADDING * scaleFactor;
                }
                heightOffset += 10 * scaleFactor + ELEMENT_PADDING * scaleFactor;
            }
        }
        return false;
    }

    private List<List<CheckBox>> calculateRows() {
        List<List<CheckBox>> rows = new ArrayList<>();
        List<CheckBox> currentRow = new ArrayList<>();
        float offset = 0;
        width = 0;
        heightPadding = 0;

        for (CheckBox option : multiCheckBox.getOptions()) {
            float optWidth = BIKO_FONT.get().getWidth(option.getName(), TEXT_SIZE * scaleFactor) + 4 * scaleFactor + ELEMENT_PADDING * scaleFactor;
            if (offset + optWidth >= (parent.getWidth() - 10 * scaleFactor)) {
                rows.add(currentRow);
                currentRow = new ArrayList<>();
                offset = 0;
                heightPadding += 10 * scaleFactor + ELEMENT_PADDING * scaleFactor;
            }
            currentRow.add(option);
            offset += optWidth;
            width = Math.max(width, offset);
        }
        if (!currentRow.isEmpty()) {
            rows.add(currentRow);
        }
        return rows;
    }

    @Override
    public float getHeight() {
        float modeHeight = BIKO_FONT.get().getMetrics().lineHeight() * TEXT_SIZE * scaleFactor + 2 * scaleFactor + PADDING * 2 * scaleFactor + ELEMENT_PADDING * scaleFactor;
        if (isOpen) {
            float dropdownHeight = 10 * scaleFactor + heightPadding + 4 * scaleFactor + ELEMENT_PADDING * scaleFactor;
            return modeHeight + dropdownHeight - 3 * scaleFactor;
        }
        return modeHeight;
    }

    private float lerp(float start, float end, float t) {
        return start + t * (end - start);
    }
}