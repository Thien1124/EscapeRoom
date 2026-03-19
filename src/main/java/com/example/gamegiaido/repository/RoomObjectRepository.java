package com.example.gamegiaido.repository;

import com.example.gamegiaido.model.RoomObject;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RoomObjectRepository extends JpaRepository<RoomObject, Long> {
    List<RoomObject> findByRoomIdOrderByRequiredStepAsc(Long roomId);

    @Query("select ro from RoomObject ro join fetch ro.room r order by r.id asc, ro.requiredStep asc")
    List<RoomObject> findAllForAdminHotspot();

    java.util.Optional<RoomObject> findByIdAndRoomId(Long id, Long roomId);

    Optional<RoomObject> findFirstByRoomIdAndLockTypeOrderByRequiredStepAsc(Long roomId, com.example.gamegiaido.model.ObjectLockType lockType);
}
