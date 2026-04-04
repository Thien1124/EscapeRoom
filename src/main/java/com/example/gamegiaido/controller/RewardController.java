package com.example.gamegiaido.controller;

import com.example.gamegiaido.service.PlayerProfileService;
import com.example.gamegiaido.service.RewardShopService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RewardController {

    private final RewardShopService rewardShopService;
    private final PlayerProfileService playerProfileService;

    public RewardController(RewardShopService rewardShopService,
                            PlayerProfileService playerProfileService) {
        this.rewardShopService = rewardShopService;
        this.playerProfileService = playerProfileService;
    }

    @GetMapping("/rewards")
    public String rewards(Authentication authentication, Model model) {
        String username = authentication.getName();
        model.addAttribute("profile", playerProfileService.getByUsername(username));
        model.addAttribute("characterIcons", rewardShopService.getIconOptions(username));
        model.addAttribute("vouchers", rewardShopService.getVoucherOptions(username));
        model.addAttribute("redeemedVouchers", rewardShopService.getRedeemedVouchers(username));
        return "rewards";
    }

    @PostMapping("/rewards/icons/buy")
    public String buyIcon(Authentication authentication,
                          @RequestParam String iconKey,
                          RedirectAttributes redirectAttributes) {
        try {
            rewardShopService.buyIcon(authentication.getName(), iconKey);
            redirectAttributes.addFlashAttribute("rewardSuccess", "Mua icon thành công.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("rewardError", ex.getMessage());
        }
        return "redirect:/rewards";
    }

    @PostMapping("/rewards/icons/equip")
    public String equipIcon(Authentication authentication,
                            @RequestParam String iconKey,
                            RedirectAttributes redirectAttributes) {
        try {
            rewardShopService.equipIcon(authentication.getName(), iconKey);
            redirectAttributes.addFlashAttribute("rewardSuccess", "Đã đổi icon nhân vật.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("rewardError", ex.getMessage());
        }
        return "redirect:/rewards";
    }

    @PostMapping("/rewards/vouchers/redeem")
    public String redeemVoucher(Authentication authentication,
                                @RequestParam Long voucherId,
                                RedirectAttributes redirectAttributes) {
        try {
            rewardShopService.redeemVoucher(authentication.getName(), voucherId);
            redirectAttributes.addFlashAttribute("rewardSuccess", "Đổi voucher thành công. Hệ thống đã trừ điểm thưởng tương ứng.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("rewardError", ex.getMessage());
        }
        return "redirect:/rewards";
    }
}
