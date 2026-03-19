package com.example.gamegiaido.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "quiz_questions")
public class QuizQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "room_object_id", nullable = false, unique = true)
    private RoomObject roomObject;

    @Column(nullable = false, length = 255)
    private String questionText;

    @Column(length = 255)
    private String clueText;

    @Column(length = 40)
    private String answerCode;

    @Column(nullable = false, length = 255)
    private String optionA;

    @Column(nullable = false, length = 255)
    private String optionB;

    @Column(nullable = false, length = 255)
    private String optionC;

    @Column(nullable = false, length = 255)
    private String optionD;

    @Column(nullable = false, length = 1)
    private String correctOption;

    public Long getId() {
        return id;
    }

    public RoomObject getRoomObject() {
        return roomObject;
    }

    public void setRoomObject(RoomObject roomObject) {
        this.roomObject = roomObject;
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
