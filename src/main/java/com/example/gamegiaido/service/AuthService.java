package com.example.gamegiaido.service;

import com.example.gamegiaido.dto.RegisterForm;
import com.example.gamegiaido.model.PlayerProfile;
import com.example.gamegiaido.model.Role;
import com.example.gamegiaido.model.UserAccount;
import com.example.gamegiaido.repository.PlayerProfileRepository;
import com.example.gamegiaido.repository.UserAccountRepository;
import com.example.gamegiaido.service.CharacterIconService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final PlayerProfileRepository playerProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final CharacterIconService characterIconService;

    public AuthService(UserAccountRepository userAccountRepository,
                       PlayerProfileRepository playerProfileRepository,
                       PasswordEncoder passwordEncoder,
                       CharacterIconService characterIconService) {
        this.userAccountRepository = userAccountRepository;
        this.playerProfileRepository = playerProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.characterIconService = characterIconService;
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
        profile.setRewardWalletInitialized(true);
        profile.setSelectedCharacterIcon(CharacterIconService.DEFAULT_ICON_KEY);
        profile.setOwnedCharacterIcons(CharacterIconService.DEFAULT_ICON_KEY);
        profile.setAccount(account);
        playerProfileRepository.save(profile);
    }
}
