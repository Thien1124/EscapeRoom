package com.example.gamegiaido.repository;

import com.example.gamegiaido.model.GameMode;
import com.example.gamegiaido.model.GameRoom;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRoomRepository extends JpaRepository<GameRoom, Long> {
    Optional<GameRoom> findFirstByTopicIdAndModeOrderByRoomOrderAsc(Long topicId, GameMode mode);

    List<GameRoom> findByTopicIdAndModeOrderByRoomOrderAsc(Long topicId, GameMode mode);
}
