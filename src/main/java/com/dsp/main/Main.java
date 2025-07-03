package com.dsp.main;

import com.dsp.main.Managers.ChatManager.ChatManager;
import com.dsp.main.Managers.FrndSys.FriendManager;
import com.dsp.main.Utils.Font.CustomFontRenderer;
import com.dsp.main.Utils.Font.FontRenderers;
import net.neoforged.bus.EventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

import static com.dsp.main.Api.mc;
import static com.dsp.main.Utils.Minecraft.UserSession.UserSessionUtil.setNameSession;

@Mod(Main.MODID)
public class Main {
    public static final CustomFontRenderer CustomFont = new CustomFontRenderer();
    private static FontRenderers fontRenderers;
    public static boolean isDetect = false;
    public static EventBus EVENT_BUS = (EventBus) NeoForge.EVENT_BUS;
    public static final String MODID = "dsp";
    public Main() {
        Api.Initialize();
        EVENT_BUS.register(new Api());
        EVENT_BUS.register((new ChatManager()));
        fontRenderers = new FontRenderers();
        fontRenderers.init();
    }
}
