package com.esprit.tn.pi.controllers;

import com.esprit.tn.pi.entities.*;
import com.esprit.tn.pi.repositories.SalleRepository;
import com.esprit.tn.pi.services.AllServices;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@AllArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@CrossOrigin(origins = "*")
@RequestMapping("/pi")
public class allControllers {
    AllServices allServices;
    SalleRepository salleRepository;;

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
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Erreur lors de la suppression de la réunion: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
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
    @GetMapping("/salles-disponibles-uniques")
    public ResponseEntity<List<Salle>> getAllSalleDisponible() {
        try {
            // Récupérer toutes les salles disponibles sans filtrage par date/heure/durée
            List<Salle> sallesDisponibles = allServices.getAllSalleDisponible(); // Cette méthode doit filtrer les salles disponibles

            return new ResponseEntity<>(sallesDisponibles, HttpStatus.OK);
        } catch (Exception e) {
            // Gestion des erreurs
            log.error("Erreur lors de la récupération des salles disponibles : ", e);
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/salles-disponibles")
    public ResponseEntity<List<Salle>> getSallesDisponibles(
            @RequestParam String date,
            @RequestParam String heure,
            @RequestParam String duree) {
        try {
            // Affichage des paramètres reçus
            System.out.println("Demande de disponibilité des salles - Date: " + date + ", Heure: " + heure + ", Durée: " + duree);

            List<Salle> sallesDisponibles = allServices.getSallesDisponiblesPourReunion(date, heure, duree);

            // Affichage des salles disponibles trouvées
            if (sallesDisponibles.isEmpty()) {
                System.out.println("Aucune salle disponible trouvée pour la date " + date + ", l'heure " + heure + " et la durée " + duree);
            } else {
                System.out.println("Nombre de salles disponibles : " + sallesDisponibles.size());
            }

            return new ResponseEntity<>(sallesDisponibles, HttpStatus.OK);
        } catch (Exception e) {
            // Affichage de l'exception
            System.out.println("Erreur lors de la récupération des salles disponibles : " + e.getMessage());
            e.printStackTrace(); // Pour afficher la pile d'erreurs si nécessaire
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/salles-avec-reservations")
    public ResponseEntity<List<Salle>> getSallesAvecReservations() {
        try {
            List<Salle> salles = salleRepository.findByReservationsIsNotNull();
            if (salles.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            // Ajouter un log pour vérifier la structure de la réponse
            System.out.println("Salles avec réservations: " + salles);

            return new ResponseEntity<>(salles, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des salles : " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/reserver-salle")
    public ResponseEntity<?> reserverSalle(
            @RequestParam Long salleId,
            @RequestParam String date,
            @RequestParam String heure,
            @RequestParam String duree,
            @RequestParam Long reunionId) {  // Ajout du paramètre reunionId
        try {
            // Récupérer la salle
            Salle salle = allServices.getSalleById(salleId);
            if (salle == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Salle non trouvée avec l'ID spécifié.");
            }

            // Vérifier la disponibilité de la salle
            boolean disponible = allServices.verifierDisponibiliteSalle(salle, date, heure, duree, reunionId);
            if (!disponible) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("La salle n'est pas disponible pour la date et l'heure spécifiées.");
            }

            // Récupérer la réunion en fonction de l'ID de la réunion
            Reunion reunion = allServices.getReunionById(reunionId);
            if (reunion == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Réunion non trouvée avec l'ID spécifié.");
            }

            // Créer la réservation
            ReservationSalle reservation = new ReservationSalle();
            reservation.setSalle(salle);
            reservation.setReunion(reunion);
            reservation.setDate(date);
            reservation.setHeure(heure);
            reservation.setDuree(duree);

            // Sauvegarder la réservation
            allServices.saveReservation(reservation);

            // Marquer la salle comme non disponible
            salle.setDisponible(false);

            // Sauvegarder la mise à jour de la salle
            salleRepository.save(salle); // Ici, nous utilisons le repository pour enregistrer la salle mise à jour.

            return ResponseEntity.ok("Salle réservée avec succès.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la réservation de la salle: " + e.getMessage());
        }
    }
    @PutMapping("/reservation/{id}")
    public ResponseEntity<?> updateSalleReservation(@PathVariable Long id, @RequestBody ReservationSalle updatedReservation) {
        log.info("Mise à jour de la réservation avec ID: {}", id);

        try {
            // Vérifie si la réservation existe
            ReservationSalle existingReservation = allServices.getReservationById(id);
            if (existingReservation == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Réservation non trouvée avec l'ID spécifié.");
            }

            // Vérifie si une nouvelle salle est fournie
            if (updatedReservation.getSalle() != null) {
                Salle nouvelleSalle = updatedReservation.getSalle();

                // Vérifie la disponibilité de la nouvelle salle
                boolean disponible = allServices.verifierDisponibiliteSalle(
                        nouvelleSalle,
                        updatedReservation.getDate(),
                        updatedReservation.getHeure(),
                        updatedReservation.getDuree(),
                        updatedReservation.getReunion().getId()
                );

                if (!disponible) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body("La salle sélectionnée n'est pas disponible pour la date et l'heure spécifiées.");
                }

                // Met à jour la salle
                existingReservation.setSalle(nouvelleSalle);
                nouvelleSalle.setDisponible(false); // Si nécessaire
                salleRepository.save(nouvelleSalle);
            }

            // Mise à jour des autres champs
            existingReservation.setDate(updatedReservation.getDate());
            existingReservation.setHeure(updatedReservation.getHeure());
            existingReservation.setDuree(updatedReservation.getDuree());

            // Sauvegarde finale
            ReservationSalle savedReservation = allServices.saveReservation(existingReservation);

            return ResponseEntity.ok(savedReservation);

        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour de la réservation: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la mise à jour de la réservation: " + e.getMessage());
        }
    }


    @DeleteMapping("/participant/{id}")
    public ResponseEntity<Map<String, String>> deleteParticipant(@PathVariable Long id) {
        Map<String, String> response = new HashMap<>();
        try {
            allServices.deleteParticipant(id);
            response.put("message", "Participant supprimé avec succès.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Erreur lors de la suppression du participant: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PutMapping("/participant/{id}")
    public ResponseEntity<?> updateParticipant(@PathVariable Long id, @RequestBody Participant updatedParticipant) {
        try {
            // Appeler le service pour mettre à jour le participant
            Participant participant = allServices.updateParticipant(id, updatedParticipant);
            return new ResponseEntity<>(participant, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            // Gestion des erreurs si le participant n'est pas trouvé
            Map<String, String> response = new HashMap<>();
            response.put("message", "Participant non trouvé avec ID : " + id);
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            // Gestion des erreurs si les données sont invalides
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            // Gestion des erreurs internes
            Map<String, String> response = new HashMap<>();
            response.put("message", "Erreur interne du serveur");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}

