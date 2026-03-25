package com.example.gamegiaido.controller;

import com.example.gamegiaido.dto.RegisterForm;
import com.example.gamegiaido.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String login(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/";
        }
        return "login";
    }

    @GetMapping("/register")
    public String showRegister(Model model) {
        model.addAttribute("registerForm", new RegisterForm());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerForm") RegisterForm registerForm,
                           BindingResult bindingResult,
                           Model model) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            authService.register(registerForm);
            return "redirect:/verify-otp?username=" + java.net.URLEncoder.encode(registerForm.getUsername().trim(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("registerError", ex.getMessage());
            return "register";
        }
    }

    @GetMapping("/verify-otp")
    public String showVerifyOtp(@org.springframework.web.bind.annotation.RequestParam("username") String username, Model model) {
        model.addAttribute("username", username);
        return "verify-otp";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@org.springframework.web.bind.annotation.RequestParam("username") String username,
                            @org.springframework.web.bind.annotation.RequestParam("otpCode") String otpCode,
                            Model model,
                            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            authService.verifyOtp(username, otpCode);
            redirectAttributes.addFlashAttribute("successMessage", "Xác thực thành công. Bạn có thể đăng nhập.");
            return "redirect:/login";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("username", username);
            return "verify-otp";
        }
    }

    @PostMapping("/resend-otp")
    public String resendOtp(@org.springframework.web.bind.annotation.RequestParam("username") String username,
                            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            authService.resendOtp(username);
            redirectAttributes.addFlashAttribute("successMessage", "Đã gửi lại mã OTP. Vui lòng kiểm tra email.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/verify-otp?username=" + java.net.URLEncoder.encode(username, java.nio.charset.StandardCharsets.UTF_8);
    }
}
