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

// Importation pour le logging
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils; // Pour le nettoyage des noms de fichiers

@Service
public class ProjetServiceImpl implements IProjetService {

    // Utilisation d'un logger pour un meilleur suivi des opérations
    private static final Logger log = LoggerFactory.getLogger(ProjetServiceImpl.class);

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
        // Définit le statut par défaut si non fourni pour les nouveaux projets
        if (projet.getStatut() == null) {
            projet.setStatut(ProjectStatus.PLANNED);
        }
        // Initialise studentEmailsList si elle est nulle pour éviter les NullPointerExceptions
        if (projet.getStudentEmailsList() == null) {
            projet.setStudentEmailsList(new ArrayList<>());
        } else {
            // Nettoyage et normalisation des emails avant la sauvegarde
            projet.setStudentEmailsList(cleanEmailsList(projet.getStudentEmailsList()));
        }
        if (projet.getTeacherEmail() != null) {
            projet.setTeacherEmail(cleanEmail(projet.getTeacherEmail()));
        }

        // Ajout de validation de base pour les dates lors de la création
        validateProjetDates(projet);

        Projet savedProjet = projetRepository.save(projet);
        log.info("Projet créé et sauvegardé avec l'ID : {}", savedProjet.getIdProjet());

        // Envoi des emails d'assignation
        sendAssignmentEmailsOnProjetCreation(savedProjet);

        return savedProjet;
    }

    @Override
    public Projet getProjetById(Long id) {
        // Utilisation d'un message d'erreur plus clair pour l'exception
        return projetRepository.findById(id)
                .orElseThrow(() -> new ProjetNotFoundException("Projet non trouvé avec l'ID : " + id));
    }

    @Override
    public List<Projet> getAllProjets() {
        log.debug("Récupération de tous les projets.");
        return projetRepository.findAll();
    }

    @Override
    @Transactional
    public void deleteProjet(Long id) {
        if (!projetRepository.existsById(id)) {
            throw new ProjetNotFoundException("Projet non trouvé avec l'ID : " + id);
        }
        projetRepository.deleteById(id);
        log.info("Projet avec l'ID {} supprimé avec succès.", id);
    }

    @Override
    @Transactional
    public Projet updateProjet(Long id, Projet projetDetails) {
        Projet existingProjet = getProjetById(id);
        log.info("Début de la mise à jour du projet avec l'ID : {}", id);

        // Capture les anciennes informations pour la comparaison des emails
        String oldTeacherEmail = cleanEmail(existingProjet.getTeacherEmail());
        List<String> oldStudentEmails = cleanEmailsList(existingProjet.getStudentEmailsList());

        // Mise à jour des champs (seuls les champs non nuls et non vides sont mis à jour)
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

        // Gestion de l'email de l'encadrant avec envoi d'email si changement
        String newTeacherEmail = cleanEmail(projetDetails.getTeacherEmail());
        if (newTeacherEmail != null && !newTeacherEmail.equals(oldTeacherEmail)) {
            existingProjet.setTeacherEmail(newTeacherEmail);
            try {
                sendTeacherAssignmentEmail(existingProjet); // Envoi de l'email au nouvel encadrant
                log.info("Email d'assignation envoyé au nouvel encadrant : {}", newTeacherEmail);
            } catch (MessagingException e) {
                log.error("Erreur lors de l'envoi de l'email à l'encadrant {} (mise à jour) : {}", newTeacherEmail, e.getMessage(), e);
            }
        } else if (newTeacherEmail == null && oldTeacherEmail != null) {
            existingProjet.setTeacherEmail(null); // L'encadrant a été retiré
            log.info("L'encadrant a été retiré du projet ID {}", id);
            // Optionnel: Envoyer un email pour informer du retrait de l'assignation
        }

        // Gestion de la liste des emails des étudiants avec envoi d'emails si ajout
        if (projetDetails.getStudentEmailsList() != null) {
            List<String> newStudentEmails = cleanEmailsList(projetDetails.getStudentEmailsList());

            for (String newEmail : newStudentEmails) {
                if (!oldStudentEmails.contains(newEmail)) {
                    try {
                        sendProjetAssignmentEmail(newEmail, existingProjet);
                        log.info("Email d'assignation envoyé à l'étudiant {} (nouvel ajout).", newEmail);
                    } catch (MessagingException e) {
                        log.error("Erreur lors de l'envoi de l'email à l'étudiant {} (nouvel ajout) : {}", newEmail, e.getMessage(), e);
                    }
                }
            }
            existingProjet.setStudentEmailsList(newStudentEmails);
        }

        // Validation des dates après la mise à jour des champs
        validateProjetDates(existingProjet);

        Projet updatedProjet = projetRepository.save(existingProjet);
        log.info("Projet ID {} mis à jour avec succès.", id);
        return updatedProjet;
    }

    @Override
    @Transactional
    public Projet uploadFile(Long projetId, MultipartFile file) throws IOException {
        Projet projet = getProjetById(projetId);

        Path uploadPath = Paths.get(uploadDir, String.valueOf(projetId)).toAbsolutePath().normalize(); // Assure un chemin absolu et normalisé
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            log.info("Dossier de téléchargement créé : {}", uploadPath);
        }

        // Nettoyage du nom de fichier pour des raisons de sécurité
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        if (fileName == null || fileName.isEmpty() || fileName.contains("..")) { // Ajout de vérification de ".."
            log.error("Nom de fichier invalide ou potentiellement malveillant : {}", fileName);
            throw new IllegalArgumentException("Nom de fichier invalide.");
        }

        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        projet.setFilePath(filePath.toString());
        Projet updatedProjet = projetRepository.save(projet);
        log.info("Fichier {} téléchargé pour le projet ID {}. Chemin sauvegardé : {}", fileName, projetId, filePath);
        return updatedProjet;
    }

    @Override
    public byte[] downloadFile(Long projetId) throws IOException {
        Projet projet = getProjetById(projetId);
        if (projet.getFilePath() == null || projet.getFilePath().isEmpty()) {
            log.warn("Aucun chemin de fichier défini pour le projet ID : {}", projetId);
            throw new FileNotFoundException("Aucun fichier trouvé pour ce projet ou chemin non défini.");
        }
        Path filePath = Paths.get(projet.getFilePath());
        if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
            log.error("Fichier non trouvé ou non lisible au chemin : {}", filePath);
            throw new FileNotFoundException("Fichier non trouvé ou non lisible : " + filePath.getFileName());
        }
        log.info("Fichier téléchargé pour le projet ID {}: {}", projetId, filePath.getFileName());
        return Files.readAllBytes(filePath);
    }

    @Override
    @Transactional
    public Projet assignTeacherEmailToProjet(Long projetId, String teacherEmail) {
        Projet projet = getProjetById(projetId);
        String oldTeacherEmail = cleanEmail(projet.getTeacherEmail());
        String cleanedNewEmail = cleanEmail(teacherEmail);

        if (!cleanedNewEmail.equals(oldTeacherEmail)) {
            projet.setTeacherEmail(cleanedNewEmail);
            Projet updatedProjet = projetRepository.save(projet);
            try {
                sendTeacherAssignmentEmail(updatedProjet);
                log.info("Encadrant {} assigné au projet ID {}.", cleanedNewEmail, projetId);
            } catch (MessagingException e) {
                log.error("Erreur lors de l'envoi de l'email à l'encadrant {} (assignation) : {}", cleanedNewEmail, e.getMessage(), e);
            }
            return updatedProjet;
        }
        log.debug("L'encadrant {} est déjà assigné au projet ID {}. Aucune modification nécessaire.", cleanedNewEmail, projetId);
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

        // Vérification si l'email existe déjà (normalisée)
        boolean exists = currentEmails.stream()
                .map(this::cleanEmail)
                .anyMatch(email -> email.equals(normalizedEmail));

        if (!exists) {
            currentEmails.add(normalizedEmail);
            projet.setStudentEmailsList(currentEmails);
            Projet updatedProjet = projetRepository.save(projet);
            try {
                sendProjetAssignmentEmail(normalizedEmail, updatedProjet);
                log.info("Étudiant {} ajouté au projet ID {}.", normalizedEmail, projetId);
            } catch (MessagingException e) {
                log.error("Erreur lors de l'envoi de l'email à l'étudiant {} (ajout) : {}", normalizedEmail, e.getMessage(), e);
            }
            return updatedProjet;
        }
        log.debug("L'étudiant {} est déjà dans le projet ID {}. Aucune modification nécessaire.", normalizedEmail, projetId);
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
                projet.setStudentEmailsList(currentEmails);
                Projet updatedProjet = projetRepository.save(projet);
                log.info("Étudiant {} retiré du projet ID {}.", normalizedEmail, projetId);
                // Optionnel: Envoyer un email pour informer du retrait de l'assignation
                return updatedProjet;
            }
        }
        log.debug("L'étudiant {} n'a pas été trouvé ou n'a pas pu être retiré du projet ID {}.", studentEmail, projetId);
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

        // Envoyer des emails aux nouveaux étudiants assignés
        for (String newEmail : newEmails) {
            if (!oldEmails.contains(newEmail)) {
                try {
                    sendProjetAssignmentEmail(newEmail, projet);
                    log.info("Email d'assignation envoyé à l'étudiant {} (set liste).", newEmail);
                } catch (MessagingException e) {
                    log.error("Erreur lors de l'envoi de l'email à l'étudiant {} (set liste) : {}", newEmail, e.getMessage(), e);
                }
            }
        }
        // Pas besoin de boucle pour les suppressions d'emails ici, la liste est directement remplacée
        projet.setStudentEmailsList(newEmails);
        Projet updatedProjet = projetRepository.save(projet);
        log.info("Liste d'emails des étudiants du projet ID {} mise à jour.", projetId);
        return updatedProjet;
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

        ProjectStatus originalStatus = projet.getStatut();
        ProjectStatus newStatus = originalStatus; // Initialise avec le statut actuel

        if (originalStatus == ProjectStatus.PLANNED && projet.getDateDebut() != null && today.isAfter(projet.getDateDebut())) {
            newStatus = ProjectStatus.IN_PROGRESS;
        } else if (originalStatus == ProjectStatus.IN_PROGRESS && projet.getDateFinPrevue() != null && today.isAfter(projet.getDateFinPrevue())) {
            List<Sprint> sprintsInProject = sprintRepository.findByProjet_IdProjet(projetId);
            boolean allSprintsCompleted = sprintsInProject.stream()
                    .allMatch(sprint -> sprint.getStatut() == SprintStatus.COMPLETED);

            if (allSprintsCompleted) {
                newStatus = ProjectStatus.COMPLETED;
            } else {
                newStatus = ProjectStatus.OVERDUE;
            }
        }

        // Sauvegarde uniquement si le statut a changé
        if (newStatus != originalStatus) {
            projet.setStatut(newStatus);
            projetRepository.save(projet);
            log.info("Le statut du projet ID {} est passé de {} à {}.", projetId, originalStatus, newStatus);
        } else {
            log.debug("Le statut du projet ID {} est déjà {}. Aucune modification nécessaire.", projetId, originalStatus);
        }
    }

    @Override
    @Transactional
    public void checkAndCompleteProjectIfAllSprintsDone(Long projetId) {
        Projet projet = getProjetById(projetId);

        if (projet.getStatut() == ProjectStatus.COMPLETED || projet.getStatut() == ProjectStatus.CANCELLED) {
            log.debug("Projet ID {}: Déjà {} ou {}. Aucune vérification de complétion basée sur les sprints n'est nécessaire.", projetId, projet.getStatut(), ProjectStatus.CANCELLED);
            return;
        }

        List<Sprint> sprintsInProject = sprintRepository.findByProjet_IdProjet(projetId);

        // Si aucun sprint, ne peut pas être complété par sprint
        if (sprintsInProject.isEmpty()) {
            log.debug("Projet ID {}: Aucun sprint trouvé. Impossible de compléter le projet basé sur la complétion des sprints.", projetId);
            return;
        }

        boolean allSprintsCompleted = sprintsInProject.stream()
                .allMatch(sprint -> sprint.getStatut() == SprintStatus.COMPLETED);

        if (allSprintsCompleted) {
            projet.setStatut(ProjectStatus.COMPLETED);
            projetRepository.save(projet);
            log.info("Projet ID {} auto-complété car tous les sprints associés sont COMPLETED.", projetId);
        } else {
            log.debug("Projet ID {}: Tous les sprints ne sont pas encore COMPLETED. Le statut reste {}.", projetId, projet.getStatut());
        }
    }

    // --- Méthodes privées d'aide ---

    // Nouvelle méthode privée pour la validation des dates
    private void validateProjetDates(Projet projet) {
        if (projet.getDateDebut() == null || projet.getDateFinPrevue() == null) {
            log.error("Tentative de sauvegarde/mise à jour d'un projet avec des dates de début ou de fin prévue nulles.");
            throw new IllegalArgumentException("Les dates de début et de fin prévue du projet ne peuvent pas être nulles.");
        }
        if (projet.getDateDebut().isAfter(projet.getDateFinPrevue())) {
            log.error("Tentative de sauvegarde/mise à jour d'un projet où la date de début est postérieure à la date de fin prévue.");
            throw new IllegalArgumentException("La date de début du projet ne peut pas être postérieure à la date de fin prévue.");
        }
    }

    // Nouvelle méthode privée pour centraliser l'envoi d'emails à la création
    private void sendAssignmentEmailsOnProjetCreation(Projet projet) {
        if (projet.getStudentEmailsList() != null && !projet.getStudentEmailsList().isEmpty()) {
            for (String studentEmail : projet.getStudentEmailsList()) {
                try {
                    sendProjetAssignmentEmail(studentEmail, projet);
                    log.info("Email d'assignation envoyé à l'étudiant {} (création).", studentEmail);
                } catch (MessagingException e) {
                    log.error("Erreur lors de l'envoi de l'email à l'étudiant {} (création) : {}", studentEmail, e.getMessage(), e);
                }
            }
        }

        if (projet.getTeacherEmail() != null && !projet.getTeacherEmail().isEmpty()) {
            try {
                sendTeacherAssignmentEmail(projet);
                log.info("Email d'assignation envoyé à l'encadrant {} (création).", projet.getTeacherEmail());
            } catch (MessagingException e) {
                log.error("Erreur lors de l'envoi de l'email à l'encadrant {} (création) : {}", projet.getTeacherEmail(), e.getMessage(), e);
            }
        }
    }

    private void sendProjetAssignmentEmail(String studentEmail, Projet projet) throws MessagingException {
        log.debug("Tentative d'envoi d'email d'assignation de projet à : {}", studentEmail);

        String studentName = studentEmail.split("@")[0]; // Simple extraction du nom avant @
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
        if (projet.getTeacherEmail() == null || projet.getTeacherEmail().isEmpty()) {
            log.warn("Tentative d'envoi d'email à un encadrant sans adresse email définie pour le projet ID {}.", projet.getIdProjet());
            return;
        }
        log.debug("Tentative d'envoi d'email d'assignation d'encadrant à : {}", projet.getTeacherEmail());

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

    /**
     * Nettoie une adresse email en la trimant, en supprimant les guillemets/accolades
     * et en la convertissant en minuscules.
     * @param email L'adresse email à nettoyer.
     * @return L'adresse email nettoyée, ou null si l'entrée est null.
     */
    public String cleanEmail(String email) {
        if (email == null) return null;
        return email.trim()
                .replaceAll("^\"|\"$", "") // Supprime les guillemets au début/fin
                .replaceAll("^\\{|\\}$", "") // Supprime les accolades au début/fin
                .trim()
                .toLowerCase();
    }

    /**
     * Nettoie une liste d'adresses email, supprime les doublons et normalise chaque email.
     * @param emails La liste d'emails à nettoyer.
     * @return Une nouvelle liste d'emails nettoyés et sans doublons.
     */
    public List<String> cleanEmailsList(List<String> emails) {
        if (emails == null) {
            log.warn("Tentative de nettoyer une liste d'emails nulle. Retourne une liste vide.");
            return new ArrayList<>();
        }
        return emails.stream()
                .map(this::cleanEmail)
                .filter(e -> e != null && !e.isEmpty()) // Filtrer les emails vides/null après nettoyage
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public List<ProjetDto> getAllProjetsDTO() {
        log.debug("Récupération de tous les projets au format DTO.");
        return projetRepository.findAll().stream()
                .map(p -> new ProjetDto(p.getIdProjet(), p.getNom()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Projet> getProjetsAffectesParEtudiant(String email) {
        String cleanedEmail = cleanEmail(email);
        if (cleanedEmail == null || cleanedEmail.isEmpty()) {
            log.warn("Tentative de rechercher des projets par un email d'étudiant vide ou nul.");
            return new ArrayList<>();
        }
        log.debug("Recherche de projets affectés par l'étudiant avec l'email : {}", cleanedEmail);
        return projetRepository.findByStudentEmailsListContainingIgnoreCase(cleanedEmail);
    }

    // Nouvelle classe d'exception personnalisée pour une meilleure gestion d'erreur
    public static class ProjetNotFoundException extends RuntimeException {
        public ProjetNotFoundException(String message) {
            super(message);
        }
    }

    // Nouvelle classe d'exception personnalisée pour la gestion des fichiers
    public static class FileNotFoundException extends IOException {
        public FileNotFoundException(String message) {
            super(message);
        }
    }
}