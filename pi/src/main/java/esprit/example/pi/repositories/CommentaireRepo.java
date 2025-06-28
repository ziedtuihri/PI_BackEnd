package esprit.example.pi.repositories;

import esprit.example.pi.entities.Commentaire;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentaireRepo extends JpaRepository<Commentaire, Long> {
}
