package com.example.gamegiaido.controller;

import com.example.gamegiaido.dto.ChangePasswordForm;
import com.example.gamegiaido.dto.RedeemRewardForm;
import com.example.gamegiaido.dto.UpdateProfileForm;
import com.example.gamegiaido.model.PlayerProfile;
import com.example.gamegiaido.service.PlayerProfileService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProfileController {

    private final PlayerProfileService playerProfileService;

    public ProfileController(PlayerProfileService playerProfileService) {
        this.playerProfileService = playerProfileService;
    }

    @GetMapping("/profile")
    public String profile(Authentication authentication, Model model) {
        PlayerProfile profile = playerProfileService.getByUsername(authentication.getName());
        UpdateProfileForm form = new UpdateProfileForm();
        form.setDisplayName(profile.getDisplayName());
        form.setAvatarUrl(profile.getAvatarUrl());
        prepareProfilePageModel(model, profile, form, new ChangePasswordForm(), new RedeemRewardForm());
        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(Authentication authentication,
                                @Valid @ModelAttribute("updateProfileForm") UpdateProfileForm updateProfileForm,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            PlayerProfile profile = playerProfileService.getByUsername(authentication.getName());
            prepareProfilePageModel(model, profile, updateProfileForm, new ChangePasswordForm(), new RedeemRewardForm());
            return "profile";
        }

        playerProfileService.updateDisplayName(authentication.getName(), updateProfileForm);
        redirectAttributes.addFlashAttribute("profileSuccess", "Cập nhật thông tin thành công.");
        return "redirect:/profile";
    }

    @PostMapping("/profile/password")
    public String changePassword(Authentication authentication,
                                 @Valid @ModelAttribute("changePasswordForm") ChangePasswordForm changePasswordForm,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            PlayerProfile profile = playerProfileService.getByUsername(authentication.getName());
            UpdateProfileForm updateProfileForm = new UpdateProfileForm();
            updateProfileForm.setDisplayName(profile.getDisplayName());
            updateProfileForm.setAvatarUrl(profile.getAvatarUrl());
            prepareProfilePageModel(model, profile, updateProfileForm, changePasswordForm, new RedeemRewardForm());
            return "profile";
        }

        try {
            playerProfileService.changePassword(authentication.getName(), changePasswordForm);
            redirectAttributes.addFlashAttribute("passwordSuccess", "Đổi mật khẩu thành công.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("passwordError", ex.getMessage());
        }
        return "redirect:/profile";
    }

    @PostMapping("/profile/redeem")
    public String redeemRewards(Authentication authentication,
                                @Valid @ModelAttribute("redeemRewardForm") RedeemRewardForm redeemRewardForm,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            PlayerProfile profile = playerProfileService.getByUsername(authentication.getName());
            UpdateProfileForm updateProfileForm = new UpdateProfileForm();
            updateProfileForm.setDisplayName(profile.getDisplayName());
            updateProfileForm.setAvatarUrl(profile.getAvatarUrl());
            prepareProfilePageModel(model, profile, updateProfileForm, new ChangePasswordForm(), redeemRewardForm);
            return "profile";
        }

        try {
            int remain = playerProfileService.redeemRewardPoints(authentication.getName(), redeemRewardForm.getPoints());
            redirectAttributes.addFlashAttribute("redeemSuccess",
                    "Đổi thưởng thành công " + redeemRewardForm.getPoints() + " điểm. Còn lại " + remain + " điểm thưởng.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("redeemError", ex.getMessage());
        }
        return "redirect:/profile";
    }

    private void prepareProfilePageModel(Model model,
                                         PlayerProfile profile,
                                         UpdateProfileForm updateProfileForm,
                                         ChangePasswordForm changePasswordForm,
                                         RedeemRewardForm redeemRewardForm) {
        model.addAttribute("profile", profile);
        model.addAttribute("updateProfileForm", updateProfileForm);
        model.addAttribute("changePasswordForm", changePasswordForm);
        model.addAttribute("redeemRewardForm", redeemRewardForm);
    }
}
