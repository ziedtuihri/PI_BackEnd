package com.internship.platform.repositories;

import com.internship.platform.entities.Participation;
import com.internship.platform.entities.StatutParticipation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParticipationRepository extends JpaRepository<Participation, Long> {
    List<Participation> findByEvenementId(Long evenementId);
    List<Participation> findByEvenementEntrepriseId(Long entrepriseId);

    long countByEvenementId(Long evenementId);
    long countByEvenementIdAndStatut(Long evenementId, StatutParticipation statut);

    Optional<Participation> findByEvenementIdAndStudentEmail(Long evenementId, String studentEmail);
}
