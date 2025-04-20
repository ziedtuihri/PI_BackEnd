package esprit.example.pi.services;

import esprit.example.pi.entities.Offer;
import esprit.example.pi.entities.Question;
import esprit.example.pi.entities.Quiz;
import esprit.example.pi.repositories.OfferRepository;
import esprit.example.pi.repositories.QuizRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class QuizService {
    private final QuizRepository quizRepository;
    private final OfferRepository  offerRepository;
    public Quiz createQuiz(Quiz quiz) {
        // Save the quiz first
        Quiz savedQuiz = quizRepository.save(quiz);

        // Link the quiz to the corresponding offer
        Long offerId = quiz.getOffer().getId();
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new IllegalArgumentException("Offer not found with id: " + offerId));

        offer.setQuiz(savedQuiz);  // Link the quiz
        offerRepository.save(offer);  // Persist the change

        return savedQuiz;
    }

    public Optional<Quiz> getQuizById(Long id) {
        return quizRepository.findById(id);
    }

    public Optional<Quiz> getQuizByOfferId(Long offerId) {
        return quizRepository.findByOfferId(offerId);
    }

    public Quiz updateQuiz(Long id, Quiz updatedQuiz) {
        Quiz existingQuiz = quizRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Quiz not found with id " + id));

        existingQuiz.setTitle(updatedQuiz.getTitle());

        // Clear and re-add questions and answers
        existingQuiz.getQuestions().clear();
        if (updatedQuiz.getQuestions() != null) {
            for (Question q : updatedQuiz.getQuestions()) {
                q.setQuiz(existingQuiz);
            }
            existingQuiz.getQuestions().addAll(updatedQuiz.getQuestions());
        }

        return quizRepository.save(existingQuiz);
    }

}
