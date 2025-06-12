package tn.esprit.pi.restcontrollers;

import tn.esprit.pi.entities.Answer;
import tn.esprit.pi.services.AnswerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/answers")
@RequiredArgsConstructor
public class AnswerController {
    @Autowired
    private AnswerService anwserService;

    @PostMapping
    public ResponseEntity<Answer> addAnswer(@RequestBody RegistrationAnswer request) {
        return ResponseEntity.ok(anwserService.saveAnswer(request));
    }

    @GetMapping("/by-question/{questionId}")
    public List<Answer> getAnswersByQuestion(@PathVariable Long questionId) {
        return anwserService.getAnswersByQuestionId(questionId);
    }
}
