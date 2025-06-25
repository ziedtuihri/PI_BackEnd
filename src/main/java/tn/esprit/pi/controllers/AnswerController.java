package tn.esprit.pi.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi.entities.Answer;
import tn.esprit.pi.services.AnswerService;

import java.util.List;

@RestController
@RequestMapping("/api/answers")
@RequiredArgsConstructor
public class AnswerController {
    private final AnswerService answerService;

    @GetMapping("/by-question/{questionId}")
    public List<Answer> getByQuestion(@PathVariable Long questionId) {
        return answerService.getAnswersByQuestionId(questionId);
    }

    @PostMapping("/question/{questionId}")
    @PreAuthorize("hasAuthority('HR_COMPANY')")
    public ResponseEntity<Answer> add(@PathVariable Long questionId, @RequestBody Answer answer) {
        return ResponseEntity.ok(answerService.saveAnswer(questionId, answer));
    }
}

