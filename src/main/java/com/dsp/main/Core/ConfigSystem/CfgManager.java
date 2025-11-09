package com.dsp.main.Core.ConfigSystem;

import com.dsp.main.Module;
import com.dsp.main.UI.ClickGui.Dropdown.ClickGuiScreen;
import com.dsp.main.UI.ClickGui.Dropdown.Settings.*;
import com.dsp.main.UI.Draggable.DragManager;
import com.dsp.main.UI.Themes.ThemesUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.dsp.main.Api;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.dsp.main.Api.getCustomConfigDir;

public class CfgManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final Path CONFIG_DIR = getCustomConfigDir().resolve("canfiguratia");
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
        if (name == null || name.isEmpty()) return;

        try {
            CfgStorage storage = new CfgStorage();
            storage.currentTheme = ThemesUtil.getCurrentStyle() != null ? ThemesUtil.getCurrentStyle().getName() : "Light";
            storage.guiScale = ClickGuiScreen.getUserScaleMultiplier();

            for (Module module : modules) {
                if (module == null) continue;

                CfgStorage.ModuleConfig moduleConfig = new CfgStorage.ModuleConfig();
                moduleConfig.name = module.getName();
                moduleConfig.enabled = module.isEnabled();
                moduleConfig.keyCode = module.getKeyCode();

                for (Setting setting : module.getSettings()) {
                    if (setting == null) continue;

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
                                if (option != null) {
                                    options.add(Map.of(option.getName(), option.isEnabled()));
                                }
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
                        case BlockListSetting blockListSetting -> {
                            settingConfig.type = "BlockListSetting";
                            settingConfig.value = new ArrayList<>(blockListSetting.getSelectedBlockNames());
                        }
                        case ItemListSetting itemListSetting -> {
                            settingConfig.type = "ItemListSetting";
                            settingConfig.value = new ArrayList<>(itemListSetting.getSelectedItemNames());
                        }
                        case ButtonSetting buttonSetting -> {
                            continue;
                        }
                        default -> {
                            continue;
                        }
                    }

                    moduleConfig.settings.add(settingConfig);
                }

                storage.modules.add(moduleConfig);
            }

            Path configPath = CONFIG_DIR.resolve(name + ".rfcl");
            try (Writer writer = new FileWriter(configPath.toFile())) {
                GSON.toJson(storage, writer);
            }

            DragManager.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadCfg(String name) {
        if (name == null || name.isEmpty()) return;

        Path configPath = CONFIG_DIR.resolve(name + ".rfcl");
        if (!Files.exists(configPath)) {
            System.out.println("Config file not found: " + configPath);
            return;
        }

        try {
            CfgStorage storage;
            try (Reader reader = new FileReader(configPath.toFile())) {
                storage = GSON.fromJson(reader, CfgStorage.class);
            }

            if (storage == null) return;

            if (storage.currentTheme != null) {
                themesUtil.setCurrentThemeByName(storage.currentTheme);
            }

            ClickGuiScreen.setUserScaleMultiplier(storage.guiScale);

            if (storage.modules == null) return;

            for (CfgStorage.ModuleConfig moduleConfig : storage.modules) {
                if (moduleConfig == null || moduleConfig.name == null) continue;

                for (Module module : modules) {
                    if (module == null) continue;

                    if (module.getName().equals(moduleConfig.name)) {
                        module.setToggled(moduleConfig.enabled);
                        module.setKeyCode(moduleConfig.keyCode);

                        if (moduleConfig.settings == null) break;

                        for (CfgStorage.SettingConfig settingConfig : moduleConfig.settings) {
                            if (settingConfig == null || settingConfig.name == null) continue;

                            for (Setting setting : module.getSettings()) {
                                if (setting == null) continue;

                                if (setting.getName().equals(settingConfig.name)) {
                                    try {
                                        switch (settingConfig.type) {
                                            case "Mode":
                                                if (setting instanceof Mode && settingConfig.value instanceof String) {
                                                    ((Mode) setting).setMode((String) settingConfig.value);
                                                }
                                                break;
                                            case "Slider":
                                                if (setting instanceof Slider && settingConfig.value instanceof Number) {
                                                    ((Slider) setting).setValue(((Number) settingConfig.value).doubleValue());
                                                }
                                                break;
                                            case "Input":
                                                if (setting instanceof Input && settingConfig.value instanceof String) {
                                                    ((Input) setting).setValue((String) settingConfig.value);
                                                }
                                                break;
                                            case "MultiCheckBox":
                                                if (setting instanceof MultiCheckBox && settingConfig.value instanceof List) {
                                                    List<Map<String, Boolean>> options = (List<Map<String, Boolean>>) settingConfig.value;
                                                    for (Map<String, Boolean> option : options) {
                                                        if (option == null) continue;
                                                        for (Map.Entry<String, Boolean> entry : option.entrySet()) {
                                                            if (entry != null && entry.getKey() != null && entry.getValue() != null) {
                                                                ((MultiCheckBox) setting).setOptionEnabled(entry.getKey(), entry.getValue());
                                                            }
                                                        }
                                                    }
                                                }
                                                break;
                                            case "BindCheckBox":
                                                if (setting instanceof BindCheckBox && settingConfig.value instanceof Number) {
                                                    ((BindCheckBox) setting).setBindKey(((Number) settingConfig.value).intValue());
                                                }
                                                break;
                                            case "CheckBox":
                                                if (setting instanceof CheckBox && settingConfig.value instanceof Boolean) {
                                                    ((CheckBox) setting).setEnabled((Boolean) settingConfig.value);
                                                }
                                                break;
                                            case "BlockListSetting":
                                                if (setting instanceof BlockListSetting && settingConfig.value instanceof List) {
                                                    List<?> rawList = (List<?>) settingConfig.value;
                                                    Set<String> blockNames = new java.util.HashSet<>();
                                                    for (Object obj : rawList) {
                                                        if (obj instanceof String) {
                                                            blockNames.add((String) obj);
                                                        }
                                                    }
                                                    ((BlockListSetting) setting).setSelectedBlocks(blockNames);
                                                }
                                                break;
                                            case "ItemListSetting":
                                                if (setting instanceof ItemListSetting && settingConfig.value instanceof List) {
                                                    List<?> rawList = (List<?>) settingConfig.value;
                                                    Set<String> itemNames = new java.util.HashSet<>();
                                                    for (Object obj : rawList) {
                                                        if (obj instanceof String) {
                                                            itemNames.add((String) obj);
                                                        }
                                                    }
                                                    ((ItemListSetting) setting).setSelectedItems(itemNames);
                                                }
                                                break;
                                        }
                                    } catch (Exception ignored) {}
                                }
                            }
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void cfgDelete(String name) {
        if (name == null || name.isEmpty()) return;

        try {
            Path configPath = CONFIG_DIR.resolve(name + ".rfcl");
            Files.deleteIfExists(configPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<String> cfgList() {
        List<String> list = new ArrayList<>();
        try {
            if (!Files.exists(CONFIG_DIR)) {
                Files.createDirectories(CONFIG_DIR);
                return list;
            }

            Files.list(CONFIG_DIR)
                    .filter(p -> p != null && p.toString().endsWith(".rfcl"))
                    .map(p -> p.getFileName().toString().replace(".rfcl", ""))
                    .forEach(list::add);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}