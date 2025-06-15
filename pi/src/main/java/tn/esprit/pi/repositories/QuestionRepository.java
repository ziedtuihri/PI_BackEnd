package tn.esprit.pi.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi.entities.Question;

import java.util.List;


public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByQuizId(Long quizId);
}
