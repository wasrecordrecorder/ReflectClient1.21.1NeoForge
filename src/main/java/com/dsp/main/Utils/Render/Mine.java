package com.dsp.main.Utils.Render;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;

public interface Mine {

    Tesselator tessellator = Tesselator.getInstance();

    Minecraft mc = Minecraft.getInstance();
    Window sr = mc.getWindow();
    Font fr = mc.font;

}
