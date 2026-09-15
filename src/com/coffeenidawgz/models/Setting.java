package com.coffeenidawgz.models;

public class Setting {
    private int id;
    private String settingKey;
    private String settingValue;

    public Setting() {}

    public Setting(int id, String settingKey, String settingValue) {
        this.id = id;
        this.settingKey = settingKey;
        this.settingValue = settingValue;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getSettingKey() { return settingKey; }
    public void setSettingKey(String settingKey) { this.settingKey = settingKey; }

    public String getSettingValue() { return settingValue; }
    public void setSettingValue(String settingValue) { this.settingValue = settingValue; }
}
