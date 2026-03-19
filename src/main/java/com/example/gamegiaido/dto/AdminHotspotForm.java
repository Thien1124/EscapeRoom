package com.example.gamegiaido.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class AdminHotspotForm {

    @NotNull(message = "Vui lòng chọn phòng")
    private Long roomId;

    @NotNull(message = "hotspot X bước 1 không được để trống")
    @Min(value = 0, message = "X bước 1 phải từ 0 đến 100")
    @Max(value = 100, message = "X bước 1 phải từ 0 đến 100")
    private Integer step1HotspotX;

    @NotNull(message = "hotspot Y bước 1 không được để trống")
    @Min(value = 0, message = "Y bước 1 phải từ 0 đến 100")
    @Max(value = 100, message = "Y bước 1 phải từ 0 đến 100")
    private Integer step1HotspotY;

    @NotNull(message = "hotspot X bước 2 không được để trống")
    @Min(value = 0, message = "X bước 2 phải từ 0 đến 100")
    @Max(value = 100, message = "X bước 2 phải từ 0 đến 100")
    private Integer step2HotspotX;

    @NotNull(message = "hotspot Y bước 2 không được để trống")
    @Min(value = 0, message = "Y bước 2 phải từ 0 đến 100")
    @Max(value = 100, message = "Y bước 2 phải từ 0 đến 100")
    private Integer step2HotspotY;

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public Integer getStep1HotspotX() {
        return step1HotspotX;
    }

    public void setStep1HotspotX(Integer step1HotspotX) {
        this.step1HotspotX = step1HotspotX;
    }

    public Integer getStep1HotspotY() {
        return step1HotspotY;
    }

    public void setStep1HotspotY(Integer step1HotspotY) {
        this.step1HotspotY = step1HotspotY;
    }

    public Integer getStep2HotspotX() {
        return step2HotspotX;
    }

    public void setStep2HotspotX(Integer step2HotspotX) {
        this.step2HotspotX = step2HotspotX;
    }

    public Integer getStep2HotspotY() {
        return step2HotspotY;
    }

    public void setStep2HotspotY(Integer step2HotspotY) {
        this.step2HotspotY = step2HotspotY;
    }
}
