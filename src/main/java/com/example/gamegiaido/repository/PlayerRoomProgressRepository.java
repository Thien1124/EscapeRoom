package com.example.gamegiaido.repository;

import com.example.gamegiaido.model.PlayerRoomProgress;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerRoomProgressRepository extends JpaRepository<PlayerRoomProgress, Long> {
    Optional<PlayerRoomProgress> findByPlayerIdAndRoomId(Long playerId, Long roomId);

    List<PlayerRoomProgress> findByPlayerIdAndRoomIdIn(Long playerId, List<Long> roomIds);
}
