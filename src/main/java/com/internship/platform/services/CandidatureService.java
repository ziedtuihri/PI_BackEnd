package com.internship.platform.services;

import com.internship.platform.entities.Candidature;
import com.internship.platform.entities.Offre;
import com.internship.platform.entities.StatutCandidature;
import com.internship.platform.repositories.CandidatureRepository;
import com.internship.platform.repositories.OffreRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CandidatureService {

    private final CandidatureRepository candidatureRepository;
    private final OffreRepository offreRepository;

    public CandidatureService(CandidatureRepository candidatureRepository, OffreRepository offreRepository) {
        this.candidatureRepository = candidatureRepository;
        this.offreRepository = offreRepository;
    }

    public Candidature applyToOffre(Long offreId, String studentEmail) {
        Offre offre = offreRepository.findById(offreId)
                .orElseThrow(() -> new RuntimeException("Offre not found with ID: " + offreId));

        // ❗Prevent duplicate application
        candidatureRepository.findByOffreIdAndStudentEmail(offreId, studentEmail)
                .ifPresent(existing -> {
                    throw new RuntimeException("Student has already applied to this offer.");
                });

        Candidature candidature = new Candidature();
        candidature.setOffre(offre);
        candidature.setStudentEmail(studentEmail);
        return candidatureRepository.save(candidature);
    }

    public List<Candidature> getCandidaturesForOffre(Long offreId) {
        return candidatureRepository.findByOffreId(offreId);
    }

    public List<Candidature> getCandidaturesForEntreprise(Long entrepriseId) {
        return candidatureRepository.findByOffreEntrepriseId(entrepriseId);
    }

    public void deleteCandidature(Long id) {
        candidatureRepository.deleteById(id);
    }

    public Candidature updateStatut(Long candidatureId, StatutCandidature statut) {
        Candidature candidature = candidatureRepository.findById(candidatureId)
                .orElseThrow(() -> new RuntimeException("Candidature not found with id: " + candidatureId));

        candidature.setStatut(statut);
        return candidatureRepository.save(candidature);
    }

    public long getCandidatureCountByOffre(Long offreId) {
        return candidatureRepository.countByOffreId(offreId);
    }

    public long getAcceptedCandidatureCountByOffre(Long offreId) {
        return candidatureRepository.countByOffreIdAndStatut(offreId, StatutCandidature.VALIDE);
    }
}
