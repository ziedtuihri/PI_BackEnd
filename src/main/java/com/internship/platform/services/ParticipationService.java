package com.internship.platform.services;

import com.internship.platform.entities.Evenement;
import com.internship.platform.entities.Participation;
import com.internship.platform.entities.StatutParticipation;
import com.internship.platform.repositories.EvenementRepository;
import com.internship.platform.repositories.ParticipationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ParticipationService {

    private final ParticipationRepository participationRepository;
    private final EvenementRepository evenementRepository;

    public ParticipationService(ParticipationRepository participationRepository,
                                EvenementRepository evenementRepository) {
        this.participationRepository = participationRepository;
        this.evenementRepository = evenementRepository;
    }

    public Participation applyToEvenement(Long evenementId, String studentEmail) {
        Evenement evenement = evenementRepository.findById(evenementId)
                .orElseThrow(() -> new RuntimeException("Evenement not found with ID: " + evenementId));

        // Prevent duplicate participation
        participationRepository.findByEvenementIdAndStudentEmail(evenementId, studentEmail)
                .ifPresent(existing -> {
                    throw new RuntimeException("Student has already applied to this event.");
                });

        Participation participation = new Participation();
        participation.setEvenement(evenement);
        participation.setStudentEmail(studentEmail);
        return participationRepository.save(participation);
    }

    public List<Participation> getParticipationsForEvenement(Long evenementId) {
        return participationRepository.findByEvenementId(evenementId);
    }

    public List<Participation> getParticipationsForEntreprise(Long entrepriseId) {
        return participationRepository.findByEvenementEntrepriseId(entrepriseId);
    }

    public void deleteParticipation(Long id) {
        participationRepository.deleteById(id);
    }

    public Participation updateStatut(Long participationId, StatutParticipation statut) {
        Participation participation = participationRepository.findById(participationId)
                .orElseThrow(() -> new RuntimeException("Participation not found with id: " + participationId));

        participation.setStatut(statut);
        return participationRepository.save(participation);
    }

    public long getParticipationCountByEvenement(Long evenementId) {
        return participationRepository.countByEvenementId(evenementId);
    }

    public long getAcceptedParticipationCountByEvenement(Long evenementId) {
        return participationRepository.countByEvenementIdAndStatut(evenementId, StatutParticipation.VALIDE);
    }
}
