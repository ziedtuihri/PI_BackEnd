package tn.esprit.pi.services;

import tn.esprit.pi.entities.Answer;
import tn.esprit.pi.entities.Question;
import tn.esprit.pi.entities.Quiz;
import tn.esprit.pi.repositories.AnswerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.pi.repositories.QuestionRepository;
import tn.esprit.pi.restcontrollers.RegistrationAnswer;
import tn.esprit.pi.restcontrollers.RegistrationQuestions;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnswerService {
    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private QuestionRepository questionRepository;

    public List<Answer> getAnswersByQuestionId(Long questionId) {
        return answerRepository.findByQuestionId(questionId);
    }

    public Answer saveAnswer(RegistrationAnswer request) {

        Question question = (Question) questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        Answer answer = new Answer(request);

        answer.setQuestion(question);

        return answerRepository.save(answer);
    }

}
