package com.example.gamegiaido.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "player_room_progresses",
        uniqueConstraints = @UniqueConstraint(name = "uk_player_room", columnNames = {"player_id", "room_id"}))
public class PlayerRoomProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private PlayerProfile player;

    @ManyToOne(optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private GameRoom room;

    @Column(nullable = false)
    private Integer currentStep = 1;

    @Column(nullable = false)
    private Boolean completed = false;

    @Column(nullable = false)
    private Integer score = 0;

    @Column(nullable = false)
    private Integer wrongAttempts = 0;

    @Column
    private Boolean won;

    @Column(nullable = false)
    private Boolean resultFinalized = false;

    @Column(nullable = false, length = 600)
    private String collectedItems = "";

    @Column(nullable = false, length = 1200)
    private String discoveredClues = "";

    public Long getId() {
        return id;
    }

    public PlayerProfile getPlayer() {
        return player;
    }

    public void setPlayer(PlayerProfile player) {
        this.player = player;
    }

    public GameRoom getRoom() {
        return room;
    }

    public void setRoom(GameRoom room) {
        this.room = room;
    }

    public Integer getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(Integer currentStep) {
        this.currentStep = currentStep;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getWrongAttempts() {
        return wrongAttempts;
    }

    public void setWrongAttempts(Integer wrongAttempts) {
        this.wrongAttempts = wrongAttempts;
    }

    public Boolean getWon() {
        return won;
    }

    public void setWon(Boolean won) {
        this.won = won;
    }

    public Boolean getResultFinalized() {
        return resultFinalized;
    }

    public void setResultFinalized(Boolean resultFinalized) {
        this.resultFinalized = resultFinalized;
    }

    public String getCollectedItems() {
        return collectedItems;
    }

    public void setCollectedItems(String collectedItems) {
        this.collectedItems = collectedItems;
    }

    public String getDiscoveredClues() {
        return discoveredClues;
    }

    public void setDiscoveredClues(String discoveredClues) {
        this.discoveredClues = discoveredClues;
    }
}
