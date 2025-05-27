
package tn.esprit.pi.dto;

import tn.esprit.pi.entities.Sprint;
import tn.esprit.pi.entities.Tache;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SprintWithTasksDTO {

    private Long idSprint;
    private String nom;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String statut;

    private List<Tache> taches;


}
