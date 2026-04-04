package com.example.gamegiaido.service;

import com.example.gamegiaido.dto.CharacterIconOption;
import com.example.gamegiaido.dto.RedeemedVoucherInfo;
import com.example.gamegiaido.dto.RewardVoucherOption;
import com.example.gamegiaido.model.PlayerProfile;
import com.example.gamegiaido.model.RewardVoucher;
import com.example.gamegiaido.model.VoucherRedemption;
import com.example.gamegiaido.repository.PlayerProfileRepository;
import com.example.gamegiaido.repository.RewardVoucherRepository;
import com.example.gamegiaido.repository.VoucherRedemptionRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RewardShopService {

    private final PlayerProfileService playerProfileService;
    private final PlayerProfileRepository playerProfileRepository;
    private final CharacterIconService characterIconService;
    private final RewardVoucherRepository rewardVoucherRepository;
    private final VoucherRedemptionRepository voucherRedemptionRepository;

    public RewardShopService(PlayerProfileService playerProfileService,
                             PlayerProfileRepository playerProfileRepository,
                             CharacterIconService characterIconService,
                             RewardVoucherRepository rewardVoucherRepository,
                             VoucherRedemptionRepository voucherRedemptionRepository) {
        this.playerProfileService = playerProfileService;
        this.playerProfileRepository = playerProfileRepository;
        this.characterIconService = characterIconService;
        this.rewardVoucherRepository = rewardVoucherRepository;
        this.voucherRedemptionRepository = voucherRedemptionRepository;
    }

    @Transactional
    public void initializeVoucherCatalogIfNeeded() {
        if (rewardVoucherRepository.count() > 0) {
            return;
        }

        createVoucher("SHOPEE-50K", "Voucher Shopee 50K", "Shopee", 220, 120, LocalDate.now().plusMonths(3));
        createVoucher("HIGHLANDS-30K", "Voucher Highlands 30K", "Highlands Coffee", 160, 80, LocalDate.now().plusMonths(2));
        createVoucher("GRABFOOD-40K", "Voucher GrabFood 40K", "Grab", 200, 90, LocalDate.now().plusMonths(2));
        createVoucher("TIKI-60K", "Voucher Tiki 60K", "Tiki", 260, 50, LocalDate.now().plusMonths(4));
        createVoucher("CGV-70K", "Voucher CGV 70K", "CGV", 300, 40, LocalDate.now().plusMonths(3));
    }

    public List<CharacterIconOption> getIconOptions(String username) {
        return playerProfileService.getCharacterIconOptions(username);
    }

    public List<RewardVoucherOption> getVoucherOptions(String username) {
        PlayerProfile profile = playerProfileService.getByUsername(username);

        return rewardVoucherRepository.findByActiveTrueOrderByPointsCostAsc().stream()
                .map(voucher -> {
                    boolean redeemed = voucherRedemptionRepository
                            .existsByPlayerIdAndVoucherId(profile.getId(), voucher.getId());
                    boolean expired = voucher.getExpiresAt().isBefore(LocalDate.now());
                    return new RewardVoucherOption(
                            voucher.getId(),
                            voucher.getCode(),
                            voucher.getName(),
                            voucher.getBrand(),
                            voucher.getPointsCost(),
                            voucher.getRemainingStock(),
                            voucher.getExpiresAt(),
                            redeemed,
                            expired
                    );
                })
                .toList();
    }

            @Transactional(readOnly = true)
            public List<RedeemedVoucherInfo> getRedeemedVouchers(String username) {
            PlayerProfile profile = playerProfileService.getByUsername(username);
            return voucherRedemptionRepository.findByPlayerIdOrderByRedeemedAtDesc(profile.getId()).stream()
                .map(redemption -> new RedeemedVoucherInfo(
                    redemption.getVoucher().getName(),
                    redemption.getVoucher().getBrand(),
                    redemption.getIssuedCode(),
                    redemption.getPointsSpent(),
                    redemption.getVoucher().getExpiresAt(),
                    redemption.getRedeemedAt()
                ))
                .toList();
            }

    @Transactional
    public void buyIcon(String username, String iconKey) {
        playerProfileService.buyCharacterIcon(username, iconKey);
    }

    @Transactional
    public void equipIcon(String username, String iconKey) {
        playerProfileService.equipCharacterIcon(username, iconKey);
    }

    @Transactional
    public void redeemVoucher(String username, Long voucherId) {
        PlayerProfile profile = playerProfileService.getByUsername(username);
        RewardVoucher voucher = rewardVoucherRepository.findById(voucherId)
                .orElseThrow(() -> new IllegalArgumentException("Voucher không tồn tại."));

        if (!Boolean.TRUE.equals(voucher.getActive())) {
            throw new IllegalArgumentException("Voucher đã ngừng áp dụng.");
        }
        if (voucher.getExpiresAt().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Voucher đã hết hạn.");
        }
        if (voucher.getRemainingStock() <= 0) {
            throw new IllegalArgumentException("Voucher đã hết lượt trong kho.");
        }
        if (voucherRedemptionRepository.existsByPlayerIdAndVoucherId(profile.getId(), voucher.getId())) {
            throw new IllegalArgumentException("Bạn chỉ được đổi voucher này 1 lần.");
        }

        int rewardPoints = profile.getRewardPoints() == null ? 0 : profile.getRewardPoints();
        if (rewardPoints < voucher.getPointsCost()) {
            throw new IllegalArgumentException("Điểm thưởng không đủ để đổi voucher này.");
        }

        profile.setRewardPoints(rewardPoints - voucher.getPointsCost());
        voucher.setRemainingStock(voucher.getRemainingStock() - 1);

        VoucherRedemption redemption = new VoucherRedemption();
        redemption.setPlayer(profile);
        redemption.setVoucher(voucher);
        redemption.setPointsSpent(voucher.getPointsCost());
        redemption.setIssuedCode(buildIssuedCode(voucher.getCode()));
        redemption.setRedeemedAt(LocalDateTime.now());

        playerProfileRepository.save(profile);
        rewardVoucherRepository.save(voucher);
        voucherRedemptionRepository.save(redemption);
    }

    private String buildIssuedCode(String baseVoucherCode) {
        String suffix = UUID.randomUUID().toString().replace("-", "")
                .substring(0, 8)
                .toUpperCase(Locale.ROOT);
        return baseVoucherCode + "-" + suffix;
    }

    private void createVoucher(String code, String name, String brand, int pointsCost, int stock, LocalDate expiresAt) {
        RewardVoucher voucher = new RewardVoucher();
        voucher.setCode(code);
        voucher.setName(name);
        voucher.setBrand(brand);
        voucher.setPointsCost(pointsCost);
        voucher.setTotalStock(stock);
        voucher.setRemainingStock(stock);
        voucher.setExpiresAt(expiresAt);
        voucher.setActive(true);
        rewardVoucherRepository.save(voucher);
    }
}
