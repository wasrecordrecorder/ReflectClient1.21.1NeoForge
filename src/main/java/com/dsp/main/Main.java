package com.dsp.main;

import com.dsp.main.Managers.ChatManager.ChatManager;
import com.dsp.main.UI.Draggable.DragElements.Keybinds;
import com.dsp.main.UI.Draggable.DragElements.PlayerInfo;
import com.dsp.main.UI.Draggable.DragElements.Potions;
import com.dsp.main.UI.Draggable.DragElements.WaterMark;
import com.dsp.main.UI.Draggable.DragManager;
import com.dsp.main.UI.Themes.ThemesUtil;
import com.dsp.main.Utils.Font.msdf.MsdfFont;
import com.dsp.main.Utils.Minecraft.Client.AutoEatUtil;
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
    public static Supplier<MsdfFont> RUS = Suppliers.memoize(() -> MsdfFont.builder().atlas("rus").data("rus").build());
    public Main() {
        // Client Init
        Api.Initialize();
        ThemeApi.init();
        DragManager.init();
        DragManager.addDraggable(new WaterMark("Watermark", 5, 5, false));
        DragManager.addDraggable(new PlayerInfo("PlayerInfo", 5, -1, false));
        DragManager.addDraggable(new Keybinds("Keybinds", 100, 100, true));
        DragManager.addDraggable(new Potions("Potions", 100, 100, true));

        // Utils event Bus
        EVENT_BUS.register(new Api());
        EVENT_BUS.register(new AutoEatUtil());
        EVENT_BUS.register((new ChatManager()));
    }
}
