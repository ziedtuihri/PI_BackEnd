package esprit.example.pi.services;

import esprit.example.pi.entities.Question;
import esprit.example.pi.repositories.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionService {
    private final QuestionRepository questionRepository;

    public List<Question> getQuestionsByQuizId(Long quizId) {
        return questionRepository.findByQuizId(quizId);
    }

    public Question addQuestion(Question question) {
        return questionRepository.save(question);
    }
}
