package com.esprit.tn.pi.services;

import com.esprit.tn.pi.entities.ReservationSalle;
import com.esprit.tn.pi.entities.Reunion;
import com.esprit.tn.pi.entities.Salle;
import com.esprit.tn.pi.repositories.ReservationSalleRepository;
import com.esprit.tn.pi.repositories.SalleRepository;
import com.esprit.tn.pi.repositories.ReunionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationSalleRepository reservationSalleRepository;
    private final SalleRepository salleRepository;
    private final ReunionRepository reunionRepository;

    public List<ReservationSalle> getAllReservations() {
        return reservationSalleRepository.findAll();
    }

    public ReservationSalle getReservationById(Long id) {
        return reservationSalleRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Réservation non trouvée avec l'ID : " + id));
    }

    public ReservationSalle saveReservation(ReservationSalle reservation) {
        return reservationSalleRepository.save(reservation);
    }

    public Salle getSalleById(Long salleId) {
        return salleRepository.findById(salleId)
                .orElseThrow(() -> new NoSuchElementException("Salle non trouvée avec l'ID : " + salleId));
    }

    public Reunion getReunionById(Long reunionId) {
        return reunionRepository.findById(reunionId)
                .orElseThrow(() -> new NoSuchElementException("Réunion non trouvée avec l'ID : " + reunionId));
    }

    public boolean verifierDisponibiliteSalle(Salle salle, String date, String heure, String duree, Long reunionId) {
        // Logique pour vérifier la disponibilité de la salle (à implémenter selon les règles métier)
        return true; // Placeholder pour démonstration
    }
}
