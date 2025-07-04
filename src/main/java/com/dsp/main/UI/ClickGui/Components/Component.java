package com.dsp.main.UI.ClickGui.Components;

import com.dsp.main.UI.ClickGui.Button;
import com.dsp.main.UI.ClickGui.Settings.Setting;
import com.mojang.blaze3d.vertex.PoseStack;

public abstract class Component {
    protected double x;
    protected double y;
    protected Button parent;
    protected Setting setting;

    public Component(Setting setting, Button parent) {
        this.setting = setting;
        this.parent = parent;
    }

    public Setting getSetting() {
        return setting;
    }

    public boolean isVisible() {
        return setting.isVisible();
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void draw(PoseStack matrices, int mouseX, int mouseY) {
        if (!setting.isVisible()) return;
        // Переопределять в наследниках
    }

    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (!setting.isVisible()) return;
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (!setting.isVisible()) return;
        // Переопределять в наследниках
    }

    public boolean isHovered(double mouseX, double mouseY) {
        if (!setting.isVisible()) return false;
        return mouseX > x && mouseX < x + parent.getWidth()
                && mouseY > y && mouseY < y + parent.getHeight();
    }
}