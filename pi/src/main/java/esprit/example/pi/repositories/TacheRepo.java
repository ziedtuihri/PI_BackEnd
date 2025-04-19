package esprit.example.pi.repositories;

import esprit.example.pi.entities.Tache;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TacheRepo extends JpaRepository <Tache ,Long> {
}
