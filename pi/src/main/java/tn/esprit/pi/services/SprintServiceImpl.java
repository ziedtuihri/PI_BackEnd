package tn.esprit.pi.services;

import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi.dto.CalendarEventDto;
import tn.esprit.pi.dto.CreateSprintDto;
import tn.esprit.pi.dto.SprintWithTasksDTO;
import tn.esprit.pi.entities.Projet;
import tn.esprit.pi.entities.Sprint;
import tn.esprit.pi.entities.Tache;
import tn.esprit.pi.entities.enumerations.ProjectStatus;
import tn.esprit.pi.entities.enumerations.SprintStatus;
import tn.esprit.pi.entities.enumerations.TaskStatus;
import tn.esprit.pi.repositories.ProjetRepo;
import tn.esprit.pi.repositories.SprintRepo;
import tn.esprit.pi.repositories.TacheRepo;
import tn.esprit.pi.email.EmailService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class SprintServiceImpl implements ISprintService {

    private static final Logger log = LoggerFactory.getLogger(SprintServiceImpl.class);

    private final SprintRepo sprintRepository;
    private final ProjetRepo projetRepository;
    private final TacheRepo tacheRepository;
    private final ProjetServiceImpl projetService;
    private final EmailService emailService;

    @Autowired
    public SprintServiceImpl(SprintRepo sprintRepository, ProjetRepo projetRepository, TacheRepo tacheRepository, ProjetServiceImpl projetService, EmailService emailService) {
        this.sprintRepository = sprintRepository;
        this.projetRepository = projetRepository;
        this.tacheRepository = tacheRepository;
        this.projetService = projetService;
        this.emailService = emailService;
    }

    // --- Opérations CRUD de Base ---

    @Override
    @Transactional
    public Sprint saveSprint(Sprint sprint) {
        if (sprint.getEtudiantsAffectes() == null) {
            sprint.setEtudiantsAffectes(new ArrayList<>());
        } else {
            sprint.setEtudiantsAffectes(projetService.cleanEmailsList(sprint.getEtudiantsAffectes()));
        }

        if (sprint.getStatut() == null) {
            sprint.setStatut(SprintStatus.PLANNED);
        }

        validateSprintDates(sprint);

        Sprint savedSprint = sprintRepository.save(sprint);
        log.info("Sprint sauvegardé avec succès, ID : {}", savedSprint.getIdSprint());

        sendSprintAssignmentEmails(savedSprint);

        return savedSprint;
    }

    @Override
    @Transactional
    public Sprint createSprint(CreateSprintDto createSprintDto) {
        log.info("Tentative de création d'un sprint pour le projet ID : {}", createSprintDto.getProjetId());
        Projet projet = projetRepository.findById(createSprintDto.getProjetId())
                .orElseThrow(() -> new ProjetServiceImpl.ProjetNotFoundException("Projet non trouvé avec l'ID : " + createSprintDto.getProjetId()));

        Sprint sprint = new Sprint();
        sprint.setNom(createSprintDto.getNom());
        sprint.setDateDebut(createSprintDto.getDateDebut());
        sprint.setDateFin(createSprintDto.getDateFin());
        sprint.setProjet(projet);
        sprint.setStatut(SprintStatus.PLANNED);
        sprint.setUrgent(createSprintDto.isUrgent());
        sprint.setDeadlineNotificationDate(createSprintDto.getDeadlineNotificationDate());

        if (createSprintDto.getEtudiantsAffectes() != null && !createSprintDto.getEtudiantsAffectes().isEmpty()) {
            sprint.setEtudiantsAffectes(projetService.cleanEmailsList(createSprintDto.getEtudiantsAffectes()));
        } else {
            sprint.setEtudiantsAffectes(new ArrayList<>());
        }

        return saveSprint(sprint);
    }

    @Override
    public Sprint getSprintById(Long id) {
        log.debug("Récupération du sprint avec l'ID : {}", id);
        return sprintRepository.findById(id)
                .orElseThrow(() -> new SprintNotFoundException("Sprint non trouvé avec l'ID : " + id));
    }

    @Override
    public List<Sprint> getAllSprints() {
        log.debug("Récupération de tous les sprints.");
        return sprintRepository.findAll();
    }

    @Override
    @Transactional
    public void deleteSprint(Long id) {
        if (!sprintRepository.existsById(id)) {
            throw new SprintNotFoundException("Sprint non trouvé avec l'ID : " + id);
        }
        sprintRepository.deleteById(id);
        log.info("Sprint avec l'ID {} supprimé avec succès.", id);
    }

    @Override
    @Transactional
    public Sprint updateSprint(Long id, Sprint sprintDetails) {
        Sprint existingSprint = getSprintById(id);
        log.info("Début de la mise à jour du sprint avec l'ID : {}", id);

        List<String> oldStudentEmails = existingSprint.getEtudiantsAffectes() != null ? new ArrayList<>(existingSprint.getEtudiantsAffectes()) : new ArrayList<>();

        if (sprintDetails.getNom() != null && !sprintDetails.getNom().isEmpty()) {
            existingSprint.setNom(sprintDetails.getNom());
        }
        if (sprintDetails.getDateDebut() != null) {
            existingSprint.setDateDebut(sprintDetails.getDateDebut());
        }
        if (sprintDetails.getDateFin() != null) {
            existingSprint.setDateFin(sprintDetails.getDateFin());
        }
        if (sprintDetails.getStatut() != null) {
            existingSprint.setStatut(sprintDetails.getStatut());
        }
        existingSprint.setUrgent(sprintDetails.isUrgent());

        if (sprintDetails.getDeadlineNotificationDate() != null) {
            existingSprint.setDeadlineNotificationDate(sprintDetails.getDeadlineNotificationDate());
        }

        if (sprintDetails.getProjet() != null && sprintDetails.getProjet().getIdProjet() != null) {
            if (!sprintDetails.getProjet().getIdProjet().equals(existingSprint.getProjet() != null ? existingSprint.getProjet().getIdProjet() : null)) {
                Projet newProjet = projetRepository.findById(sprintDetails.getProjet().getIdProjet())
                        .orElseThrow(() -> new ProjetServiceImpl.ProjetNotFoundException("Projet spécifié non trouvé avec l'ID : " + sprintDetails.getProjet().getIdProjet()));
                existingSprint.setProjet(newProjet);
                log.info("Projet du sprint ID {} changé pour le projet ID {}.", id, newProjet.getIdProjet());
            }
        } else if (sprintDetails.getProjet() == null && existingSprint.getProjet() != null) {
            existingSprint.setProjet(null);
            log.info("Projet détaché du sprint ID {}.", id);
        }

        if (sprintDetails.getEtudiantsAffectes() != null) {
            List<String> newStudentEmails = projetService.cleanEmailsList(sprintDetails.getEtudiantsAffectes());

            for (String newEmail : newStudentEmails) {
                if (!oldStudentEmails.contains(newEmail)) {
                    try {
                        sendSprintAssignmentEmail(newEmail, existingSprint);
                        log.info("Email d'assignation envoyé à l'étudiant {} (mise à jour sprint).", newEmail);
                    } catch (MessagingException e) {
                        log.error("Erreur lors de l'envoi de l'email à l'étudiant {} (mise à jour sprint) : {}", newEmail, e.getMessage(), e);
                    }
                }
            }
            existingSprint.setEtudiantsAffectes(newStudentEmails);
        } else {
            existingSprint.setEtudiantsAffectes(new ArrayList<>());
        }

        validateSprintDates(existingSprint);

        Sprint updatedSprint = sprintRepository.save(existingSprint);
        log.info("Sprint ID {} mis à jour avec succès.", id);
        return updatedSprint;
    }

    // --- Opérations d'affectation d'étudiants ---

    @Override
    @Transactional
    public Sprint affecterEtudiantAuSprint(Long sprintId, String etudiantEmail) {
        Sprint sprint = getSprintById(sprintId);
        List<String> currentEmails = sprint.getEtudiantsAffectes();
        if (currentEmails == null) {
            currentEmails = new ArrayList<>();
        }

        String normalizedEmail = projetService.cleanEmail(etudiantEmail);

        boolean exists = currentEmails.stream()
                .map(projetService::cleanEmail)
                .anyMatch(email -> email.equals(normalizedEmail));

        if (!exists) {
            currentEmails.add(normalizedEmail);
            sprint.setEtudiantsAffectes(currentEmails);
            Sprint updatedSprint = sprintRepository.save(sprint);
            try {
                sendSprintAssignmentEmail(normalizedEmail, updatedSprint);
                log.info("Étudiant {} affecté au sprint ID {}.", normalizedEmail, sprintId);
            } catch (MessagingException e) {
                log.error("Erreur lors de l'envoi de l'email à l'étudiant {} (affectation) : {}", normalizedEmail, e.getMessage(), e);
            }
            return updatedSprint;
        }
        log.debug("L'étudiant {} est déjà affecté au sprint ID {}. Aucune modification nécessaire.", normalizedEmail, sprintId);
        return sprint;
    }

    @Override
    @Transactional
    public Sprint supprimerEtudiantDuSprint(Long sprintId, String etudiantEmail) {
        Sprint sprint = getSprintById(sprintId);
        List<String> currentEmails = sprint.getEtudiantsAffectes();
        if (currentEmails != null) {
            String normalizedEmail = projetService.cleanEmail(etudiantEmail);
            boolean removed = currentEmails.removeIf(email -> projetService.cleanEmail(email).equals(normalizedEmail));
            if (removed) {
                sprint.setEtudiantsAffectes(currentEmails);
                Sprint updatedSprint = sprintRepository.save(sprint);
                log.info("Étudiant {} supprimé du sprint ID {}.", normalizedEmail, sprintId);
                return updatedSprint;
            }
        }
        log.debug("L'étudiant {} n'a pas été trouvé dans la liste des affectations du sprint ID {}. Aucune modification nécessaire.", etudiantEmail, sprintId);
        return sprint;
    }

    @Override
    public List<String> getEtudiantsAffectesAuSprint(Long sprintId) {
        Sprint sprint = getSprintById(sprintId);
        return sprint.getEtudiantsAffectes() != null ? new ArrayList<>(sprint.getEtudiantsAffectes()) : new ArrayList<>();
    }

    // --- Filtrage spécifique aux étudiants ---
    @Override
    public List<Sprint> getSprintsByStudentEmail(String studentEmail) {
        String cleanedEmail = projetService.cleanEmail(studentEmail);
        if (cleanedEmail == null || cleanedEmail.isEmpty()) {
            log.warn("Tentative de rechercher des sprints par un email d'étudiant vide ou nul.");
            return new ArrayList<>();
        }
        log.debug("Recherche de sprints affectés par l'étudiant avec l'email : {}", cleanedEmail);
        return sprintRepository.findByEtudiantsAffectesContainingIgnoreCase(cleanedEmail);
    }

    // --- Lié au calendrier ---

    @Override
    public List<CalendarEventDto> getAllCalendarEvents() {
        log.debug("Génération de tous les événements de calendrier à partir des sprints.");
        return sprintRepository.findAll().stream()
                .map(sprint -> {
                    CalendarEventDto event = new CalendarEventDto();
                    event.setId(sprint.getIdSprint());
                    event.setTitle(sprint.getNom());
                    event.setStart(sprint.getDateDebut() != null ? sprint.getDateDebut().toString() : null);
                    event.setEnd(sprint.getDateFin() != null ? sprint.getDateFin().toString() : null);
                    event.setColor("#337ab7");
                    event.setCategory("Sprint");
                    event.setDescription("Sprint pour le projet : " + (sprint.getProjet() != null ? sprint.getProjet().getNom() : "N/A") +
                            " (Statut : " + sprint.getStatut() + ")");
                    return event;
                })
                .collect(Collectors.toList());
    }

    // --- Opérations de recherche ---

    @Override
    public List<Sprint> searchSprintsByNom(String nom) {
        if (nom == null || nom.trim().isEmpty()) {
            log.warn("Tentative de rechercher des sprints par un nom vide ou nul.");
            return new ArrayList<>();
        }
        log.debug("Recherche de sprints par nom contenant : {}", nom);
        return sprintRepository.findByNomContainingIgnoreCase(nom);
    }

    // --- Tâches et DTOs ---

    @Override
    public Optional<SprintWithTasksDTO> getSprintWithTasks(Long sprintId) {
        log.debug("Récupération du sprint avec tâches pour l'ID : {}", sprintId);
        return sprintRepository.findById(sprintId)
                .map(sprint -> {
                    SprintWithTasksDTO dto = new SprintWithTasksDTO();
                    dto.setSprintId(sprint.getIdSprint());
                    dto.setNom(sprint.getNom());
                    dto.setDateDebut(sprint.getDateDebut());
                    dto.setDateFin(sprint.getDateFin());
                    dto.setStatut(sprint.getStatut());
                    dto.setUrgent(sprint.isUrgent());
                    dto.setDeadlineNotificationDate(sprint.getDeadlineNotificationDate());
                    dto.setProjetId(sprint.getProjet() != null ? sprint.getProjet().getIdProjet() : null);
                    dto.setProjetNom(sprint.getProjet() != null ? sprint.getProjet().getNom() : null);
                    dto.setEtudiantsAffectes(sprint.getEtudiantsAffectes());

                    if (sprint.getTaches() != null) {
                        dto.setTasks(sprint.getTaches().stream()
                                .map(tache -> new SprintWithTasksDTO.TaskDTO(tache.getIdTache(), tache.getNom(), tache.getStatut()))
                                .collect(Collectors.toList()));
                    } else {
                        dto.setTasks(Collections.emptyList());
                    }
                    return dto;
                });
    }

    @Override
    @Transactional
    public Tache createTaskForSprint(Long sprintId, Tache tache) {
        log.info("Création d'une tâche pour le sprint ID : {}", sprintId);
        Sprint sprint = getSprintById(sprintId);
        tache.setSprint(sprint);
        if (tache.getStatut() == null) {
            tache.setStatut(TaskStatus.TODO);
        }
        return tacheRepository.save(tache);
    }

    // --- Calculs de Vélocité ---

    @Override
    public double calculateSprintVelocity(Long sprintId) {
        log.debug("Calcul de la vélocité pour le sprint ID : {}", sprintId);
        Sprint sprint = getSprintById(sprintId);
        return sprint.getTaches().stream()
                .filter(tache -> tache.getStatut() == TaskStatus.DONE)
                .mapToDouble(Tache::getStoryPoints)
                .sum();
    }

    @Override
    @Transactional // Add @Transactional to ensure tasks are loaded
    public List<Object[]> getVelocityHistory() {
        log.info("Retrieving velocity history for completed sprints.");
        List<Object[]> velocityHistory = new ArrayList<>();

        // Fetch all completed sprints. You might need to add findByStatut to SprintRepo.
        List<Sprint> completedSprints = sprintRepository.findByStatut(SprintStatus.COMPLETED);

        for (Sprint sprint : completedSprints) {
            double committedPoints = 0.0;
            double completedPoints = 0.0;

            // Ensure tasks are loaded. If the relationship is LAZY, this needs to be inside @Transactional
            // or use a JOIN FETCH query in the repository.
            // Assuming getTaches() is configured to load tasks or this method is transactional.
            if (sprint.getTaches() != null) {
                for (Tache tache : sprint.getTaches()) {
                    if (tache.getStoryPoints() != null) {
                        committedPoints += tache.getStoryPoints(); // Sum all story points for committed
                        if (tache.getStatut() == TaskStatus.DONE) {
                            completedPoints += tache.getStoryPoints(); // Sum only DONE task story points for completed
                        }
                    }
                }
            }
            velocityHistory.add(new Object[]{sprint.getNom(), committedPoints, completedPoints});
        }
        log.info("Generated velocity history for {} sprints.", velocityHistory.size());
        return velocityHistory;
    }


    // --- Génération de Sprint Initial ---

    @Override
    @Transactional
    public Sprint generateInitialSprintForProject(Projet projet) {
        if (projet == null || projet.getIdProjet() == null) {
            log.error("Tentative de générer un sprint initial avec un projet nul ou sans ID.");
            throw new IllegalArgumentException("Le projet ne doit pas être nul et doit avoir un ID.");
        }
        log.info("Tentative de générer un sprint initial pour le projet ID : {}", projet.getIdProjet());

        List<Sprint> existingSprints = sprintRepository.findByProjet_IdProjet(projet.getIdProjet());
        if (!existingSprints.isEmpty()) {
            log.warn("Sprint(s) existe(nt) déjà pour le projet ID {}. Ne génère pas de sprint initial.", projet.getIdProjet());
            return existingSprints.get(0);
        }

        Sprint initialSprint = new Sprint();
        initialSprint.setNom("Sprint Initial pour " + projet.getNom());
        initialSprint.setDateDebut(projet.getDateDebut());
        initialSprint.setDateFin(projet.getDateDebut() != null ? projet.getDateDebut().plusWeeks(2) : null);
        initialSprint.setProjet(projet);
        initialSprint.setStatut(SprintStatus.PLANNED);
        initialSprint.setUrgent(false);
        initialSprint.setEtudiantsAffectes(new ArrayList<>());
        if (projet.getStudentEmailsList() != null) {
            initialSprint.setEtudiantsAffectes(new ArrayList<>(projet.getStudentEmailsList()));
        }

        return saveSprint(initialSprint);
    }

    // --- Sprints spécifiques au projet ---

    @Override
    public List<Sprint> findByProjetId(Long projetId) {
        log.debug("Recherche de sprints pour le projet ID : {}", projetId);
        return sprintRepository.findByProjet_IdProjet(projetId);
    }

    // --- Notifications de deadline ---

    @Override
    public List<Sprint> getSprintsWithUpcomingDeadlines() {
        LocalDate today = LocalDate.now();
        log.debug("Recherche de sprints avec des échéances imminentes à partir d'aujourd'hui : {}", today);
        return sprintRepository.findAll().stream()
                .filter(sprint -> (sprint.getStatut() == SprintStatus.PLANNED || sprint.getStatut() == SprintStatus.IN_PROGRESS)
                        && sprint.isUrgent()
                        && sprint.getDeadlineNotificationDate() != null
                        && today.isAfter(sprint.getDeadlineNotificationDate()))
                .collect(Collectors.toList());
    }

    // --- Complétion automatique de sprint ---

    @Override
    @Transactional
    public void checkAndCompleteSprintIfAllTasksDone(Long sprintId) {
        Sprint sprint = getSprintById(sprintId);

        if (sprint.getStatut() == SprintStatus.COMPLETED || sprint.getStatut() == SprintStatus.CANCELLED) {
            log.debug("Sprint ID {} : Déjà {} ou {}. Aucune vérification de complétion basée sur les tâches nécessaire.", sprintId, sprint.getStatut(), SprintStatus.CANCELLED);
            return;
        }

        List<Tache> tasksInSprint = tacheRepository.findBySprint_IdSprint(sprintId);

        if (tasksInSprint.isEmpty()) {
            log.debug("Sprint ID {} : Aucune tâche trouvée. Impossible de compléter le sprint basé sur la complétion des tâches.", sprintId);
            return;
        }

        boolean allTasksCompleted = tasksInSprint.stream()
                .allMatch(tache -> tache.getStatut() == TaskStatus.DONE);

        if (allTasksCompleted) {
            sprint.setStatut(SprintStatus.COMPLETED);
            sprintRepository.save(sprint);
            log.info("Sprint ID {} auto-complété car toutes les tâches associées sont COMPLETED.", sprintId);

            if (sprint.getProjet() != null) {
                projetService.checkAndCompleteProjectIfAllSprintsDone(sprint.getProjet().getIdProjet());
            }
        } else {
            log.debug("Sprint ID {} : Toutes les tâches ne sont pas encore COMPLETED. Le statut reste {}.", sprintId, sprint.getStatut());
        }
    }

    @Override
    @Transactional
    public void checkAndCompleteProjectIfAllSprintsDone(Long projectId) {
        projetService.checkAndCompleteProjectIfAllSprintsDone(projectId);
    }

    // --- Méthodes d'aide (Privées) ---

    public static class SprintNotFoundException extends RuntimeException {
        public SprintNotFoundException(String message) {
            super(message);
        }
    }

    private void validateSprintDates(Sprint sprint) {
        if (sprint.getDateDebut() == null || sprint.getDateFin() == null) {
            log.error("Tentative de sauvegarder/mettre à jour un sprint avec des dates de début ou de fin nulles.");
            throw new IllegalArgumentException("Les dates de début et de fin du sprint ne peuvent pas être nulles.");
        }
        if (sprint.getDateDebut().isAfter(sprint.getDateFin())) {
            log.error("Tentative de sauvegarder/mettre à jour un sprint où la date de début est postérieure à la date de fin.");
            throw new IllegalArgumentException("La date de début du sprint ne peut pas être postérieure à la date de fin.");
        }
    }

    private void sendSprintAssignmentEmails(Sprint sprint) {
        if (sprint.getEtudiantsAffectes() != null && !sprint.getEtudiantsAffectes().isEmpty()) {
            for (String studentEmail : sprint.getEtudiantsAffectes()) {
                try {
                    sendSprintAssignmentEmail(studentEmail, sprint);
                } catch (MessagingException e) {
                    log.error("Erreur lors de l'envoi de l'email à l'étudiant {} (affectation sprint) : {}", studentEmail, e.getMessage(), e);
                }
            }
        }
    }

    private void sendSprintAssignmentEmail(String studentEmail, Sprint sprint) throws MessagingException {
        log.debug("Tentative d'envoi d'email d'affectation de sprint à : {}", studentEmail);

        String studentName = studentEmail.split("@")[0];
        String projectName = (sprint.getProjet() != null) ? sprint.getProjet().getNom() : "Non spécifié";

        String subject = "Vous avez été affecté(e) au sprint : " + sprint.getNom() + " (Projet: " + projectName + ")";
        String body = "Bonjour " + studentName + ",\n\n"
                + "Vous avez été affecté(e) au sprint suivant :\n\n"
                + "Nom du Sprint : " + sprint.getNom() + "\n"
                + "Projet Associé : " + projectName + "\n"
                + "Date de Début : " + (sprint.getDateDebut() != null ? sprint.getDateDebut().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A") + "\n"
                + "Date de Fin : " + (sprint.getDateFin() != null ? sprint.getDateFin().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A") + "\n"
                + "Statut : " + (sprint.getStatut() != null ? sprint.getStatut().name() : "N/A") + "\n"
                + "Urgent : " + (sprint.isUrgent() ? "Oui" : "Non") + "\n\n"
                + "Cordialement,\nVotre équipe de gestion de projets";

        emailService.sendEmail(studentEmail, subject, body);
    }
}