package tn.esprit.pi.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid; // Assuming you are using Jakarta Validation for Spring Boot 3+
import java.util.List;
import java.util.Optional; // Keep if your service methods return Optional

// DTOs and Entities used by the controller
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.pi.dto.CalendarEventDto;
import tn.esprit.pi.dto.CreateSprintDto;
import tn.esprit.pi.dto.SprintWithTasksDTO;
import tn.esprit.pi.entities.Projet;
import tn.esprit.pi.entities.Sprint;
import tn.esprit.pi.entities.Tache; // Used for creating tasks within a sprint

// Service Interfaces and specific implementations for exception handling
import tn.esprit.pi.services.IProjetService;
import tn.esprit.pi.services.ISprintService;
import tn.esprit.pi.services.ProjetServiceImpl; // For ProjetNotFoundException
import tn.esprit.pi.services.SprintServiceImpl; // For SprintNotFoundException

@RestController
@RequestMapping("/api/sprints") // Base URL for all sprint-related endpoints
public class SprintController {

    private static final Logger log = LoggerFactory.getLogger(SprintController.class);

    private final ISprintService sprintService;
    private final IProjetService projetService; // Injected because generateInitialSprintForProject might need it directly

    // Constructor Injection for services (Spring's recommended way)
    public SprintController(ISprintService sprintService, IProjetService projetService) {
        this.sprintService = sprintService;
        this.projetService = projetService;
    }

    // --- Core CRUD Operations ---

    /**
     * Creates a new sprint based on the provided data.
     * Handles POST requests to: `/api/sprints`
     *
     * @param createSprintDto The DTO containing the details for the new sprint.
     * @return A `ResponseEntity` containing the created `Sprint` object and HTTP status 201 (Created),
     * or an appropriate error status (e.g., 400 Bad Request, 404 Not Found, 500 Internal Server Error).
     */
    @PostMapping
    public ResponseEntity<Sprint> createSprint(@Valid @RequestBody CreateSprintDto createSprintDto) {
        log.info("Attempting to create a new sprint with name: {}", createSprintDto.getNom());
        try {
            Sprint createdSprint = sprintService.createSprint(createSprintDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdSprint);
        } catch (ProjetServiceImpl.ProjetNotFoundException e) {
            log.warn("Sprint creation failed: Project with ID {} not found. {}", createSprintDto.getProjetId(), e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (IllegalArgumentException e) {
            log.warn("Sprint creation failed due to validation error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            log.error("An unexpected error occurred during sprint creation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Retrieves a sprint by its unique ID.
     * Handles GET requests to: `/api/sprints/{id}`
     *
     * @param id The ID of the sprint to retrieve.
     * @return A `ResponseEntity` containing the `Sprint` object and HTTP status 200 (OK),
     * or 404 (Not Found) if no sprint exists with the given ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Sprint> getSprintById(@PathVariable Long id) {
        log.info("Attempting to retrieve sprint with ID: {}", id);
        try {
            Sprint sprint = sprintService.getSprintById(id);
            return ResponseEntity.ok(sprint);
        } catch (SprintServiceImpl.SprintNotFoundException e) {
            log.warn("Sprint with ID {} not found. {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("An unexpected error occurred while retrieving sprint ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves all sprints available in the system.
     * Handles GET requests to: `/api/sprints`
     *
     * @return A `ResponseEntity` containing a list of `Sprint` objects and HTTP status 200 (OK),
     * or 204 (No Content) if no sprints are found.
     */
    @GetMapping
    public ResponseEntity<List<Sprint>> getAllSprints() {
        log.info("Attempting to retrieve all sprints.");
        List<Sprint> sprints = sprintService.getAllSprints();
        if (sprints.isEmpty()) {
            log.info("No sprints found.");
            return ResponseEntity.noContent().build(); // 204 No Content
        }
        return ResponseEntity.ok(sprints);
    }

    /**
     * Updates an existing sprint identified by its ID.
     * Handles PUT requests to: `/api/sprints/{id}`
     *
     * @param id The ID of the sprint to update.
     * @param sprintDetails The `Sprint` object containing the updated details.
     * @return A `ResponseEntity` containing the updated `Sprint` object and HTTP status 200 (OK),
     * or an appropriate error status (e.g., 400 Bad Request, 404 Not Found, 500 Internal Server Error).
     */
    @PutMapping("/{id}")
    public ResponseEntity<Sprint> updateSprint(@PathVariable Long id, @Valid @RequestBody Sprint sprintDetails) {
        log.info("Attempting to update sprint with ID: {}", id);
        try {
            // It's good practice to ensure the ID from the path matches the ID in the body, if present,
            // or explicitly set the ID from the path to prevent ID mismatch issues.
            // sprintDetails.setIdSprint(id); // Uncomment if you want to force ID from path
            Sprint updatedSprint = sprintService.updateSprint(id, sprintDetails);
            return ResponseEntity.ok(updatedSprint);
        } catch (SprintServiceImpl.SprintNotFoundException e) {
            log.warn("Sprint update failed: Sprint with ID {} not found. {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            log.warn("Sprint update failed due to validation error for sprint ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("An unexpected error occurred while updating sprint ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Deletes a sprint by its unique ID.
     * Handles DELETE requests to: `/api/sprints/{id}`
     *
     * @param id The ID of the sprint to delete.
     * @return A `ResponseEntity` with HTTP status 204 (No Content) for successful deletion,
     * or 404 (Not Found) if the sprint does not exist.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSprint(@PathVariable Long id) {
        log.info("Attempting to delete sprint with ID: {}", id);
        try {
            sprintService.deleteSprint(id);
            return ResponseEntity.noContent().build(); // 204 No Content on successful deletion
        } catch (SprintServiceImpl.SprintNotFoundException e) {
            log.warn("Sprint deletion failed: Sprint with ID {} not found. {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("An unexpected error occurred while deleting sprint ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // --- Student Assignment Endpoints ---

    /**
     * Assigns a student (identified by email) to a specific sprint.
     * Handles POST requests to: `/api/sprints/{sprintId}/etudiants`
     *
     * @param sprintId The ID of the sprint to which the student will be assigned.
     * @param etudiantEmail The email address of the student to assign. Sent as plain text in the request body.
     * @return A `ResponseEntity` containing the updated `Sprint` object (with the student assigned) and HTTP status 200 (OK),
     * or an appropriate error status.
     */
    @PostMapping("/{sprintId}/etudiants")
    public ResponseEntity<Sprint> affecterEtudiantAuSprint(
            @PathVariable Long sprintId,
            @RequestBody String etudiantEmail) { // Expecting plain text email in the body
        log.info("Request to assign student '{}' to sprint ID: {}", etudiantEmail, sprintId);
        try {
            Sprint updatedSprint = sprintService.affecterEtudiantAuSprint(sprintId, etudiantEmail.trim());
            return ResponseEntity.ok(updatedSprint);
        } catch (SprintServiceImpl.SprintNotFoundException | IllegalArgumentException e) {
            log.warn("Failed to assign student '{}' to sprint ID {}: {}", etudiantEmail, sprintId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            log.error("An unexpected error occurred while assigning student '{}' to sprint ID {}: {}", etudiantEmail, sprintId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Removes a student (identified by email) from a specific sprint.
     * Handles DELETE requests to: `/api/sprints/{sprintId}/etudiants/{etudiantEmail}`
     *
     * @param sprintId The ID of the sprint from which the student will be removed.
     * @param etudiantEmail The email address of the student to remove. This is a path variable, so it should be URL-encoded.
     * @return A `ResponseEntity` containing the updated `Sprint` object (with the student removed) and HTTP status 200 (OK),
     * or an appropriate error status.
     */
    @DeleteMapping("/{sprintId}/etudiants/{etudiantEmail}")
    public ResponseEntity<Sprint> supprimerEtudiantDuSprint(
            @PathVariable Long sprintId,
            @PathVariable String etudiantEmail) { // etudiantEmail comes already decoded from @PathVariable
        log.info("Request to remove student '{}' from sprint ID: {}", etudiantEmail, sprintId);
        try {
            Sprint updatedSprint = sprintService.supprimerEtudiantDuSprint(sprintId, etudiantEmail);
            return ResponseEntity.ok(updatedSprint);
        } catch (SprintServiceImpl.SprintNotFoundException | IllegalArgumentException e) {
            log.warn("Failed to remove student '{}' from sprint ID {}: {}", etudiantEmail, sprintId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            log.error("An unexpected error occurred while removing student '{}' from sprint ID {}: {}", etudiantEmail, sprintId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Retrieves the list of student email addresses assigned to a specific sprint.
     * Handles GET requests to: `/api/sprints/{sprintId}/etudiants`
     *
     * @param sprintId The ID of the sprint.
     * @return A `ResponseEntity` containing a list of student email strings and HTTP status 200 (OK).
     */
    @GetMapping("/{sprintId}/etudiants")
    public ResponseEntity<List<String>> getEtudiantsAffectesAuSprint(@PathVariable Long sprintId) {
        log.info("Request to get assigned students for sprint ID: {}", sprintId);
        try {
            List<String> studentEmails = sprintService.getEtudiantsAffectesAuSprint(sprintId);
            return ResponseEntity.ok(studentEmails);
        } catch (SprintServiceImpl.SprintNotFoundException e) {
            log.warn("Failed to retrieve assigned students: Sprint with ID {} not found. {}", sprintId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("An unexpected error occurred while retrieving assigned students for sprint ID {}: {}", sprintId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves all sprints that a specific student is assigned to.
     * Handles GET requests to: `/api/sprints/by-student-email`
     *
     * @param studentEmail The email address of the student. Provided as a query parameter.
     * @return A `ResponseEntity` containing a list of `Sprint` objects and HTTP status 200 (OK).
     */
    @GetMapping("/by-student-email")
    public ResponseEntity<List<Sprint>> getSprintsByStudentEmail(@RequestParam String studentEmail) {
        log.info("Request to find sprints by student email: {}", studentEmail);
        try {
            List<Sprint> sprints = sprintService.getSprintsByStudentEmail(studentEmail);
            if (sprints.isEmpty()) {
                log.info("No sprints found for student email: {}", studentEmail);
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(sprints);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid student email provided for search: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("An unexpected error occurred while searching sprints by student email {}: {}", studentEmail, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    // --- Project-related Sprint Endpoints ---

    /**
     * Generates an initial sprint for a given project. This is a special case for project setup.
     * Handles POST requests to: `/api/sprints/generate-initial/{projectId}`
     *
     * @param projectId The ID of the project for which to generate the sprint.
     * @return A `ResponseEntity` containing the generated `Sprint` object and HTTP status 201 (Created),
     * or an error status (e.g., 400 Bad Request, 404 Not Found, 409 Conflict if a sprint already exists).
     */
    @PostMapping("/generate-initial/{projectId}")
    public ResponseEntity<Sprint> generateInitialSprintForProject(@PathVariable Long projectId) {
        log.info("Request to generate an initial sprint for project ID: {}", projectId);
        try {
            // The service method expects a Projet object; retrieve it first.
            Projet project = projetService.getProjetById(projectId);
            Sprint initialSprint = sprintService.generateInitialSprintForProject(project);
            return ResponseEntity.status(HttpStatus.CREATED).body(initialSprint);
        } catch (ProjetServiceImpl.ProjetNotFoundException e) {
            log.warn("Initial sprint generation failed: Project not found for ID {}. {}", projectId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            log.warn("Initial sprint generation failed: Validation error. {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (IllegalStateException e) {
            log.warn("Initial sprint generation failed: {}. This might indicate a sprint already exists or another business rule violation.", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null); // 409 Conflict for existing resource
        } catch (Exception e) {
            log.error("An unexpected error occurred during initial sprint generation for project ID {}: {}", projectId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Finds all sprints associated with a specific project ID.
     * Handles GET requests to: `/api/sprints/by-project/{projectId}`
     *
     * @param projectId The ID of the project.
     * @return A `ResponseEntity` containing a list of `Sprint` objects and HTTP status 200 (OK),
     * or 204 (No Content) if no sprints are found for the project.
     */
    @GetMapping("/by-project/{projectId}")
    public ResponseEntity<List<Sprint>> findSprintsByProjetId(@PathVariable Long projectId) {
        log.info("Request to find sprints for project ID: {}", projectId);
        List<Sprint> sprints = sprintService.findByProjetId(projectId);
        if (sprints.isEmpty()) {
            log.info("No sprints found for project ID: {}", projectId);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(sprints);
    }

    // --- Task-related Endpoints for a Sprint ---

    /**
     * Creates a new task and associates it with a specific sprint.
     * Handles POST requests to: `/api/sprints/{sprintId}/taches`
     *
     * @param sprintId The ID of the sprint to which the task will be added.
     * @param tache The `Tache` object containing the details for the new task.
     * @return A `ResponseEntity` containing the created `Tache` object and HTTP status 201 (Created).
     */
    @PostMapping("/{sprintId}/taches")
    public ResponseEntity<Tache> createTaskForSprint(@PathVariable Long sprintId, @Valid @RequestBody Tache tache) {
        log.info("Request to create a task for sprint ID: {}", sprintId);
        try {
            Tache createdTask = sprintService.createTaskForSprint(sprintId, tache);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
        } catch (SprintServiceImpl.SprintNotFoundException e) {
            log.warn("Task creation failed: Sprint with ID {} not found. {}", sprintId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (IllegalArgumentException e) {
            log.warn("Task creation for sprint {} failed due to validation error: {}", sprintId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            log.error("An unexpected error occurred while creating task for sprint ID {}: {}", sprintId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Retrieves a sprint along with its associated tasks.
     * Handles GET requests to: `/api/sprints/{sprintId}/withTasks`
     *
     * @param sprintId The ID of the sprint to retrieve with its tasks.
     * @return A `ResponseEntity` containing a `SprintWithTasksDTO` object and HTTP status 200 (OK),
     * or 404 (Not Found) if the sprint doesn't exist.
     */
    @GetMapping("/{sprintId}/withTasks")
    public ResponseEntity<SprintWithTasksDTO> getSprintWithTasks(@PathVariable Long sprintId) {
        log.info("Request to retrieve sprint with tasks for ID: {}", sprintId);
        Optional<SprintWithTasksDTO> sprintWithTasksDTO = sprintService.getSprintWithTasks(sprintId);
        return sprintWithTasksDTO.map(ResponseEntity::ok)
                .orElseGet(() -> {
                    log.warn("Sprint with tasks for ID {} not found.", sprintId);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                });
    }

    // --- Velocity and Completion Endpoints ---

    /**
     * Calculates the velocity for a specific sprint.
     * Handles GET requests to: `/api/sprints/{sprintId}/velocity`
     *
     * @param sprintId The ID of the sprint for which to calculate velocity.
     * @return A `ResponseEntity` containing the calculated velocity (double) and HTTP status 200 (OK),
     * or 404 (Not Found) if the sprint doesn't exist.
     */
    @GetMapping("/{sprintId}/velocity")
    public ResponseEntity<Double> calculateSprintVelocity(@PathVariable Long sprintId) {
        log.info("Request to calculate velocity for sprint ID: {}", sprintId);
        try {
            double velocity = sprintService.calculateSprintVelocity(sprintId);
            return ResponseEntity.ok(velocity);
        } catch (SprintServiceImpl.SprintNotFoundException e) {
            log.warn("Velocity calculation failed: Sprint with ID {} not found. {}", sprintId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("An unexpected error occurred while calculating velocity for sprint ID {}: {}", sprintId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves historical velocity data for completed sprints.
     * Handles GET requests to: `/api/sprints/velocity-history`
     *
     * @return A `ResponseEntity` containing a list of objects (e.g., arrays or DTOs) representing velocity history,
     * and HTTP status 200 (OK).
     */
    @GetMapping("/velocity-history")
    public ResponseEntity<List<Object[]>> getVelocityHistory() {
        log.info("Request to retrieve velocity history.");
        try {
            List<Object[]> history = sprintService.getVelocityHistory();
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("An unexpected error occurred while retrieving velocity history: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Checks if a sprint can be marked as completed based on its tasks' status
     * and potentially completes it and its parent project.
     * Handles PUT requests to: `/api/sprints/{sprintId}/check-completion`
     *
     * @param sprintId The ID of the sprint to check.
     * @return A `ResponseEntity` with HTTP status 200 (OK) if the check and completion process was attempted (success or not),
     * or an appropriate error status (e.g., 404 Not Found, 400 Bad Request, 500 Internal Server Error).
     */
    @PutMapping("/{sprintId}/check-completion")
    public ResponseEntity<Void> checkAndCompleteSprint(@PathVariable Long sprintId) {
        log.info("Request to check and potentially complete sprint ID: {}", sprintId);
        try {
            sprintService.checkAndCompleteSprintIfAllTasksDone(sprintId);
            return ResponseEntity.ok().build();
        } catch (SprintServiceImpl.SprintNotFoundException e) {
            log.warn("Sprint completion check failed: Sprint with ID {} not found. {}", sprintId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalStateException e) {
            log.warn("Sprint completion check failed for ID {}: {}", sprintId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); // e.g., not all tasks are done yet
        } catch (Exception e) {
            log.error("An unexpected error occurred while checking and completing sprint ID {}: {}", sprintId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Triggers a check to see if a project can be completed based on its sprints' status.
     * Handles PUT requests to: `/api/sprints/project/{projectId}/check-completion`
     * (Note: This might typically reside in a `ProjetController`, but your service has it here, so mirroring it.)
     *
     * @param projectId The ID of the project to check.
     * @return A `ResponseEntity` with HTTP status 200 (OK) on success, or an error status.
     */
    @PutMapping("/project/{projectId}/check-completion")
    public ResponseEntity<Void> checkAndCompleteProject(@PathVariable Long projectId) {
        log.info("Request to check and potentially complete project ID: {}", projectId);
        try {
            // Delegating to the sprint service which in turn uses the projet service.
            sprintService.checkAndCompleteProjectIfAllSprintsDone(projectId);
            return ResponseEntity.ok().build();
        } catch (ProjetServiceImpl.ProjetNotFoundException e) {
            log.warn("Project completion check failed: Project not found for ID {}. {}", projectId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalStateException e) {
            log.warn("Project completion check failed for ID {}: {}", projectId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); // e.g., not all sprints are done yet
        } catch (Exception e) {
            log.error("An unexpected error occurred while checking and completing project ID {}: {}", projectId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // --- Calendar Events ---

    /**
     * Retrieves all calendar events, likely derived from sprints and tasks.
     * Handles GET requests to: `/api/sprints/calendar-events`
     *
     * @return A `ResponseEntity` containing a list of `CalendarEventDto` objects and HTTP status 200 (OK).
     */
    @GetMapping("/calendar-events")
    public ResponseEntity<List<CalendarEventDto>> getAllCalendarEvents() {
        log.info("Request to retrieve all calendar events.");
        try {
            List<CalendarEventDto> events = sprintService.getAllCalendarEvents();
            if (events.isEmpty()) {
                log.info("No calendar events found.");
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            log.error("An unexpected error occurred while retrieving calendar events: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // --- Search Endpoints ---

    /**
     * Searches for sprints by name (case-insensitive, partial match).
     * Handles GET requests to: `/api/sprints/search`
     *
     * @param nom The name or partial name to search for. Provided as a query parameter.
     * @return A `ResponseEntity` containing a list of matching `Sprint` objects and HTTP status 200 (OK).
     */
    @GetMapping("/search")
    public ResponseEntity<List<Sprint>> searchSprintsByNom(@RequestParam String nom) {
        log.info("Request to search sprints by name containing: {}", nom);
        try {
            List<Sprint> sprints = sprintService.searchSprintsByNom(nom);
            if (sprints.isEmpty()) {
                log.info("No sprints found matching name: {}", nom);
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(sprints);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid search term provided: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("An unexpected error occurred while searching sprints by name {}: {}", nom, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // --- Other potentially useful methods from your service, mapped to endpoints if needed ---
    // For example, if you had a specific method to get sprints by status, you would map it here.
    // @GetMapping("/by-status/{status}")
    // public ResponseEntity<List<Sprint>> getSprintsByStatus(@PathVariable String status) { /* ... */ }

}