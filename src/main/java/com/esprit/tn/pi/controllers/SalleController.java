package com.esprit.tn.pi.controllers;

import com.esprit.tn.pi.entities.Salle;
import com.esprit.tn.pi.services.SalleService;
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
    private final SalleService SalleService;

    @PostMapping("/add")
    public ResponseEntity<Salle> ajouterSalle(@RequestBody Salle salle) {
        Salle createdSalle = SalleService.ajouterSalle(salle);
        return new ResponseEntity<>(createdSalle, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Salle>> getAllSalles() {
        return new ResponseEntity<>(SalleService.getAllSalle(), HttpStatus.OK);
    }


    @GetMapping("/salles-avec-reservations")
    public ResponseEntity<List<Salle>> getSallesAvecReservationsDisponibles() {
        try {
            List<Salle> sallesDisponibles = SalleService.getSallesAvecReservationsDisponibles();
            if (sallesDisponibles.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(sallesDisponibles, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/salles-disponibles")
    public ResponseEntity<List<Salle>> getSallesDisponibles(
            @RequestParam String date,
            @RequestParam String heure,
            @RequestParam String duree) {
        try {
            System.out.println("Demande de disponibilité des salles - Date: " + date + ", Heure: " + heure + ", Durée: " + duree);
            List<Salle> sallesDisponibles = SalleService.getSallesDisponiblesPourReunion(date, heure, duree);
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
            List<Salle> sallesDisponibles = SalleService.getAllSalleDisponible(); // Cette méthode doit filtrer les salles disponibles
            return new ResponseEntity<>(sallesDisponibles, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
