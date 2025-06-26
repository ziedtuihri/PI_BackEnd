package tn.esprit.pi.controller;

import lombok.RequiredArgsConstructor; // Use Lombok's @RequiredArgsConstructor for constructor injection
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.pi.dto.NoteDisplayDto;
import tn.esprit.pi.dto.NoteRequestDto; // Assuming you have this DTO for incoming note requests
import tn.esprit.pi.entities.Note;
import tn.esprit.pi.services.INoteService;
import tn.esprit.pi.user.User; // Assuming User entity is in this package

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor // Automatically injects final fields (like INoteService)
public class NoteController {

    private final INoteService noteService; // Inject the service using constructor injection

    /**
     * Retrieves all notes in the system.
     * GET /api/notes/get
     * @return A list of all Note entities.
     */
    @GetMapping("/get")
    public ResponseEntity<List<Note>> getAllNotes() {
        List<Note> notes = noteService.getAllNotes();
        return ResponseEntity.ok(notes);
    }

    /**
     * Calculates the average score for a specific user within a given project.
     * GET /api/notes/moyenne/projet?projetId={id}&userId={id}
     * @param projetId The ID of the project.
     * @param userId The ID of the user.
     * @return The calculated project average for the user.
     */
    @GetMapping("/moyenne/projet")
    public ResponseEntity<Double> getMoyenneProjet(
            @RequestParam Long projetId,
            @RequestParam Integer userId) {
        double average = noteService.calculerMoyenneProjet(projetId, userId);
        return ResponseEntity.ok(average);
    }

    /**
     * Calculates the general average score for a specific user across all their projects.
     * GET /api/notes/moyenne/utilisateur/{userId}
     * @param userId The ID of the user.
     * @return The calculated general average for the user.
     */
    @GetMapping("/moyenne/utilisateur/{userId}")
    public ResponseEntity<Double> getMoyenneGeneraleUtilisateur(@PathVariable Integer userId) {
        double average = noteService.calculerMoyenneGeneraleUtilisateur(userId);
        return ResponseEntity.ok(average);
    }

    /**
     * Calculates the general average score for all users in the system.
     * GET /api/notes/moyenne/tous
     * @return A map where keys are User entities and values are their general average scores.
     */
    @GetMapping("/moyenne/tous")
    public ResponseEntity<Map<User, Double>> getMoyenneGeneraleTousUtilisateurs() {
        Map<User, Double> averages = noteService.calculerMoyenneGeneraleTousUtilisateurs();
        return ResponseEntity.ok(averages);
    }

    /**
     * Assigns a note to a user for a specific evaluation and sprint.
     * POST /api/notes/affecter
     * @param dto The NoteRequestDto containing evaluationId, sprintId, userId, and valeur.
     * @return The created Note entity, or a bad request response if assignment fails.
     */
    @PostMapping("/affecter")
    public Note affecterNote(@RequestBody NoteRequestDto dto) {
        return noteService.affecterNoteAUtilisateur(dto.getEvaluationId(),
                dto.getSprintId(), dto.getUserId(), dto.getValeur());
    }

    /**
     * Retrieves a list of notes formatted for display, including user, sprint, and project details.
     * GET /api/notes/display
     * @return A list of NoteDisplayDto objects.
     */
    @GetMapping("/display")
    public ResponseEntity<List<NoteDisplayDto>> getNoteDisplayList() {
        List<NoteDisplayDto> displayList = noteService.getNoteDisplayList();
        return ResponseEntity.ok(displayList);
    }
}
