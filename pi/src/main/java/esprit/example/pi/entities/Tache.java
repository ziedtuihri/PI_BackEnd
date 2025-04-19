package esprit.example.pi.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import esprit.example.pi.entities.enumerations.Status;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Tache {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long idTache;

        private String nom;
        private String description;
        private LocalDate dateDebut;
        private LocalDate dateFin;

        @Enumerated(EnumType.STRING)
        private Status statut;


        @JsonIgnore
        @ManyToOne
        @JoinColumn(name = "projet_id")
        private Projet projet;

        @JsonIgnore
        @ManyToOne
        @JoinColumn(name = "sprint_id")
        private Sprint sprint;
}
