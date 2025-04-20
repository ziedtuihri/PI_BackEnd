package esprit.example.pi.dto;

import esprit.example.pi.entities.enumerations.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSprintDto {
    private String nom;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Status statut;
    private String description;
    private Long projetId;
}
