package com.example.gamegiaido.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateProfileForm {

    @NotBlank(message = "Tên hiển thị không được để trống")
    @Size(min = 2, max = 80, message = "Tên hiển thị từ 2 đến 80 ký tự")
    private String displayName;

    @Size(max = 500, message = "Đường dẫn ảnh tối đa 500 ký tự")
    private String avatarUrl;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
