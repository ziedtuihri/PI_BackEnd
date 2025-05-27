package tn.esprit.pi.dto;

import lombok.Data;

@Data
public class NoteRequestDto {
    private Long evaluationId;
    //private Long tacheId;
    private  Long sprintId;
    private Integer userId;
    private double valeur;

}
