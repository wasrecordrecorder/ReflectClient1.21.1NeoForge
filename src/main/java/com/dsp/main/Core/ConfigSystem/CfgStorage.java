package com.dsp.main.Core.ConfigSystem;

import java.util.ArrayList;
import java.util.List;

public class CfgStorage {
    public List<ModuleConfig> modules = new ArrayList<>();
    public String currentTheme;
    public float guiScale = 1.0f;

    public static class ModuleConfig {
        public String name;
        public boolean enabled;
        public int keyCode;
        public List<SettingConfig> settings = new ArrayList<>();
    }

    public static class SettingConfig {
        public String name;
        public String type;
        public Object value;
    }
}