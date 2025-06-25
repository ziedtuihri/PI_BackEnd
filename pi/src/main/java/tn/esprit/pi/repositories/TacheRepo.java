package tn.esprit.pi.repositories;

import tn.esprit.pi.entities.Tache;
import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi.entities.enumerations.TaskStatus;

import java.util.List;

public interface TacheRepo extends JpaRepository <Tache ,Long> {
    // Recommandé : Utilise le nom de la relation (sprint) puis le nom de la propriété (idSprint)
    // Cela se lit comme "trouve les tâches par le IdSprint du Sprint"
    List<Tache> findBySprint_IdSprint(Long sprintId);


    List<Tache> findByStatut(TaskStatus status);
    List<Tache> findByEtudiantsAffectesContainingIgnoreCase(String email);

}