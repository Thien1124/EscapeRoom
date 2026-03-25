package com.example.gamegiaido.dto;

public class LeaderboardEntry {

    private final Long playerId;
    private final String displayName;
    private final String avatarUrl;
    private final Integer totalScore;
    private final Integer totalWin;
    private final Double avgClicksPerWin;

    public LeaderboardEntry(Long playerId,
                            String displayName,
                            String avatarUrl,
                            Integer totalScore,
                            Integer totalWin,
                            Double avgClicksPerWin) {
        this.playerId = playerId;
        this.displayName = displayName;
        this.avatarUrl = avatarUrl;
        this.totalScore = totalScore;
        this.totalWin = totalWin;
        this.avgClicksPerWin = avgClicksPerWin;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public Integer getTotalScore() {
        return totalScore;
    }

    public Integer getTotalWin() {
        return totalWin;
    }

    public Double getAvgClicksPerWin() {
        return avgClicksPerWin;
    }

    public int getRoundedAvgClicksPerWin() {
        if (avgClicksPerWin == null || avgClicksPerWin >= 999999d) {
            return 0;
        }
        return (int) Math.round(avgClicksPerWin);
    }
}
