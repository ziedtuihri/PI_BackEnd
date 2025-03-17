package esprit.example.pi.repositories;

import esprit.example.pi.entities.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EvaluationRepo extends JpaRepository<Evaluation,Long> {

    @Query("SELECT e FROM Evaluation e WHERE e.projet.idProjet = :projetId")
    List<Evaluation> findByProjetId(@Param("projetId") Long projetId);


}
