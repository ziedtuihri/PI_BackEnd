package tn.esprit.pi.anwer.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi.anwer.entities.Quiz;
import tn.esprit.pi.anwer.services.QuizService;

import java.util.HashMap;
import java.util.Optional;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {
    private final QuizService quizService;

    @PostMapping("/{offerId}")
    public ResponseEntity<Quiz> create(@PathVariable Long offerId,
                                       @RequestBody Quiz quiz,
                                       HttpServletRequest request) {
        return ResponseEntity.ok(quizService.createQuiz(quiz, offerId, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Quiz> getById(@PathVariable Long id) {
        return quizService.getQuizById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-offer/{offerId}")
    public ResponseEntity<Quiz> getByOffer(@PathVariable Long offerId) {
        return quizService.getQuizByOfferId(offerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Quiz> update(@PathVariable Long id, @RequestBody Quiz quiz) {
        return ResponseEntity.ok(quizService.updateQuiz(id, quiz));
    }
}
