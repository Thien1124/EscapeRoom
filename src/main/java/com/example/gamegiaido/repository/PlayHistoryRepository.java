package com.example.gamegiaido.repository;

import com.example.gamegiaido.model.PlayHistory;
import com.example.gamegiaido.model.PlayResult;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayHistoryRepository extends JpaRepository<PlayHistory, Long> {
    List<PlayHistory> findByPlayerIdOrderByPlayedAtDesc(Long playerId);

    List<PlayHistory> findByPlayerIdAndRoomIdIn(Long playerId, List<Long> roomIds);

    Optional<PlayHistory> findTopByPlayerIdAndRoomIdAndResultOrderByScoreDescPlayedAtDesc(Long playerId, Long roomId, PlayResult result);
}
