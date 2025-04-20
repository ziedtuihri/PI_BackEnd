package esprit.example.pi.controller;

import esprit.example.pi.dto.CalendarEventDto;
import esprit.example.pi.dto.CreateSprintDto; // Importez le DTO de création
import esprit.example.pi.entities.Sprint;
import esprit.example.pi.services.ISprintService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sprints")
public class SprintController {

    private final ISprintService sprintService;

    @Autowired
    public SprintController(ISprintService sprintService) {
        this.sprintService = sprintService;
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @PostMapping
    public ResponseEntity<Sprint> createSprint(@RequestBody CreateSprintDto createSprintDto) {
        try {
            Sprint savedSprint = sprintService.createSprint(createSprintDto);
            return new ResponseEntity<>(savedSprint, HttpStatus.CREATED); // 201 Created
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // 400 Bad Request en cas d'erreur (projet non trouvé)
        }
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @GetMapping("/{id}")
    public ResponseEntity<Sprint> getSprintById(@PathVariable Long id) {
        Sprint sprint = sprintService.getSprintById(id);
        if (sprint != null) {
            return new ResponseEntity<>(sprint, HttpStatus.OK); // 200 OK
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404 Not Found
        }
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @GetMapping
    public ResponseEntity<List<Sprint>> getAllSprints() {
        List<Sprint> sprints = sprintService.getAllSprints();
        return new ResponseEntity<>(sprints, HttpStatus.OK); // 200 OK
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @PutMapping("/{id}")
    public ResponseEntity<Sprint> updateSprint(@PathVariable Long id, @RequestBody Sprint sprint) {
        Sprint updatedSprint = sprintService.updateSprint(id, sprint);
        if (updatedSprint != null) {
            return new ResponseEntity<>(updatedSprint, HttpStatus.OK); // 200 OK
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404 Not Found
        }
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSprint(@PathVariable Long id) {
        sprintService.deleteSprint(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 No Content
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @PostMapping("/{id}/etudiants")
    public ResponseEntity<Sprint> affecterEtudiantAuSprint(@PathVariable Long id, @RequestBody String nomEtudiant) {
        Sprint updatedSprint = sprintService.affecterEtudiantAuSprint(id, nomEtudiant);
        return updatedSprint != null ?
                new ResponseEntity<>(updatedSprint, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @DeleteMapping("/{id}/etudiants/{nomEtudiant}")
    public ResponseEntity<Sprint> supprimerEtudiantDuSprint(@PathVariable Long id, @PathVariable String nomEtudiant) {
        Sprint updatedSprint = sprintService.supprimerEtudiantDuSprint(id, nomEtudiant);
        return updatedSprint != null ?
                new ResponseEntity<>(updatedSprint, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @GetMapping("/{id}/etudiants")
    public ResponseEntity<List<String>> getEtudiantsAffectesAuSprint(@PathVariable Long id) {
        List<String> etudiants = sprintService.getEtudiantsAffectesAuSprint(id);
        return etudiants != null ?
                new ResponseEntity<>(etudiants, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @GetMapping("/calendar-events")
    @Operation(summary = "Récupère les événements à afficher dans le calendrier",
            description = "Retourne une liste d'événements incluant les projets et les sprints")
    public ResponseEntity<List<CalendarEventDto>> getCalendarEvents() {
        List<CalendarEventDto> events = sprintService.getAllCalendarEvents();
        return new ResponseEntity<>(events, HttpStatus.OK);
    }
}