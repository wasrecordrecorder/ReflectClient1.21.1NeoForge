package com.dsp.main.UI.ClickGui.Settings;

import net.minecraft.client.Minecraft;

public class BindCheckBox extends Setting {
    private int bindKey;
    private boolean enabled;
    private final Runnable callback;

    public BindCheckBox(String name, int defaultKey, Runnable callback) {
        super(name);
        this.bindKey = defaultKey;
        this.callback = callback;
        this.enabled = false;
    }

    public int getBindKey() {
        return bindKey;
    }

    public void setBindKey(int key) {
        this.bindKey = key;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void toggle() {
        this.enabled = !enabled;
    }

    public void execute() {
        if(enabled && Minecraft.getInstance().screen == null) callback.run();
    }
}