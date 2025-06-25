package tn.esprit.pi.repositories;

import tn.esprit.pi.entities.Projet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjetRepo extends JpaRepository <Projet ,Long> {


    List<Projet> findByStudentEmailsListContainingIgnoreCase(String email);

}
