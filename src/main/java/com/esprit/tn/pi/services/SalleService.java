package com.esprit.tn.pi.services;

import com.esprit.tn.pi.entities.ReservationSalle;
import com.esprit.tn.pi.entities.Reunion;
import com.esprit.tn.pi.entities.Salle;
import com.esprit.tn.pi.repositories.ReunionRepository;
import com.esprit.tn.pi.repositories.SalleRepository;
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
        LocalDateTime debutReunion = LocalDateTime.parse(date + "T" + heure);
        int dureeMinutes = Integer.parseInt(dureeStr);
        LocalDateTime finReunion = debutReunion.plusMinutes(dureeMinutes);

        for (Salle salle : toutesLesSalles) {
            boolean disponible = true;

            // Vérification si la salle est déjà associée à une réunion
            if (salle.getReunion() != null) {
                Reunion reunionExistante = salle.getReunion();
                LocalDateTime debutReunionExistante = LocalDateTime.parse(reunionExistante.getDate() + "T" + reunionExistante.getHeure());
                int dureeReunionExistante = Integer.parseInt(reunionExistante.getDuree());
                LocalDateTime finReunionExistante = debutReunionExistante.plusMinutes(dureeReunionExistante);

                // Vérification si les créneaux horaires se chevauchent
                if (!(finReunion.isBefore(debutReunionExistante) || debutReunion.isAfter(finReunionExistante))) {
                    // Si les horaires se chevauchent, la salle n'est pas disponible
                    disponible = false;
                    break; // Sortie de la boucle dès que la salle est indisponible
                }
            }

            // Vérification des réservations existantes pour la salle
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

                    // Durée de la réservation, par défaut 1 heure si null
                    int dureeRes = res.getDuree() != null ? Integer.parseInt(res.getDuree()) : 60;
                    LocalDateTime finReservation = debutReservation.plusMinutes(dureeRes);

                    // Vérification du chevauchement des périodes de réservation
                    if (!(finReunion.isBefore(debutReservation) || debutReunion.isAfter(finReservation))) {
                        // Si la réservation chevauche, rendre la salle indisponible
                        disponible = false;
                        break; // Sortie de la boucle dès qu'on trouve un chevauchement
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


    @Transactional
    public Salle updateSalle(Long salleId, Salle updatedSalle) {
        // Récupérer la salle existante depuis la base de données
        Salle salleExistante = salleRepository.findById(salleId)
                .orElseThrow(() -> new IllegalArgumentException("Salle non trouvée avec l'ID : " + salleId));

        // Mettre à jour les propriétés de la salle (nom, capacité, état, etc.)
        salleExistante.setNom(updatedSalle.getNom());
        salleExistante.setCapacite(updatedSalle.getCapacite());
        salleExistante.setEtat(updatedSalle.getEtat());

        // Vérifier si une réunion est passée dans la requête
        if (updatedSalle.getReunion() != null) {
            Reunion reunionExistante = reunionRepository.findById(updatedSalle.getReunion().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Réunion non trouvée avec l'ID : " + updatedSalle.getReunion().getId()));

            // Si la salle existante a déjà une réunion, il faut libérer l'ancienne salle
            if (salleExistante.getReunion() != null) {
                // Libérer l'ancienne salle associée à la réunion
                Salle ancienneSalle = salleExistante.getReunion().getSalle();
                if (ancienneSalle != null) {
                    ancienneSalle.setDisponible(true); // Libérer l'ancienne salle
                    ancienneSalle.setReunion(null); // Retirer la réunion de l'ancienne salle
                    salleRepository.save(ancienneSalle); // Sauvegarder l'ancienne salle mise à jour
                }
            }

            // Associer la réunion à la salle mise à jour
            salleExistante.setReunion(reunionExistante);
            reunionExistante.setSalle(salleExistante); // Lier la réunion à la salle

            // Marquer la nouvelle salle comme non disponible
            salleExistante.setDisponible(false);

            // Sauvegarder la réunion et la salle mises à jour
            reunionRepository.save(reunionExistante);
        } else {
            // Si aucune réunion n'est passée, retirer la réunion de la salle
            salleExistante.setReunion(null);
            salleExistante.setDisponible(true); // Marquer la salle comme disponible
        }

        // Sauvegarder la salle mise à jour
        return salleRepository.saveAndFlush(salleExistante);
    }


}
