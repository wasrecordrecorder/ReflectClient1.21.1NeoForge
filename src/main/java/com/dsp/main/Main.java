package com.dsp.main;

import com.dsp.main.Core.ChatManager.ChatManager;
import com.dsp.main.Core.Discord.DiscordRPC;
import com.dsp.main.Core.Other.FreeLook;
import com.dsp.main.Core.Sound.SoundRegister;
import com.dsp.main.UI.Draggable.DragElements.*;
import com.dsp.main.UI.Draggable.DragManager;
import com.dsp.main.UI.Themes.ThemesUtil;
import com.dsp.main.Utils.AI.AssetLoad;
import com.dsp.main.Utils.Engine.Particle.EngineSetup;
import com.dsp.main.Utils.Font.msdf.MsdfFont;
import com.dsp.main.Utils.Minecraft.Client.AutoEatUtil;
import net.neoforged.bus.EventBus;
import net.neoforged.bus.api.IEventBus;
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

    public Main(IEventBus BusRegister) {
        // Client Init
        ThemeApi.init();
        Api.Initialize();
        DragManager.addDraggable(new WaterMark("WaterMark", 5, 5, false));
        DragManager.addDraggable(new PlayerInfo("PlayerInfo", 5, -1, false));
        DragManager.addDraggable(new Keybinds("Keybinds", 100, 100, true));
        DragManager.addDraggable(new Potions("Potions", 100, 120, true));
        DragManager.addDraggable(new Cooldowns("Cooldowns", 100, 140, true));
        DragManager.addDraggable(new TargetHud("TargetHud", 100 ,160, true));
        DragManager.addDraggable(new StaffList("StaffList", 100 ,180, true));
        DragManager.addDraggable(new InventoryHud("InventoryHud", 100, 200, true));
        DragManager.init();
        DiscordRPC.startDiscordRPC();
        AssetLoad.LoadAsset();
        // Utils event Bus
        SoundRegister.SOUND_EVENTS.register(BusRegister);
        EVENT_BUS.register(new Api());
        EVENT_BUS.register(new AutoEatUtil());
        EVENT_BUS.register(new ChatManager());
        EVENT_BUS.register(new FreeLook());
    }
}