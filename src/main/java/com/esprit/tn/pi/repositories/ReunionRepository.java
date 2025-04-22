package com.esprit.tn.pi.repositories;

import com.esprit.tn.pi.entities.Reunion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReunionRepository  extends JpaRepository<Reunion, Long> {
    public List<Reunion> findBySalleIdAndDate(Long salleId, String date);

    List<Reunion> findBySalleId(Long salleId);


}
