package tn.esprit.pi.anwer.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi.anwer.entities.Offer;
import tn.esprit.pi.anwer.entities.Quiz;
import tn.esprit.pi.anwer.services.OfferService;

import java.util.List;

@RestController
@RequestMapping("/api/offers")
@RequiredArgsConstructor
public class OfferController {
    private final OfferService offerService;

    @GetMapping
    public ResponseEntity<List<Offer>> getAllOffers() {
        List<Offer> offers = offerService.getAllOffers();
        return ResponseEntity.ok(offers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Offer> getOfferById(@PathVariable Long id) {
        return offerService.getOfferById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('HR_COMPANY')")
    public ResponseEntity<Offer> createOffer(@RequestBody Offer offer, HttpServletRequest request) {
        Offer savedOffer = offerService.createOffer(offer, request);
        return ResponseEntity.ok(savedOffer);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('HR_COMPANY')")
    public ResponseEntity<Offer> updateOffer(@PathVariable Long id, @RequestBody Offer updatedOffer) {
        Offer savedOffer = offerService.updateOffer(id, updatedOffer);
        return ResponseEntity.ok(savedOffer);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('HR_COMPANY')")
    public ResponseEntity<Void> deleteOffer(@PathVariable Long id) {
        offerService.deleteOffer(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{offerId}/quiz")
    @PreAuthorize("hasAuthority('HR_COMPANY')")
    public ResponseEntity<Offer> attachQuizToOffer(@PathVariable Long offerId, @RequestBody Quiz quiz) {
        Offer offerWithQuiz = offerService.attachQuizToOffer(offerId, quiz);
        return ResponseEntity.ok(offerWithQuiz);
    }
}