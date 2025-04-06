package esprit.example.pi.services;

import esprit.example.pi.entities.Quiz;
import esprit.example.pi.repositories.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class QuizService {
    private final QuizRepository quizRepository;

    public Quiz createQuiz(Quiz quiz) {
        return quizRepository.save(quiz);
    }

    public Optional<Quiz> getQuizById(Long id) {
        return quizRepository.findById(id);
    }
}
