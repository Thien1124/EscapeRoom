package com.example.gamegiaido.repository;

import com.example.gamegiaido.dto.LeaderboardEntry;
import com.example.gamegiaido.model.PlayerProfile;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PlayerProfileRepository extends JpaRepository<PlayerProfile, Long> {
    Optional<PlayerProfile> findByAccountUsername(String username);

    List<PlayerProfile> findTop20ByOrderByTotalScoreDescTotalWinDesc();

    @Query("""
            SELECT new com.example.gamegiaido.dto.LeaderboardEntry(
                p.id,
                p.displayName,
                p.avatarUrl,
                p.totalScore,
                p.totalWin,
                COALESCE(AVG(CASE WHEN h.result = com.example.gamegiaido.model.PlayResult.WIN THEN h.actionCount ELSE null END), 999999d)
            )
            FROM PlayerProfile p
            LEFT JOIN PlayHistory h ON h.player.id = p.id
            GROUP BY p.id, p.displayName, p.avatarUrl, p.totalScore, p.totalWin
            ORDER BY p.totalScore DESC, p.totalWin DESC,
                COALESCE(AVG(CASE WHEN h.result = com.example.gamegiaido.model.PlayResult.WIN THEN h.actionCount ELSE null END), 999999d) ASC,
                p.id ASC
            """)
    List<LeaderboardEntry> findLeaderboardEntries(Pageable pageable);
}
