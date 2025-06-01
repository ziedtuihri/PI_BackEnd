package tn.esprit.pi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class NoteDisplayDto {
    private Integer userId;
    private String userName;
    private Long sprintId;
    private String sprintNom;
    private double valeur;
    private Long projetId;
    private String projetNom;
 //   private double coef; // ✅ nouveau champ pour le coefficient




    // Getters et setters
}