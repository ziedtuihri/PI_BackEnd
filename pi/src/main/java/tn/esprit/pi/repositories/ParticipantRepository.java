package com.esprit.tn.pi.repositories;

import com.esprit.tn.pi.entities.Participant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
}
