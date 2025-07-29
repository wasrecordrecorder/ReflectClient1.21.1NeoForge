package com.dsp.main.Mixin.Accesors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(net.minecraft.client.MouseHandler.class)
public interface MouseHandlerAccessor {
    @Invoker("onPress")
    void invokeOnPress(long window, int button, int action, int mods);
}