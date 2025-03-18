package esprit.example.pi.repositories;

import esprit.example.pi.entities.Projet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjetRepo extends JpaRepository <Projet ,Long> {
}
