package tn.esprit.pi.services;

import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.pi.dto.ProjetDto;
import tn.esprit.pi.email.EmailService;
import tn.esprit.pi.entities.Projet;
import tn.esprit.pi.entities.Sprint;
import tn.esprit.pi.entities.enumerations.ProjectStatus;
import tn.esprit.pi.entities.enumerations.ProjectType;
import tn.esprit.pi.entities.enumerations.SprintStatus;
import tn.esprit.pi.repositories.ProjetRepo;
import tn.esprit.pi.repositories.SprintRepo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProjetServiceImpl implements IProjetService {

    private final ProjetRepo projetRepository;
    private final EmailService emailService;
    private final SprintRepo sprintRepository;

    @Value("${file.upload-dir:./uploads/projets/}")
    private String uploadDir;

    @Autowired
    public ProjetServiceImpl(ProjetRepo projetRepository, EmailService emailService, SprintRepo sprintRepository) {
        this.projetRepository = projetRepository;
        this.emailService = emailService;
        this.sprintRepository = sprintRepository;
    }

    @Override
    @Transactional
    public Projet saveProjet(Projet projet) {
        // Set default status if not provided for new projects
        if (projet.getStatut() == null) {
            projet.setStatut(ProjectStatus.PLANNED);
        }
        // Initialize studentEmailsList if it's null
        if (projet.getStudentEmailsList() == null) {
            projet.setStudentEmailsList(new ArrayList<>());
        }
        Projet savedProjet = projetRepository.save(projet);

        // Send emails upon project creation
        if (savedProjet.getStudentEmailsList() != null && !savedProjet.getStudentEmailsList().isEmpty()) {
            for (String studentEmail : savedProjet.getStudentEmailsList()) {
                try {
                    sendProjetAssignmentEmail(studentEmail, savedProjet);
                } catch (MessagingException e) {
                    System.err.println("Erreur lors de l'envoi de l'email à l'étudiant " + studentEmail + " (création) : " + e.getMessage());
                }
            }
        }

        if (savedProjet.getTeacherEmail() != null && !savedProjet.getTeacherEmail().isEmpty()) {
            try {
                sendTeacherAssignmentEmail(savedProjet);
            } catch (MessagingException e) {
                System.err.println("Erreur lors de l'envoi de l'email à l'encadrant " + savedProjet.getTeacherEmail() + " (création) : " + e.getMessage());
            }
        }
        return savedProjet;
    }

    @Override
    public Projet getProjetById(Long id) {
        return projetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé avec l'ID : " + id));
    }

    @Override
    public List<Projet> getAllProjets() {
        return projetRepository.findAll();
    }

    @Override
    @Transactional
    public void deleteProjet(Long id) {
        if (!projetRepository.existsById(id)) {
            throw new RuntimeException("Projet non trouvé avec l'ID : " + id);
        }
        projetRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Projet updateProjet(Long id, Projet projetDetails) {
        Projet existingProjet = getProjetById(id);
        String oldTeacherEmail = existingProjet.getTeacherEmail();
        List<String> oldStudentEmails = existingProjet.getStudentEmailsList() != null ? new ArrayList<>(existingProjet.getStudentEmailsList()) : new ArrayList<>();

        if (projetDetails.getNom() != null && !projetDetails.getNom().isEmpty()) {
            existingProjet.setNom(projetDetails.getNom());
        }
        if (projetDetails.getDescription() != null) {
            existingProjet.setDescription(projetDetails.getDescription());
        }
        if (projetDetails.getProjectType() != null) {
            existingProjet.setProjectType(projetDetails.getProjectType());
        }
        if (projetDetails.getDateDebut() != null) {
            existingProjet.setDateDebut(projetDetails.getDateDebut());
        }
        if (projetDetails.getDateFinPrevue() != null) {
            existingProjet.setDateFinPrevue(projetDetails.getDateFinPrevue());
        }
        if (projetDetails.getDateFinReelle() != null) {
            existingProjet.setDateFinReelle(projetDetails.getDateFinReelle());
        }
        if (projetDetails.getStatut() != null) {
            existingProjet.setStatut(projetDetails.getStatut());
        }

        String newTeacherEmail = projetDetails.getTeacherEmail();
        if (newTeacherEmail != null) {
            if (!cleanEmail(newTeacherEmail).equals(cleanEmail(oldTeacherEmail))) {
                existingProjet.setTeacherEmail(newTeacherEmail);
                Projet updatedProjet = projetRepository.save(existingProjet);
                try {
                    sendTeacherAssignmentEmail(updatedProjet);
                } catch (MessagingException e) {
                    System.err.println("Erreur lors de l'envoi de l'email à l'encadrant (mise à jour) " + newTeacherEmail + ": " + e.getMessage());
                }
            }
        } else if (oldTeacherEmail != null && projetDetails.getTeacherEmail() == null) {
            existingProjet.setTeacherEmail(null);
        }

        if (projetDetails.getStudentEmailsList() != null) {
            List<String> newStudentEmails = cleanEmailsList(projetDetails.getStudentEmailsList());

            for (String newEmail : newStudentEmails) {
                if (!oldStudentEmails.contains(newEmail)) {
                    try {
                        sendProjetAssignmentEmail(newEmail, existingProjet);
                    } catch (MessagingException e) {
                        System.err.println("Erreur lors de l'envoi de l'email à l'étudiant " + newEmail + " (mise à jour liste) : " + e.getMessage());
                    }
                }
            }
            existingProjet.setStudentEmailsList(newStudentEmails); // Corrected
        }

        if (existingProjet.getDateDebut() == null || existingProjet.getDateFinPrevue() == null) {
            throw new IllegalArgumentException("Les dates de début et de fin prévue du projet ne peuvent pas être nulles.");
        }
        if (existingProjet.getDateDebut().isAfter(existingProjet.getDateFinPrevue())) {
            throw new IllegalArgumentException("La date de début du projet ne peut pas être postérieure à la date de fin prévue.");
        }

        return projetRepository.save(existingProjet);
    }

    @Override
    @Transactional
    public Projet uploadFile(Long projetId, MultipartFile file) throws IOException {
        Projet projet = getProjetById(projetId);

        Path uploadPath = Paths.get(uploadDir + projetId);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("Nom de fichier invalide.");
        }
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        projet.setFilePath(filePath.toString());
        return projetRepository.save(projet);
    }

    @Override
    public byte[] downloadFile(Long projetId) throws IOException {
        Projet projet = getProjetById(projetId);
        if (projet.getFilePath() == null || projet.getFilePath().isEmpty()) {
            throw new IOException("Aucun fichier trouvé pour ce projet ou chemin non défini.");
        }
        Path filePath = Paths.get(projet.getFilePath());
        if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
            throw new IOException("Fichier non trouvé ou non lisible : " + filePath.getFileName());
        }
        return Files.readAllBytes(filePath);
    }

    @Override
    @Transactional
    public Projet assignTeacherEmailToProjet(Long projetId, String teacherEmail) {
        Projet projet = getProjetById(projetId);
        String oldTeacherEmail = projet.getTeacherEmail();

        String cleanedNewEmail = cleanEmail(teacherEmail);

        if (!cleanedNewEmail.equals(cleanEmail(oldTeacherEmail))) {
            projet.setTeacherEmail(cleanedNewEmail);
            Projet updatedProjet = projetRepository.save(projet);
            try {
                sendTeacherAssignmentEmail(updatedProjet);
            } catch (MessagingException e) {
                System.err.println("Erreur lors de l'envoi de l'email à l'encadrant (assignation) " + updatedProjet.getTeacherEmail() + ": " + e.getMessage());
            }
            return updatedProjet;
        }
        return projet;
    }

    @Override
    @Transactional
    public Projet addStudentEmailToProjet(Long projetId, String studentEmail) {
        Projet projet = getProjetById(projetId);

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
            projet.setStudentEmailsList(currentEmails); // Corrected
            Projet updatedProjet = projetRepository.save(projet);
            try {
                sendProjetAssignmentEmail(normalizedEmail, updatedProjet);
            } catch (MessagingException e) {
                System.err.println("Erreur lors de l'envoi de l'email à l'étudiant " + normalizedEmail + " (ajout) : " + e.getMessage());
            }
            return updatedProjet;
        }
        return projet;
    }

    @Override
    @Transactional
    public Projet removeStudentEmailFromProjet(Long projetId, String studentEmail) {
        Projet projet = getProjetById(projetId);

        List<String> currentEmails = projet.getStudentEmailsList();
        if (currentEmails != null) {
            String normalizedEmail = cleanEmail(studentEmail);
            boolean removed = currentEmails.removeIf(email -> cleanEmail(email).equals(normalizedEmail));
            if (removed) {
                projet.setStudentEmailsList(currentEmails); // Corrected
                return projetRepository.save(projet);
            }
        }
        return projet;
    }

    @Override
    @Transactional
    public Projet setStudentEmailsToProjet(Long projetId, List<String> studentEmails) {
        Projet projet = getProjetById(projetId);

        List<String> oldEmails = projet.getStudentEmailsList() != null
                ? cleanEmailsList(projet.getStudentEmailsList())
                : new ArrayList<>();
        List<String> newEmails = cleanEmailsList(studentEmails);

        for (String newEmail : newEmails) {
            if (!oldEmails.contains(newEmail)) {
                try {
                    sendProjetAssignmentEmail(newEmail, projet);
                } catch (MessagingException e) {
                    System.err.println("Erreur lors de l'envoi de l'email à l'étudiant " + newEmail + " (mise à jour liste) : " + e.getMessage());
                }
            }
        }

        projet.setStudentEmailsList(newEmails); // Corrected
        return projetRepository.save(projet);
    }

    @Override
    public List<String> getStudentEmailsForProjet(Long projetId) {
        Projet projet = getProjetById(projetId);
        return projet.getStudentEmailsList() != null ? new ArrayList<>(projet.getStudentEmailsList()) : new ArrayList<>();
    }

    @Override
    @Transactional
    public void updateProjetStatusBasedOnDate(Long projetId) {
        Projet projet = getProjetById(projetId);
        LocalDate today = LocalDate.now();

        if (projet.getStatut() == ProjectStatus.PLANNED && projet.getDateDebut() != null && today.isAfter(projet.getDateDebut())) {
            projet.setStatut(ProjectStatus.IN_PROGRESS);
            projetRepository.save(projet);
            System.out.println("Project ID " + projetId + " status changed from PLANNED to IN_PROGRESS due to date.");
        } else if (projet.getStatut() == ProjectStatus.IN_PROGRESS && projet.getDateFinPrevue() != null && today.isAfter(projet.getDateFinPrevue())) {
            List<Sprint> sprintsInProject = sprintRepository.findByProjet_IdProjet(projetId);
            boolean allSprintsCompleted = sprintsInProject.stream()
                    .allMatch(sprint -> sprint.getStatut() == SprintStatus.COMPLETED);

            if (allSprintsCompleted) {
                projet.setStatut(ProjectStatus.COMPLETED);
                System.out.println("Project ID " + projetId + " status changed to COMPLETED (all sprints done & past end date).");
            } else {
                projet.setStatut(ProjectStatus.OVERDUE);
                System.out.println("Project ID " + projetId + " status changed from IN_PROGRESS to OVERDUE due to date.");
            }
            projetRepository.save(projet);
        }
    }

    @Override
    @Transactional
    public void checkAndCompleteProjectIfAllSprintsDone(Long projetId) {
        Projet projet = getProjetById(projetId);

        if (projet.getStatut() == ProjectStatus.COMPLETED || projet.getStatut() == ProjectStatus.CANCELLED) {
            System.out.println("Project ID " + projetId + ": Already COMPLETED or CANCELLED. No sprint-based completion check needed.");
            return;
        }

        List<Sprint> sprintsInProject = sprintRepository.findByProjet_IdProjet(projetId);

        if (sprintsInProject.isEmpty()) {
            System.out.println("Project ID " + projetId + ": No sprints found. Cannot complete project based on sprint completion.");
            return;
        }

        boolean allSprintsCompleted = sprintsInProject.stream()
                .allMatch(sprint -> sprint.getStatut() == SprintStatus.COMPLETED);

        if (allSprintsCompleted) {
            projet.setStatut(ProjectStatus.COMPLETED);
            projetRepository.save(projet);
            System.out.println("Project " + projetId + " auto-completed because all associated sprints are COMPLETED.");
        }
    }

    // --- Helper Methods for Email and Data Cleaning ---

    private void sendProjetAssignmentEmail(String studentEmail, Projet projet) throws MessagingException {
        System.out.println("Sending project assignment email to: " + studentEmail);

        String studentName = studentEmail.split("@")[0];
        String teacherInfo = (projet.getTeacherEmail() != null && !projet.getTeacherEmail().isEmpty())
                ? projet.getTeacherEmail()
                : "Non spécifié";

        emailService.sendProjetAssignmentEmail(
                studentEmail,
                studentName,
                projet.getNom(),
                projet.getDescription(),
                projet.getDateDebut() != null ? projet.getDateDebut().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A",
                projet.getDateFinPrevue() != null ? projet.getDateFinPrevue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A",
                teacherInfo,
                projet.getProjectType()
        );
    }

    private void sendTeacherAssignmentEmail(Projet projet) throws MessagingException {
        System.out.println("Sending teacher assignment email to: " + projet.getTeacherEmail());

        if (projet.getTeacherEmail() != null && !projet.getTeacherEmail().isEmpty()) {
            String teacherEmail = projet.getTeacherEmail();
            String projectName = projet.getNom();
            String projectDescription = projet.getDescription();
            String projectType = projet.getProjectType() != null ? projet.getProjectType().name() : "Non spécifié";
            String studentsList = (projet.getStudentEmailsList() != null && !projet.getStudentEmailsList().isEmpty())
                    ? String.join(", ", projet.getStudentEmailsList())
                    : "Aucun étudiant assigné pour le moment.";

            String subject = "Vous avez été assigné(e) comme encadrant du projet : " + projectName;
            String body = "Bonjour,\n\n"
                    + "Vous avez été désigné(e) comme encadrant(e) pour le projet suivant :\n\n"
                    + "Nom du projet : " + projectName + "\n"
                    + "Description : " + (projectDescription != null && !projectDescription.isEmpty() ? projectDescription : "N/A") + "\n"
                    + "Type de projet : " + projectType + "\n"
                    + "Date de début : " + (projet.getDateDebut() != null ? projet.getDateDebut().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A") + "\n"
                    + "Date de fin prévue : " + (projet.getDateFinPrevue() != null ? projet.getDateFinPrevue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A") + "\n"
                    + "Statut : " + (projet.getStatut() != null ? projet.getStatut().name() : "N/A") + "\n"
                    + "Étudiants assignés : " + studentsList + "\n\n"
                    + "Cordialement,\nVotre équipe de gestion de projets";

            emailService.sendEmail(teacherEmail, subject, body);
        }
    }

    public String cleanEmail(String email) {
        if (email == null) return null;
        return email.trim()
                .replaceAll("^\"|\"$", "")
                .replaceAll("^\\{|\\}$", "")
                .trim()
                .toLowerCase();
    }

    public List<String> cleanEmailsList(List<String> emails) {
        if (emails == null) return new ArrayList<>();
        return emails.stream()
                .map(this::cleanEmail)
                .distinct()
                .collect(Collectors.toList());
    }
    @Override
    public List<ProjetDto> getAllProjetsDTO() {
        return projetRepository.findAll().stream()
                .map(p -> new ProjetDto(p.getIdProjet(), p.getNom()))
                .collect(Collectors.toList());
    }
    @Override
    public List<Projet> getProjetsAffectesParEtudiant(String email) {
        return projetRepository.findByStudentEmailsListContainingIgnoreCase(email);
    }


}