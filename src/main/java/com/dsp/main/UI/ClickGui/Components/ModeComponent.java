package com.dsp.main.UI.ClickGui.Components;

import com.dsp.main.UI.ClickGui.Button;
import com.dsp.main.UI.ClickGui.Settings.Mode;
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
    private static final float ELEMENT_PADDING = 1.0f; // Padding между элементами

    private final Mode modeSetting;
    private boolean isOpen = false;
    private float openAnimationProgress = 0.0f;
    private float width = 0;
    private float heightPadding = 0;

    public ModeComponent(Mode setting, Button parent) {
        super(setting, parent);
        this.modeSetting = setting;
    }

    @Override
    public void draw(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!isVisible()) return;

        PoseStack poseStack = graphics.pose();

        // Отрисовка имени настройки
        float nameY = (float) y + PADDING;
        BuiltText nameText = Builder.text()
                .font(BIKO_FONT.get())
                .text(modeSetting.getName())
                .color(new Color(160, 163, 175))
                .size(TEXT_SIZE)
                .thickness(0.05f)
                .build();
        nameText.render(new Matrix4f(), (int) x + 2, (int) nameY); // Смещено влево на 2 пикселя

        // Отрисовка фона текущего режима
        float modeTextWidth = BIKO_FONT.get().getWidth(modeSetting.getMode(), TEXT_SIZE);
        float currentModeWidth = modeTextWidth + 5;
        float modeX = (float) (x + parent.getWidth() - currentModeWidth - 7); // Смещено влево на 2 пикселя
        float modeY = (float) y + PADDING;
        float modeHeight = BIKO_FONT.get().getMetrics().lineHeight() * TEXT_SIZE + 2;

        DrawShader.drawRoundBlur(poseStack, modeX, modeY -1, currentModeWidth, modeHeight, ROUNDING, new Color(33, 33, 39, 200).hashCode(), 90, 0.7f);

        // Отрисовка текста текущего режима
        boolean isHovered = mouseX >= modeX && mouseX <= modeX + currentModeWidth && mouseY >= modeY && mouseY <= modeY + modeHeight;
        BuiltText currentModeText = Builder.text()
                .font(BIKO_FONT.get())
                .text(modeSetting.getMode())
                .color(isHovered ? new Color(180, 180, 200) : new Color(200, 200, 255))
                .size(TEXT_SIZE)
                .thickness(0.07f)
                .build();
        currentModeText.render(new Matrix4f(), (int) (modeX + (currentModeWidth - modeTextWidth) / 2), (int) (modeY + 1));

        if (isOpen) {
            openAnimationProgress = lerp(openAnimationProgress, 1.0f, ANIMATION_SPEED);
            List<List<String>> rows = calculateRows();
            float dropdownWidth = width + 9;
            float dropdownHeight = 14 + heightPadding;
            float dropdownX = (float) x + 3; // Смещено влево на 2 пикселя
            float dropdownY = (float) y + modeHeight + PADDING + ELEMENT_PADDING; // Добавлен padding
            float animatedHeight = dropdownHeight * openAnimationProgress + 2;

            DrawShader.drawRoundBlur(poseStack, dropdownX - 2, dropdownY, dropdownWidth, animatedHeight, ROUNDING, new Color(10, 10, 12, 200).hashCode(), 90, 0.7f);
            DrawHelper.rectangle(poseStack, dropdownX - 2, dropdownY, dropdownWidth, animatedHeight, ROUNDING, new Color(20, 30, 40, 150).hashCode());

            float offset = 0;
            float heightOffset = 0;

            for (List<String> row : rows) {
                offset = 0;
                for (String modeOption : row) {
                    float optWidth = BIKO_FONT.get().getWidth(modeOption, TEXT_SIZE) + 5;
                    float optX = dropdownX + 2 + offset;
                    float optY = dropdownY + 3 + heightOffset;
                    float optHeight = BIKO_FONT.get().getMetrics().lineHeight() * TEXT_SIZE + 1;

                    boolean isOptionHovered = mouseX >= optX && mouseX <= optX + optWidth && mouseY >= optY && mouseY <= optY + optHeight;
                    boolean isSelected = modeOption.equals(modeSetting.getMode());

                    DrawHelper.rectangle(poseStack, optX - 1, optY, optWidth, optHeight, ROUNDING, new Color(38, 39, 48, 150).hashCode());
                    if (isSelected || isOptionHovered) {
                        DrawHelper.rectangle(poseStack, optX -1, optY, optWidth, optHeight, ROUNDING, new Color(86, 111, 138, 150).hashCode());
                    }

                    // Текст режима
                    BuiltText modeText = Builder.text()
                            .font(BIKO_FONT.get())
                            .text(modeOption)
                            .color(isSelected ? new Color(200, 200, 255) : isOptionHovered ? new Color(180, 180, 200) : new Color(100, 100, 120))
                            .size(TEXT_SIZE)
                            .thickness(isSelected ? 0.07f : 0.05f)
                            .build();
                    modeText.render(new Matrix4f(), (int) optX + 1, (int) optY + 1);

                    offset += optWidth + ELEMENT_PADDING;
                }
                heightOffset += 10 + ELEMENT_PADDING;
            }
        } else {
            openAnimationProgress = lerp(openAnimationProgress, 0.0f, ANIMATION_SPEED);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (!isVisible() || button != 0) return;

        // Проверка клика на текущий режим для открытия/закрытия выпадающего списка
        float modeTextWidth = BIKO_FONT.get().getWidth(modeSetting.getMode(), TEXT_SIZE);
        float currentModeWidth = modeTextWidth + 8;
        float modeX = (float) (x + parent.getWidth() - currentModeWidth - 7);
        float modeY = (float) y + PADDING;
        float modeHeight = BIKO_FONT.get().getMetrics().lineHeight() * TEXT_SIZE + 2;

        if (mouseX >= modeX && mouseX <= modeX + currentModeWidth && mouseY >= modeY && mouseY <= modeY + modeHeight) {
            isOpen = !isOpen;
            calculateRows(); // Предварительный расчет размеров
            return;
        }

        // Проверка клика на режимы в выпадающем списке
        if (isOpen) {
            float dropdownX = (float) x + 3;
            float dropdownY = (float) y + modeHeight + PADDING * 2 + ELEMENT_PADDING;
            float offset = 0;
            float heightOffset = 0;

            for (List<String> row : calculateRows()) {
                offset = 0;
                for (String modeOption : row) {
                    float optWidth = BIKO_FONT.get().getWidth(modeOption, TEXT_SIZE) + 4;
                    float optX = dropdownX + 8 + offset;
                    float optY = dropdownY + 1 + heightOffset;
                    float optHeight = BIKO_FONT.get().getMetrics().lineHeight() * TEXT_SIZE + 1;

                    if (mouseX >= optX && mouseX <= optX + optWidth && mouseY >= optY && mouseY <= optY + optHeight) {
                        modeSetting.setMode(modeOption);
                        isOpen = false; // Закрытие списка после выбора
                        return;
                    }
                    offset += optWidth + ELEMENT_PADDING;
                }
                heightOffset += 10 + ELEMENT_PADDING;
            }
        }
    }

    @Override
    public boolean isHovered(double mouseX, double mouseY) {
        if (!isVisible()) return false;

        // Проверка наведения на текущий режим
        float modeTextWidth = BIKO_FONT.get().getWidth(modeSetting.getMode(), TEXT_SIZE);
        float currentModeWidth = modeTextWidth + 8;
        float modeX = (float) (x + parent.getWidth() - currentModeWidth - 7);
        float modeY = (float) y + PADDING;
        float modeHeight = BIKO_FONT.get().getMetrics().lineHeight() * TEXT_SIZE + 2;

        if (mouseX >= modeX && mouseX <= modeX + currentModeWidth && mouseY >= modeY && mouseY <= modeY + modeHeight) {
            return true;
        }

        // Проверка наведения на режимы в выпадающем списке
        if (isOpen) {
            float dropdownX = (float) x + 3;
            float dropdownY = (float) y + modeHeight + PADDING * 2 + ELEMENT_PADDING;
            float offset = 0;
            float heightOffset = 0;

            for (List<String> row : calculateRows()) {
                offset = 0;
                for (String modeOption : row) {
                    float optWidth = BIKO_FONT.get().getWidth(modeOption, TEXT_SIZE) + 4;
                    float optX = dropdownX + 8 + offset;
                    float optY = dropdownY + 1 + heightOffset;
                    float optHeight = BIKO_FONT.get().getMetrics().lineHeight() * TEXT_SIZE + 1;

                    if (mouseX >= optX && mouseX <= optX + optWidth && mouseY >= optY && mouseY <= optY + optHeight) {
                        return true;
                    }
                    offset += optWidth + ELEMENT_PADDING;
                }
                heightOffset += 10 + ELEMENT_PADDING;
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
            float optWidth = BIKO_FONT.get().getWidth(modeOption, TEXT_SIZE) + 4 + ELEMENT_PADDING;
            if (offset + optWidth >= (parent.getWidth() - 10)) {
                rows.add(currentRow);
                currentRow = new ArrayList<>();
                offset = 0;
                heightPadding += 10 + ELEMENT_PADDING;
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
        float modeHeight = BIKO_FONT.get().getMetrics().lineHeight() * TEXT_SIZE + 2 + PADDING * 2 + ELEMENT_PADDING;
        if (isOpen) {
            float dropdownHeight = 6 + heightPadding + 4 + ELEMENT_PADDING;
            return modeHeight + dropdownHeight + 2;
        }
        return modeHeight;
    }

    private float lerp(float start, float end, float t) {
        return start + t * (end - start);
    }
}