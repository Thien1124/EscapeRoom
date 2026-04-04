package com.example.gamegiaido.dto;

public class CharacterIconOption {

    private final String key;
    private final String name;
    private final String iconClass;
    private final int cost;
    private final int difficultyLevel;
    private final boolean owned;
    private final boolean selected;

    public CharacterIconOption(String key,
                               String name,
                               String iconClass,
                               int cost,
                               int difficultyLevel,
                               boolean owned,
                               boolean selected) {
        this.key = key;
        this.name = name;
        this.iconClass = iconClass;
        this.cost = cost;
        this.difficultyLevel = difficultyLevel;
        this.owned = owned;
        this.selected = selected;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getIconClass() {
        return iconClass;
    }

    public int getCost() {
        return cost;
    }

    public int getDifficultyLevel() {
        return difficultyLevel;
    }

    public boolean isOwned() {
        return owned;
    }

    public boolean isSelected() {
        return selected;
    }
}
