package com.dsp.main.UI.ClickGui.Settings;

import net.minecraft.client.Minecraft;

public class BindCheckBox extends Setting {
    private int bindKey;
    private final Runnable callback;

    public BindCheckBox(String name, int defaultKey, Runnable callback) {
        super(name);
        this.bindKey = defaultKey;
        this.callback = callback;
    }

    public int getBindKey() {
        return bindKey;
    }

    public void setBindKey(int key) {
        this.bindKey = key;
    }

    public void execute() {
        if(Minecraft.getInstance().screen == null) callback.run();
    }
}