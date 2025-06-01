package tn.esprit.pi.repositories;

import tn.esprit.pi.entities.Reunion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReunionRepository  extends JpaRepository<Reunion, Long> {
    List<Reunion> findBySalleId(Long salleId);

}
