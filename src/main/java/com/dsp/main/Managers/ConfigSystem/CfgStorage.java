package com.dsp.main.Managers.ConfigSystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Вспомогательный класс для хранения структуры конфига
public class CfgStorage {
    // Список модулей в конфиге
    public List<ModuleConfig> modules = new ArrayList<>();

    // Структура для модуля
    public static class ModuleConfig {
        public String name;
        public boolean enabled;
        public int keyCode;
        public List<SettingConfig> settings = new ArrayList<>();
    }

    // Структура для настройки
    public static class SettingConfig {
        public String name;
        public String type; // Тип настройки (Mode, Slider, Input, MultiCheckBox, BindCheckBox, CheckBox)
        public Object value; // Значение настройки (зависит от типа)
    }
}