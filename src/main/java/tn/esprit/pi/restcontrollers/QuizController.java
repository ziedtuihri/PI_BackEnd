package tn.esprit.pi.restcontrollers;

import jakarta.mail.MessagingException;
import tn.esprit.pi.entities.Quiz;
import tn.esprit.pi.services.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Optional;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {
    private final QuizService quizService;

    @PostMapping
    public ResponseEntity<Quiz> createQuiz(@RequestBody RegistrationQuiz request) throws MessagingException {
        return ResponseEntity.ok(quizService.createQuiz(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Quiz> getQuizById(@PathVariable Long id) {
        return quizService.getQuizById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-offer/{offerId}")
    public ResponseEntity<?> getQuizByOfferId(@PathVariable Long offerId) {
        Optional<Quiz> quizOptional = quizService.getQuizByOfferId(offerId);
        if (quizOptional.isPresent()) {
            return ResponseEntity.ok(quizOptional.get());
        } else {
            // Instead of 404, return an empty object with status 200
            return ResponseEntity.ok(new HashMap<>());
            // Alternatively, you could return null with status 200:
            // return ResponseEntity.ok(null);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Quiz> updateQuiz(@PathVariable Long id, @RequestBody Quiz updatedQuiz) {
        Quiz savedQuiz = quizService.updateQuiz(id, updatedQuiz);
        return ResponseEntity.ok(savedQuiz);
    }

}
