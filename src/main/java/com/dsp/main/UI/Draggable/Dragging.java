package com.dsp.main.UI.Draggable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.mojang.blaze3d.platform.Window;


public class Dragging {
    @Expose
    @SerializedName("x")
    private float xPos;

    @Expose
    @SerializedName("y")
    private float yPos;

    private float startX, startY;
    private boolean dragging;

    private float width, height;

    @Expose
    @SerializedName("name")
    private final String name;

    public Module getModule() {
        return module;
    }

    private final Module module;

    public Dragging(Module module, String name, float initialX, float initialY) {
        this.module = module;
        this.name = name;
        this.xPos = initialX;
        this.yPos = initialY;
    }

    public float getX() { return xPos; }
    public float getY() { return yPos; }

    public void setX(float x) { this.xPos = x; }
    public void setY(float y) { this.yPos = y; }

    public float getWidth() { return width; }
    public void setWidth(float width) { this.width = width; }

    public float getHeight() { return height; }
    public void setHeight(float height) { this.height = height; }

    public String getName() { return name; }

    public void onDraw(int mouseX, int mouseY, Window window) {
        if (dragging) {
            xPos = mouseX - startX;
            yPos = mouseY - startY;

            // Ограничение по краям окна
            float maxX = window.getGuiScaledWidth() - width;
            float maxY = window.getGuiScaledHeight() - height;

            xPos = Math.max(0, Math.min(xPos, maxX));
            yPos = Math.max(0, Math.min(yPos, maxY));
        }
    }

    public void onClick(double mouseX, double mouseY, int button) {
        if (button == 0 && isInside(mouseX, mouseY)) {
            dragging = true;
            startX = (float)(mouseX - xPos);
            startY = (float)(mouseY - yPos);
        }
    }

    public void onRelease(int button) {
        if (button == 0) {
            dragging = false;
        }
    }

    public void onClickGui(double mouseX, double mouseY, int button) {
        if (button == 2 && isInside(mouseX, mouseY)) {
            dragging = true;
            startX = (float)(mouseX - xPos);
            startY = (float)(mouseY - yPos);
        }
    }

    public void onReleaseGui(int button) {
        if (button == 2) {
            dragging = false;
        }
    }

    private boolean isInside(double mouseX, double mouseY) {
        return mouseX >= xPos && mouseX <= xPos + width &&
                mouseY >= yPos && mouseY <= yPos + height;
    }
}
