package tn.esprit.pi.restcontrollers;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RegistrationAnswer {

    private String content;

    private boolean correct;

    private int questionId;

}
