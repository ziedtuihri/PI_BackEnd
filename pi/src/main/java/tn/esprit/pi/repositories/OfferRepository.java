package tn.esprit.pi.anwer.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi.anwer.entities.Offer;

public interface OfferRepository extends JpaRepository<Offer, Long> {
}
