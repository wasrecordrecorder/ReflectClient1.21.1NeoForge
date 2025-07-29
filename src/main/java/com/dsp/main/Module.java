package com.dsp.main;

import com.dsp.main.Core.Sound.SoundRegister;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.Setting;
import com.dsp.main.UI.Notifications.Notification;
import com.dsp.main.UI.Notifications.NotificationManager;
import net.minecraft.sounds.SoundEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.dsp.main.Api.*;
import static com.dsp.main.Functions.Misc.ClientSetting.TogglSound;
import static com.dsp.main.Functions.Render.Notifications.Option;
import static com.dsp.main.Main.EVENT_BUS;
import static com.dsp.main.Main.isDetect;

public abstract class Module {
    public String name;
    public boolean toggled;
    public int keyCode;
    public String description;
    public Category category;
    public List<Setting> setingList = new ArrayList<>();


    public Module(String name, int key, Category category, String description) {
        this.name = name;
        this.keyCode = key;
        this.category = category;
        this.description = description;
    }

    public void addSetting(Setting seting) {
        setingList.add(seting);
    }
    public void addSettings(Setting... settings) {
        Collections.addAll(setingList, settings);
    }

    public List<Setting> getSettings() {
        return setingList;
    }

    public void onEnable() {
        EVENT_BUS.register(this);
    }

    public void onDisable() {
        EVENT_BUS.unregister(this);
    }

    public String getName() {
        return name;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public Category getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public boolean isEnabled() {
        return toggled;
    }

    public void toggle() {
        if (isDetect) {
            return;
        }

        toggled = !toggled;
        if (toggled) {
            onEnable();
            if (mc.player != null) {
                if (TogglSound.isEnabled()) mc.player.playSound(SoundRegister.ON.get(), 1, 1);
            }
            if (Option.isOptionEnabled("Functions Toggle")) notificationManager.send(Notification.Type.INFO, getName() + " was Enabled !");
        } else {
            onDisable();
            if (mc.player != null) {
                if (TogglSound.isEnabled()) mc.player.playSound(SoundRegister.OFF.get(), 1, 1);
            }
            if (Option.isOptionEnabled("Functions Toggle")) notificationManager.send(Notification.Type.INFO, getName() + " was Disabled !");
        }
    }

    public void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
    }

    public void setToggled(boolean toggled) {
        if (isDetect && toggled) {
            return;
        }

        this.toggled = toggled;
        if (this.toggled) {
            this.onEnable();
        } else {
            this.onDisable();
        }
    }

    public enum Category {
        COMBAT,
        MOVEMENT,
        RENDER,
        PLAYER,
        MISC
    }

    public static void disableAllModules() {
        notificationManager.send(Notification.Type.ERROR, "Danger mode enabled, disabling all modules");
        for (Module module : Functions) {
            if (module.isEnabled()) {
                module.setToggled(false);
            }
        }
    }
    public static List<Module> getModulesByCategory(Category category) {
        List<Module> list = new ArrayList<>();
        for (Module m : Functions) {
            if (m.getCategory() == category) {
                list.add(m);
            }
        }
        return list;
    }

}
