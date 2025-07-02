package com.dsp.main.ClickGui.Settings;

public class CheckBox extends Setting {
    private boolean enabled;

    public CheckBox(String name, boolean defaultVal) {
        super(name);
        this.enabled = defaultVal;
    }

    public void toggle() {
        this.enabled = !this.enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
