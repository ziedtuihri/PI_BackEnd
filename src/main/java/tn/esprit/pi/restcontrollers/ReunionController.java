package tn.esprit.pi.restcontrollers;

import tn.esprit.pi.entities.Reunion;
import tn.esprit.pi.services.ReunionService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)

@RequestMapping("/pi/reunions")
public class ReunionController {
    private final ReunionService  reunionService;

    @PostMapping("/create")
    public ResponseEntity<Reunion> createReunion(@RequestBody Reunion reunion) {
        Reunion savedReunion = reunionService.createReunion(reunion);
        return new ResponseEntity<>(savedReunion, HttpStatus.CREATED);
    }

    @PutMapping("/reunion/{id}")
    public ResponseEntity<?> updateReunion(@PathVariable("id") Long id, @RequestBody Reunion updatedReunion) {
        try {
            Reunion reunion = reunionService.updateReunion(id, updatedReunion);
            if (reunion == null) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "La réunion n'a pas pu être mise à jour.");
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Réunion mise à jour avec succès");
            response.put("reunion", reunion);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (NoSuchElementException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Réunion non trouvée avec ID : " + id);
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }catch (Exception e) {
        e.printStackTrace(); // Log complet
        Map<String, String> response = new HashMap<>();
        response.put("message", "Erreur interne : " + e.getMessage()); // Message explicite pour Angular
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    }

    @DeleteMapping("/reunion/{id}")
    public ResponseEntity<Map<String, String>> deleteReunion(@PathVariable Long id) {
        Map<String, String> response = new HashMap<>();
        try {
            reunionService.deleteReunion(id);
            response.put("message", "Réunion supprimée avec succès.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Erreur lors de la suppression de la réunion: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("")
    public List<Reunion> getUpcomingReunions() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        return reunionService.getAllReunions()
                .stream()
                .filter(reunion -> {
                    try {
                        LocalDate reunionDate = LocalDate.parse(reunion.getDate(), dateFormatter);
                        LocalTime reunionTime = LocalTime.parse(reunion.getHeure(), timeFormatter);
                        LocalDateTime reunionDateTime = LocalDateTime.of(reunionDate, reunionTime);
                        return reunionDateTime.isAfter(now);
                    } catch (Exception e) {
                        System.out.println("Erreur de parsing pour la réunion ID " + reunion.getId() + ": " + e.getMessage());
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/evenements")
    public ResponseEntity<?> getEvenements() {
        try {
            return new ResponseEntity<>(reunionService.getEvenements(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Erreur lors de la récupération des événements", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}