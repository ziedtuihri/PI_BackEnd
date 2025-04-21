package tn.esprit.pi.repositories;

import tn.esprit.pi.entities.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface OfferRepository extends JpaRepository<Offer, Long> {
    List<Offer> findByType(String type);
}
