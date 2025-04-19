package com.esprit.tn.pi.controllers;

import com.esprit.tn.pi.entities.Participant;
import com.esprit.tn.pi.entities.Reunion;
import com.esprit.tn.pi.entities.Salle;
import com.esprit.tn.pi.entities.User;
import com.esprit.tn.pi.services.AllServices;
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
@RequestMapping("/pi")
public class allControllers {
    AllServices allServices;

    @PostMapping("/user")
    public ResponseEntity<User> ajouterUtilisateur(@RequestBody User user) {
        User createdUser = allServices.ajouterUser(user);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }
    @PostMapping("/participant")
    public ResponseEntity<Participant> ajouterParticipant(@RequestBody Participant participant) {
        Participant createdParticipant = allServices.ajouterParticipant(participant);
        return new ResponseEntity<>(createdParticipant, HttpStatus.CREATED);
    }

    @PostMapping("/create")
    public ResponseEntity<Reunion> createReunion(@RequestBody Reunion reunion) {
        Reunion savedReunion = allServices.createReunion(reunion);
        return new ResponseEntity<>(savedReunion, HttpStatus.CREATED);
    }

    @PostMapping("/salle")
    public ResponseEntity<Salle> ajouterSalle(@RequestBody Salle salle) {
        Salle createdSalle = allServices.ajouterSalle(salle);
        return new ResponseEntity<>(createdSalle, HttpStatus.CREATED);
    }


    @PutMapping("/reunion/{id}")
    public ResponseEntity<?> updateReunion(@PathVariable("id") Long id, @RequestBody Reunion updatedReunion) {
        try {
            System.out.println("🔄 Mise à jour de la réunion ID : " + id);
            Reunion reunion = allServices.updateReunion(id, updatedReunion);
            return new ResponseEntity<>(reunion, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            System.err.println("❌ Réunion non trouvée avec ID : " + id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Réunion non trouvée avec ID : " + id);
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            System.err.println("❌ Erreur de validation : " + e.getMessage());
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
            allServices.deleteReunion(id);
            response.put("message", "Réunion supprimée avec succès.");
            return ResponseEntity.ok(response); // Retourner une réponse JSON avec un message de succès
        } catch (Exception e) {
            response.put("message", "Erreur lors de la suppression de la réunion: " + e.getMessage());
            return ResponseEntity.status(500).body(response); // Retourner une réponse JSON avec le message d'erreur
        }
    }


    @GetMapping("/evenements")
    public ResponseEntity<?> getEvenements() {
        try {
            return new ResponseEntity<>(allServices.getEvenements(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Erreur lors de la récupération des événements", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/participants")
    public List<Participant> getAllParticipants() {
        return allServices.getAllParticipants();
    }

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return allServices.getAllUsers();
    }

    @GetMapping("/salle")
    public List<Salle> getAllSalle() {
        return allServices.getAllSalle();
    }


    @GetMapping("/reunions")
    public List<Reunion> getAllReunions() {
        return allServices.getAllReunions();
    }
}

