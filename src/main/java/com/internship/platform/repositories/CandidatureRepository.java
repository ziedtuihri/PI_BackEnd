package com.internship.platform.repositories;

import com.internship.platform.entities.Candidature;
import com.internship.platform.entities.StatutCandidature;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CandidatureRepository extends JpaRepository<Candidature, Long> {
    List<Candidature> findByOffreId(Long offreId);
    List<Candidature> findByOffreEntrepriseId(Long entrepriseId);

    long countByOffreId(Long offreId);
    long countByOffreIdAndStatut(Long offreId, StatutCandidature statut);

    // Check if student has already applied to the same offer
    Optional<Candidature> findByOffreIdAndStudentEmail(Long offreId, String studentEmail);
}
