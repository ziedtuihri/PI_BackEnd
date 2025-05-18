package tn.esprit.pi.repositories;

import tn.esprit.pi.entities.Salle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SalleRepository extends JpaRepository<Salle, Long> {
    List<Salle> findByReservationsIsNotNull();}
