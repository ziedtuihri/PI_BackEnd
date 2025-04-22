package com.esprit.tn.pi.repositories;

import com.esprit.tn.pi.entities.ReservationSalle;
import com.esprit.tn.pi.entities.Reunion;
import com.esprit.tn.pi.entities.Salle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationSalleRepository extends JpaRepository<ReservationSalle, Long> {
    Optional<ReservationSalle> findByReunionId(Long reunionId);
    List<ReservationSalle> findByReunion(Reunion reunion); // Recherche des réservations associées à une réunion
    List<ReservationSalle> findBySalle(Salle salle);

    List<ReservationSalle> findBySalleId(Long salleId);

}
