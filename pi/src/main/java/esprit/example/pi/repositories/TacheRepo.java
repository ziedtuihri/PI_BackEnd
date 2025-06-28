package esprit.example.pi.repositories;

import esprit.example.pi.entities.Tache;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TacheRepo extends JpaRepository <Tache ,Long> {
    List<Tache> findBySprint_IdSprint(Long sprintId);
}
