package com.example.gamegiaido.dto;

import com.example.gamegiaido.model.GameMode;
import jakarta.validation.constraints.NotNull;

public class StartGameForm {

    @NotNull(message = "Vui lòng chọn chủ đề")
    private Long topicId;

    @NotNull(message = "Vui lòng chọn chế độ")
    private GameMode mode;

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public GameMode getMode() {
        return mode;
    }

    public void setMode(GameMode mode) {
        this.mode = mode;
    }
}
