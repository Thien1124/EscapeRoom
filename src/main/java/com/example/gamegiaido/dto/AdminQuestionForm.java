package com.example.gamegiaido.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AdminQuestionForm {

    @NotNull(message = "Vui lòng chọn phòng")
    private Long roomId;

    @NotBlank(message = "Nội dung câu hỏi không được để trống")
    @Size(max = 255, message = "Câu hỏi tối đa 255 ký tự")
    private String questionText;

    @Size(max = 255, message = "Manh mối tối đa 255 ký tự")
    private String clueText;

    @Size(max = 40, message = "Mã mở khóa tối đa 40 ký tự")
    private String answerCode;

    @Size(max = 255, message = "Đáp án A tối đa 255 ký tự")
    private String optionA;

    @Size(max = 255, message = "Đáp án B tối đa 255 ký tự")
    private String optionB;

    @Size(max = 255, message = "Đáp án C tối đa 255 ký tự")
    private String optionC;

    @Size(max = 255, message = "Đáp án D tối đa 255 ký tự")
    private String optionD;

    @Pattern(regexp = "|[ABCD]", message = "Đáp án đúng phải là A/B/C/D")
    private String correctOption;

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getClueText() {
        return clueText;
    }

    public void setClueText(String clueText) {
        this.clueText = clueText;
    }

    public String getAnswerCode() {
        return answerCode;
    }

    public void setAnswerCode(String answerCode) {
        this.answerCode = answerCode;
    }

    public String getOptionA() {
        return optionA;
    }

    public void setOptionA(String optionA) {
        this.optionA = optionA;
    }

    public String getOptionB() {
        return optionB;
    }

    public void setOptionB(String optionB) {
        this.optionB = optionB;
    }

    public String getOptionC() {
        return optionC;
    }

    public void setOptionC(String optionC) {
        this.optionC = optionC;
    }

    public String getOptionD() {
        return optionD;
    }

    public void setOptionD(String optionD) {
        this.optionD = optionD;
    }

    public String getCorrectOption() {
        return correctOption;
    }

    public void setCorrectOption(String correctOption) {
        this.correctOption = correctOption;
    }
}
