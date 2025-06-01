package com.internship.platform.Controllers;

import com.internship.platform.entities.Candidature;
import com.internship.platform.entities.StatutCandidature;
import com.internship.platform.services.CandidatureService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/candidatures")
@CrossOrigin(origins = "http://localhost:4200")
public class CandidatureController {

    private final CandidatureService candidatureService;

    public CandidatureController(CandidatureService candidatureService) {
        this.candidatureService = candidatureService;
    }

    @PostMapping("/apply")
    public Candidature applyToOffre(@RequestParam Long offreId, @RequestParam String studentEmail) {
        return candidatureService.applyToOffre(offreId, studentEmail);
    }

    @GetMapping("/entreprise/{entrepriseId}")
    public List<Candidature> getByEntreprise(@PathVariable Long entrepriseId) {
        return candidatureService.getCandidaturesForEntreprise(entrepriseId);
    }

    @GetMapping("/offre/{offreId}")
    public List<Candidature> getByOffre(@PathVariable Long offreId) {
        return candidatureService.getCandidaturesForOffre(offreId);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        candidatureService.deleteCandidature(id);
    }

    @PatchMapping("/{candidatureId}/status")
    public ResponseEntity<Candidature> updateCandidatureStatus(
            @PathVariable Long candidatureId,
            @RequestParam StatutCandidature statut) {
        Candidature updated = candidatureService.updateStatut(candidatureId, statut);
        return ResponseEntity.ok(updated);
    }

    // 🔢 Count total candidatures for an offer
    @GetMapping("/offre/{offreId}/count")
    public long countTotal(@PathVariable Long offreId) {
        return candidatureService.getCandidatureCountByOffre(offreId);
    }

    // ✅ Count only accepted candidatures for an offer
    @GetMapping("/offre/{offreId}/count/accepted")
    public long countAccepted(@PathVariable Long offreId) {
        return candidatureService.getAcceptedCandidatureCountByOffre(offreId);
    }
}
