package com.dsp.main.UI.Draggable.DragElements;

import com.dsp.main.UI.Draggable.DraggableElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class WaterMark extends DraggableElement {
    public WaterMark(String name, float initialX, float initialY, float width, float height) {
        super(name, initialX, initialY, width, height);
    }

    @Override
    public void render(GuiGraphics guiGraphics) {
        guiGraphics.fill((int) xPos, (int) yPos, (int) (xPos + width), (int) (yPos + height), 0xFF00FF00);
        guiGraphics.drawString(Minecraft.getInstance().font, "FPS: " + Minecraft.getInstance().getFps(),
                (int) xPos + 5, (int) yPos + 5, 0xFFFFFF);
    }
}