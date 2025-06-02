package tn.esprit.pi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tn.esprit.pi.entities.enumerations.SprintStatus;
import tn.esprit.pi.entities.enumerations.TaskStatus; // <--- ADD THIS IMPORT
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SprintWithTasksDTO {
    private Long sprintId;
    private String nom;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private SprintStatus statut;
    private boolean isUrgent;
    private LocalDate deadlineNotificationDate;
    private Long projetId;
    private String projetNom;
    private List<String> etudiantsAffectes;
    private List<TaskDTO> tasks;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskDTO {
        private Long taskId;
        private String nom;
        private TaskStatus statut; // <--- CHANGE THIS FROM Object to TaskStatus
    }
}