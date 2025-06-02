package tn.esprit.pi.services;

import tn.esprit.pi.dto.TacheCreationDTO;
import tn.esprit.pi.entities.Tache;
import tn.esprit.pi.entities.enumerations.TaskStatus;
import tn.esprit.pi.repositories.TacheRepo;
import tn.esprit.pi.repositories.SprintRepo;
import tn.esprit.pi.entities.Sprint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional; // Import Optional

@Service
public class TacheServiceImpl implements ITacheService {

    private final TacheRepo tacheRepository;
    private final SprintRepo sprintRepository;
    private final ISprintService sprintService;
    private final IProjetService projetService; // Inject IProjetService for cleanEmail

    @Autowired
    public TacheServiceImpl(TacheRepo tacheRepository, SprintRepo sprintRepository,
                            ISprintService sprintService, IProjetService projetService) { // Add IProjetService
        this.tacheRepository = tacheRepository;
        this.sprintRepository = sprintRepository;
        this.sprintService = sprintService;
        this.projetService = projetService; // Initialize
    }

    @Override
    @Transactional
    public Tache createTacheFromDTO(TacheCreationDTO tacheCreationDTO) {
        Tache tache = new Tache();

        tache.setNom(tacheCreationDTO.getNom());
        tache.setDescription(tacheCreationDTO.getDescription());
        tache.setDateDebut(tacheCreationDTO.getDateDebut());
        tache.setDateFin(tacheCreationDTO.getDateFin());
        // Set default status if not provided in DTO
        tache.setStatut(tacheCreationDTO.getStatut() != null ? tacheCreationDTO.getStatut() : TaskStatus.TODO);
        tache.setStoryPoints(tacheCreationDTO.getStoryPoints());
        tache.setEstimatedHours(tacheCreationDTO.getEstimatedHours());
        tache.setLoggedHours(0.0); // Initialize logged hours to 0
        tache.setEtudiantsAffectes(new ArrayList<>()); // Initialize an empty list

        // Basic validation
        if (tache.getNom() == null || tache.getNom().trim().isEmpty()) {
            throw new RuntimeException("Le nom de la tâche ne peut pas être vide.");
        }
        if (tache.getDateDebut() == null || tache.getDateFin() == null) {
            throw new RuntimeException("Les dates de début et fin ne peuvent pas être nulles.");
        }
        if (tache.getDateDebut().isAfter(tache.getDateFin())) {
            throw new RuntimeException("La date de début ne peut pas être après la date de fin.");
        }

        // Link to Sprint and Project if sprintId is provided in DTO
        if (tacheCreationDTO.getSprintId() != null) {
            Sprint sprint = sprintRepository.findById(tacheCreationDTO.getSprintId())
                    .orElseThrow(() -> new RuntimeException("Sprint non trouvé avec ID : " + tacheCreationDTO.getSprintId()));
            tache.setSprint(sprint);
            tache.setProjet(sprint.getProjet()); // Task inherits project from sprint

            // Inherit assigned students from the sprint
            if (sprint.getEtudiantsAffectes() != null) {
                tache.setEtudiantsAffectes(new ArrayList<>(sprint.getEtudiantsAffectes()));
            }

            validateTaskDatesWithSprint(tache); // Validate task dates against sprint dates
        } else {
            // If no sprint, ensure sprint and project are null
            tache.setSprint(null);
            tache.setProjet(null);
            // If the DTO itself provides initial student assignments without a sprint, use them
            if (tacheCreationDTO.getEtudiantsAffectes() != null && !tacheCreationDTO.getEtudiantsAffectes().isEmpty()) {
                tache.setEtudiantsAffectes(new ArrayList<>(tacheCreationDTO.getEtudiantsAffectes()));
            }
        }

        return tacheRepository.save(tache);
    }

    @Override
    @Transactional
    public Tache updateTacheFromDTO(Long id, TacheCreationDTO tacheCreationDTO) {
        Tache existingTache = tacheRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée avec ID : " + id));

        // Update fields if provided in DTO
        Optional.ofNullable(tacheCreationDTO.getNom()).ifPresent(existingTache::setNom);
        Optional.ofNullable(tacheCreationDTO.getDescription()).ifPresent(existingTache::setDescription);
        Optional.ofNullable(tacheCreationDTO.getDateDebut()).ifPresent(existingTache::setDateDebut);
        Optional.ofNullable(tacheCreationDTO.getDateFin()).ifPresent(existingTache::setDateFin);
        Optional.ofNullable(tacheCreationDTO.getStatut()).ifPresent(existingTache::setStatut);
        Optional.ofNullable(tacheCreationDTO.getStoryPoints()).ifPresent(existingTache::setStoryPoints);
        Optional.ofNullable(tacheCreationDTO.getEstimatedHours()).ifPresent(existingTache::setEstimatedHours);
        // Do NOT update loggedHours or etudiantsAffectes from a creation DTO for an update,
        // unless you explicitly intend this DTO to be a full replace.
        // For etudiantsAffectes, use dedicated affecter/supprimer methods.

        // Handle sprint re-assignment
        if (tacheCreationDTO.getSprintId() != null) {
            // Only update if sprint ID is different
            if (existingTache.getSprint() == null || !existingTache.getSprint().getIdSprint().equals(tacheCreationDTO.getSprintId())) {
                Sprint newSprint = sprintRepository.findById(tacheCreationDTO.getSprintId())
                        .orElseThrow(() -> new RuntimeException("Sprint non trouvé avec ID : " + tacheCreationDTO.getSprintId()));
                existingTache.setSprint(newSprint);
                existingTache.setProjet(newSprint.getProjet());
                // When sprint changes, update assigned students to match new sprint's students
                existingTache.setEtudiantsAffectes(new ArrayList<>(newSprint.getEtudiantsAffectes() != null ? newSprint.getEtudiantsAffectes() : new ArrayList<>()));
            }
        } else { // If sprintId is explicitly null, detach from sprint
            existingTache.setSprint(null);
            existingTache.setProjet(null);
            // If removing from sprint, clear inherited student assignments, or let affecter handle explicit assignments
            existingTache.setEtudiantsAffectes(new ArrayList<>());
        }

        // Re-validate dates after updates
        if (existingTache.getDateDebut() == null || existingTache.getDateFin() == null) {
            throw new RuntimeException("Les dates ne peuvent pas être nulles.");
        }
        if (existingTache.getDateDebut().isAfter(existingTache.getDateFin())) {
            throw new RuntimeException("La date de début ne peut pas être après la date de fin.");
        }

        validateTaskDatesWithSprint(existingTache); // Validate updated task dates against sprint dates
        return tacheRepository.save(existingTache);
    }

    @Override
    @Transactional
    public Tache updateTaskStatus(Long taskId, TaskStatus newStatus) {
        Tache tache = tacheRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée avec ID : " + taskId));

        if (newStatus == null) {
            throw new RuntimeException("Le statut de la tâche ne peut pas être nul.");
        }

        tache.setStatut(newStatus);
        Tache updated = tacheRepository.save(tache);

        // Check and complete the parent sprint if all tasks are done and it's linked to a sprint
        if (newStatus == TaskStatus.DONE && updated.getSprint() != null) {
            sprintService.checkAndCompleteSprintIfAllTasksDone(updated.getSprint().getIdSprint());
        }
        return updated;
    }

    @Override
    @Transactional // Ensure transactional for persistence operations
    public Tache saveTache(Tache tache) {
        // Initialize default values if null
        if (tache.getLoggedHours() == null) tache.setLoggedHours(0.0);
        if (tache.getEtudiantsAffectes() == null) tache.setEtudiantsAffectes(new ArrayList<>());

        // Link to Sprint and Project if sprint is set
        if (tache.getSprint() != null && tache.getSprint().getIdSprint() != null) {
            Sprint sprint = sprintRepository.findById(tache.getSprint().getIdSprint())
                    .orElseThrow(() -> new RuntimeException("Sprint non trouvé avec ID : " + tache.getSprint().getIdSprint()));
            tache.setSprint(sprint);
            tache.setProjet(sprint.getProjet());
            // Inherit assigned students from the sprint
            if (sprint.getEtudiantsAffectes() != null) {
                tache.setEtudiantsAffectes(new ArrayList<>(sprint.getEtudiantsAffectes()));
            }
        } else {
            tache.setSprint(null);
            tache.setProjet(null);
        }

        // Basic validation
        if (tache.getNom() == null || tache.getNom().trim().isEmpty()) {
            throw new RuntimeException("Le nom de la tâche ne peut pas être vide.");
        }
        if (tache.getDateDebut() == null || tache.getDateFin() == null) {
            throw new RuntimeException("Les dates de début et fin ne peuvent pas être nulles.");
        }
        if (tache.getDateDebut().isAfter(tache.getDateFin())) {
            throw new RuntimeException("La date de début ne peut pas être après la date de fin.");
        }

        validateTaskDatesWithSprint(tache); // Validate task dates against sprint dates
        return tacheRepository.save(tache);
    }

    @Override
    public Tache getTacheById(Long id) {
        return tacheRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée avec ID : " + id));
    }

    @Override
    public List<Tache> getAllTaches() {
        return tacheRepository.findAll();
    }

    @Override
    @Transactional
    public void deleteTache(Long id) {
        Tache tache = tacheRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée avec ID : " + id));
        // You might want to remove the task from its sprint's tasks list if it's a bidirectional relationship
        if (tache.getSprint() != null) {
            tache.getSprint().getTaches().remove(tache); // Assuming a getTaches() on Sprint
            sprintRepository.save(tache.getSprint()); // Save updated sprint
        }
        tacheRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Tache updateTache(Long id, Tache updatedTache) {
        Tache existingTache = tacheRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée avec ID : " + id));

        // Update all fields from updatedTache (assuming full replacement or carefully chosen fields)
        existingTache.setNom(updatedTache.getNom());
        existingTache.setDescription(updatedTache.getDescription());
        existingTache.setDateDebut(updatedTache.getDateDebut());
        existingTache.setDateFin(updatedTache.getDateFin());
        existingTache.setStatut(updatedTache.getStatut());
        existingTache.setStoryPoints(updatedTache.getStoryPoints());
        existingTache.setEstimatedHours(updatedTache.getEstimatedHours());
        // For etudiantsAffectes and loggedHours, you might have specific methods
        // or a DTO for updates to control how these are changed.
        // For now, assuming they are part of a full update via Tache entity.
        existingTache.setLoggedHours(updatedTache.getLoggedHours());
        existingTache.setEtudiantsAffectes(new ArrayList<>(updatedTache.getEtudiantsAffectes())); // Copy to ensure mutable list

        // Handle sprint re-assignment via the Tache entity itself
        if (updatedTache.getSprint() != null && updatedTache.getSprint().getIdSprint() != null) {
            if (existingTache.getSprint() == null || !existingTache.getSprint().getIdSprint().equals(updatedTache.getSprint().getIdSprint())) {
                Sprint newSprint = sprintRepository.findById(updatedTache.getSprint().getIdSprint())
                        .orElseThrow(() -> new RuntimeException("Sprint non trouvé avec ID : " + updatedTache.getSprint().getIdSprint()));
                existingTache.setSprint(newSprint);
                existingTache.setProjet(newSprint.getProjet());
                existingTache.setEtudiantsAffectes(new ArrayList<>(newSprint.getEtudiantsAffectes() != null ? newSprint.getEtudiantsAffectes() : new ArrayList<>()));
            }
        } else {
            existingTache.setSprint(null);
            existingTache.setProjet(null);
            existingTache.setEtudiantsAffectes(new ArrayList<>());
        }
        // Basic validation after updates
        if (existingTache.getNom() == null || existingTache.getNom().trim().isEmpty()) {
            throw new RuntimeException("Le nom de la tâche ne peut pas être vide.");
        }
        if (existingTache.getDateDebut() == null || existingTache.getDateFin() == null) {
            throw new RuntimeException("Les dates de début et fin ne peuvent pas être nulles.");
        }
        if (existingTache.getDateDebut().isAfter(existingTache.getDateFin())) {
            throw new RuntimeException("La date de début ne peut pas être après la date de fin.");
        }

        validateTaskDatesWithSprint(existingTache);
        return tacheRepository.save(existingTache);
    }


    @Override
    @Transactional
    public Tache logTimeOnTask(Long taskId, Double hoursToLog) {
        if (hoursToLog == null || hoursToLog <= 0) {
            throw new RuntimeException("Les heures à loguer doivent être positives.");
        }
        Tache tache = tacheRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée avec ID : " + taskId));

        Double currentLoggedHours = tache.getLoggedHours() != null ? tache.getLoggedHours() : 0.0;
        tache.setLoggedHours(currentLoggedHours + hoursToLog);
        return tacheRepository.save(tache);
    }

    @Override
    public List<Tache> getTasksBySprintId(Long sprintId) {
        // You'll need a method in your TacheRepo like: List<Tache> findBySprintIdSprint(Long sprintId);
        return tacheRepository.findBySprint_IdSprint(sprintId);
    }

    @Override
    public List<Tache> getTasksByAssignedToUserId(Long userId) {
        // This method assumes you have a direct link to a User entity by ID in your Tache
        // If 'etudiantsAffectes' (List<String> emails) is your primary assignment method,
        // then this method might not be directly implementable or would require joining.
        // For now, I'll return an empty list or throw an UnsupportedOperationException
        // if this assignment by userId isn't supported by your Tache entity directly.
        // If you have a User entity linked to Tache, you'd do:
        // return tacheRepository.findByAssignedUsersId(userId); // Assuming a User entity and 'assignedUsers' field
        System.err.println("Warning: getTasksByAssignedToUserId(Long userId) is called. Consider using getTachesByStudentEmail if assignment is by email.");
        return new ArrayList<>(); // Or throw new UnsupportedOperationException("Assignment by User ID not supported by Tache entity's structure.");
    }

    @Override
    public List<Tache> getTasksByStatus(TaskStatus status) {
        // You'll need a method in your TacheRepo like: List<Tache> findByStatut(TaskStatus status);
        return tacheRepository.findByStatut(status);
    }

    @Override
    public List<Tache> getTachesByStudentEmail(String studentEmail) {
        String cleanedEmail = projetService.cleanEmail(studentEmail); // Use cleanEmail for consistency
        // You'll need a method in your TacheRepo like: List<Tache> findByEtudiantsAffectesContainingIgnoreCase(String email);
        return tacheRepository.findByEtudiantsAffectesContainingIgnoreCase(cleanedEmail);
    }

    @Override
    @Transactional
    public Tache affecterEtudiantToTache(Long tacheId, String etudiantEmail) {
        Tache tache = tacheRepository.findById(tacheId)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée avec ID : " + tacheId));

        String cleanedEmail = projetService.cleanEmail(etudiantEmail);

        if (tache.getEtudiantsAffectes() == null) {
            tache.setEtudiantsAffectes(new ArrayList<>());
        }

        if (!tache.getEtudiantsAffectes().contains(cleanedEmail)) {
            tache.getEtudiantsAffectes().add(cleanedEmail);
            return tacheRepository.save(tache);
        } else {
            // Already assigned, maybe log a warning or return existing.
            System.out.println("L'étudiant " + cleanedEmail + " est déjà affecté à la tâche " + tacheId);
            return tache;
        }
    }

    @Override
    @Transactional
    public Tache supprimerEtudiantFromTache(Long tacheId, String etudiantEmail) {
        Tache tache = tacheRepository.findById(tacheId)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée avec ID : " + tacheId));

        String cleanedEmail = projetService.cleanEmail(etudiantEmail);

        if (tache.getEtudiantsAffectes() != null && tache.getEtudiantsAffectes().remove(cleanedEmail)) {
            return tacheRepository.save(tache);
        } else {
            throw new RuntimeException("L'étudiant " + cleanedEmail + " n'est pas affecté à la tâche " + tacheId + " ou liste vide.");
        }
    }

    // Private helper method for date validation
    private void validateTaskDatesWithSprint(Tache tache) {
        if (tache.getSprint() != null) {
            Sprint sprint = tache.getSprint();
            if (sprint.getDateDebut() == null || sprint.getDateFin() == null) {
                System.err.println("WARN : Sprint (ID: " + sprint.getIdSprint() + ") a des dates nulles. Impossible de valider les dates de la tâche.");
                return; // Cannot validate if sprint dates are null
            }
            if (tache.getDateDebut().isBefore(sprint.getDateDebut()) || tache.getDateFin().isAfter(sprint.getDateFin())) {
                throw new RuntimeException("Les dates de la tâche (" + tache.getDateDebut() + " - " + tache.getDateFin()
                        + ") doivent être comprises dans celles du sprint (" + sprint.getDateDebut() + " - " + sprint.getDateFin() + ").");
            }
        }
    }
}