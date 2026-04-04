package com.example.gamegiaido.repository;

import com.example.gamegiaido.model.RewardVoucher;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RewardVoucherRepository extends JpaRepository<RewardVoucher, Long> {
    List<RewardVoucher> findByActiveTrueOrderByPointsCostAsc();
}
