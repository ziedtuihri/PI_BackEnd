<<<<<<< HEAD:pi/src/main/java/tn/esprit/pi/services/IProjetService.java
package tn.esprit.pi.services;

import tn.esprit.pi.dto.CalendarEventDto;
import tn.esprit.pi.dto.ProjetDto;
import tn.esprit.pi.entities.Projet;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.pi.entities.Sprint;
import tn.esprit.pi.entities.Tache;
=======
package esprit.example.pi.services;

import esprit.example.pi.dto.CalendarEventDto;
import esprit.example.pi.entities.Projet;
import org.springframework.web.multipart.MultipartFile;
>>>>>>> cd4a61c9982a52bc082634662ee55f2633f8d5e8:pi/src/main/java/esprit/example/pi/services/IProjetService.java

import java.io.IOException;
import java.util.List;

public interface IProjetService {
<<<<<<< HEAD:pi/src/main/java/tn/esprit/pi/services/IProjetService.java

    Projet saveProjet(Projet projet);

    Projet getProjetById(Long id);

    List<Projet> getAllProjets();

    void deleteProjet(Long id);

    Projet updateProjet(Long id, Projet projet);

    Projet uploadFile(Long projetId, MultipartFile file) throws IOException;
    byte[] downloadFile(Long projetId) throws IOException;

    Projet assignTeacherEmailToProjet(Long projetId, String teacherEmail);

    Projet addStudentEmailToProjet(Long projetId, String studentEmail);

    Projet removeStudentEmailFromProjet(Long projetId, String studentEmail);

    Projet setStudentEmailsToProjet(Long projetId, List<String> studentEmails);

    List<String> getStudentEmailsForProjet(Long projetId);

    // --- NEW METHODS FOR AUTOMATIC STATUS UPDATES ---
    /**
     * Checks and updates the status of a project based on its dates (dateDebut, dateFinPrevue).
     * Changes status from PLANNED to IN_PROGRESS, or IN_PROGRESS to OVERDUE/COMPLETED.
     * @param projetId The ID of the project to update.
     */
    void updateProjetStatusBasedOnDate(Long projetId);

    /**
     * Checks if all sprints within a project are completed and updates the project's status to COMPLETED if so.
     * @param projetId The ID of the project to check.
     */
    void checkAndCompleteProjectIfAllSprintsDone(Long projetId);
    // --- END NEW METHODS ---

    List<ProjetDto> getAllProjetsDTO();

    List<Projet> getProjetsAffectesParEtudiant(String email);
    String cleanEmail(String email); // <-- ADD THIS METHOD

}
=======
    Projet ajouterEtudiantAuProjet(Long projetId, String nomEtudiant);

    Projet supprimerEtudiantDuProjet(Long projetId, String nomEtudiant);

    List<String> getEtudiantsDuProjet(Long projetId);


    Projet saveProjet(Projet projet);


    Projet getProjetById(Long id);


    List<Projet> getAllProjets();


    void deleteProjet(Long id);

    Projet updateProjet(Long id, Projet projet);
    Projet uploadFile(Long projetId, MultipartFile file) throws IOException;
    byte[] downloadFile(Long projetId) throws IOException;



}
>>>>>>> cd4a61c9982a52bc082634662ee55f2633f8d5e8:pi/src/main/java/esprit/example/pi/services/IProjetService.java
