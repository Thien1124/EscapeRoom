package com.example.gamegiaido.dto;

import com.example.gamegiaido.model.GameMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class AdminRoomForm {

    @NotNull(message = "Vui lòng chọn chủ đề")
    private Long topicId;

    @NotBlank(message = "Tên phòng không được để trống")
    @Size(max = 120, message = "Tên phòng tối đa 120 ký tự")
    private String name;

    @NotBlank(message = "Mô tả không được để trống")
    @Size(max = 255, message = "Mô tả tối đa 255 ký tự")
    private String description;

    @NotNull(message = "Vui lòng chọn chế độ")
    private GameMode mode;

    @NotNull(message = "Thứ tự phòng không được để trống")
    @Positive(message = "Thứ tự phòng phải > 0")
    private Integer roomOrder;

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

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

    public GameMode getMode() {
        return mode;
    }

    public void setMode(GameMode mode) {
        this.mode = mode;
    }

    public Integer getRoomOrder() {
        return roomOrder;
    }

    public void setRoomOrder(Integer roomOrder) {
        this.roomOrder = roomOrder;
    }
}
