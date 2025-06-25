package tn.esprit.pi.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi.entities.Question;
import tn.esprit.pi.services.QuestionService;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {
    private final QuestionService questionService;

    @GetMapping("/by-quiz/{quizId}")
    public List<Question> getByQuiz(@PathVariable Long quizId) {
        return questionService.getQuestionsByQuizId(quizId);
    }

    @PostMapping("/quiz/{quizId}")
    @PreAuthorize("hasAuthority('HR_COMPANY')")
    public ResponseEntity<Question> add(@PathVariable Long quizId, @RequestBody Question question) {
        return ResponseEntity.ok(questionService.addQuestion(quizId, question));
    }
}
