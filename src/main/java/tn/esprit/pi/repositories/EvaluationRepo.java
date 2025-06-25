package tn.esprit.pi.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tn.esprit.pi.entities.Evaluation;
import tn.esprit.pi.entities.Note;

import java.util.List;

public interface EvaluationRepo extends JpaRepository<Evaluation,Long> {
    @Query("SELECT e FROM Evaluation e WHERE e.projet.idProjet = :projetId")
        //List<Evaluation> findByProjetId(@Param("projetId") Long projetId);
    List<Evaluation> findByProjetId(Long projetId);

    @Query("SELECT e FROM Evaluation e WHERE e.sprint.idSprint = :sprintId")
    List<Evaluation> findBySprintId(Long sprintId);
}
