package com.example.gamegiaido.service;

import com.example.gamegiaido.dto.ChangePasswordForm;
import com.example.gamegiaido.dto.CharacterIconOption;
import com.example.gamegiaido.dto.UpdateProfileForm;
import com.example.gamegiaido.model.UserAccount;
import com.example.gamegiaido.repository.UserAccountRepository;
import com.example.gamegiaido.model.PlayerProfile;
import com.example.gamegiaido.repository.PlayerProfileRepository;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlayerProfileService {

    private final PlayerProfileRepository playerProfileRepository;
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final CharacterIconService characterIconService;

    public PlayerProfileService(PlayerProfileRepository playerProfileRepository,
                                UserAccountRepository userAccountRepository,
                                PasswordEncoder passwordEncoder,
                                CharacterIconService characterIconService) {
        this.playerProfileRepository = playerProfileRepository;
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.characterIconService = characterIconService;
    }

    public PlayerProfile getByUsername(String username) {
        PlayerProfile profile = playerProfileRepository.findByAccountUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hồ sơ người chơi"));
        return initializeRewardWalletIfNeeded(profile);
    }

    @Transactional
    public PlayerProfile updateDisplayName(String username, UpdateProfileForm form) {
        PlayerProfile profile = getByUsername(username);
        profile.setDisplayName(form.getDisplayName().trim());
        profile.setAvatarUrl(normalizeAvatarUrl(form.getAvatarUrl()));
        return playerProfileRepository.save(profile);
    }

    private String normalizeAvatarUrl(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
            return null;
        }
        return avatarUrl.trim();
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

    @Transactional
    public int redeemRewardPoints(String username, int pointsToRedeem) {
        if (pointsToRedeem <= 0) {
            throw new IllegalArgumentException("Điểm đổi phải lớn hơn 0");
        }

        PlayerProfile profile = getByUsername(username);
        int currentRewards = profile.getRewardPoints() == null ? 0 : profile.getRewardPoints();
        if (currentRewards < pointsToRedeem) {
            throw new IllegalArgumentException("Điểm thưởng không đủ để đổi.");
        }

        profile.setRewardPoints(currentRewards - pointsToRedeem);
        playerProfileRepository.save(profile);
        return profile.getRewardPoints();
    }

    public List<CharacterIconOption> getCharacterIconOptions(String username) {
        PlayerProfile profile = getByUsername(username);
        Set<String> owned = parseOwnedIcons(profile.getOwnedCharacterIcons());
        String selectedKey = characterIconService.normalizeIconKey(profile.getSelectedCharacterIcon());

        return characterIconService.getCatalog().stream()
                .map(spec -> new CharacterIconOption(
                        spec.key(),
                        spec.name(),
                        spec.iconClass(),
                        spec.cost(),
                        spec.difficultyLevel(),
                        owned.contains(spec.key()),
                        selectedKey.equals(spec.key())
                ))
                .toList();
    }

    public String getSelectedCharacterIconClass(String username) {
        PlayerProfile profile = getByUsername(username);
        return characterIconService.iconClass(profile.getSelectedCharacterIcon());
    }

    @Transactional
    public void buyCharacterIcon(String username, String iconKey) {
        PlayerProfile profile = getByUsername(username);
        String normalizedKey = characterIconService.normalizeIconKey(iconKey);
        CharacterIconService.IconSpec spec = characterIconService.getSpec(normalizedKey);

        Set<String> owned = parseOwnedIcons(profile.getOwnedCharacterIcons());
        if (owned.contains(spec.key())) {
            throw new IllegalArgumentException("Bạn đã sở hữu icon này.");
        }

        int rewardPoints = profile.getRewardPoints() == null ? 0 : profile.getRewardPoints();
        if (rewardPoints < spec.cost()) {
            throw new IllegalArgumentException("Không đủ điểm thưởng để mua icon.");
        }

        profile.setRewardPoints(rewardPoints - spec.cost());
        owned.add(spec.key());
        profile.setOwnedCharacterIcons(String.join(",", owned));
        playerProfileRepository.save(profile);
    }

    @Transactional
    public void equipCharacterIcon(String username, String iconKey) {
        PlayerProfile profile = getByUsername(username);
        String normalizedKey = characterIconService.normalizeIconKey(iconKey);

        Set<String> owned = parseOwnedIcons(profile.getOwnedCharacterIcons());
        if (!owned.contains(normalizedKey)) {
            throw new IllegalArgumentException("Bạn chưa sở hữu icon này.");
        }

        profile.setSelectedCharacterIcon(normalizedKey);
        playerProfileRepository.save(profile);
    }

    private Set<String> parseOwnedIcons(String csv) {
        Set<String> owned = new LinkedHashSet<>();
        owned.add(CharacterIconService.DEFAULT_ICON_KEY);
        if (csv == null || csv.isBlank()) {
            return owned;
        }
        for (String token : csv.split(",")) {
            String key = token == null ? "" : token.trim();
            if (!key.isEmpty()) {
                owned.add(characterIconService.normalizeIconKey(key));
            }
        }
        return owned.stream().collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private PlayerProfile initializeRewardWalletIfNeeded(PlayerProfile profile) {
        if (Boolean.TRUE.equals(profile.getRewardWalletInitialized())) {
            return profile;
        }

        int currentRewards = profile.getRewardPoints() == null ? 0 : profile.getRewardPoints();
        int totalScore = profile.getTotalScore() == null ? 0 : profile.getTotalScore();
        if (currentRewards < totalScore) {
            profile.setRewardPoints(totalScore);
        }
        profile.setRewardWalletInitialized(true);
        return playerProfileRepository.save(profile);
    }
}
