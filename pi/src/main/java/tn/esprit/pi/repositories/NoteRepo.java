package tn.esprit.pi.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.pi.entities.Note;
import java.util.List;
import java.util.Optional;

@Repository
public interface NoteRepo extends JpaRepository<Note, Long> {

    // Finds notes associated with a specific evaluation ID
    List<Note> findByEvaluationIdEvaluation(Long evaluationId);

    // Finds notes for a user within a project, by traversing Evaluation -> Projet and directly to User
    List<Note> findByEvaluation_Projet_IdProjetAndUser_Id(Long projetId, Integer userId);

    // Finds all notes directly associated with a specific user
    List<Note> findByUser_Id(Integer userId);

    // Checks if a note already exists for a given user within a specific sprint
    Optional<Note> findByUser_IdAndSprint_IdSprint(Integer userId, Long sprintId);

    // Finds notes associated with a user through the Sprint entity
    List<Note> findBySprint_User_Id(Integer userId);
}