package com.example.gamegiaido.config;

import com.example.gamegiaido.service.RewardShopService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class RewardShopInitializer implements ApplicationRunner {

    private final RewardShopService rewardShopService;

    public RewardShopInitializer(RewardShopService rewardShopService) {
        this.rewardShopService = rewardShopService;
    }

    @Override
    public void run(ApplicationArguments args) {
        rewardShopService.initializeVoucherCatalogIfNeeded();
    }
}
