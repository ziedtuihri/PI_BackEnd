package tn.esprit.pi.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid; // Assuming you are using Jakarta Validation for Spring Boot 3+
import java.util.List;
import java.util.Optional;

// DTOs and Entities used by the controller
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.pi.dto.TacheCreationDTO;
import tn.esprit.pi.entities.Tache;
import tn.esprit.pi.entities.enumerations.TaskStatus;

// Service Interface for Tache
import tn.esprit.pi.services.ITacheService;

@RestController
@RequestMapping("/api/taches") // Base URL for all task-related endpoints
public class TacheController {

    private static final Logger log = LoggerFactory.getLogger(TacheController.class);

    private final ITacheService tacheService;

    // Constructor Injection for the service
    public TacheController(ITacheService tacheService) {
        this.tacheService = tacheService;
    }

    // --- Core CRUD Operations ---

    /**
     * Creates a new task from a DTO.
     * Handles POST requests to: `/api/taches`
     *
     * @param tacheCreationDTO The DTO containing the details for the new task.
     * @return A `ResponseEntity` containing the created `Tache` object and HTTP status 201 (Created),
     * or an appropriate error status (e.g., 400 Bad Request, 500 Internal Server Error).
     */
    @PostMapping
    public ResponseEntity<Tache> createTache(@Valid @RequestBody TacheCreationDTO tacheCreationDTO) {
        log.info("Attempting to create a new task with name: {}", tacheCreationDTO.getNom());
        try {
            Tache createdTache = tacheService.createTacheFromDTO(tacheCreationDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTache);
        } catch (IllegalArgumentException e) {
            log.warn("Task creation failed due to validation error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (RuntimeException e) { // Catching generic RuntimeException from service layer
            log.error("Error creating task: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } catch (Exception e) {
            log.error("An unexpected error occurred during task creation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Retrieves a task by its unique ID.
     * Handles GET requests to: `/api/taches/{id}`
     *
     * @param id The ID of the task to retrieve.
     * @return A `ResponseEntity` containing the `Tache` object and HTTP status 200 (OK),
     * or 404 (Not Found) if no task exists with the given ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Tache> getTacheById(@PathVariable Long id) {
        log.info("Attempting to retrieve task with ID: {}", id);
        try {
            Tache tache = tacheService.getTacheById(id);
            return ResponseEntity.ok(tache);
        } catch (RuntimeException e) { // Catching generic RuntimeException from service layer
            log.warn("Task with ID {} not found. {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("An unexpected error occurred while retrieving task ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves all tasks available in the system.
     * Handles GET requests to: `/api/taches`
     *
     * @return A `ResponseEntity` containing a list of `Tache` objects and HTTP status 200 (OK),
     * or 204 (No Content) if no tasks are found.
     */
    @GetMapping
    public ResponseEntity<List<Tache>> getAllTaches() {
        log.info("Attempting to retrieve all tasks.");
        List<Tache> taches = tacheService.getAllTaches();
        if (taches.isEmpty()) {
            log.info("No tasks found.");
            return ResponseEntity.noContent().build(); // 204 No Content
        }
        return ResponseEntity.ok(taches);
    }

    /**
     * Updates an existing task identified by its ID using a DTO.
     * Handles PUT requests to: `/api/taches/{id}`
     *
     * @param id The ID of the task to update.
     * @param tacheCreationDTO The DTO containing the updated task details.
     * @return A `ResponseEntity` containing the updated `Tache` object and HTTP status 200 (OK),
     * or an appropriate error status (e.g., 400 Bad Request, 404 Not Found, 500 Internal Server Error).
     */
    @PutMapping("/{id}")
    public ResponseEntity<Tache> updateTache(@PathVariable Long id, @Valid @RequestBody TacheCreationDTO tacheCreationDTO) {
        log.info("Attempting to update task with ID: {}", id);
        try {
            Tache updatedTache = tacheService.updateTacheFromDTO(id, tacheCreationDTO);
            return ResponseEntity.ok(updatedTache);
        } catch (IllegalArgumentException e) {
            log.warn("Task update failed due to validation error for task ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (RuntimeException e) { // Catching generic RuntimeException from service layer
            log.warn("Task update failed: Task with ID {} not found or other issue. {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("An unexpected error occurred while updating task ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Deletes a task by its unique ID.
     * Handles DELETE requests to: `/api/taches/{id}`
     *
     * @param id The ID of the task to delete.
     * @return A `ResponseEntity` with HTTP status 204 (No Content) for successful deletion,
     * or 404 (Not Found) if the task does not exist.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTache(@PathVariable Long id) {
        log.info("Attempting to delete task with ID: {}", id);
        try {
            tacheService.deleteTache(id);
            return ResponseEntity.noContent().build(); // 204 No Content on successful deletion
        } catch (RuntimeException e) { // Catching generic RuntimeException from service layer
            log.warn("Task deletion failed: Task with ID {} not found. {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("An unexpected error occurred while deleting task ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // --- Specific Task Operations ---

    /**
     * Updates the status of a specific task.
     * Handles PUT requests to: `/api/taches/{taskId}/status`
     *
     * @param taskId The ID of the task to update.
     * @param newStatus The new `TaskStatus` to set. Provided as a query parameter.
     * @return A `ResponseEntity` containing the updated `Tache` object and HTTP status 200 (OK),
     * or an appropriate error status.
     */
    @PutMapping("/{taskId}/status")
    public ResponseEntity<Tache> updateTaskStatus(@PathVariable Long taskId, @RequestParam TaskStatus newStatus) {
        log.info("Attempting to update status for task ID {} to: {}", taskId, newStatus);
        try {
            Tache updatedTache = tacheService.updateTaskStatus(taskId, newStatus);
            return ResponseEntity.ok(updatedTache);
        } catch (IllegalArgumentException e) {
            log.warn("Task status update failed for task ID {} due to validation error: {}", taskId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (RuntimeException e) { // Catching generic RuntimeException from service layer
            log.warn("Task status update failed: Task with ID {} not found. {}", taskId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("An unexpected error occurred while updating status for task ID {}: {}", taskId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Logs hours spent on a specific task.
     * Handles PUT requests to: `/api/taches/{taskId}/log-time`
     *
     * @param taskId The ID of the task.
     * @param hoursToLog The number of hours to add to the logged hours. Provided as a query parameter.
     * @return A `ResponseEntity` containing the updated `Tache` object and HTTP status 200 (OK),
     * or an appropriate error status.
     */
    @PutMapping("/{taskId}/log-time")
    public ResponseEntity<Tache> logTimeOnTask(@PathVariable Long taskId, @RequestParam Double hoursToLog) {
        log.info("Attempting to log {} hours on task ID: {}", hoursToLog, taskId);
        try {
            Tache updatedTache = tacheService.logTimeOnTask(taskId, hoursToLog);
            return ResponseEntity.ok(updatedTache);
        } catch (IllegalArgumentException e) {
            log.warn("Logging time failed for task ID {} due to validation error: {}", taskId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (RuntimeException e) { // Catching generic RuntimeException from service layer
            log.warn("Logging time failed: Task with ID {} not found. {}", taskId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("An unexpected error occurred while logging time on task ID {}: {}", taskId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // --- Filtering and Association Endpoints ---

    /**
     * Retrieves tasks associated with a specific sprint.
     * Handles GET requests to: `/api/taches/by-sprint/{sprintId}`
     *
     * @param sprintId The ID of the sprint.
     * @return A `ResponseEntity` containing a list of `Tache` objects and HTTP status 200 (OK),
     * or 204 (No Content) if no tasks are found for the sprint.
     */
    @GetMapping("/by-sprint/{sprintId}")
    public ResponseEntity<List<Tache>> getTasksBySprintId(@PathVariable Long sprintId) {
        log.info("Request to get tasks for sprint ID: {}", sprintId);
        List<Tache> taches = tacheService.getTasksBySprintId(sprintId);
        if (taches.isEmpty()) {
            log.info("No tasks found for sprint ID: {}", sprintId);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(taches);
    }

    /**
     * Retrieves tasks based on their status.
     * Handles GET requests to: `/api/taches/by-status`
     *
     * @param status The `TaskStatus` to filter by. Provided as a query parameter.
     * @return A `ResponseEntity` containing a list of `Tache` objects and HTTP status 200 (OK),
     * or 204 (No Content) if no tasks are found with that status.
     */
    @GetMapping("/by-status")
    public ResponseEntity<List<Tache>> getTasksByStatus(@RequestParam TaskStatus status) {
        log.info("Request to get tasks by status: {}", status);
        List<Tache> taches = tacheService.getTasksByStatus(status);
        if (taches.isEmpty()) {
            log.info("No tasks found with status: {}", status);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(taches);
    }

    /**
     * Retrieves tasks assigned to a specific student email.
     * Handles GET requests to: `/api/taches/by-student-email`
     *
     * @param studentEmail The email address of the student. Provided as a query parameter.
     * @return A `ResponseEntity` containing a list of `Tache` objects and HTTP status 200 (OK),
     * or 204 (No Content) if no tasks are found for the student.
     */
    @GetMapping("/by-student-email")
    public ResponseEntity<List<Tache>> getTachesByStudentEmail(@RequestParam String studentEmail) {
        log.info("Request to get tasks by student email: {}", studentEmail);
        List<Tache> taches = tacheService.getTachesByStudentEmail(studentEmail);
        if (taches.isEmpty()) {
            log.info("No tasks found for student email: {}", studentEmail);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(taches);
    }

    // --- Student Assignment to Task Endpoints ---

    /**
     * Assigns a student (identified by email) to a specific task.
     * Handles POST requests to: `/api/taches/{tacheId}/etudiants`
     *
     * @param tacheId The ID of the task to which the student will be assigned.
     * @param etudiantEmail The email address of the student to assign. Sent as plain text in the request body.
     * @return A `ResponseEntity` containing the updated `Tache` object and HTTP status 200 (OK),
     * or an appropriate error status.
     */
    @PostMapping("/{tacheId}/etudiants")
    public ResponseEntity<Tache> affecterEtudiantToTache(
            @PathVariable Long tacheId,
            @RequestBody String etudiantEmail) { // Expecting plain text email
        log.info("Request to assign student '{}' to task ID: {}", etudiantEmail, tacheId);
        try {
            Tache updatedTache = tacheService.affecterEtudiantToTache(tacheId, etudiantEmail.trim());
            return ResponseEntity.ok(updatedTache);
        } catch (RuntimeException e) { // Catching generic RuntimeException from service layer
            log.warn("Student assignment to task failed for ID {}: {}", tacheId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Or BAD_REQUEST
        } catch (Exception e) {
            log.error("An unexpected error occurred while assigning student '{}' to task ID {}: {}", etudiantEmail, tacheId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Removes a student (identified by email) from a specific task.
     * Handles DELETE requests to: `/api/taches/{tacheId}/etudiants/{etudiantEmail}`
     *
     * @param tacheId The ID of the task from which the student will be removed.
     * @param etudiantEmail The email address of the student to remove. This is a path variable, so it should be URL-encoded.
     * @return A `ResponseEntity` containing the updated `Tache` object and HTTP status 200 (OK),
     * or an appropriate error status.
     */
    @DeleteMapping("/{tacheId}/etudiants/{etudiantEmail}")
    public ResponseEntity<Tache> supprimerEtudiantFromTache(
            @PathVariable Long tacheId,
            @PathVariable String etudiantEmail) { // etudiantEmail comes already decoded from @PathVariable
        log.info("Request to remove student '{}' from task ID: {}", etudiantEmail, tacheId);
        try {
            Tache updatedTache = tacheService.supprimerEtudiantFromTache(tacheId, etudiantEmail);
            return ResponseEntity.ok(updatedTache);
        } catch (IllegalArgumentException e) {
            log.warn("Student removal from task failed for ID {}: {}", tacheId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (RuntimeException e) { // Catching generic RuntimeException from service layer
            log.warn("Student removal from task failed: Task with ID {} not found. {}", tacheId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            log.error("An unexpected error occurred while removing student '{}' from task ID {}: {}", etudiantEmail, tacheId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Note: getTasksByAssignedToUserId(Long userId) is in your service but not directly mapped here
    // as it seems to refer to an internal user ID, not an email which is more common in web APIs.
    // If you need it, you'd add:
    // @GetMapping("/by-user/{userId}")
    // public ResponseEntity<List<Tache>> getTasksByAssignedToUserId(@PathVariable Long userId) { /* ... */ }

}