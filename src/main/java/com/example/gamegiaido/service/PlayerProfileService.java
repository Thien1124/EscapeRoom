package com.example.gamegiaido.service;

import com.example.gamegiaido.dto.ChangePasswordForm;
import com.example.gamegiaido.dto.UpdateProfileForm;
import com.example.gamegiaido.model.UserAccount;
import com.example.gamegiaido.repository.UserAccountRepository;
import com.example.gamegiaido.model.PlayerProfile;
import com.example.gamegiaido.repository.PlayerProfileRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlayerProfileService {

    private final PlayerProfileRepository playerProfileRepository;
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public PlayerProfileService(PlayerProfileRepository playerProfileRepository,
                                UserAccountRepository userAccountRepository,
                                PasswordEncoder passwordEncoder) {
        this.playerProfileRepository = playerProfileRepository;
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public PlayerProfile getByUsername(String username) {
        return playerProfileRepository.findByAccountUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hồ sơ người chơi"));
    }

    @Transactional
    public PlayerProfile updateDisplayName(String username, UpdateProfileForm form) {
        PlayerProfile profile = getByUsername(username);
        profile.setDisplayName(form.getDisplayName().trim());
        return playerProfileRepository.save(profile);
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
