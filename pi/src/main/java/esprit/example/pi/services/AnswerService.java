package esprit.example.pi.services;

import esprit.example.pi.entities.Answer;
import esprit.example.pi.repositories.AnswerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnswerService {
    private AnswerRepository answerRepository;

    public List<Answer> getAnswersByQuestionId(Long questionId) {
        return answerRepository.findByQuestionId(questionId);
    }

    public Answer saveAnswer(Answer anwser) {
        return answerRepository.save(anwser);
    }
}
