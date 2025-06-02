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
import tn.esprit.pi.entities.enumerations.TaskStatus; // Assuming you have a TaskStatus enum
import tn.esprit.pi.repositories.ProjetRepo;
import tn.esprit.pi.repositories.SprintRepo;
import tn.esprit.pi.repositories.TacheRepo; // Needed for task-related checks
import tn.esprit.pi.email.EmailService; // Needed for sending emails

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SprintServiceImpl implements ISprintService {

    private final SprintRepo sprintRepository;
    private final ProjetRepo projetRepository; // Needed for project-related checks
    private final TacheRepo tacheRepository;   // Needed for task-related checks in sprint completion
    private final ProjetServiceImpl projetService; // To reuse cleanEmail helper
    private final EmailService emailService; // For sending notifications

    @Autowired
    public SprintServiceImpl(SprintRepo sprintRepository, ProjetRepo projetRepository, TacheRepo tacheRepository, ProjetServiceImpl projetService, EmailService emailService) {
        this.sprintRepository = sprintRepository;
        this.projetRepository = projetRepository;
        this.tacheRepository = tacheRepository;
        this.projetService = projetService;
        this.emailService = emailService;
    }

    // --- Basic CRUD Operations ---

    @Override
    @Transactional
    public Sprint saveSprint(Sprint sprint) {
        // Initialize and clean etudiantsAffectes list
        if (sprint.getEtudiantsAffectes() == null) {
            sprint.setEtudiantsAffectes(new ArrayList<>());
        } else {
            sprint.setEtudiantsAffectes(projetService.cleanEmailsList(sprint.getEtudiantsAffectes()));
        }

        // Set default status if not provided
        if (sprint.getStatut() == null) {
            sprint.setStatut(SprintStatus.PLANNED);
        }

        Sprint savedSprint = sprintRepository.save(sprint);

        // Send assignment emails to new students if any
        if (savedSprint.getEtudiantsAffectes() != null && !savedSprint.getEtudiantsAffectes().isEmpty()) {
            for (String studentEmail : savedSprint.getEtudiantsAffectes()) {
                try {
                    sendSprintAssignmentEmail(studentEmail, savedSprint);
                } catch (MessagingException e) {
                    System.err.println("Erreur lors de l'envoi de l'email à l'étudiant " + studentEmail + " (affectation sprint) : " + e.getMessage());
                }
            }
        }
        return savedSprint;
    }


    @Override
    @Transactional
    public Sprint createSprint(CreateSprintDto createSprintDto) {
        // Retrieve the associated project
        Projet projet = projetRepository.findById(createSprintDto.getProjetId())
                .orElseThrow(() -> new RuntimeException("Projet non trouvé avec l'ID : " + createSprintDto.getProjetId()));

        Sprint sprint = new Sprint();
        sprint.setNom(createSprintDto.getNom());
        sprint.setDateDebut(createSprintDto.getDateDebut());
        sprint.setDateFin(createSprintDto.getDateFin());
        sprint.setProjet(projet);
        sprint.setStatut(SprintStatus.PLANNED); // Default status for new sprint
        sprint.setUrgent(createSprintDto.isUrgent());
        sprint.setDeadlineNotificationDate(createSprintDto.getDeadlineNotificationDate());

        // Handle student assignments for the new sprint
        if (createSprintDto.getEtudiantsAffectes() != null && !createSprintDto.getEtudiantsAffectes().isEmpty()) {
            sprint.setEtudiantsAffectes(projetService.cleanEmailsList(createSprintDto.getEtudiantsAffectes()));
        } else {
            sprint.setEtudiantsAffectes(new ArrayList<>());
        }

        return saveSprint(sprint); // Use saveSprint to handle email sending etc.
    }

    @Override
    public Sprint getSprintById(Long id) {
        return sprintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sprint non trouvé avec l'ID : " + id));
    }

    @Override
    public List<Sprint> getAllSprints() {
        return sprintRepository.findAll();
    }

    @Override
    @Transactional
    public void deleteSprint(Long id) {
        if (!sprintRepository.existsById(id)) {
            throw new RuntimeException("Sprint non trouvé avec l'ID : " + id);
        }
        sprintRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Sprint updateSprint(Long id, Sprint sprintDetails) {
        Sprint existingSprint = getSprintById(id);

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
        if (sprintDetails.isUrgent() != existingSprint.isUrgent()) { // Boolean check
            existingSprint.setUrgent(sprintDetails.isUrgent());
        }
        if (sprintDetails.getDeadlineNotificationDate() != null) {
            existingSprint.setDeadlineNotificationDate(sprintDetails.getDeadlineNotificationDate());
        }
        // If a project is set in sprintDetails, update the association
        if (sprintDetails.getProjet() != null && sprintDetails.getProjet().getIdProjet() != null) {
            Projet newProjet = projetRepository.findById(sprintDetails.getProjet().getIdProjet())
                    .orElseThrow(() -> new RuntimeException("Projet spécifié non trouvé."));
            existingSprint.setProjet(newProjet);
        }

        // Handle student email list updates
        if (sprintDetails.getEtudiantsAffectes() != null) {
            List<String> newStudentEmails = projetService.cleanEmailsList(sprintDetails.getEtudiantsAffectes());

            // Send emails to newly added students
            for (String newEmail : newStudentEmails) {
                if (!oldStudentEmails.contains(newEmail)) {
                    try {
                        sendSprintAssignmentEmail(newEmail, existingSprint);
                    } catch (MessagingException e) {
                        System.err.println("Erreur lors de l'envoi de l'email à l'étudiant " + newEmail + " (mise à jour sprint) : " + e.getMessage());
                    }
                }
            }
            existingSprint.setEtudiantsAffectes(newStudentEmails);
        } else {
            // If the incoming list is null, decide whether to clear or keep existing
            // For now, let's clear if explicitly passed as null or empty
            existingSprint.setEtudiantsAffectes(new ArrayList<>());
        }

        return sprintRepository.save(existingSprint);
    }

    // --- Student Assignment Operations ---

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
            } catch (MessagingException e) {
                System.err.println("Erreur lors de l'envoi de l'email à l'étudiant " + normalizedEmail + " (affectation) : " + e.getMessage());
            }
            return updatedSprint;
        }
        return sprint; // Student already assigned
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
                return sprintRepository.save(sprint);
            }
        }
        return sprint; // Student not found in list
    }

    @Override
    public List<String> getEtudiantsAffectesAuSprint(Long sprintId) {
        Sprint sprint = getSprintById(sprintId);
        return sprint.getEtudiantsAffectes() != null ? new ArrayList<>(sprint.getEtudiantsAffectes()) : new ArrayList<>();
    }


    // --- Student-specific Filtering (NEW - as per discussion) ---
    @Override
    public List<Sprint> getSprintsByStudentEmail(String studentEmail) { // <-- CORRECTED METHOD NAME AND PARAMETER NAME
        String cleanedEmail = projetService.cleanEmail(studentEmail); // Use 'studentEmail' directly
        return sprintRepository.findByEtudiantsAffectesContainingIgnoreCase(cleanedEmail);
    }


    // --- Calendar Related ---

    @Override
    public List<CalendarEventDto> getAllCalendarEvents() {
        // Implement logic to convert sprints/tasks to calendar events
        // Placeholder implementation
        return sprintRepository.findAll().stream()
                .map(sprint -> {
                    CalendarEventDto event = new CalendarEventDto();
                    event.setId(sprint.getIdSprint());
                    event.setTitle(sprint.getNom());
                    event.setStart(sprint.getDateDebut().toString()); // Convert LocalDate to String
                    event.setEnd(sprint.getDateFin().toString());     // Convert LocalDate to String
                    event.setColor("#337ab7"); // Example color for sprints
                    event.setCategory("Sprint");
                    // Optionally add project name or status to description
                    event.setDescription("Sprint for Project: " + (sprint.getProjet() != null ? sprint.getProjet().getNom() : "N/A"));
                    return event;
                })
                .collect(Collectors.toList());
    }

    // --- Search Operations ---

    @Override
    public List<Sprint> searchSprintsByNom(String nom) {
        // Implement search logic (e.g., using JpaRepository method like findByNomContainingIgnoreCase)
        // Placeholder
        return sprintRepository.findByNomContainingIgnoreCase(nom); // Assuming you add this to SprintRepo
    }


    // --- Tasks and DTOs ---

    @Override
    public Optional<SprintWithTasksDTO> getSprintWithTasks(Long sprintId) {
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

                    // Populate tasks if loaded
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
        Sprint sprint = getSprintById(sprintId);
        tache.setSprint(sprint);
        // Set default status for new task if not provided
        if (tache.getStatut() == null) {
            tache.setStatut(TaskStatus.TODO); // Assuming TaskStatus.TODO is your default
        }
        return tacheRepository.save(tache);
    }

    // --- Velocity Calculations ---

    @Override
    public double calculateSprintVelocity(Long sprintId) {
        Sprint sprint = getSprintById(sprintId);
        // Assuming your Tache entity has a field for 'points' or 'estimations'
        // For simplicity, let's assume 'points' is an integer in Tache
        // Sum points of completed tasks
        return sprint.getTaches().stream()
                .filter(tache -> tache.getStatut() == TaskStatus.DONE) // Assuming DONE status
                .mapToDouble(Tache::getStoryPoints) // Corrected to use getStoryPoints
                .sum();
    }

    @Override
    public List<Object[]> getVelocityHistory() {
        // This is a complex method that would require a custom query or more elaborate
        // logic to get historical data for committed vs completed points across sprints.
        // Placeholder for now. You might need to project specific fields from sprints
        // and their tasks to get this data efficiently.
        // Example: [sprintName, committedPoints, completedPoints]
        System.out.println("Fetching velocity history - Placeholder implementation.");
        return Collections.emptyList(); // Return empty list for now
    }


    // --- Initial Sprint Generation ---

    @Override
    @Transactional
    public Sprint generateInitialSprintForProject(Projet projet) {
        if (projet == null || projet.getIdProjet() == null) {
            throw new IllegalArgumentException("Le projet ne doit pas être nul.");
        }

        // Check if an initial sprint already exists for this project
        List<Sprint> existingSprints = sprintRepository.findByProjet_IdProjet(projet.getIdProjet());
        if (!existingSprints.isEmpty()) {
            // Option 1: Throw an error if only one initial sprint is allowed
            // throw new IllegalStateException("Un sprint initial existe déjà pour ce projet.");
            // Option 2: Just return the first one found (if you allow multiple sprints and don't care about "initial" specific)
            System.out.println("Sprint(s) already exist for project " + projet.getNom() + ". Not generating initial sprint.");
            return existingSprints.get(0); // Return the first existing sprint
        }

        Sprint initialSprint = new Sprint();
        initialSprint.setNom("Initial Sprint for " + projet.getNom());
        initialSprint.setDateDebut(projet.getDateDebut());
        // Set end date, e.g., 2 weeks from start or same as project start if tasks define duration
        initialSprint.setDateFin(projet.getDateDebut().plusWeeks(2));
        initialSprint.setProjet(projet);
        initialSprint.setStatut(SprintStatus.PLANNED);
        initialSprint.setUrgent(false);
        initialSprint.setEtudiantsAffectes(new ArrayList<>()); // Initially empty or copy from project
        if (projet.getStudentEmailsList() != null) {
            initialSprint.setEtudiantsAffectes(new ArrayList<>(projet.getStudentEmailsList()));
        }

        // Save the new sprint
        return saveSprint(initialSprint); // Use saveSprint to handle email sending etc.
    }


    // --- Project-specific sprints ---

    @Override
    public List<Sprint> findByProjetId(Long projetId) {
        return sprintRepository.findByProjet_IdProjet(projetId);
    }


    // --- Deadline Notifications ---

    @Override
    public List<Sprint> getSprintsWithUpcomingDeadlines() {
        LocalDate today = LocalDate.now();
        // Assuming a method like findByDeadlineNotificationDateGreaterThanEqualAndDeadlineNotificationDateLessThanEqual
        // Or a custom query to get sprints with deadlines in the near future (e.g., next 7 days)
        // For simplicity, let's just get all planned/in_progress sprints that are urgent and past notification date
        return sprintRepository.findAll().stream()
                .filter(sprint -> (sprint.getStatut() == SprintStatus.PLANNED || sprint.getStatut() == SprintStatus.IN_PROGRESS)
                        && sprint.isUrgent()
                        && sprint.getDeadlineNotificationDate() != null
                        && today.isAfter(sprint.getDeadlineNotificationDate()))
                .collect(Collectors.toList());
        // You would likely have a scheduled task call this and send emails
    }


    // --- Automatic Sprint Completion ---

    @Override
    @Transactional
    public void checkAndCompleteSprintIfAllTasksDone(Long sprintId) {
        Sprint sprint = getSprintById(sprintId);

        // Don't re-process completed or cancelled sprints
        if (sprint.getStatut() == SprintStatus.COMPLETED || sprint.getStatut() == SprintStatus.CANCELLED) {
            System.out.println("Sprint ID " + sprintId + ": Already COMPLETED or CANCELLED. No task-based completion check needed.");
            return;
        }

        List<Tache> tasksInSprint = tacheRepository.findBySprint_IdSprint(sprintId);

        if (tasksInSprint.isEmpty()) {
            System.out.println("Sprint ID " + sprintId + ": No tasks found. Cannot complete sprint based on task completion.");
            return;
        }

        boolean allTasksCompleted = tasksInSprint.stream()
                .allMatch(tache -> tache.getStatut() == TaskStatus.DONE); // Assuming TaskStatus.DONE

        if (allTasksCompleted) {
            sprint.setStatut(SprintStatus.COMPLETED);
            sprintRepository.save(sprint);
            System.out.println("Sprint " + sprintId + " auto-completed because all associated tasks are DONE.");

            // Also check if the parent project can be completed
            if (sprint.getProjet() != null) {
                projetService.checkAndCompleteProjectIfAllSprintsDone(sprint.getProjet().getIdProjet());
            }
        }
    }

    @Override
    @Transactional
    public void checkAndCompleteProjectIfAllSprintsDone(Long projectId) {
        // This method is already implemented in ProjetServiceImpl,
        // so we just call it.
        projetService.checkAndCompleteProjectIfAllSprintsDone(projectId);
    }


    // --- Helper Methods (Private) ---

    private void sendSprintAssignmentEmail(String studentEmail, Sprint sprint) throws MessagingException {
        System.out.println("Sending sprint assignment email to: " + studentEmail);

        String studentName = studentEmail.split("@")[0]; // Simple name extraction
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