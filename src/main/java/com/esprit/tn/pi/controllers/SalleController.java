package com.esprit.tn.pi.controllers;

import com.esprit.tn.pi.entities.Salle;
import com.esprit.tn.pi.services.SalleService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@RestController
@AllArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@CrossOrigin(origins = "*")
@RequestMapping("/pi/salles")
public class SalleController {
    private final SalleService salleService;

    @PostMapping("/add")
    public ResponseEntity<Salle> ajouterSalle(@RequestBody Salle salle) {
        Salle createdSalle = salleService.ajouterSalle(salle);
        return new ResponseEntity<>(createdSalle, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Salle>> getAllSalles() {
        return new ResponseEntity<>(salleService.getAllSalle(), HttpStatus.OK);
    }


    @GetMapping("/salles-avec-reservations")
    public ResponseEntity<List<Salle>> getSallesAvecReservationsDisponibles() {
        try {
            List<Salle> sallesDisponibles = salleService.getSallesAvecReservationsDisponibles();
            return new ResponseEntity<>(sallesDisponibles, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PutMapping("/update-reunion-salle/{reunionId}/{salleId}")
    @Transactional
    public ResponseEntity<Void> updateReunionSalle(@PathVariable Long reunionId, @PathVariable Long salleId) {
        try {
            salleService.updateReunionSalle(reunionId, salleId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @GetMapping("/salles-disponibles")
    public ResponseEntity<List<Salle>> getSallesDisponibles(
            @RequestParam String date,
            @RequestParam String heure,
            @RequestParam String duree) {
        try {
            System.out.println("Demande de disponibilité des salles - Date: " + date + ", Heure: " + heure + ", Durée: " + duree);
            List<Salle> sallesDisponibles = salleService.getSallesDisponiblesPourReunion(date, heure, duree);
            if (sallesDisponibles.isEmpty()) {
                System.out.println("Aucune salle disponible trouvée pour la date " + date + ", l'heure " + heure + " et la durée " + duree);
            } else {
                System.out.println("Nombre de salles disponibles : " + sallesDisponibles.size());
            }
            return new ResponseEntity<>(sallesDisponibles, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("Erreur lors de la récupération des salles disponibles : " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/salles-disponibles-uniques")
    public ResponseEntity<List<Salle>> getAllSalleDisponible() {
        try {
            List<Salle> sallesDisponibles = salleService.getAllSalleDisponible(); // Cette méthode doit filtrer les salles disponibles
            return new ResponseEntity<>(sallesDisponibles, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Salle> updateSalle(@PathVariable Long id, @RequestBody Salle salle) {
        try {
            Salle updatedSalle = salleService.updateSalle(id, salle);
            return ResponseEntity.ok(updatedSalle);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteSalle(@PathVariable Long id) {
        try {
            salleService.deleteSalle(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
