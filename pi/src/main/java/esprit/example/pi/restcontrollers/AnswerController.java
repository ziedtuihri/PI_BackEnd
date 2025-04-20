package esprit.example.pi.restcontrollers;

import esprit.example.pi.entities.Answer;
import esprit.example.pi.services.AnswerService;
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
    public ResponseEntity<Answer> addAnswer(@RequestBody Answer answer) {
        return ResponseEntity.ok(anwserService.saveAnswer(answer));
    }

    @GetMapping("/by-question/{questionId}")
    public List<Answer> getAnswersByQuestion(@PathVariable Long questionId) {
        return anwserService.getAnswersByQuestionId(questionId);
    }
}
