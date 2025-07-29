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
    protected float animatedX;
    protected float animatedY;
    protected boolean hasAnimated;

    public DraggableElement(String name, float initialX, float initialY, boolean canBeDragged) {
        this.name = name;
        this.xPos = initialX;
        this.yPos = initialY;
        this.canBeDragged = canBeDragged;
        this.animatedX = initialX;
        this.animatedY = initialY;
        this.hasAnimated = false;
    }

    public float getX() { return xPos; }
    public float getY() { return yPos; }
    public String getName() { return name; }
    public boolean canBeDragged() { return canBeDragged; }
    public boolean isDragging() { return dragging; }

    public void setX(float x) { this.xPos = x; }
    public void setY(float y) { this.yPos = y; }
    public void setCanBeDragged(boolean canBeDragged) { this.canBeDragged = canBeDragged; }

    public abstract float getWidth();
    public abstract float getHeight();

    public void onDraw(int mouseX, int mouseY, Window window) {
        if (dragging && canBeDragged && isChatOpen()) {
            float rawX = mouseX - startX;
            float rawY = mouseY - startY;
            float maxX = window.getGuiScaledWidth() - getWidth();
            float maxY = window.getGuiScaledHeight() - getHeight();

            float[] snappedPos = DragManager.snapToGrid(rawX, rawY, getWidth(), getHeight(), maxX, maxY);
            xPos = Math.max(0, Math.min(snappedPos[0], maxX));
            yPos = Math.max(0, Math.min(snappedPos[1], maxY));
            animatedX = xPos;
            animatedY = yPos;
        }
    }

    public void onClick(double mouseX, double mouseY, int button) {
        if (button == 0 && isInside(mouseX, mouseY) && canBeDragged && isChatOpen()) {
            dragging = true;
            startX = (float) (mouseX - xPos);
            startY = (float) (mouseY - yPos);
            animatedX = xPos;
            animatedY = yPos;
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

    public void updateAnimation(float deltaTime) {
        float speed = 12f;
        animatedX = lerp(animatedX, getX(), deltaTime * speed);
        animatedY = lerp(animatedY, getY(), deltaTime * speed);
    }

    private float lerp(float start, float end, float t) {
        return start + (end - start) * t;
    }
}