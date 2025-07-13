package com.dsp.main.UI.ClickGui.Components;

import com.dsp.main.UI.ClickGui.Button;
import com.dsp.main.UI.ClickGui.Settings.Setting;
import net.minecraft.client.gui.GuiGraphics;

public abstract class Component {
    protected double x;
    protected double y;
    protected Button parent;
    protected Setting setting;
    protected final float scaleFactor;

    public Component(Setting setting, Button parent, float scaleFactor) {
        this.setting = setting;
        this.parent = parent;
        this.scaleFactor = scaleFactor;
    }

    public Setting getSetting() {
        return setting;
    }

    public boolean isVisible() {
        return setting.isVisible();
    }

    public void setX(double x) {
        this.x = x * scaleFactor;
    }

    public void setY(double y) {
        this.y = y * scaleFactor;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public abstract float getHeight();

    public void draw(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!setting.isVisible()) return;
    }

    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (!setting.isVisible()) return;
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (!setting.isVisible()) return;
    }

    public boolean isHovered(double mouseX, double mouseY) {
        if (!setting.isVisible()) return false;
        return mouseX > x && mouseX < x + parent.getWidth()
                && mouseY > y && mouseY < y + getHeight();
    }
    public boolean isInputActive() {
        return false;
    }
}