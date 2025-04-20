package esprit.example.pi.repositories;

import esprit.example.pi.entities.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface OfferRepository extends JpaRepository<Offer, Long> {
    List<Offer> findByType(String type);
}
