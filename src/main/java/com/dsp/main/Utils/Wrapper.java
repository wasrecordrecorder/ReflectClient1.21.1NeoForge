package com.dsp.main.Utils;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;

public interface Wrapper {
	Minecraft MC = Minecraft.getInstance();
	Window WINDOW = Minecraft.getInstance().getWindow();
}
