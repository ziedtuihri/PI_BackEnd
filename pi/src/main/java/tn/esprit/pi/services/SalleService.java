package com.esprit.tn.pi.services;

import com.esprit.tn.pi.entities.ReservationSalle;
import com.esprit.tn.pi.entities.Reunion;
import com.esprit.tn.pi.entities.Salle;
import com.esprit.tn.pi.repositories.ReservationSalleRepository;
import com.esprit.tn.pi.repositories.ReunionRepository;
import com.esprit.tn.pi.repositories.SalleRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalleService {

    private final SalleRepository salleRepository;
    private final ReunionRepository reunionRepository;
    private final ReservationSalleRepository  reservationSalleRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public Salle ajouterSalle(Salle s) {
        return salleRepository.save(s);
    }

    public List<Salle> getAllSalle() {
        return salleRepository.findAll();
    }

    public Salle getSalleById(Long id) {
        return salleRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Salle non trouvée avec l'ID : " + id));
    }

    public List<Salle> getAllSalleDisponible() {
        // Récupérer toutes les salles
        List<Salle> allSalles = salleRepository.findAll();

        // Filtrer uniquement les salles disponibles
        List<Salle> availableSalles = allSalles.stream()
                .filter(Salle::isDisponible)  // Filtrer selon la disponibilité
                .collect(Collectors.toList());

        return availableSalles;
    }

    public List<Salle> getSallesDisponiblesPourReunion(String date, String heure, String dureeStr) {
        List<Salle> toutesLesSalles = salleRepository.findAll();
        List<Salle> sallesDisponibles = new ArrayList<>();

        // Conversion de la date et de l'heure en LocalDateTime
        LocalDateTime debutReunion;
        try {
            debutReunion = LocalDateTime.parse(date + "T" + heure);
        } catch (Exception e) {
            throw new IllegalArgumentException("Format de date ou d'heure incorrect.");
        }

        int dureeMinutes;
        try {
            dureeMinutes = Integer.parseInt(dureeStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("La durée doit être un nombre valide.");
        }

        LocalDateTime finReunion = debutReunion.plusMinutes(dureeMinutes);

        for (Salle salle : toutesLesSalles) {
            boolean disponible = true;

            // Vérification des réunions existantes pour la salle
            if (salle.getReunions() != null) {
                for (Reunion reunionExistante : salle.getReunions()) {
                    try {
                        LocalDateTime debutReunionExistante = LocalDateTime.parse(reunionExistante.getDate() + "T" + reunionExistante.getHeure());
                        int dureeReunionExistante = Integer.parseInt(reunionExistante.getDuree());
                        LocalDateTime finReunionExistante = debutReunionExistante.plusMinutes(dureeReunionExistante);

                        // Vérification des conflits
                        if (!(finReunion.isBefore(debutReunionExistante) || debutReunion.isAfter(finReunionExistante))) {
                            disponible = false;
                            break; // Sortie de la boucle dès qu'un conflit est détecté
                        }
                    } catch (Exception e) {
                        // Ignorer les erreurs de format pour cette réunion
                        continue;
                    }
                }
            }

            // Vérification des réservations existantes pour la salle
            List<ReservationSalle> reservations = salle.getReservations();
            if (reservations != null) {
                for (ReservationSalle res : reservations) {
                    if (res.getDate() == null || res.getHeure() == null) continue;

                    try {
                        LocalDateTime debutReservation = LocalDateTime.parse(res.getDate() + "T" + res.getHeure());
                        int dureeRes = res.getDuree() != null ? Integer.parseInt(res.getDuree()) : 60;
                        LocalDateTime finReservation = debutReservation.plusMinutes(dureeRes);

                        // Vérification des conflits
                        if (!(finReunion.isBefore(debutReservation) || debutReunion.isAfter(finReservation))) {
                            disponible = false;
                            break; // Sortie de la boucle dès qu'un conflit est détecté
                        }
                    } catch (Exception e) {
                        // Ignorer les erreurs de format pour cette réservation
                        continue;
                    }
                }
            }

            // Si la salle est encore disponible, l'ajouter à la liste des salles disponibles
            if (disponible) {
                sallesDisponibles.add(salle);
            }
        }

        return sallesDisponibles;
    }



    @Transactional
    public List<Salle> getSallesAvecReservationsDisponibles() {
        List<Salle> toutesLesSalles = salleRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        for (Salle salle : toutesLesSalles) {
            boolean estDisponible = true;

            if (salle.getReunions() != null && !salle.getReunions().isEmpty()) {
                List<Reunion> reunions = new ArrayList<>(salle.getReunions());

                for (Reunion reunion : reunions) {
                    LocalDateTime dateHeureReunion = LocalDateTime.parse(
                            reunion.getDate() + "T" + reunion.getHeure()
                    );

                    // Si au moins une réunion n'est pas encore passée, la salle reste occupée
                    if (dateHeureReunion.isAfter(now)) {
                        estDisponible = false;
                        break;
                    }
                }

                // Si toutes les réunions sont passées, la salle devient disponible
                if (estDisponible) {
                    salle.setDisponible(true);
                    salle.setReunions(new ArrayList<>());
                    salle.setReservations(new ArrayList<>());
                }
            } else {
                // Si pas de réunions, la salle est déjà disponible
                salle.setDisponible(true);
            }

            // Sauvegarder l'état mis à jour
            salleRepository.save(salle);
        }

        // Rafraîchir les entités pour garantir l'état le plus récent
        return salleRepository.findAll();
    }


    @Transactional
    public Salle updateSalle(Long salleId, Salle updatedSalle) {
        Salle salleExistante = salleRepository.findById(salleId)
                .orElseThrow(() -> new IllegalArgumentException("Salle non trouvée avec l'ID : " + salleId));

        // Mise à jour des propriétés principales
        salleExistante.setNom(updatedSalle.getNom());
        salleExistante.setCapacite(updatedSalle.getCapacite());
        salleExistante.setDisponible(updatedSalle.isDisponible());

        // Vérification de l'état des réunions uniquement si la salle est marquée comme disponible = false
        if (!updatedSalle.isDisponible() && salleExistante.getReunions() != null) {
            for (Reunion reunion : salleExistante.getReunions()) {
                reunion.setSalle(salleExistante);
                reunionRepository.save(reunion);
            }
        }

        // Enregistrement de la salle mise à jour
        return salleRepository.saveAndFlush(salleExistante);
    }


@Transactional
public void updateReunionSalle(Long reunionId, Long nouvelleSalleId) {
    // Récupérer la réunion
    Reunion reunion = reunionRepository.findById(reunionId)
            .orElseThrow(() -> new NoSuchElementException("Réunion non trouvée avec l'ID : " + reunionId));

    // Récupérer l'ancienne salle associée à la réunion (si elle existe)
    Salle ancienneSalle = reunion.getSalle();

    // Récupérer la nouvelle salle
    Salle nouvelleSalle = salleRepository.findById(nouvelleSalleId)
            .orElseThrow(() -> new NoSuchElementException("Salle non trouvée avec l'ID : " + nouvelleSalleId));

    // Vérifier si la salle est disponible
    if (!nouvelleSalle.isDisponible()) {
        throw new IllegalArgumentException("La salle sélectionnée n'est pas disponible.");
    }

    // Libérer l'ancienne salle si elle existe
    if (ancienneSalle != null && !ancienneSalle.getId().equals(nouvelleSalleId)) {
        ancienneSalle.setDisponible(true);
        salleRepository.save(ancienneSalle);

        // Supprimer l'ancienne réservation
        List<ReservationSalle> anciennesReservations = reservationSalleRepository.findByReunion(reunion);
        if (anciennesReservations != null && !anciennesReservations.isEmpty()) {
            for (ReservationSalle ancienneReservation : anciennesReservations) {
                reservationSalleRepository.delete(ancienneReservation);
            }
        }
    }

    // Associer la réunion à la nouvelle salle
    reunion.setSalle(nouvelleSalle);
    reunionRepository.save(reunion);

    // Créer une nouvelle réservation pour la salle
    ReservationSalle nouvelleReservation = new ReservationSalle();
    nouvelleReservation.setSalle(nouvelleSalle);
    nouvelleReservation.setReunion(reunion);
    reservationSalleRepository.save(nouvelleReservation);

    // Marquer la nouvelle salle comme utilisée
    nouvelleSalle.setDisponible(false);
    salleRepository.save(nouvelleSalle);
}



    public void deleteSalle(Long id) {
        // Optionnel : vérifier si la salle existe avant suppression
        if (!salleRepository.existsById(id)) {
            throw new RuntimeException("Salle non trouvée avec l'id : " + id);
        }
        salleRepository.deleteById(id);
    }


}
