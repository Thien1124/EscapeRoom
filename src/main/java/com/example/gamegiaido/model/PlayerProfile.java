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
@Table(name = "player_profiles")
public class PlayerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String displayName;

    @Column(nullable = false)
    private Integer totalScore = 0;

    @Column(nullable = false)
    private Integer rewardPoints = 0;

    @Column(nullable = false)
    private Boolean rewardWalletInitialized = true;

    @Column(nullable = false, length = 40)
    private String selectedCharacterIcon = "agent_default";

    @Column(nullable = false, length = 600)
    private String ownedCharacterIcons = "agent_default";

    @Column(nullable = false)
    private Integer totalWin = 0;

    @Column(length = 500)
    private String avatarUrl;

    @OneToOne(optional = false)
    @JoinColumn(name = "account_id", nullable = false, unique = true)
    private UserAccount account;

    public Long getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Integer getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }

    public Integer getRewardPoints() {
        return rewardPoints;
    }

    public void setRewardPoints(Integer rewardPoints) {
        this.rewardPoints = rewardPoints;
    }

    public Integer getTotalWin() {
        return totalWin;
    }

    public void setTotalWin(Integer totalWin) {
        this.totalWin = totalWin;
    }

    public Boolean getRewardWalletInitialized() {
        return rewardWalletInitialized;
    }

    public void setRewardWalletInitialized(Boolean rewardWalletInitialized) {
        this.rewardWalletInitialized = rewardWalletInitialized;
    }

    public String getSelectedCharacterIcon() {
        return selectedCharacterIcon;
    }

    public void setSelectedCharacterIcon(String selectedCharacterIcon) {
        this.selectedCharacterIcon = selectedCharacterIcon;
    }

    public String getOwnedCharacterIcons() {
        return ownedCharacterIcons;
    }

    public void setOwnedCharacterIcons(String ownedCharacterIcons) {
        this.ownedCharacterIcons = ownedCharacterIcons;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public UserAccount getAccount() {
        return account;
    }

    public void setAccount(UserAccount account) {
        this.account = account;
    }
}
