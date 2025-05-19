package tn.esprit.pi.anwer.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi.anwer.entities.Answer;

import java.util.List;

public interface AnswerRepository extends JpaRepository<Answer, Long> {
    List<Answer> findByQuestionId(Long questionId);
}
