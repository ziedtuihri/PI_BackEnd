package tn.esprit.pi.repositories;

import tn.esprit.pi.entities.Projet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjetRepo extends JpaRepository <Projet ,Long> {
}
