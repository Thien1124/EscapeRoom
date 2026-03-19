package com.example.gamegiaido.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AdminRoomKeyForm {

    @NotNull(message = "Vui lòng chọn phòng")
    private Long roomId;

    @NotBlank(message = "Mã chìa khóa không được để trống")
    @Size(max = 80, message = "Mã chìa khóa tối đa 80 ký tự")
    private String keyCode;

    @NotBlank(message = "Tên chìa khóa không được để trống")
    @Size(max = 120, message = "Tên chìa khóa tối đa 120 ký tự")
    private String keyName;

    @Size(max = 500, message = "URL ảnh tối đa 500 ký tự")
    private String imageUrl;

    @NotNull(message = "Vui lòng nhập tọa độ X")
    @Min(value = 0, message = "X phải từ 0 đến 100")
    @Max(value = 100, message = "X phải từ 0 đến 100")
    private Integer spotX;

    @NotNull(message = "Vui lòng nhập tọa độ Y")
    @Min(value = 0, message = "Y phải từ 0 đến 100")
    @Max(value = 100, message = "Y phải từ 0 đến 100")
    private Integer spotY;

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
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
