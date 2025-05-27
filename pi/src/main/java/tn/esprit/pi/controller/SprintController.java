package tn.esprit.pi.controller;

import tn.esprit.pi.dto.CalendarEventDto;
import tn.esprit.pi.dto.CreateSprintDto; // Importez le DTO de création
import tn.esprit.pi.dto.SprintWithTasksDTO;
import tn.esprit.pi.entities.Sprint;
import tn.esprit.pi.entities.Tache;
import tn.esprit.pi.repositories.SprintRepo;
import tn.esprit.pi.services.ISprintService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/sprints")
public class SprintController {

    private final ISprintService sprintService;


    @Autowired
    public SprintController(ISprintService sprintService) {
        this.sprintService = sprintService;
    }

    @PostMapping
    public ResponseEntity<Sprint> createSprint(@RequestBody CreateSprintDto createSprintDto) {
        try {
            Sprint savedSprint = sprintService.createSprint(createSprintDto);
            return new ResponseEntity<>(savedSprint, HttpStatus.CREATED); // 201 Created
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // 400 Bad Request en cas d'erreur (projet non trouvé)
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sprint> getSprintById(@PathVariable Long id) {
        Sprint sprint = sprintService.getSprintById(id);
        if (sprint != null) {
            return new ResponseEntity<>(sprint, HttpStatus.OK); // 200 OK
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404 Not Found
        }
    }

    @GetMapping
    public ResponseEntity<List<Sprint>> getAllSprints() {
        List<Sprint> sprints = sprintService.getAllSprints();
        return new ResponseEntity<>(sprints, HttpStatus.OK); // 200 OK
    }

    @PutMapping("/{id}")
    public ResponseEntity<Sprint> updateSprint(@PathVariable Long id, @RequestBody Sprint sprint) {
        Sprint updatedSprint = sprintService.updateSprint(id, sprint);
        if (updatedSprint != null) {
            return new ResponseEntity<>(updatedSprint, HttpStatus.OK); // 200 OK
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404 Not Found
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSprint(@PathVariable Long id) {
        sprintService.deleteSprint(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 No Content
    }

    @PostMapping("/{id}/etudiants")
    public ResponseEntity<Sprint> affecterEtudiantAuSprint(@PathVariable Long id, @RequestBody String nomEtudiant) {
        Sprint updatedSprint = sprintService.affecterEtudiantAuSprint(id, nomEtudiant);
        return updatedSprint != null ?
                new ResponseEntity<>(updatedSprint, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}/etudiants/{nomEtudiant}")
    public ResponseEntity<Sprint> supprimerEtudiantDuSprint(@PathVariable Long id, @PathVariable String nomEtudiant) {
        Sprint updatedSprint = sprintService.supprimerEtudiantDuSprint(id, nomEtudiant);
        return updatedSprint != null ?
                new ResponseEntity<>(updatedSprint, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/{id}/etudiants")
    public ResponseEntity<List<String>> getEtudiantsAffectesAuSprint(@PathVariable Long id) {
        List<String> etudiants = sprintService.getEtudiantsAffectesAuSprint(id);
        return etudiants != null ?
                new ResponseEntity<>(etudiants, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/calendar-events")
    @Operation(summary = "Récupère les événements à afficher dans le calendrier",
            description = "Retourne une liste d'événements incluant les projets et les sprints")
    public ResponseEntity<List<CalendarEventDto>> getCalendarEvents() {
        List<CalendarEventDto> events = sprintService.getAllCalendarEvents();
        return new ResponseEntity<>(events, HttpStatus.OK);
    }


    @GetMapping("/search")
    public ResponseEntity<List<Sprint>> searchSprints(@RequestParam(value = "nom") String nom) {
        List<Sprint> sprints = sprintService.searchSprintsByNom(nom);
        return new ResponseEntity<>(sprints, HttpStatus.OK);
    }

    @GetMapping("/{id}/withTasks") // Nouvelle méthode pour récupérer le sprint avec ses tâches
    public ResponseEntity<SprintWithTasksDTO> getSprintWithTasks(@PathVariable Long id) {
        Optional<SprintWithTasksDTO> sprintWithTasksDTO = sprintService.getSprintWithTasks(id);
        if (sprintWithTasksDTO.isPresent()) {
            return new ResponseEntity<>(sprintWithTasksDTO.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/{sprintId}/taches")
    public ResponseEntity<Tache> createTacheForSprint(@PathVariable Long sprintId, @RequestBody Tache tache) {
        Tache savedTache = sprintService.createTaskForSprint(sprintId, tache);
        if (savedTache != null) {
            return new ResponseEntity<>(savedTache, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Sprint non trouvé
        }
    }

    @GetMapping("/projet/{projetId}")
    public ResponseEntity<List<Sprint>> getSprintsByProjet(@PathVariable Long projetId) {
        List<Sprint> sprints = sprintService.getSprintsByProjetId(projetId);
        return ResponseEntity.ok(sprints);
    }


}
