package com.esprit.tn.pi.services;

import com.esprit.tn.pi.entities.ReservationSalle;
import com.esprit.tn.pi.entities.Salle;
import com.esprit.tn.pi.repositories.SalleRepository;
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

        LocalDateTime debutReunion = LocalDateTime.parse(date + "T" + heure);
        int dureeMinutes = Integer.parseInt(dureeStr);
        LocalDateTime finReunion = debutReunion.plusMinutes(dureeMinutes);

        for (Salle salle : toutesLesSalles) {
            boolean disponible = true;

            List<ReservationSalle> reservations = salle.getReservations();
            if (reservations != null) {
                for (ReservationSalle res : reservations) {
                    if (res.getDate() == null || res.getHeure() == null) continue;

                    LocalDateTime debutReservation;
                    try {
                        debutReservation = LocalDateTime.parse(res.getDate() + "T" + res.getHeure());
                    } catch (Exception e) {
                        continue;
                    }

                    int dureeRes = res.getDuree() != null ? Integer.parseInt(res.getDuree()) : 60; // Par défaut 1h si null
                    LocalDateTime finReservation = debutReservation.plusMinutes(dureeRes);

                    // Vérifie si les périodes se chevauchent
                    if (!(finReunion.isBefore(debutReservation) || debutReunion.isAfter(finReservation))) {
                        disponible = false;
                        break;
                    }
                }
            }

            if (disponible) {
                sallesDisponibles.add(salle);
            }
        }

        return sallesDisponibles;
    }


    public List<Salle> getSallesAvecReservationsDisponibles() {
        List<Salle> toutesLesSalles = salleRepository.findAll();
        LocalDate currentDate = LocalDate.now();

        for (Salle salle : toutesLesSalles) {
            boolean estDisponible = true;
            if (salle.getReservations() != null && !salle.getReservations().isEmpty()) {
                for (ReservationSalle reservation : salle.getReservations()) {
                    if (reservation.getReunion() != null) {
                        LocalDate dateReunion = LocalDate.parse(reservation.getReunion().getDate());

                        if (dateReunion.isBefore(currentDate)) {
                            estDisponible = true;
                        } else {
                            estDisponible = false;
                            break;
                        }
                    }
                }
            }
            salle.setDisponible(estDisponible);
        }
        return toutesLesSalles;
    }

}
