package tn.esprit.pi.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import tn.esprit.pi.restcontrollers.RegistrationAnswer;

@Entity
@Getter
@Setter
public class Answer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;
    private boolean correct;

    @ManyToOne
    @JoinColumn(name = "question_id")
    @JsonBackReference
    private Question question;

    public Answer() {

    }

    public Answer(RegistrationAnswer request) {
            this.content = request.getContent();
            this.correct = request.isCorrect();

    }
}
