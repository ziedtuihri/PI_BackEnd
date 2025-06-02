package tn.esprit.pi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.bind.annotation.RestController;
import tn.esprit.pi.dto.CalendarEventDto;
import tn.esprit.pi.dto.CreateSprintDto;
import tn.esprit.pi.dto.SprintWithTasksDTO;
import tn.esprit.pi.entities.Sprint;
import tn.esprit.pi.entities.Tache; // Import Tache entity if createTask needs to return it
import tn.esprit.pi.services.ISprintService;
import tn.esprit.pi.dto.TacheCreationDTO; // IMPORTANT: Make sure this DTO exists
import tn.esprit.pi.services.ITacheService; // IMPORTANT: Make sure this interface and its implementation exist

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sprints")
//@CrossOrigin(origins = "http://localhost:4200") // Enabled for frontend communication
public class SprintController {

    private final ISprintService sprintService;
    private final ITacheService tacheService; // Injected for task creation/management

    @Autowired
    public SprintController(ISprintService sprintService, ITacheService tacheService) {
        this.sprintService = sprintService;
        this.tacheService = tacheService;
    }

    // Helper to get connected user's email from Spring Security context
    private String getConnectedUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return null; // No user authenticated or it's an anonymous session
        }
        return authentication.getName(); // This usually returns the username (which is your email)
    }

    // Helper to check if connected user has a specific role
    // Assumes Spring Security prefixes roles with "ROLE_" (e.g., "ROLE_USER", "ROLE_TEACHER")
    private boolean hasRole(String roleName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        // Spring Security often prefixes roles with "ROLE_" by default
        String prefixedRoleName = "ROLE_" + roleName.toUpperCase();
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(prefixedRoleName));
    }

    // Convenience method to check if the user is a student (based on "USER" role)
    private boolean isStudent() {
        return hasRole("USER"); // As clarified: "USER" role means "ETUDIANT" (student)
    }

    // Convenience method to check if the user is a teacher or HR_Company (admin-like roles for projects/sprints)
    private boolean isTeacherOrHR() {
        return hasRole("TEACHER") || hasRole("HR_COMPANY");
    }


    // --- Sprint CRUD Operations ---

    // Create a new sprint (Accessible only by TEACHER or HR_COMPANY)
    @PostMapping
    public ResponseEntity<Sprint> createSprint(@RequestBody CreateSprintDto dto) {
        String connectedUserEmail = getConnectedUserEmail();
        if (connectedUserEmail == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // 401
        }
        if (!isTeacherOrHR()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN); // 403 - Students cannot create sprints
        }
        try {
            Sprint sprint = sprintService.createSprint(dto);
            return new ResponseEntity<>(sprint, HttpStatus.CREATED); // 201
        } catch (Exception e) {
            System.err.println("Error creating sprint: " + e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST); // 400
        }
    }

    // Get a sprint by ID (Filtered for students: only if assigned)
    @GetMapping("/{id}")
    public ResponseEntity<Sprint> getSprintById(@PathVariable Long id) {
        String connectedUserEmail = getConnectedUserEmail();
        if (connectedUserEmail == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // 401
        }

        try {
            Sprint sprint = sprintService.getSprintById(id);

            // If the connected user is a student, check if they are assigned to this sprint
            if (isStudent()) {
                if (sprint.getEtudiantsAffectes() == null || !sprint.getEtudiantsAffectes().contains(connectedUserEmail)) {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN); // 403 - Student not assigned
                }
            }
            return new ResponseEntity<>(sprint, HttpStatus.OK); // 200
        } catch (RuntimeException e) {
            System.err.println("Error getting sprint by ID " + id + ": " + e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND); // 404
        }
    }

    // Get all sprints (Filtered for students: only assigned sprints; for others: all sprints)
    @GetMapping
    public ResponseEntity<List<Sprint>> getAllSprints() {
        String connectedUserEmail = getConnectedUserEmail();
        if (connectedUserEmail == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // 401
        }

        List<Sprint> sprints;
        if (isStudent()) {
            // If the connected user is a student, retrieve only sprints assigned to them
            sprints = sprintService.getSprintsByStudentEmail(connectedUserEmail);
        } else {
            // For Teacher or HR_Company, retrieve all sprints
            sprints = sprintService.getAllSprints();
        }
        return new ResponseEntity<>(sprints, HttpStatus.OK); // 200
    }

    // Update a sprint (Accessible only by TEACHER or HR_COMPANY)
    @PutMapping("/{id}")
    public ResponseEntity<Sprint> updateSprint(@PathVariable Long id, @RequestBody Sprint sprintDetails) {
        String connectedUserEmail = getConnectedUserEmail();
        if (connectedUserEmail == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // 401
        }
        if (!isTeacherOrHR()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN); // 403 - Students cannot update sprints
        }
        try {
            Sprint updated = sprintService.updateSprint(id, sprintDetails);
            return new ResponseEntity<>(updated, HttpStatus.OK); // 200
        } catch (RuntimeException e) {
            System.err.println("Error updating sprint " + id + ": " + e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND); // 404
        }
    }

    // Delete a sprint (Accessible only by TEACHER or HR_COMPANY)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSprint(@PathVariable Long id) {
        String connectedUserEmail = getConnectedUserEmail();
        if (connectedUserEmail == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // 401
        }
        if (!isTeacherOrHR()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN); // 403 - Students cannot delete sprints
        }
        try {
            sprintService.deleteSprint(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204
        } catch (RuntimeException e) {
            System.err.println("Error deleting sprint " + id + ": " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404
        }
    }


    // --- Student Assignment Operations ---

    // Assign a student to a sprint (Accessible only by TEACHER or HR_COMPANY)
    @PostMapping("/{id}/etudiants")
    public ResponseEntity<Sprint> affecterEtudiant(@PathVariable Long id, @RequestParam String email) {
        String connectedUserEmail = getConnectedUserEmail();
        if (connectedUserEmail == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // 401
        }
        if (!isTeacherOrHR()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN); // 403
        }
        try {
            Sprint sprint = sprintService.affecterEtudiantAuSprint(id, email);
            return new ResponseEntity<>(sprint, HttpStatus.OK); // 200
        } catch (RuntimeException e) {
            System.err.println("Error affecting student " + email + " to sprint " + id + ": " + e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST); // 400
        }
    }

    // Remove a student from a sprint (Accessible only by TEACHER or HR_COMPANY)
    @DeleteMapping("/{id}/etudiants")
    public ResponseEntity<Sprint> supprimerEtudiant(@PathVariable Long id, @RequestParam String email) {
        String connectedUserEmail = getConnectedUserEmail();
        if (connectedUserEmail == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // 401
        }
        if (!isTeacherOrHR()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN); // 403
        }
        try {
            Sprint sprint = sprintService.supprimerEtudiantDuSprint(id, email);
            return new ResponseEntity<>(sprint, HttpStatus.OK); // 200
        } catch (RuntimeException e) {
            System.err.println("Error removing student " + email + " from sprint " + id + ": " + e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST); // 400
        }
    }

    // Get list of students assigned to a sprint (Filtered for students: only if assigned to that sprint)
    @GetMapping("/{id}/etudiants")
    public ResponseEntity<List<String>> getEtudiants(@PathVariable Long id) {
        String connectedUserEmail = getConnectedUserEmail();
        if (connectedUserEmail == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // 401
        }

        try {
            Sprint sprint = sprintService.getSprintById(id); // Fetch sprint to check assignment
            if (isStudent()) {
                // If student is not assigned to this sprint, they cannot see its assigned students
                if (sprint.getEtudiantsAffectes() == null || !sprint.getEtudiantsAffectes().contains(connectedUserEmail)) {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN); // 403
                }
            }
            List<String> etudiants = sprint.getEtudiantsAffectes(); // Return the sprint's actual list
            return new ResponseEntity<>(etudiants, HttpStatus.OK); // 200
        } catch (RuntimeException e) {
            System.err.println("Error fetching assigned students for sprint " + id + ": " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404
        }
    }


    // --- Search and DTOs ---

    // Search sprints by name (Filtered for students: only if assigned)
    @GetMapping("/search")
    public ResponseEntity<List<Sprint>> searchSprints(@RequestParam String nom) {
        String connectedUserEmail = getConnectedUserEmail();
        if (connectedUserEmail == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // 401
        }

        List<Sprint> sprints;
        if (isStudent()) {
            // Get all sprints matching the name, then filter them by student's assignment
            sprints = sprintService.searchSprintsByNom(nom).stream()
                    .filter(s -> s.getEtudiantsAffectes() != null && s.getEtudiantsAffectes().contains(connectedUserEmail))
                    .collect(Collectors.toList());
        } else {
            // For Teacher or HR_Company, get all sprints matching the name
            sprints = sprintService.searchSprintsByNom(nom);
        }
        return new ResponseEntity<>(sprints, HttpStatus.OK); // 200
    }

    // Get sprint details with tasks (Filtered for students: only if assigned)
    @GetMapping("/{id}/with-tasks")
    public ResponseEntity<SprintWithTasksDTO> getSprintWithTasks(@PathVariable Long id) {
        String connectedUserEmail = getConnectedUserEmail();
        if (connectedUserEmail == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // 401
        }

        try {
            Optional<SprintWithTasksDTO> dtoOpt = sprintService.getSprintWithTasks(id);
            if (dtoOpt.isPresent()) {
                SprintWithTasksDTO dto = dtoOpt.get();
                if (isStudent()) {
                    // Check if the student is assigned to this specific sprint
                    if (dto.getEtudiantsAffectes() == null || !dto.getEtudiantsAffectes().contains(connectedUserEmail)) {
                        return new ResponseEntity<>(HttpStatus.FORBIDDEN); // 403 - Student not assigned
                    }
                }
                return new ResponseEntity<>(dto, HttpStatus.OK); // 200
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404
            }
        } catch (RuntimeException e) {
            System.err.println("Error getting sprint " + id + " with tasks: " + e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR); // 500
        }
    }


    // --- Task Operations (Requires ITacheService) ---

    // Create a task within a sprint (Accessible only by TEACHER or HR_COMPANY)
    @PostMapping("/{sprintId}/taches")
    public ResponseEntity<Tache> createTache(@PathVariable Long sprintId, @RequestBody TacheCreationDTO tacheCreationDTO) {
        String connectedUserEmail = getConnectedUserEmail();
        if (connectedUserEmail == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // 401
        }
        if (!isTeacherOrHR()) { // Only Teachers or HR_Company can create tasks for now
            return new ResponseEntity<>(HttpStatus.FORBIDDEN); // 403
        }
        try {
            // Call ITacheService to create the task, linking it to the sprint
            Tache createdTache = tacheService.createTacheFromDTO(tacheCreationDTO);
            return new ResponseEntity<>(createdTache, HttpStatus.CREATED); // 201
        } catch (RuntimeException e) {
            System.err.println("Error creating task for sprint " + sprintId + ": " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // 400
        }
    }


    // --- Calendar Event Operations ---

    // Get all calendar events (sprints + projects) - Filtered for students if necessary
    @GetMapping("/calendar")
    public ResponseEntity<List<CalendarEventDto>> getAllCalendarEvents() {
        String connectedUserEmail = getConnectedUserEmail();
        if (connectedUserEmail == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // 401
        }

        List<CalendarEventDto> events = sprintService.getAllCalendarEvents();

        if (isStudent()) {
            // If the underlying service doesn't inherently filter, filter here.
            // This is a client-side filter after fetching all, ideally the service layer should do this.
            // Retrieve all sprints the student is assigned to
            List<Sprint> assignedSprints = sprintService.getSprintsByStudentEmail(connectedUserEmail);
            List<Long> assignedSprintIds = assignedSprints.stream()
                    .map(Sprint::getIdSprint)
                    .collect(Collectors.toList());

            // Filter calendar events: only include sprint events the student is assigned to
            events = events.stream()
                    .filter(event -> "Sprint".equals(event.getCategory()) && assignedSprintIds.contains(event.getId()))
                    .collect(Collectors.toList());
            // Note: If project events are also in CalendarEventDto and need filtering,
            // similar logic for student-assigned projects would be needed.
        }
        return new ResponseEntity<>(events, HttpStatus.OK); // 200
    }

    // --- Other Sprint-related Endpoints (Add as needed) ---

    // Example: Calculate Sprint Velocity
    @GetMapping("/{id}/velocity")
    public ResponseEntity<Double> calculateSprintVelocity(@PathVariable Long id) {
        String connectedUserEmail = getConnectedUserEmail();
        if (connectedUserEmail == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        // Decide if students should see velocity. Typically, this is a manager metric.
        if (isStudent()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN); // Students cannot see velocity
        }
        try {
            double velocity = sprintService.calculateSprintVelocity(id);
            return new ResponseEntity<>(velocity, HttpStatus.OK);
        } catch (RuntimeException e) {
            System.err.println("Error calculating velocity for sprint " + id + ": " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Example: Get Velocity History
    @GetMapping("/velocity/history")
    public ResponseEntity<List<Object[]>> getVelocityHistory() {
        String connectedUserEmail = getConnectedUserEmail();
        if (connectedUserEmail == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        if (isStudent()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN); // Students cannot see velocity history
        }
        List<Object[]> history = sprintService.getVelocityHistory();
        return new ResponseEntity<>(history, HttpStatus.OK);
    }
}