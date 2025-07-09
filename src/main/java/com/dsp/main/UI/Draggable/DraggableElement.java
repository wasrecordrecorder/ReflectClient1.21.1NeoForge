package com.dsp.main.UI.Draggable;

import com.google.gson.annotations.Expose;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public abstract class DraggableElement {
    @Expose
    protected float xPos;
    @Expose
    protected float yPos;
    @Expose
    protected String name;
    protected boolean dragging;
    protected float startX, startY;
    @Expose
    protected boolean canBeDragged;

    public DraggableElement(String name, float initialX, float initialY, boolean canBeDragged) {
        this.name = name;
        this.xPos = initialX;
        this.yPos = initialY;
        this.canBeDragged = canBeDragged;
    }

    public float getX() { return xPos; }
    public float getY() { return yPos; }
    public String getName() { return name; }
    public boolean canBeDragged() { return canBeDragged; }

    public void setX(float x) { this.xPos = x; }
    public void setY(float y) { this.yPos = y; }
    public void setCanBeDragged(boolean canBeDragged) { this.canBeDragged = canBeDragged; }

    public abstract float getWidth();
    public abstract float getHeight();

    public void onDraw(int mouseX, int mouseY, Window window) {
        if (dragging && canBeDragged && isChatOpen()) {
            xPos = mouseX - startX;
            yPos = mouseY - startY;
            float maxX = window.getGuiScaledWidth() - getWidth();
            float maxY = window.getGuiScaledHeight() - getHeight();
            xPos = Math.max(0, Math.min(xPos, maxX));
            yPos = Math.max(0, Math.min(yPos, maxY));
        }
    }

    public void onClick(double mouseX, double mouseY, int button) {
        if (button == 0 && isInside(mouseX, mouseY) && canBeDragged && isChatOpen()) {
            dragging = true;
            startX = (float) (mouseX - xPos);
            startY = (float) (mouseY - yPos);
        }
    }

    public void onRelease(int button) {
        if (button == 0) {
            dragging = false;
        }
    }

    private boolean isInside(double mouseX, double mouseY) {
        return mouseX >= xPos && mouseX <= xPos + getWidth() &&
                mouseY >= yPos && mouseY <= yPos + getHeight();
    }

    protected boolean isChatOpen() {
        Minecraft mc = Minecraft.getInstance();
        return mc.screen != null && mc.screen.getClass().getName().contains("ChatScreen");
    }

    public abstract void render(GuiGraphics guiGraphics);
}