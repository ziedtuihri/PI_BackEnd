package tn.esprit.pi.services;

import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import tn.esprit.pi.auth.RegistrationRequest;
import tn.esprit.pi.entities.Offer;
import tn.esprit.pi.entities.Quiz;
import tn.esprit.pi.repositories.OfferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pi.restcontrollers.RegistrationOffer;
import tn.esprit.pi.user.User;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OfferService {

    @Autowired
    private OfferRepository offerRepository;

    public List<Offer> getAllOffers() {
        return offerRepository.findAll();
    }

    public Optional<Offer> getOfferById(Long id) {
        return offerRepository.findById(id);
    }

    public Offer createOffer(RegistrationOffer request) throws MessagingException {
        Offer offer = new Offer(request);
        return offerRepository.save(offer);
    }

    public Offer updateOffer(Long id, RegistrationOffer request) {
        return offerRepository.findById(id).map(offer -> {
            offer.setTitle(request.getTitle());
            offer.setDescription(request.getDescription());
            offer.setCompany(request.getCompany());
            offer.setType(request.getType());
            offer.setStartDate(request.getStartDate());
            offer.setEndDate(request.getEndDate());
            // etc.
            return offerRepository.save(offer);
        }).orElseThrow(() -> new RuntimeException("Offer not found"));
    }

    public void deleteOffer(Long id) {
        offerRepository.deleteById(id);
    }

    // lier un quiz à une offre
    public Offer attachQuizToOffer(Long offerId, Quiz quiz) {
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Offer not found"));

        offer.setQuiz(quiz);
        return offerRepository.save(offer);
    }

}
