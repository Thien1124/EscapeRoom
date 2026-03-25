package com.example.gamegiaido.service;

import com.example.gamegiaido.dto.RegisterForm;
import com.example.gamegiaido.model.PlayerProfile;
import com.example.gamegiaido.model.Role;
import com.example.gamegiaido.model.UserAccount;
import com.example.gamegiaido.repository.PlayerProfileRepository;
import com.example.gamegiaido.repository.UserAccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final PlayerProfileRepository playerProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public AuthService(UserAccountRepository userAccountRepository,
                       PlayerProfileRepository playerProfileRepository,
                       PasswordEncoder passwordEncoder,
                       EmailService emailService) {
        this.userAccountRepository = userAccountRepository;
        this.playerProfileRepository = playerProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Transactional
    public void register(RegisterForm form) {
        if (userAccountRepository.existsByUsername(form.getUsername())) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại");
        }

        UserAccount account = userAccountRepository.findByUsername(form.getUsername().trim()).orElse(null);
        if (account != null && account.isVerified()) {
            throw new IllegalArgumentException("Tên đăng nhập (Email) đã được sử dụng");
        } else if (account == null) {
            account = new UserAccount();
            account.setUsername(form.getUsername().trim());
            account.setRole(Role.PLAYER);
        }

        account.setPassword(passwordEncoder.encode(form.getPassword()));
        account.setVerified(false);
        
        String otp = String.format("%06d", new java.util.Random().nextInt(999999));
        account.setOtpCode(otp);
        account.setOtpExpiry(java.time.LocalDateTime.now().plusMinutes(5));
        userAccountRepository.save(account);

        if (account.getProfile() == null) {
            PlayerProfile profile = new PlayerProfile();
            profile.setDisplayName(form.getDisplayName().trim());
            profile.setAccount(account);
            playerProfileRepository.save(profile);
        } else {
            PlayerProfile profile = account.getProfile();
            profile.setDisplayName(form.getDisplayName().trim());
            playerProfileRepository.save(profile);
        }

        emailService.sendOtpEmail(account.getUsername(), otp);
    }

    @Transactional
    public void verifyOtp(String username, String otpCode) {
        UserAccount account = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Tài khoản không tồn tại"));

        if (account.isVerified()) {
            throw new IllegalArgumentException("Tài khoản đã được xác thực");
        }

        if (account.getOtpExpiry() == null || account.getOtpExpiry().isBefore(java.time.LocalDateTime.now())) {
            throw new IllegalArgumentException("Mã OTP đã hết hạn");
        }

        if (!account.getOtpCode().equals(otpCode)) {
            throw new IllegalArgumentException("Mã OTP không chính xác");
        }

        account.setVerified(true);
        account.setOtpCode(null);
        account.setOtpExpiry(null);
        userAccountRepository.save(account);
    }

    @Transactional
    public void resendOtp(String username) {
        UserAccount account = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Tài khoản không tồn tại"));

        if (account.isVerified()) {
            throw new IllegalArgumentException("Tài khoản đã được xác thực");
        }

        String otp = String.format("%06d", new java.util.Random().nextInt(999999));
        account.setOtpCode(otp);
        account.setOtpExpiry(java.time.LocalDateTime.now().plusMinutes(5));
        userAccountRepository.save(account);

        emailService.sendOtpEmail(account.getUsername(), otp);
    }
}
