package com.example.gamegiaido.dto;

public class CollectibleSpot {

    private final String key;
    private final String keyName;
    private final int x;
    private final int y;
    private final String imageUrl;
    private final boolean collected;

    public CollectibleSpot(String key, String keyName, int x, int y, String imageUrl, boolean collected) {
        this.key = key;
        this.keyName = keyName;
        this.x = x;
        this.y = y;
        this.imageUrl = imageUrl;
        this.collected = collected;
    }

    public String getKey() {
        return key;
    }

    public String getKeyName() {
        return keyName;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public boolean isCollected() {
        return collected;
    }
}
