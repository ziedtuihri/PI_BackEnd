package tn.esprit.pi.repositories;

import tn.esprit.pi.entities.Sprint;
import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi.entities.enumerations.SprintStatus;

import java.time.LocalDate;
import java.util.List;

public interface SprintRepo extends JpaRepository<Sprint, Long> {

    // Recherche par nom, insensible à la casse
    List<Sprint> findByNomContainingIgnoreCase(String nom);

    // Recherche par ID du projet associé (supposant un champ 'projet' dans Sprint et 'idProjet' dans Projet)
    List<Sprint> findByProjet_IdProjet(Long projetId);

    // Recherche par statut
    List<Sprint> findByStatut(SprintStatus statut);

    // Recherche par statut et date de fin avant une date donnée
    List<Sprint> findByStatutAndDateFinBefore(SprintStatus statut, LocalDate date);

    // FIX THIS LINE:
    // Recherche des sprints urgents dont la date de fin est après ou égale à une date donnée
    List<Sprint> findByIsUrgentTrueAndDateFinGreaterThanEqual(LocalDate date);
    //           ^^^^^^^ This must be 'IsUrgent' to match 'private boolean isUrgent;' in your entity.

    // Recherche tous les sprints triés par date de début croissante
    List<Sprint> findAllByOrderByDateDebutAsc();

    // Recherche par email dans la liste des étudiants affectés (en supposant 'etudiantsAffectes' est une List<String>)
    List<Sprint> findByEtudiantsAffectesContainingIgnoreCase(String email);

    List<Sprint> findByProjetIdProjet(Long projetId);
}