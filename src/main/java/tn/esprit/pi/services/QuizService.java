package tn.esprit.pi.services;

import org.springframework.beans.factory.annotation.Autowired;
import tn.esprit.pi.entities.Offer;
import tn.esprit.pi.entities.Question;
import tn.esprit.pi.entities.Quiz;
import tn.esprit.pi.repositories.OfferRepository;
import tn.esprit.pi.repositories.QuizRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pi.restcontrollers.RegistrationQuiz;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class QuizService {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private OfferRepository  offerRepository;


    public Quiz createQuiz(RegistrationQuiz request) {
        // Save the quiz first
        Quiz quiz = new Quiz(request);
        Quiz savedQuiz = quizRepository.save(quiz);

        // Link the quiz to the corresponding offer

        Long offerId = request.getOffreId();

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
