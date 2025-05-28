package tn.esprit.pi.services;

import tn.esprit.pi.dto.CalendarEventDto;
import tn.esprit.pi.entities.Projet;
import org.springframework.web.multipart.MultipartFile;

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

    // Nouvelle méthode pour affecter (ou mettre à jour) l'email de l'encadrant
    Projet assignTeacherEmailToProjet(Long projetId, String teacherEmail);

    // Nouvelle méthode pour ajouter un seul email d'étudiant à un projet
    Projet addStudentEmailToProjet(Long projetId, String studentEmail);

    // Nouvelle méthode pour supprimer un seul email d'étudiant d'un projet
    Projet removeStudentEmailFromProjet(Long projetId, String studentEmail);

    // Méthode pour affecter toute une liste d'emails d'étudiants (remplace/définit la liste actuelle)
    Projet setStudentEmailsToProjet(Long projetId, List<String> studentEmails);

    // Ajoutez cette méthode si vous voulez récupérer la liste des emails d'étudiants pour un projet
    List<String> getStudentEmailsForProjet(Long projetId);
}