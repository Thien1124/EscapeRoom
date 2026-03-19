package com.example.gamegiaido.repository;

import com.example.gamegiaido.model.PlayerProfile;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerProfileRepository extends JpaRepository<PlayerProfile, Long> {
    Optional<PlayerProfile> findByAccountUsername(String username);

    List<PlayerProfile> findTop20ByOrderByTotalScoreDescTotalWinDesc();
}
