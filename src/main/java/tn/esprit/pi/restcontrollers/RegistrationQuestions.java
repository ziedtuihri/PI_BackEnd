package tn.esprit.pi.restcontrollers;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RegistrationQuestions {

    private String content;

    private int quizId;


}
