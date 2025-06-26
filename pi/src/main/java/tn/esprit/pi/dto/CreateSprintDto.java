package tn.esprit.pi.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class CreateSprintDto {
    private String nom;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Long projetId; // To link to the parent project
    private boolean isUrgent;
    private LocalDate deadlineNotificationDate;
    private List<String> etudiantsAffectes; // For initial assignment
}