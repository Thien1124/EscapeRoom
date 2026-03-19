package com.example.gamegiaido.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class AdminObjectHotspotForm {

    @NotNull(message = "Vui lòng chọn vật thể")
    private Long objectId;

    @NotNull(message = "Hotspot X không được để trống")
    @Min(value = 0, message = "Hotspot X phải từ 0 đến 100")
    @Max(value = 100, message = "Hotspot X phải từ 0 đến 100")
    private Integer hotspotX;

    @NotNull(message = "Hotspot Y không được để trống")
    @Min(value = 0, message = "Hotspot Y phải từ 0 đến 100")
    @Max(value = 100, message = "Hotspot Y phải từ 0 đến 100")
    private Integer hotspotY;

    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long objectId) {
        this.objectId = objectId;
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
}
