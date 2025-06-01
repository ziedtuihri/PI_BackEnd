package com.internship.platform.Controllers;

import com.internship.platform.entities.Participation;
import com.internship.platform.entities.StatutParticipation;
import com.internship.platform.services.ParticipationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/participations")
@CrossOrigin(origins = "http://localhost:4200")
public class ParticipationController {

    private final ParticipationService participationService;

    public ParticipationController(ParticipationService participationService) {
        this.participationService = participationService;
    }

    @PostMapping("/apply")
    public Participation applyToEvent(@RequestParam Long evenementId, @RequestParam String studentEmail) {
        return participationService.applyToEvenement(evenementId, studentEmail);
    }

    @GetMapping("/entreprise/{entrepriseId}")
    public List<Participation> getByEntreprise(@PathVariable Long entrepriseId) {
        return participationService.getParticipationsForEntreprise(entrepriseId);
    }

    @GetMapping("/evenement/{evenementId}")
    public List<Participation> getByEvenement(@PathVariable Long evenementId) {
        return participationService.getParticipationsForEvenement(evenementId);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        participationService.deleteParticipation(id);
    }

    @PatchMapping("/{participationId}/status")
    public ResponseEntity<Participation> updateParticipationStatus(
            @PathVariable Long participationId,
            @RequestParam StatutParticipation statut) {
        Participation updated = participationService.updateStatut(participationId, statut);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/evenement/{evenementId}/count")
    public long countTotal(@PathVariable Long evenementId) {
        return participationService.getParticipationCountByEvenement(evenementId);
    }

    @GetMapping("/evenement/{evenementId}/count/accepted")
    public long countAccepted(@PathVariable Long evenementId) {
        return participationService.getAcceptedParticipationCountByEvenement(evenementId);
    }
}
