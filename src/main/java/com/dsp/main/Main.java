package com.dsp.main;

import com.dsp.main.Managers.ChatManager.ChatManager;
import com.dsp.main.UI.Themes.ThemesUtil;
import com.dsp.main.Utils.Font.msdf.MsdfFont;
import net.neoforged.bus.EventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

@Mod(Main.MODID)
public class Main {
    private final ThemesUtil ThemeApi = new ThemesUtil();
    public static boolean isDetect = false;
    public static EventBus EVENT_BUS = (EventBus) NeoForge.EVENT_BUS;
    public static final String MODID = "dsp";
    public static Supplier<MsdfFont> BIKO_FONT = Suppliers.memoize(() -> MsdfFont.builder().atlas("biko").data("biko").build());
    public static Supplier<MsdfFont> ICONS = Suppliers.memoize(() -> MsdfFont.builder().atlas("atlas").data("atlas").build());
    public Main() {
        Api.Initialize();
        ThemeApi.init();
        EVENT_BUS.register(new Api());
        EVENT_BUS.register((new ChatManager()));
    }
}
