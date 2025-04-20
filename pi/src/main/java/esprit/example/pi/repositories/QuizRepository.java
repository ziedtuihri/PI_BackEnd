package esprit.example.pi.repositories;

import esprit.example.pi.entities.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    // This will find a quiz where it is referenced by an offer with the given ID
    @Query("SELECT q FROM Quiz q JOIN Offer o ON o.quiz.id = q.id WHERE o.id = :offerId")
    Optional<Quiz> findByOfferId(@Param("offerId") Long offerId);
}
