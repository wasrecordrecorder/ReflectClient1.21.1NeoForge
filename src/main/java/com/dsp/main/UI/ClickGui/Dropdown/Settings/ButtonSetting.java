package com.dsp.main.UI.ClickGui.Dropdown.Settings;

import java.util.function.Consumer;

public class ButtonSetting extends Setting {
    private final String buttonText;
    private final Consumer<ButtonSetting> onClick;

    public ButtonSetting(String name, String buttonText, Consumer<ButtonSetting> onClick) {
        super(name);
        this.buttonText = buttonText;
        this.onClick = onClick;
    }

    public void click() {
        if (onClick != null) {
            onClick.accept(this);
        }
    }

    public String getButtonText() {
        return buttonText;
    }
}