package com.example.gamegiaido.repository;

import com.example.gamegiaido.model.RoomKeyConfig;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomKeyConfigRepository extends JpaRepository<RoomKeyConfig, Long> {

    List<RoomKeyConfig> findByRoomIdOrderByIdAsc(Long roomId);
}
