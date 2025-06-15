package tn.esprit.pi.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pi.entities.Answer;
import tn.esprit.pi.entities.Question;
import tn.esprit.pi.repositories.AnswerRepository;
import tn.esprit.pi.repositories.QuestionRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnswerService {
    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;

    public List<Answer> getAnswersByQuestionId(Long questionId) {
        return answerRepository.findByQuestionId(questionId);
    }

    public Answer saveAnswer(Long questionId, Answer answer) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        answer.setQuestion(question);
        return answerRepository.save(answer);
    }
}

