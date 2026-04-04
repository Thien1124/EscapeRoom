package com.example.gamegiaido.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class RedeemRewardForm {

    @NotNull(message = "Vui lòng nhập số điểm cần đổi")
    @Min(value = 1, message = "Điểm đổi phải lớn hơn 0")
    private Integer points;

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }
}
