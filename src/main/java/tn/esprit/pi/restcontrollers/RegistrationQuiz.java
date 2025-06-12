package tn.esprit.pi.restcontrollers;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import tn.esprit.pi.entities.Question;

import java.util.List;

@Getter
@Setter
@Builder
public class RegistrationQuiz {

    private String title;
    private String description;

    private Long offreId;

    //private List<Question> questions;
}
