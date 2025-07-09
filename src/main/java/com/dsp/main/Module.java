package com.dsp.main;

import com.dsp.main.UI.ClickGui.Settings.Setting;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static com.dsp.main.Api.mc;
import static com.dsp.main.Api.Functions;
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
            if (Api.isEnabled("ClientSounds")) {
                if (mc.player != null) {
//                    if (togglSound.isMode("1")) {
//                        mc.player.playSound(SoundsUtil.Enable, volume.getValueFloat(), pitch.getValueFloat());
//                    } else {
//                        mc.player.playSound(SoundsUtil.On, volume.getValueFloat(), pitch.getValueFloat());
//                    }
                }
            }
            //NotificationManager.show(new Notification(NotificationType.INFO, getName() + " Был включен!", 1));
        } else {
            onDisable();


            if (Api.isEnabled("ClientSounds")) {
//                if (mc.player != null) {
//                    if (togglSound.isMode("1")) {
//                       mc.player.playSound(SoundsUtil.Disable, volume.getValueFloat() + 10, pitch.getValueFloat());
//                    } else {
//                        mc.player.playSound(SoundsUtil.Off, volume.getValueFloat() + 10, pitch.getValueFloat());
//                    }
//                }
            }
//            NotificationManager.show(new Notification(NotificationType.ERROR,  getName() + " Был выключен!", 1));
        }
    }

    public void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
    }

    public void setToggled(boolean toggled) {
        if (isDetect && toggled) {
//            NotificationManager.show(new Notification(NotificationType.ERROR,  "Невозможно включить модуль!", 2));
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
//        NotificationManager.show(new Notification(NotificationType.WARNING,  "Режим проверки активирован!", 2));
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
