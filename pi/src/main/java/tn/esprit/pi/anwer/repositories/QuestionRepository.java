package tn.esprit.pi.anwer.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi.anwer.entities.Question;

import java.util.List;


public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByQuizId(Long quizId);
}
