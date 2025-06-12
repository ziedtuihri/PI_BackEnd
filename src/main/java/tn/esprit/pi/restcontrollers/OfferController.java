package tn.esprit.pi.restcontrollers;

import jakarta.mail.MessagingException;
import tn.esprit.pi.entities.Offer;
import tn.esprit.pi.entities.Quiz;
import tn.esprit.pi.services.OfferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/offers")
@RequiredArgsConstructor
public class OfferController {
    private final OfferService offerService;


    @GetMapping
    public List<Offer> getAllOffers() {
        return offerService.getAllOffers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Offer> getOfferById(@PathVariable Long id) {
        return offerService.getOfferById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Offer> createOffer(@RequestBody RegistrationOffer request) throws MessagingException {
        return ResponseEntity.ok(offerService.createOffer(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Offer> updateOffer(@PathVariable Long id, @RequestBody RegistrationOffer request) throws MessagingException {
        return ResponseEntity.ok(offerService.updateOffer(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOffer(@PathVariable Long id) {
        offerService.deleteOffer(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{offerId}/quiz")
    public ResponseEntity<Offer> attachQuizToOffer(
            @PathVariable Long offerId,
            @RequestBody Quiz quiz
    ) {
        return ResponseEntity.ok(offerService.attachQuizToOffer(offerId, quiz));
    }
}
