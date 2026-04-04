package com.example.gamegiaido.dto;

import java.time.LocalDate;

public class RewardVoucherOption {

    private final Long id;
    private final String code;
    private final String name;
    private final String brand;
    private final int pointsCost;
    private final int remainingStock;
    private final LocalDate expiresAt;
    private final boolean redeemed;
    private final boolean expired;

    public RewardVoucherOption(Long id,
                               String code,
                               String name,
                               String brand,
                               int pointsCost,
                               int remainingStock,
                               LocalDate expiresAt,
                               boolean redeemed,
                               boolean expired) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.brand = brand;
        this.pointsCost = pointsCost;
        this.remainingStock = remainingStock;
        this.expiresAt = expiresAt;
        this.redeemed = redeemed;
        this.expired = expired;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getBrand() {
        return brand;
    }

    public int getPointsCost() {
        return pointsCost;
    }

    public int getRemainingStock() {
        return remainingStock;
    }

    public LocalDate getExpiresAt() {
        return expiresAt;
    }

    public boolean isRedeemed() {
        return redeemed;
    }

    public boolean isExpired() {
        return expired;
    }
}
