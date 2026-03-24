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
import com.example.gamegiaido.dto.ChangePasswordForm;

@Service
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final PlayerProfileRepository playerProfileRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserAccountRepository userAccountRepository,
            PlayerProfileRepository playerProfileRepository,
            PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.playerProfileRepository = playerProfileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void register(RegisterForm form) {
        if (userAccountRepository.existsByUsername(form.getUsername())) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại");
        }

        UserAccount account = new UserAccount();
        account.setUsername(form.getUsername().trim());
        account.setPassword(passwordEncoder.encode(form.getPassword()));
        account.setRole(Role.PLAYER);
        userAccountRepository.save(account);

        PlayerProfile profile = new PlayerProfile();
        profile.setDisplayName(form.getDisplayName().trim());
        profile.setAccount(account);
        playerProfileRepository.save(profile);
    }

    @Transactional
    public void changePassword(String username, ChangePasswordForm form) {
        UserAccount account = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));

        if (!passwordEncoder.matches(form.getCurrentPassword(), account.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không đúng");
        }

        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp");
        }

        account.setPassword(passwordEncoder.encode(form.getNewPassword()));
        userAccountRepository.save(account);
    }
}
