package com.example.gamegiaido.repository;

import com.example.gamegiaido.model.PlayHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayHistoryRepository extends JpaRepository<PlayHistory, Long> {
    List<PlayHistory> findByPlayerIdOrderByPlayedAtDesc(Long playerId);
}
