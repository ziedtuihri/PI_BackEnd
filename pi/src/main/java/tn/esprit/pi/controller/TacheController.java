package tn.esprit.pi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.bind.annotation.RestController;
import tn.esprit.pi.dto.TacheCreationDTO; // Make sure this DTO exists and has fields for new tasks
import tn.esprit.pi.entities.Tache;
import tn.esprit.pi.entities.enumerations.TaskStatus; // Import TaskStatus enum
import tn.esprit.pi.services.ITacheService; // Your Tache service interface

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/taches")
//@CrossOrigin(origins = "http://localhost:4200") // Enable CORS for your Angular frontend
public class TacheController {

    private final ITacheService tacheService;

    @Autowired
    public TacheController(ITacheService tacheService) {
        this.tacheService = tacheService;
    }

    // --- Helper Methods for Authentication and Authorization ---
    private String getConnectedUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return null; // No user authenticated
        }
        return authentication.getName(); // Returns the username (email)
    }

    private boolean hasRole(String roleName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        String prefixedRoleName = "ROLE_" + roleName.toUpperCase(); // Assuming "ROLE_" prefix from Spring Security
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(prefixedRoleName));
    }

    private boolean isStudent() {
        return hasRole("USER"); // As clarified: "USER" role means "ETUDIANT" (student)
    }

    private boolean isTeacherOrHR() {
        return hasRole("TEACHER") || hasRole("HR_COMPANY");
    }


    // --- CRUD Operations ---

    /**
     * Creates a new task.
     * Accessible only by TEACHER or HR_COMPANY roles.
     * @param tacheCreationDTO DTO containing task details.
     * @return The created Tache entity.
     */
    @PostMapping
    public ResponseEntity<Tache> createTache(@RequestBody TacheCreationDTO tacheCreationDTO) {
        String connectedUserEmail = getConnectedUserEmail();
        if (connectedUserEmail == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // 401
        }
        if (!isTeacherOrHR()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN); // 403 - Only Teacher/HR can create tasks
        }
        try {
            Tache createdTache = tacheService.createTacheFromDTO(tacheCreationDTO);
            return new ResponseEntity<>(createdTache, HttpStatus.CREATED); // 201
        } catch (RuntimeException e) {
            System.err.println("Error creating task: " + e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST); // 400
        }
    }

    /**
     * Retrieves a task by its ID.
     * For students, only accessible if they are assigned to the task.
     * For TEACHER/HR_COMPANY, any task is accessible.
     * @param id The ID of the task to retrieve.
     * @return The Tache entity.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Tache> getTacheById(@PathVariable Long id) {
        String connectedUserEmail = getConnectedUserEmail();
        if (connectedUserEmail == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // 401
        }

        try {
            Tache tache = tacheService.getTacheById(id);

            if (isStudent()) {
                if (tache.getEtudiantsAffectes() == null || !tache.getEtudiantsAffectes().contains(connectedUserEmail)) {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN); // 403 - Student not assigned to this task
                }
            }
            return new ResponseEntity<>(tache, HttpStatus.OK); // 200
        } catch (RuntimeException e) {
            System.err.println("Error getting task by ID " + id + ": " + e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND); // 404
        }
    }

    /**
     * Retrieves all tasks.
     * For students, only tasks they are assigned to are returned.
     * For TEACHER/HR_COMPANY, all tasks are returned.
     * @return A list of Tache entities.
     */
    @GetMapping
    public ResponseEntity<List<Tache>> getAllTaches() {
        String connectedUserEmail = getConnectedUserEmail();
        if (connectedUserEmail == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // 401
        }

        List<Tache> taches;
        if (isStudent()) {
            taches = tacheService.getTachesByStudentEmail(connectedUserEmail);
        } else {
            taches = tacheService.getAllTaches();
        }
        return new ResponseEntity<>(taches, HttpStatus.OK); // 200
    }

    /**
     * Updates an existing task.
     * Accessible only by TEACHER or HR_COMPANY roles.
     * @param id The ID of the task to update.
     * @param tacheDetails The updated Tache entity details.
     * @return The updated Tache entity.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Tache> updateTache(@PathVariable Long id, @RequestBody Tache tacheDetails) {
        String connectedUserEmail = getConnectedUserEmail();
        if (connectedUserEmail == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // 401
        }
        if (!isTeacherOrHR()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN); // 403 - Only Teacher/HR can update tasks
        }
        try {
            Tache updated = tacheService.updateTache(id, tacheDetails);
            return new ResponseEntity<>(updated, HttpStatus.OK); // 200
        } catch (RuntimeException e) {
            System.err.println("Error updating task " + id + ": " + e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND); // 404
        }
    }

    /**
     * Deletes a task by its ID.
     * Accessible only by TEACHER or HR_COMPANY roles.
     * @param id The ID of the task to delete.
     * @return No content if successful.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTache(@PathVariable Long id) {
        String connectedUserEmail = getConnectedUserEmail();
        if (connectedUserEmail == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // 401
        }
        if (!isTeacherOrHR()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN); // 403 - Only Teacher/HR can delete tasks
        }
        try {
            tacheService.deleteTache(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204
        } catch (RuntimeException e) {
            System.err.println("Error deleting task " + id + ": " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404
        }
    }


    // --- Specific Task Operations ---

    /**
     * Updates the status of a task.
     * TEACHER/HR_COMPANY can update any task's status.
     * Students can only update the status of tasks they are assigned to, and only set it to 'DONE'.
     * @param id The ID of the task.
     * @param newStatus The new status to set.
     * @return The updated Tache entity.
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<Tache> updateTacheStatus(@PathVariable Long id, @RequestParam TaskStatus newStatus) {
        String connectedUserEmail = getConnectedUserEmail();
        if (connectedUserEmail == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // 401
        }

        try {
            Tache existingTache = tacheService.getTacheById(id);

            if (!isTeacherOrHR()) { // Not an admin role
                if (isStudent()) {
                    // Student can only update their assigned tasks
                    if (existingTache.getEtudiantsAffectes() == null || !existingTache.getEtudiantsAffectes().contains(connectedUserEmail)) {
                        return new ResponseEntity<>(HttpStatus.FORBIDDEN); // 403 - Student not assigned
                    }
                    // Student can only set status to DONE
                    if (newStatus != TaskStatus.DONE) {
                        return new ResponseEntity<>(HttpStatus.FORBIDDEN); // 403 - Student can only mark as DONE
                    }
                } else {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN); // 403 - Any other role not permitted
                }
            }

            Tache updatedTache = tacheService.updateTaskStatus(id, newStatus);
            return new ResponseEntity<>(updatedTache, HttpStatus.OK); // 200
        } catch (RuntimeException e) {
            System.err.println("Error updating status for task " + id + ": " + e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST); // 400
        }
    }

    /**
     * Logs hours spent on a task.
     * Accessible only by TEACHER or HR_COMPANY roles. (Consider allowing students to log time on their own tasks)
     * @param taskId The ID of the task.
     * @param hoursToLog The number of hours to add to logged time.
     * @return The updated Tache entity.
     */
    @PutMapping("/{taskId}/log-time")
    public ResponseEntity<Tache> logTimeOnTask(@PathVariable Long taskId, @RequestParam Double hoursToLog) {
        String connectedUserEmail = getConnectedUserEmail();
        if (connectedUserEmail == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // 401
        }
        // Current rule: only Teacher/HR can log time.
        // You might extend this to allow students to log time on their own tasks.
        if (!isTeacherOrHR()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN); // 403
        }
        try {
            Tache updatedTache = tacheService.logTimeOnTask(taskId, hoursToLog);
            return new ResponseEntity<>(updatedTache, HttpStatus.OK); // 200
        } catch (RuntimeException e) {
            System.err.println("Error logging time on task " + taskId + ": " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // 400
        }
    }

    // --- Student Assignment Operations for Tasks ---

    /**
     * Assigns a student (by email) to a task.
     * Accessible only by TEACHER or HR_COMPANY roles.
     * @param id The ID of the task.
     * @param email The email of the student to assign.
     * @return The updated Tache entity.
     */
    @PostMapping("/{id}/etudiants")
    public ResponseEntity<Tache> affecterEtudiantToTache(@PathVariable Long id, @RequestParam String email) {
        String connectedUserEmail = getConnectedUserEmail();
        if (connectedUserEmail == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // 401
        }
        if (!isTeacherOrHR()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN); // 403
        }
        try {
            Tache tache = tacheService.affecterEtudiantToTache(id, email);
            return new ResponseEntity<>(tache, HttpStatus.OK); // 200
        } catch (RuntimeException e) {
            System.err.println("Error affecting student " + email + " to task " + id + ": " + e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST); // 400
        }
    }

    /**
     * Removes a student (by email) from a task.
     * Accessible only by TEACHER or HR_COMPANY roles.
     * @param id The ID of the task.
     * @param email The email of the student to remove.
     * @return The updated Tache entity.
     */
    @DeleteMapping("/{id}/etudiants")
    public ResponseEntity<Tache> supprimerEtudiantFromTache(@PathVariable Long id, @RequestParam String email) {
        String connectedUserEmail = getConnectedUserEmail();
        if (connectedUserEmail == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // 401
        }
        if (!isTeacherOrHR()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN); // 403
        }
        try {
            Tache tache = tacheService.supprimerEtudiantFromTache(id, email);
            return new ResponseEntity<>(tache, HttpStatus.OK); // 200
        } catch (RuntimeException e) {
            System.err.println("Error removing student " + email + " from task " + id + ": " + e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST); // 400
        }
    }

    /**
     * Retrieves the list of students assigned to a task.
     * For students, only accessible if they are assigned to that specific task.
     * For TEACHER/HR_COMPANY, all assigned students are visible.
     * @param id The ID of the task.
     * @return A list of emails of assigned students.
     */
    @GetMapping("/{id}/etudiants")
    public ResponseEntity<List<String>> getEtudiantsAffectesToTache(@PathVariable Long id) {
        String connectedUserEmail = getConnectedUserEmail();
        if (connectedUserEmail == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // 401
        }

        try {
            Tache tache = tacheService.getTacheById(id);
            if (isStudent()) {
                if (tache.getEtudiantsAffectes() == null || !tache.getEtudiantsAffectes().contains(connectedUserEmail)) {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN); // 403
                }
            }
            List<String> etudiants = tache.getEtudiantsAffectes();
            return new ResponseEntity<>(etudiants, HttpStatus.OK); // 200
        } catch (RuntimeException e) {
            System.err.println("Error fetching assigned students for task " + id + ": " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404
        }
    }


    // --- Search and Filtering ---

    /**
     * Retrieves tasks associated with a specific sprint ID.
     * For students, only tasks they are assigned to within that sprint are returned.
     * @param sprintId The ID of the sprint.
     * @return A list of Tache entities.
     */
    @GetMapping("/by-sprint/{sprintId}")
    public ResponseEntity<List<Tache>> getTachesBySprint(@PathVariable Long sprintId) {
        String connectedUserEmail = getConnectedUserEmail();
        if (connectedUserEmail == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // 401
        }

        try {
            List<Tache> taches = tacheService.getTasksBySprintId(sprintId);

            if (isStudent()) {
                // Students can only see tasks within that sprint they are assigned to.
                taches = taches.stream()
                        .filter(t -> t.getEtudiantsAffectes() != null && t.getEtudiantsAffectes().contains(connectedUserEmail))
                        .collect(Collectors.toList());
            }
            return new ResponseEntity<>(taches, HttpStatus.OK); // 200
        } catch (RuntimeException e) {
            System.err.println("Error getting tasks for sprint " + sprintId + ": " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404
        }
    }

    /**
     * Retrieves tasks based on their status.
     * For students, only their assigned tasks matching the status are returned.
     * @param status The TaskStatus to filter by.
     * @return A list of Tache entities.
     */
    @GetMapping("/by-status/{status}")
    public ResponseEntity<List<Tache>> getTachesByStatus(@PathVariable TaskStatus status) {
        String connectedUserEmail = getConnectedUserEmail();
        if (connectedUserEmail == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // 401
        }

        List<Tache> taches = tacheService.getTasksByStatus(status);

        if (isStudent()) {
            taches = taches.stream()
                    .filter(t -> t.getEtudiantsAffectes() != null && t.getEtudiantsAffectes().contains(connectedUserEmail))
                    .collect(Collectors.toList());
        }
        return new ResponseEntity<>(taches, HttpStatus.OK); // 200
    }
}