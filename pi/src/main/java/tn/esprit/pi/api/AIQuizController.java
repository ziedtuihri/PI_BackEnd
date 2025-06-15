package tn.esprit.pi.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai-quiz")
@RequiredArgsConstructor
public class AIQuizController {
    private final tn.esprit.pi.api.CohereQuizService cohereQuizService;

    @PostMapping("/generate")
    @PreAuthorize("hasAuthority('HR_COMPANY')")
    public ResponseEntity<List<tn.esprit.pi.api.AIQuizDTO>> generateQuiz(@RequestBody Map<String, Object> payload) {
        String description = (String) payload.get("description");
        String prompt = (String) payload.get("prompt");
        int count = (int) payload.getOrDefault("count", 3);

        return ResponseEntity.ok(cohereQuizService.generateQuestions(description, prompt, count));
    }
}
