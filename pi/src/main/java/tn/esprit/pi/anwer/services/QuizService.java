package tn.esprit.pi.anwer.services;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pi.anwer.entities.Offer;
import tn.esprit.pi.anwer.entities.Quiz;
import tn.esprit.pi.anwer.repositories.OfferRepository;
import tn.esprit.pi.anwer.repositories.QuizRepository;
import tn.esprit.pi.security.JwtService;
import tn.esprit.pi.user.User;
import tn.esprit.pi.user.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QuizService {
    private final QuizRepository quizRepository;
    private final OfferRepository offerRepository;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public Quiz createQuiz(Quiz quiz, Long offerId, HttpServletRequest request) {
        String token = extractToken(request);

        @SuppressWarnings("unchecked")
        List<String> authorities = (List<String>) jwtService.extractClaim(token, claims -> claims.get("authorities"));

        if (authorities == null || !authorities.contains("HR_COMPANY")) {
            throw new SecurityException("Only HR_COMPANY can create quizzes.");
        }

        String email = jwtService.extractUsername(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found for email: " + email));

        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Offer not found"));

        if (!offer.getCompanyId().equals(user.getId().longValue())) {
            throw new SecurityException("You are not allowed to attach a quiz to this offer.");
        }

        quiz = quizRepository.save(quiz);
        offer.setQuiz(quiz);
        offerRepository.save(offer);

        return quiz;
    }

    public Optional<Quiz> getQuizById(Long id) {
        return quizRepository.findById(id);
    }

    public Optional<Quiz> getQuizByOfferId(Long offerId) {
        return offerRepository.findById(offerId).map(Offer::getQuiz);
    }

    public Quiz updateQuiz(Long id, Quiz updated) {
        return quizRepository.findById(id).map(existing -> {
            existing.setTitle(updated.getTitle());
            existing.setDescription(updated.getDescription());
            existing.setQuestions(updated.getQuestions()); // includes answers
            return quizRepository.save(existing);
        }).orElseThrow(() -> new RuntimeException("Quiz not found"));
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or malformed Authorization header");
        }
        return header.substring(7);
    }
}
