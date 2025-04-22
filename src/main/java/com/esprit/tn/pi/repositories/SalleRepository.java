package com.esprit.tn.pi.repositories;

import com.esprit.tn.pi.entities.Salle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SalleRepository extends JpaRepository<Salle, Long> {
    Optional<Salle> findFirstByDisponibleTrueAndCapaciteGreaterThanEqual(int capacite);
    // Trouver les salles ayant des réservations non nulles
    List<Salle> findByReservationsIsNotNull();

    // Ou, si vous devez vérifier que la salle a au moins une réservation
    List<Salle> findByReservationsIsNotEmpty();
}
