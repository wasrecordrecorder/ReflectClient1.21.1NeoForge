package com.dsp.main;

import com.dsp.main.Utils.Font.CustomFontRenderer;
import com.dsp.main.Utils.Font.FontRenderers;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.EventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.awt.*;
import java.util.HashMap;

import static com.dsp.main.Api.mc;

@Mod(Main.MODID)
public class Main {
    public static final CustomFontRenderer CustomFont = new CustomFontRenderer();
    private static FontRenderers fontRenderers;
    public static boolean isDetect = false;
    public static EventBus EVENT_BUS = (EventBus) MinecraftForge.EVENT_BUS;
    public static final String MODID = "dsp";
    public Main() {
        Api.Initialize();
        EVENT_BUS.register(new Api());
        fontRenderers = new FontRenderers();
        fontRenderers.init();
        mc.getWindow().setTitle("Different Colored Emeralds");
    }
}
