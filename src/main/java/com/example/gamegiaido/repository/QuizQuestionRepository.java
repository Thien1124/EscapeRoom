package com.example.gamegiaido.repository;

import com.example.gamegiaido.model.QuizQuestion;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {
    Optional<QuizQuestion> findByRoomObjectId(Long roomObjectId);
}
