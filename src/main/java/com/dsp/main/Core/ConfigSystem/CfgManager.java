package com.dsp.main.Core.ConfigSystem;

import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.*;
import com.dsp.main.UI.Draggable.DragManager;
import com.dsp.main.UI.Themes.ThemesUtil; // Added import for ThemesUtil
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.dsp.main.Api;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CfgManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final Path CONFIG_DIR = Paths.get(System.getenv("APPDATA"), "Some", "canfigutatia");
    private static final List<Module> modules = Api.Functions;
    private static final ThemesUtil themesUtil = new ThemesUtil();

    static {
        try {
            Files.createDirectories(CONFIG_DIR);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveCfg(String name) {
        CfgStorage storage = new CfgStorage();
        storage.currentTheme = ThemesUtil.getCurrentStyle() != null ? ThemesUtil.getCurrentStyle().getName() : "Light";

        for (Module module : modules) {
            CfgStorage.ModuleConfig moduleConfig = new CfgStorage.ModuleConfig();
            moduleConfig.name = module.getName();
            moduleConfig.enabled = module.isEnabled();
            moduleConfig.keyCode = module.getKeyCode();
            for (Setting setting : module.getSettings()) {
                CfgStorage.SettingConfig settingConfig = new CfgStorage.SettingConfig();
                settingConfig.name = setting.getName();

                switch (setting) {
                    case Mode mode -> {
                        settingConfig.type = "Mode";
                        settingConfig.value = mode.getMode();
                    }
                    case Slider slider -> {
                        settingConfig.type = "Slider";
                        settingConfig.value = slider.getValue();
                    }
                    case Input input -> {
                        settingConfig.type = "Input";
                        settingConfig.value = input.getValue();
                    }
                    case MultiCheckBox multiCheckBox -> {
                        settingConfig.type = "MultiCheckBox";
                        List<Map<String, Boolean>> options = new ArrayList<>();
                        for (CheckBox option : multiCheckBox.getOptions()) {
                            Map<String, Boolean> optionMap = Map.of(option.getName(), option.isEnabled());
                            options.add(optionMap);
                        }
                        settingConfig.value = options;
                    }
                    case BindCheckBox bindCheckBox -> {
                        settingConfig.type = "BindCheckBox";
                        settingConfig.value = bindCheckBox.getBindKey();
                    }
                    case CheckBox checkBox -> {
                        settingConfig.type = "CheckBox";
                        settingConfig.value = checkBox.isEnabled();
                    }
                    default -> {
                    }
                }

                moduleConfig.settings.add(settingConfig);
            }

            storage.modules.add(moduleConfig);
        }
        Path configPath = CONFIG_DIR.resolve(name + ".rfcl");
        try (Writer writer = new FileWriter(configPath.toFile())) {
            GSON.toJson(storage, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        DragManager.save();
    }

    public static void loadCfg(String name) {
        Path configPath = CONFIG_DIR.resolve(name + ".rfcl");
        if (!Files.exists(configPath)) {
            System.out.println("Config file not found: " + configPath);
            return;
        }
        CfgStorage storage;
        try (Reader reader = new FileReader(configPath.toFile())) {
            storage = GSON.fromJson(reader, CfgStorage.class);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        if (storage.currentTheme != null) {
            themesUtil.setCurrentThemeByName(storage.currentTheme);
        }

        for (CfgStorage.ModuleConfig moduleConfig : storage.modules) {
            for (Module module : modules) {
                if (module.getName().equals(moduleConfig.name)) {
                    module.setToggled(moduleConfig.enabled);
                    module.setKeyCode(moduleConfig.keyCode);
                    for (CfgStorage.SettingConfig settingConfig : moduleConfig.settings) {
                        for (Setting setting : module.getSettings()) {
                            if (setting.getName().equals(settingConfig.name)) {
                                switch (settingConfig.type) {
                                    case "Mode":
                                        if (setting instanceof Mode) {
                                            ((Mode) setting).setMode((String) settingConfig.value);
                                        }
                                        break;
                                    case "Slider":
                                        if (setting instanceof Slider) {
                                            ((Slider) setting).setValue(((Number) settingConfig.value).doubleValue());
                                        }
                                        break;
                                    case "Input":
                                        if (setting instanceof Input) {
                                            ((Input) setting).setValue((String) settingConfig.value);
                                        }
                                        break;
                                    case "MultiCheckBox":
                                        if (setting instanceof MultiCheckBox) {
                                            List<Map<String, Boolean>> options = (List<Map<String, Boolean>>) settingConfig.value;
                                            for (Map<String, Boolean> option : options) {
                                                for (Map.Entry<String, Boolean> entry : option.entrySet()) {
                                                    ((MultiCheckBox) setting).setOptionEnabled(entry.getKey(), entry.getValue());
                                                }
                                            }
                                        }
                                        break;
                                    case "BindCheckBox":
                                        if (setting instanceof BindCheckBox) {
                                            ((BindCheckBox) setting).setBindKey(((Number) settingConfig.value).intValue());
                                        }
                                        break;
                                    case "CheckBox":
                                        if (setting instanceof CheckBox) {
                                            ((CheckBox) setting).setEnabled((Boolean) settingConfig.value);
                                        }
                                        break;
                                }
                            }
                        }
                    }
                    break;
                }
            }
        }
    }
    public static void cfgDelete(String name) {
        Path configPath = CONFIG_DIR.resolve(name + ".rfcl");
        try {
            Files.deleteIfExists(configPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static List<String> cfgList() {
        List<String> list = new ArrayList<>();
        try {
            Files.list(CONFIG_DIR)
                    .filter(p -> p.toString().endsWith(".rfcl"))
                    .map(p -> p.getFileName().toString().replace(".rfcl", ""))
                    .forEach(list::add);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }
}