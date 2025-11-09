package com.dsp.main.UI.ClickGui.Dropdown.Components;

import com.dsp.main.UI.ClickGui.Dropdown.Button;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.Mode;
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

public class ModeComponent extends Component {
    private static final float ROUNDING = 3.0f;
    private static final float ANIMATION_SPEED = 0.2f;
    private static final float TEXT_SIZE = 8.0f;
    private static final float PADDING = 4.0f;
    private static final float ELEMENT_PADDING = 1.0f;

    private final Mode modeSetting;
    private boolean isOpen = false;
    private float openAnimationProgress = 0.0f;
    private float width = 0;
    private float heightPadding = 0;

    public ModeComponent(Mode setting, Button parent, float scaleFactor) {
        super(setting, parent, scaleFactor);
        this.modeSetting = setting;
    }

    @Override
    public void draw(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!isVisible()) return;

        PoseStack poseStack = graphics.pose();
        float nameY = (float) (y + PADDING * scaleFactor);
        BuiltText nameText = Builder.text()
                .font(BIKO_FONT.get())
                .text(modeSetting.getName())
                .color(new Color(160, 163, 175))
                .size(TEXT_SIZE * scaleFactor)
                .thickness(0.05f)
                .build();
        nameText.render(new Matrix4f(), (float) (x + 2 * scaleFactor), nameY);

        float modeTextWidth = BIKO_FONT.get().getWidth(modeSetting.getMode(), TEXT_SIZE * scaleFactor);
        float currentModeWidth = modeTextWidth + 5 * scaleFactor;
        float modeX = (float) (x + parent.getWidth() - currentModeWidth - 7 * scaleFactor);
        float modeY = (float) (y + PADDING * scaleFactor);
        float modeHeight = BIKO_FONT.get().getMetrics().lineHeight() * TEXT_SIZE * scaleFactor + 2 * scaleFactor;

        DrawShader.drawRoundBlur(poseStack, modeX, modeY - 1 * scaleFactor, currentModeWidth, modeHeight, ROUNDING * scaleFactor, new Color(33, 33, 39, 200).hashCode(), 90, 0.7f);

        boolean isHovered = mouseX >= modeX && mouseX <= modeX + currentModeWidth && mouseY >= modeY && mouseY <= modeY + modeHeight;
        BuiltText currentModeText = Builder.text()
                .font(BIKO_FONT.get())
                .text(modeSetting.getMode())
                .color(isHovered ? new Color(180, 180, 200) : new Color(200, 200, 255))
                .size(TEXT_SIZE * scaleFactor)
                .thickness(0.07f)
                .build();
        currentModeText.render(new Matrix4f(), (modeX + (currentModeWidth - modeTextWidth) / 2), (modeY + 1 * scaleFactor));

        if (isOpen) {
            openAnimationProgress = lerp(openAnimationProgress, 1.0f, ANIMATION_SPEED);
            List<List<String>> rows = calculateRows();
            float dropdownWidth = width + 9 * scaleFactor;
            float dropdownHeight = 14 * scaleFactor + heightPadding;
            float dropdownX = (float) (x + 3 * scaleFactor);
            float dropdownY = (float) (y + modeHeight + PADDING * scaleFactor + ELEMENT_PADDING * scaleFactor);
            float animatedHeight = dropdownHeight * openAnimationProgress + 2 * scaleFactor;

            DrawShader.drawRoundBlur(poseStack, dropdownX - 2 * scaleFactor, dropdownY, dropdownWidth, animatedHeight, ROUNDING * scaleFactor, new Color(10, 10, 12, 200).hashCode(), 90, 0.7f);
            DrawHelper.rectangle(poseStack, dropdownX - 2 * scaleFactor, dropdownY, dropdownWidth, animatedHeight, ROUNDING * scaleFactor, new Color(20, 30, 40, 150).hashCode());

            float offset = 0;
            float heightOffset = 0;

            for (List<String> row : rows) {
                offset = 0;
                for (String modeOption : row) {
                    float optWidth = BIKO_FONT.get().getWidth(modeOption, TEXT_SIZE * scaleFactor) + 5 * scaleFactor;
                    float optX = dropdownX + 2 * scaleFactor + offset;
                    float optY = dropdownY + 3 * scaleFactor + heightOffset;
                    float optHeight = BIKO_FONT.get().getMetrics().lineHeight() * TEXT_SIZE * scaleFactor + 1 * scaleFactor;

                    boolean isOptionHovered = mouseX >= optX && mouseX <= optX + optWidth && mouseY >= optY && mouseY <= optY + optHeight;
                    boolean isSelected = modeOption.equals(modeSetting.getMode());

                    DrawHelper.rectangle(poseStack, optX - 1 * scaleFactor, optY, optWidth, optHeight, ROUNDING * scaleFactor, new Color(38, 39, 48, 150).hashCode());
                    if (isSelected || isOptionHovered) {
                        DrawHelper.rectangle(poseStack, optX - 1 * scaleFactor, optY, optWidth, optHeight, ROUNDING * scaleFactor, new Color(86, 111, 138, 150).hashCode());
                    }

                    BuiltText modeText = Builder.text()
                            .font(BIKO_FONT.get())
                            .text(modeOption)
                            .color(isSelected ? new Color(200, 200, 255) : isOptionHovered ? new Color(180, 180, 200) : new Color(100, 100, 120))
                            .size(TEXT_SIZE * scaleFactor)
                            .thickness(isSelected ? 0.07f : 0.05f)
                            .build();
                    modeText.render(new Matrix4f(), (optX + 1 * scaleFactor), (optY + 1 * scaleFactor));

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

        float modeTextWidth = BIKO_FONT.get().getWidth(modeSetting.getMode(), TEXT_SIZE * scaleFactor);
        float currentModeWidth = modeTextWidth + 8 * scaleFactor;
        float modeX = (float) (x + parent.getWidth() - currentModeWidth - 7 * scaleFactor);
        float modeY = (float) (y + PADDING * scaleFactor);
        float modeHeight = BIKO_FONT.get().getMetrics().lineHeight() * TEXT_SIZE * scaleFactor + 2 * scaleFactor;

        if (mouseX >= modeX && mouseX <= modeX + currentModeWidth && mouseY >= modeY && mouseY <= modeY + modeHeight) {
            isOpen = !isOpen;
            calculateRows();
            return;
        }

        if (isOpen) {
            float dropdownX = (float) (x + 3 * scaleFactor);
            float dropdownY = (float) (y + modeHeight + PADDING * scaleFactor + ELEMENT_PADDING * scaleFactor);
            float offset = 0;
            float heightOffset = 0;

            for (List<String> row : calculateRows()) {
                offset = 0;
                for (String modeOption : row) {
                    float optWidth = BIKO_FONT.get().getWidth(modeOption, TEXT_SIZE * scaleFactor) + 4 * scaleFactor;
                    float optX = dropdownX + 8 * scaleFactor + offset;
                    float optY = dropdownY + 1 * scaleFactor + heightOffset;
                    float optHeight = BIKO_FONT.get().getMetrics().lineHeight() * TEXT_SIZE * scaleFactor + 1 * scaleFactor;

                    if (mouseX >= optX && mouseX <= optX + optWidth && mouseY >= optY && mouseY <= optY + optHeight) {
                        modeSetting.setMode(modeOption);
                        isOpen = false;
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

        float modeTextWidth = BIKO_FONT.get().getWidth(modeSetting.getMode(), TEXT_SIZE * scaleFactor);
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

            for (List<String> row : calculateRows()) {
                offset = 0;
                for (String modeOption : row) {
                    float optWidth = BIKO_FONT.get().getWidth(modeOption, TEXT_SIZE * scaleFactor) + 4 * scaleFactor;
                    float optX = dropdownX + 8 * scaleFactor + offset;
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

    private List<List<String>> calculateRows() {
        List<List<String>> rows = new ArrayList<>();
        List<String> currentRow = new ArrayList<>();
        float offset = 0;
        width = 0;
        heightPadding = 0;

        for (String modeOption : modeSetting.getModes()) {
            float optWidth = BIKO_FONT.get().getWidth(modeOption, TEXT_SIZE * scaleFactor) + 4 * scaleFactor + ELEMENT_PADDING * scaleFactor;
            if (offset + optWidth >= (parent.getWidth() - 10 * scaleFactor)) {
                rows.add(currentRow);
                currentRow = new ArrayList<>();
                offset = 0;
                heightPadding += 10 * scaleFactor + ELEMENT_PADDING * scaleFactor;
            }
            currentRow.add(modeOption);
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
            float dropdownHeight = 6 * scaleFactor + heightPadding + 4 * scaleFactor + ELEMENT_PADDING * scaleFactor;
            return modeHeight + dropdownHeight + 2 * scaleFactor;
        }
        return modeHeight;
    }

    private float lerp(float start, float end, float t) {
        return start + t * (end - start);
    }
}