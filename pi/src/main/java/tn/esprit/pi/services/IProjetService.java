package tn.esprit.pi.services;

import tn.esprit.pi.dto.CalendarEventDto;
import tn.esprit.pi.dto.ProjetDto;
import tn.esprit.pi.entities.Projet;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.pi.entities.Sprint;
import tn.esprit.pi.entities.Tache;

import java.io.IOException;
import java.util.List;

public interface IProjetService {

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