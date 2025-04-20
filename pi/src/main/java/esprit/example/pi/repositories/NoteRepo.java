package esprit.example.pi.repositories;

import esprit.example.pi.entities.Note;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteRepo extends JpaRepository<Note,Long> {


    List<Note> findByEvaluationIdEvaluation(Long evaluationId);

}
