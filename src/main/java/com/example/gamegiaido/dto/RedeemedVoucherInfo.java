package com.example.gamegiaido.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class RedeemedVoucherInfo {

    private final String voucherName;
    private final String brand;
    private final String issuedCode;
    private final int pointsSpent;
    private final LocalDate expiresAt;
    private final LocalDateTime redeemedAt;

    public RedeemedVoucherInfo(String voucherName,
                               String brand,
                               String issuedCode,
                               int pointsSpent,
                               LocalDate expiresAt,
                               LocalDateTime redeemedAt) {
        this.voucherName = voucherName;
        this.brand = brand;
        this.issuedCode = issuedCode;
        this.pointsSpent = pointsSpent;
        this.expiresAt = expiresAt;
        this.redeemedAt = redeemedAt;
    }

    public String getVoucherName() {
        return voucherName;
    }

    public String getBrand() {
        return brand;
    }

    public String getIssuedCode() {
        return issuedCode;
    }

    public int getPointsSpent() {
        return pointsSpent;
    }

    public LocalDate getExpiresAt() {
        return expiresAt;
    }

    public LocalDateTime getRedeemedAt() {
        return redeemedAt;
    }
}
