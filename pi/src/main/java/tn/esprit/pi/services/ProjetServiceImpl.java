package tn.esprit.pi.services;

import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.pi.email.EmailService;
import tn.esprit.pi.entities.Projet;
import tn.esprit.pi.repositories.ProjetRepo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProjetServiceImpl implements IProjetService {

    private final ProjetRepo projetRepository;
    private final EmailService emailService;

    @Value("${file.upload-dir:./uploads/projets/}")
    private String uploadDir;

    @Autowired
    public ProjetServiceImpl(ProjetRepo projetRepository, EmailService emailService) {
        this.projetRepository = projetRepository;
        this.emailService = emailService;
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
            throw new RuntimeException("Aucun fichier trouvé pour ce projet ou chemin non défini");
        }
        Path filePath = Paths.get(projet.getFilePath());
        return Files.readAllBytes(filePath);
    }

    @Override
    public Projet assignTeacherEmailToProjet(Long projetId, String teacherEmail) {
        Projet projet = getProjetById(projetId);
        if (projet == null) {
            throw new RuntimeException("Projet non trouvé.");
        }
        projet.setTeacherEmail(cleanEmail(teacherEmail));
        return projetRepository.save(projet);
    }

    @Override
    @Transactional
    public Projet addStudentEmailToProjet(Long projetId, String studentEmail) {
        Projet projet = getProjetById(projetId);
        if (projet == null) {
            throw new RuntimeException("Projet non trouvé.");
        }

        List<String> currentEmails = projet.getStudentEmailsList();
        if (currentEmails == null) {
            currentEmails = new ArrayList<>();
        }

        String normalizedEmail = cleanEmail(studentEmail);

        boolean exists = currentEmails.stream()
                .map(this::cleanEmail)
                .anyMatch(email -> email.equals(normalizedEmail));

        if (!exists) {
            currentEmails.add(normalizedEmail);
            projet.setStudentEmailsList(currentEmails);
            Projet updatedProjet = projetRepository.save(projet);

            try {
                sendProjetAssignmentEmail(normalizedEmail, updatedProjet);
            } catch (MessagingException e) {
                System.err.println("Erreur lors de l'envoi de l'email à " + normalizedEmail + ": " + e.getMessage());
            }
            return updatedProjet;
        }

        return projet;
    }

    @Override
    @Transactional
    public Projet removeStudentEmailFromProjet(Long projetId, String studentEmail) {
        Projet projet = getProjetById(projetId);
        if (projet == null) {
            throw new RuntimeException("Projet non trouvé.");
        }

        List<String> currentEmails = projet.getStudentEmailsList();
        if (currentEmails != null) {
            String normalizedEmail = cleanEmail(studentEmail);
            currentEmails.removeIf(email -> cleanEmail(email).equals(normalizedEmail));
            projet.setStudentEmailsList(currentEmails);
            return projetRepository.save(projet);
        }
        return projet;
    }

    @Override
    @Transactional
    public Projet setStudentEmailsToProjet(Long projetId, List<String> studentEmails) {
        Projet projet = getProjetById(projetId);
        if (projet == null) {
            throw new RuntimeException("Projet non trouvé.");
        }

        List<String> oldEmails = projet.getStudentEmailsList() != null
                ? cleanEmailsList(projet.getStudentEmailsList())
                : new ArrayList<>();
        List<String> newEmails = cleanEmailsList(studentEmails);

        for (String newEmail : newEmails) {
            if (!oldEmails.contains(newEmail)) {
                // Nouvel email ajouté, envoyer notification
                try {
                    sendProjetAssignmentEmail(newEmail, projet);
                } catch (MessagingException e) {
                    System.err.println("Erreur lors de l'envoi de l'email à " + newEmail + ": " + e.getMessage());
                }
            }
        }

        projet.setStudentEmailsList(newEmails);
        return projetRepository.save(projet);
    }

    @Override
    public List<String> getStudentEmailsForProjet(Long projetId) {
        Projet projet = getProjetById(projetId);
        if (projet == null) {
            throw new RuntimeException("Projet non trouvé.");
        }
        return projet.getStudentEmailsList() != null ? new ArrayList<>(projet.getStudentEmailsList()) : new ArrayList<>();
    }

    // ------------------- Méthodes privées -------------------

    private String cleanEmail(String email) {
        if (email == null) return null;
        return email.trim()
                .replaceAll("^\"|\"$", "")    // Enlève guillemets début/fin
                .replaceAll("^\\{|\\}$", "")  // Enlève accolades début/fin
                .trim()
                .toLowerCase();
    }

    private List<String> cleanEmailsList(List<String> emails) {
        if (emails == null) return new ArrayList<>();
        return emails.stream()
                .map(this::cleanEmail)
                .distinct()
                .toList();
    }

    private void sendProjetAssignmentEmail(String studentEmail, Projet projet) throws MessagingException {
        String studentName = studentEmail.split("@")[0];
        String teacherInfo = (projet.getTeacherEmail() != null && !projet.getTeacherEmail().isEmpty())
                ? projet.getTeacherEmail()
                : "Non spécifié";

        emailService.sendProjetAssignmentEmail(
                studentEmail,
                studentName,
                projet.getNom(),
                projet.getDescription(),
                projet.getDateDebut() != null
                        ? projet.getDateDebut().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        : "N/A",
                teacherInfo
        );
    }
}
