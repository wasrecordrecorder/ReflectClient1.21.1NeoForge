package com.dsp.main.UI.ClickGui.Settings;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;

public class Setting {
    String name;

    public static IntSupplier heightSupplier  = () -> 16;
    private BooleanSupplier visibleSupplier = () -> true;

    public Setting(String name) {
        this.name = name;
    }

    @SuppressWarnings("unchecked")
    public <T extends Setting> T setHeightSupplier(IntSupplier heightSupplier) {
        Setting.heightSupplier = Objects.requireNonNull(heightSupplier);
        return (T) this;
    }
    @SuppressWarnings("unchecked")
    public <T extends Setting> T setVisible(BooleanSupplier visiblePredicate) {
        this.visibleSupplier = Objects.requireNonNull(visiblePredicate, "Predicate for visibility cannot be null");
        return (T) this;
    }

    public boolean isVisible() {
        return visibleSupplier.getAsBoolean();
    }

    public String getName() {
        return name;
    }
}