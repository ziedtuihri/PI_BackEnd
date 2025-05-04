package esprit.example.pi.services;

import esprit.example.pi.entities.Projet;
import esprit.example.pi.repositories.ProjetRepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProjetServiceImpl implements IProjetService {

    private final ProjetRepo projetRepository;
    @Value("${file.upload-dir:./uploads/projets/}") // <-- Ajoutez cette ligne
    private String uploadDir;

    @Autowired
    public ProjetServiceImpl(ProjetRepo projetRepository) {

        this.projetRepository = projetRepository;
    }

    @Override
    public Projet ajouterEtudiantAuProjet(Long projetId, String nomEtudiant) {
        Optional<Projet> projetOptional = projetRepository.findById(projetId);
        if (projetOptional.isPresent()) {
            Projet projet = projetOptional.get();
            List<String> etudiants = projet.getListeEtudiants();
            if (etudiants == null) {
                etudiants = new ArrayList<>();
            }
            etudiants.add(nomEtudiant);
            projet.setListeEtudiants(etudiants);
            return projetRepository.save(projet);
        }
        return null;
    }

    @Override
    public Projet supprimerEtudiantDuProjet(Long projetId, String nomEtudiant) {
        Optional<Projet> projetOptional = projetRepository.findById(projetId);
        if (projetOptional.isPresent()) {
            Projet projet = projetOptional.get();
            List<String> etudiants = projet.getListeEtudiants();
            if (etudiants != null) {
                etudiants.remove(nomEtudiant);
                projet.setListeEtudiants(etudiants);
                return projetRepository.save(projet);
            }
        }
        return null;
    }

    @Override
    public List<String> getEtudiantsDuProjet(Long projetId) {
        Optional<Projet> projetOptional = projetRepository.findById(projetId);
        return projetOptional.map(Projet::getListeEtudiants).orElse(null);
    }


    @Override
    public Projet saveProjet(Projet projet) {
        return projetRepository.save(projet);
    }


    @Override
    public Projet getProjetById(Long id) {
        Optional<Projet> projet = projetRepository.findById(id);
        return projet.orElse(null);
    }


    @Override
    public List<Projet> getAllProjets() {
        return projetRepository.findAll();
    }


    @Override
    public void deleteProjet(Long id) {
        projetRepository.deleteById(id);
    }


    @Override
    public Projet updateProjet(Long id, Projet projet) {
        if (projetRepository.existsById(id)) {
            projet.setIdProjet(id);
            return projetRepository.save(projet);
        } else {
            return null;
        }
    }

    @Override
    public Projet uploadFile(Long projetId, MultipartFile file) throws IOException {
        Projet projet = getProjetById(projetId);
        if (projet == null) {
            throw new RuntimeException("Projet non trouvé avec l'ID : " + projetId);
        }

        Path uploadPath = Paths.get(uploadDir + projetId);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String fileName = file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        projet.setFilePath(filePath.toString());
        return projetRepository.save(projet);
    }

    @Override
    public byte[] downloadFile(Long projetId) throws IOException {
        Projet projet = getProjetById(projetId);
        if (projet == null || projet.getFilePath() == null) {
            throw new RuntimeException("Aucun fichier trouvé pour ce projet");
        }
        Path filePath = Paths.get(projet.getFilePath());
        return Files.readAllBytes(filePath);
    }
}
