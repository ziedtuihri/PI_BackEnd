package tn.esprit.pi.services;

import org.springframework.beans.factory.annotation.Autowired;
import tn.esprit.pi.entities.Question;
import tn.esprit.pi.entities.Quiz;
import tn.esprit.pi.repositories.OfferRepository;
import tn.esprit.pi.repositories.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pi.repositories.QuizRepository;
import tn.esprit.pi.restcontrollers.RegistrationQuestions;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuizRepository quizRepository;

    public List<Question> getQuestionsByQuizId(Long quizId) {
        return questionRepository.findByQuizId(quizId);
    }

    public Question addQuestion(RegistrationQuestions request) {

        Quiz quiz = quizRepository.findById((long) request.getQuizId())
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        Question question = new Question(request);
        question.setQuiz(quiz);

        return questionRepository.save(question);
    }
}
