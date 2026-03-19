package com.example.gamegiaido.dto;

import com.example.gamegiaido.model.GameRoom;

public class RoomMapItem {

    private final GameRoom room;
    private final boolean unlocked;
    private final boolean completed;

    public RoomMapItem(GameRoom room, boolean unlocked, boolean completed) {
        this.room = room;
        this.unlocked = unlocked;
        this.completed = completed;
    }

    public GameRoom getRoom() {
        return room;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public boolean isCompleted() {
        return completed;
    }
}
