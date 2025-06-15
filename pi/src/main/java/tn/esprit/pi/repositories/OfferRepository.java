package tn.esprit.pi.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi.entities.Offer;

public interface OfferRepository extends JpaRepository<Offer, Long> {
}
