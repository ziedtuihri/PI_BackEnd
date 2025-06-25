package tn.esprit.pi.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi.entities.Quiz;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
}
