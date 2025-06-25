package tn.esprit.pi.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import tn.esprit.pi.entities.enumerations.TaskStatus;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TacheCreationDTO {

    private String nom;
    private String description;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private TaskStatus statut;

    private Long sprintId; // Matches what Angular sends: a direct ID

    private Integer storyPoints; // Added
    private Double estimatedHours; // Added

    // THIS IS THE CRUCIAL LINE:
    private List<String> etudiantsAffectes;

    // No need for loggedHours or etudiantsAffectes here,
    // as they are handled by the backend logic on creation.
    // For updates, you might have a different DTO or handle partial updates.
}