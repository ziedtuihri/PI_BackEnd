package tn.esprit.pi.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIQuizDTO {
    private String question;
    private List<String> options;
    private String correct;
}
