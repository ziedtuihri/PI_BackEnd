package tn.esprit.pi.services;

import tn.esprit.pi.entities.Offer;

import java.util.List;
import java.util.Optional;

public interface IOfferService {
    List<Offer> getAllOffers();

    Optional<Offer> getOfferById(Long id);
}
