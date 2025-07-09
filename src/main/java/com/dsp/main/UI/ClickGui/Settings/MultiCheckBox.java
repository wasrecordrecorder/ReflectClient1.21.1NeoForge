package com.dsp.main.UI.ClickGui.Settings;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

public class MultiCheckBox extends Setting {
    private final List<CheckBox> options = new ArrayList<>();

    public MultiCheckBox(String name, List<CheckBox> options) {
        super(name);
        this.options.addAll(options);
    }

    public MultiCheckBox(String name, List<CheckBox> options, BooleanSupplier visibleSupplier) {
        this(name, options);
        setVisible(visibleSupplier);
    }

    public List<CheckBox> getOptions() {
        return options;
    }

    public void setOptionEnabled(String optionName, boolean enabled) {
        for (CheckBox option : options) {
            if (option.getName().equalsIgnoreCase(optionName)) {
                option.setEnabled(enabled);
                return;
            }
        }
    }

    public boolean isOptionEnabled(String optionName) {
        for (CheckBox option : options) {
            if (option.getName().equalsIgnoreCase(optionName)) {
                return option.isEnabled();
            }
        }
        return false;
    }
    public boolean hasAnyEnabled() {
        for (CheckBox option : options) {
            if (option.isEnabled()) {
                return true;
            }
        }
        return false;
    }
}