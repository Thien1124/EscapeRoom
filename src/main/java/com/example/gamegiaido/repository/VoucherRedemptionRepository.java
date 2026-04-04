package com.example.gamegiaido.repository;

import com.example.gamegiaido.model.VoucherRedemption;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoucherRedemptionRepository extends JpaRepository<VoucherRedemption, Long> {
    boolean existsByPlayerIdAndVoucherId(Long playerId, Long voucherId);

    List<VoucherRedemption> findByPlayerIdOrderByRedeemedAtDesc(Long playerId);
}
