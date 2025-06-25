package tn.esprit.pi.restcontrollers;

import tn.esprit.pi.entities.ReservationSalle;
import tn.esprit.pi.entities.Reunion;
import tn.esprit.pi.entities.Salle;
import tn.esprit.pi.services.ReservationService;
import tn.esprit.pi.services.SalleService;
import tn.esprit.pi.services.ReunionService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)

@RequestMapping("/pi/reservations")
public class ReservationSalleController {
    private final ReservationService reservationService;
    private final SalleService salleService;
    private final ReunionService reunionService;

    @PostMapping("/reserver-salle")
    public ResponseEntity<?> reserverSalle(
            @RequestParam Long salleId,
            @RequestParam String date,
            @RequestParam String heure,
            @RequestParam String duree,
            @RequestParam Long reunionId) {
        try {
            // Récupérer la salle
            Salle salle = salleService.getSalleById(salleId);
            if (salle == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Salle non trouvée avec l'ID spécifié.");
            }
            boolean disponible = reunionService.verifierDisponibiliteSalle(salle, date, heure, duree, reunionId);
            if (!disponible) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("La salle n'est pas disponible pour la date et l'heure spécifiées.");
            }

            Reunion reunion = reunionService.getReunionById(reunionId);
            if (reunion == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Réunion non trouvée avec l'ID spécifié.");
            }
            ReservationSalle reservation = new ReservationSalle();
            reservation.setSalle(salle);
            reservation.setReunion(reunion);
            reservation.setDate(date);
            reservation.setHeure(heure);
            reservation.setDuree(duree);
            reservationService.saveReservation(reservation);
            salle.setDisponible(false);
            salleService.ajouterSalle(salle);
            return ResponseEntity.ok("Salle réservée avec succès.");
        } catch (Exception e) {
            log.error("Erreur lors de la réservation de la salle", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la réservation de la salle: " + e.getMessage());
        }
    }

    @PutMapping("/reservation/{id}")
    public ResponseEntity<?> updateSalleReservation(@PathVariable Long id, @RequestBody ReservationSalle updatedReservation) {
        try {
            ReservationSalle existingReservation = reservationService.getReservationById(id);
            if (existingReservation == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Réservation non trouvée avec l'ID spécifié.");
            }
            existingReservation.setSalle(updatedReservation.getSalle());
            existingReservation.setDate(updatedReservation.getDate());
            existingReservation.setHeure(updatedReservation.getHeure());
            existingReservation.setDuree(updatedReservation.getDuree());
            reservationService.saveReservation(existingReservation);
            return ResponseEntity.ok("Réservation mise à jour avec succès.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la mise à jour de la réservation: " + e.getMessage());
        }
    }
}