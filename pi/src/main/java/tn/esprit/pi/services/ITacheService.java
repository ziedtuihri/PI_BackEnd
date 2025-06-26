package tn.esprit.pi.services;

import tn.esprit.pi.dto.TacheCreationDTO;
import tn.esprit.pi.entities.Tache;
import tn.esprit.pi.entities.enumerations.TaskStatus;

import java.util.List;

public interface ITacheService {
    Tache saveTache(Tache tache); // Generic save, potentially used by other services internally
    Tache getTacheById(Long id);
    List<Tache> getAllTaches();
    void deleteTache(Long id);
    Tache updateTache(Long id, Tache updatedTache); // Generic update
    Tache logTimeOnTask(Long taskId, Double hoursToLog); // Specific business logic for time logging
    List<Tache> getTasksBySprintId(Long sprintId); // Important for task-per-sprint view
    List<Tache> getTasksByAssignedToUserId(Long userId); // Good for user-specific task lists (if using userId for assignment)
    List<Tache> getTasksByStatus(TaskStatus status); // Useful for filtering by status

    // --- New Methods Added/Modified for Controller's Needs ---

    // For retrieving tasks specifically assigned to a student by email
    List<Tache> getTachesByStudentEmail(String studentEmail); // <-- ADD THIS

    // For creating a task using a DTO
    Tache createTacheFromDTO(TacheCreationDTO tacheCreationDTO);

    // For updating a task using a DTO (if different from generic update)
    Tache updateTacheFromDTO(Long id, TacheCreationDTO tacheCreationDTO);

    /**
     * Updates the status of a specific task.
     * This method is designed to trigger a check on the parent sprint's completion
     * if the task's new status is 'DONE'.
     *
     * @param taskId The ID of the task to update.
     * @param newStatus The new status to set for the task.
     * @return The updated Tache entity.
     */
    Tache updateTaskStatus(Long taskId, TaskStatus newStatus);

    // For assigning a student to a task (by email)
    Tache affecterEtudiantToTache(Long tacheId, String etudiantEmail); // <-- ADD THIS

    // For removing a student from a task (by email)
    Tache supprimerEtudiantFromTache(Long tacheId, String etudiantEmail); // <-- ADD THIS
}
