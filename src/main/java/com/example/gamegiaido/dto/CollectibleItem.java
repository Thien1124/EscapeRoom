package com.example.gamegiaido.dto;

public class CollectibleItem {

    private final String key;
    private final String name;
    private final String icon;

    public CollectibleItem(String key, String name, String icon) {
        this.key = key;
        this.name = name;
        this.icon = icon;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getIcon() {
        return icon;
    }
}
