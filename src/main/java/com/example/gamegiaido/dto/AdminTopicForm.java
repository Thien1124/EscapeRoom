package com.example.gamegiaido.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AdminTopicForm {

    @NotBlank(message = "Tên chủ đề không được để trống")
    @Size(max = 100, message = "Tên chủ đề tối đa 100 ký tự")
    private String name;

    @NotBlank(message = "Mô tả không được để trống")
    @Size(max = 255, message = "Mô tả tối đa 255 ký tự")
    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
