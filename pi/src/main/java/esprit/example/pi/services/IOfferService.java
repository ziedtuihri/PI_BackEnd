package esprit.example.pi.services;

import esprit.example.pi.entities.Offer;
import esprit.example.pi.entities.Quiz;

import java.util.List;
import java.util.Optional;

public interface IOfferService {
    List<Offer> getAllOffers();

    Optional<Offer> getOfferById(Long id);
}
