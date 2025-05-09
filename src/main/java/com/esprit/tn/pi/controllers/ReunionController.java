package com.esprit.tn.pi.controllers;

import com.esprit.tn.pi.entities.Reunion;
import com.esprit.tn.pi.services.ReunionService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@AllArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@CrossOrigin(origins = "*")
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
            return new ResponseEntity<>(reunion, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Réunion non trouvée avec ID : " + id);
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> response = new HashMap<>();
            response.put("message", "Erreur interne du serveur");
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
    public List<Reunion> getAllReunions() {
        return reunionService.getAllReunions();
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
