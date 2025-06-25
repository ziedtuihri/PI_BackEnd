package tn.esprit.pi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NoteDisplayDto {
    private Integer userId;
    private String userFullName;
    private Long sprintId;
    private String sprintNom;
    private double valeur;
    private Long projectId;
    private String projectNom;
}