package tn.esprit.pi.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import tn.esprit.pi.restcontrollers.RegistrationQuiz;

import java.util.ArrayList;
import java.util.List;


@Entity
@Getter
@Setter
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String description;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Question> questions;

    @OneToOne(mappedBy = "quiz")
    @JsonBackReference
    private Offer offer;

    public Quiz(RegistrationQuiz request) {
        this.description = request.getDescription();
        this.title = request.getTitle();
        //this.questions = request.getQuestions();

    }

    public Quiz() {

    }
}
