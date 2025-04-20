package esprit.example.pi.services;

import esprit.example.pi.dto.CalendarEventDto;
import esprit.example.pi.entities.Projet;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface IProjetService {
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
