package com.dsp.main.Managers.ConfigSystem;

import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Settings.*;
import com.dsp.main.UI.Draggable.DragManager;
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
    private static final Path CONFIG_DIR = Paths.get(System.getenv("APPDATA"), "Some", "canfigutatia");
    private static final List<Module> modules = Api.modules;

    // Инициализация: создание директории, если не существует
    static {
        try {
            Files.createDirectories(CONFIG_DIR);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Сохранение конфига
    public static void saveCfg(String name) {
        CfgStorage storage = new CfgStorage();
        for (Module module : modules) {
            CfgStorage.ModuleConfig moduleConfig = new CfgStorage.ModuleConfig();
            moduleConfig.name = module.getName();
            moduleConfig.enabled = module.isEnabled();
            moduleConfig.keyCode = module.getKeyCode();
            for (Setting setting : module.getSettings()) {
                CfgStorage.SettingConfig settingConfig = new CfgStorage.SettingConfig();
                settingConfig.name = setting.getName();

                if (setting instanceof Mode) {
                    Mode mode = (Mode) setting;
                    settingConfig.type = "Mode";
                    settingConfig.value = mode.getMode();
                } else if (setting instanceof Slider) {
                    Slider slider = (Slider) setting;
                    settingConfig.type = "Slider";
                    settingConfig.value = slider.getValue();
                } else if (setting instanceof Input) {
                    Input input = (Input) setting;
                    settingConfig.type = "Input";
                    settingConfig.value = input.getValue();
                } else if (setting instanceof MultiCheckBox) {
                    MultiCheckBox multiCheckBox = (MultiCheckBox) setting;
                    settingConfig.type = "MultiCheckBox";
                    List<Map<String, Boolean>> options = new ArrayList<>();
                    for (CheckBox option : multiCheckBox.getOptions()) {
                        Map<String, Boolean> optionMap = Map.of(option.getName(), option.isEnabled());
                        options.add(optionMap);
                    }
                    settingConfig.value = options;
                } else if (setting instanceof BindCheckBox) {
                    BindCheckBox bindCheckBox = (BindCheckBox) setting;
                    settingConfig.type = "BindCheckBox";
                    settingConfig.value = bindCheckBox.getBindKey();
                } else if (setting instanceof CheckBox) {
                    CheckBox checkBox = (CheckBox) setting;
                    settingConfig.type = "CheckBox";
                    settingConfig.value = checkBox.isEnabled();
                }

                moduleConfig.settings.add(settingConfig);
            }

            storage.modules.add(moduleConfig);
        }
        Path configPath = CONFIG_DIR.resolve(name + ".json");
        try (Writer writer = new FileWriter(configPath.toFile())) {
            GSON.toJson(storage, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        DragManager.save();
    }
    public static void loadCfg(String name) {
        Path configPath = CONFIG_DIR.resolve(name + ".json");
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
}