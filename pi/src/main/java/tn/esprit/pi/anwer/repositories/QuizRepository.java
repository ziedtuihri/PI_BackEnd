package tn.esprit.pi.anwer.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi.anwer.entities.Quiz;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
}
