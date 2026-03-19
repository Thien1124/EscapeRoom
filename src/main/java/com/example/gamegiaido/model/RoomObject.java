package com.example.gamegiaido.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "room_objects")
public class RoomObject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String objectName;

    @Column(nullable = false, length = 255)
    private String hint;

    @Column(name = "hotspot_x")
    private Integer hotspotX;

    @Column(name = "hotspot_y")
    private Integer hotspotY;

    @Column(nullable = false)
    private Boolean locked = true;

    @Column(nullable = false)
    private Integer requiredStep;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ObjectLockType lockType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private GameRoom room;

    public Long getId() {
        return id;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public Integer getHotspotX() {
        return hotspotX;
    }

    public void setHotspotX(Integer hotspotX) {
        this.hotspotX = hotspotX;
    }

    public Integer getHotspotY() {
        return hotspotY;
    }

    public void setHotspotY(Integer hotspotY) {
        this.hotspotY = hotspotY;
    }

    public Boolean getLocked() {
        return locked;
    }

    public void setLocked(Boolean locked) {
        this.locked = locked;
    }

    public Integer getRequiredStep() {
        return requiredStep;
    }

    public void setRequiredStep(Integer requiredStep) {
        this.requiredStep = requiredStep;
    }

    public ObjectLockType getLockType() {
        return lockType;
    }

    public void setLockType(ObjectLockType lockType) {
        this.lockType = lockType;
    }

    public GameRoom getRoom() {
        return room;
    }

    public void setRoom(GameRoom room) {
        this.room = room;
    }
}
