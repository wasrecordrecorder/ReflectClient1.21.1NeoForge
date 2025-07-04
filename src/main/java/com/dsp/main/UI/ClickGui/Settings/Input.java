package com.dsp.main.UI.ClickGui.Settings;

public class Input extends Setting {
    private String value;
    private final String name;

    public Input(String name, String defaultValue) {
        super(name);
        this.name = name;
        this.value = defaultValue;
    }

    public String getValue() {
        return value;
    }
    public String getName() {
        return this.name;
    }


    public void setValue(String value) {
        this.value = value;
    }
}
