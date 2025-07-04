package com.dsp.main.UI.ClickGui.Settings;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Slider extends Setting {
    private double min, max, inc;
    public double value;
    private double defaultvalue;

    public Slider(String name, double min, double max, double defaultvalue, double inc) {
        super(name);
        this.min = min;
        this.max = max;
        this.defaultvalue = defaultvalue;
        this.inc = inc;
        setValue(defaultvalue);
    }

    public void setInc(double inc) {
        this.inc = inc;
    }

    public double getDefaultvalue() {
        return defaultvalue;
    }

    public void setDefaultvalue(double defaultvalue) {
        this.defaultvalue = defaultvalue;
    }

    public double getValue() {
        return value;
    }

    public float getValueFloat() {
        return (float) value;
    }

    public int getValueInt() {
        return (int) value;
    }

    /**
     * Прямое назначение без округления – используйте setValue для установки со сдвигом.
     */
    public void setValDouble(double in) {
        this.value = in;
    }

    /**
     * Устанавливает значение с округлением по шагу inc.
     *
     * 1. Сначала вычисляем количество шагов от min: (value - min) / inc, округляем до целого.
     * 2. Вычисляем новое значение, прибавляя min.
     * 3. Ограничиваем значение в пределах [min, max].
     * 4. Округляем итоговое значение до нужного количества десятичных знаков, соответствующего инкременту.
     *
     * @param value новое значение
     */
    public void setValue(double value) {
        // Шаговое округление относительно min
        value = Math.round((value - min) / inc) * inc + min;
        // Ограничение значения в диапазоне [min, max]
        value = clamp((float) value, (float) min, (float) max);

        // Определяем число десятичных знаков по inc
        int scale = BigDecimal.valueOf(inc).scale();
        // Округляем итоговое значение до 'scale' десятичных знаков
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(scale, RoundingMode.HALF_UP);
        this.value = bd.doubleValue();
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public double getInc() {
        return inc;
    }

    public static float clamp(float num, float min, float max) {
        return num < min ? min : num > max ? max : num;
    }

    public void inc(boolean isPositive) {
        if (isPositive) {
            setValue(getValue() + getInc());
        } else {
            setValue(getValue() - getInc());
        }
    }
}
