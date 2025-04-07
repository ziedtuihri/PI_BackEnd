package esprit.example.pi.services;

import esprit.example.pi.entities.Offer;
import esprit.example.pi.entities.Quiz;
import esprit.example.pi.repositories.OfferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OfferService {
    private final OfferRepository offerRepository;

    public List<Offer> getAllOffers() {
        return offerRepository.findAll();
    }

    public Optional<Offer> getOfferById(Long id) {
        return offerRepository.findById(id);
    }

    public Offer createOffer(Offer offer) {
        return offerRepository.save(offer);
    }

    public Offer updateOffer(Long id, Offer updatedOffer) {
        return offerRepository.findById(id).map(offer -> {
            offer.setTitle(updatedOffer.getTitle());
            offer.setDescription(updatedOffer.getDescription());
            offer.setCompany(updatedOffer.getCompany());
            offer.setType(updatedOffer.getType());
            offer.setStartDate(updatedOffer.getStartDate());
            offer.setEndDate(updatedOffer.getEndDate());
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
