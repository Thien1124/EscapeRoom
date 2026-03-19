package com.example.gamegiaido.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "room_key_configs")
public class RoomKeyConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private GameRoom room;

    @Column(name = "key_code", nullable = false, length = 80)
    private String keyCode;

    @Column(name = "key_name", nullable = false, length = 120)
    private String keyName;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "spot_x", nullable = false)
    private Integer spotX;

    @Column(name = "spot_y", nullable = false)
    private Integer spotY;

    public Long getId() {
        return id;
    }

    public GameRoom getRoom() {
        return room;
    }

    public void setRoom(GameRoom room) {
        this.room = room;
    }

    public String getKeyCode() {
        return keyCode;
    }

    public void setKeyCode(String keyCode) {
        this.keyCode = keyCode;
    }

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getSpotX() {
        return spotX;
    }

    public void setSpotX(Integer spotX) {
        this.spotX = spotX;
    }

    public Integer getSpotY() {
        return spotY;
    }

    public void setSpotY(Integer spotY) {
        this.spotY = spotY;
    }
}
