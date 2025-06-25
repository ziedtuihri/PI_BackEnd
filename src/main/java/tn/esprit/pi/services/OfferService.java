package tn.esprit.pi.services;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pi.entities.Offer;
import tn.esprit.pi.entities.Quiz;
import tn.esprit.pi.repositories.OfferRepository;
import tn.esprit.pi.security.JwtService;
import tn.esprit.pi.user.User;
import tn.esprit.pi.user.UserRepository;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class OfferService {

    private final OfferRepository offerRepository;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public List<Offer> getAllOffers() {
        return offerRepository.findAll();
    }

    public Optional<Offer> getOfferById(Long id) {
        return offerRepository.findById(id);
    }

    public Offer createOffer(Offer offer, HttpServletRequest request) {
        String token = extractToken(request);
        System.out.println("[OfferService] Token: " + token);

        String email = jwtService.extractUsername(token);
        System.out.println("[OfferService] Email from JWT: " + email);

        // Verify user exists
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        System.out.println("[OfferService] User ID: " + user.getId());

        offer.setCompanyId(user.getId().longValue());
        return offerRepository.save(offer);
    }

    public Offer updateOffer(Long id, Offer updatedOffer) {
        return offerRepository.findById(id)
                .map(offer -> {
                    offer.setTitle(updatedOffer.getTitle());
                    offer.setDescription(updatedOffer.getDescription());
                    offer.setLocation(updatedOffer.getLocation());
                    offer.setCompany(updatedOffer.getCompany());
                    offer.setType(updatedOffer.getType());
                    offer.setStartDate(updatedOffer.getStartDate());
                    offer.setEndDate(updatedOffer.getEndDate());
                    return offerRepository.save(offer);
                })
                .orElseThrow(() -> new RuntimeException("Offer not found"));
    }

    public void deleteOffer(Long id) {
        offerRepository.deleteById(id);
    }

    public Offer attachQuizToOffer(Long offerId, Quiz quiz) {
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Offer not found"));
        offer.setQuiz(quiz);
        return offerRepository.save(offer);
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or malformed Authorization header");
        }
        return authHeader.substring(7);
    }
}
