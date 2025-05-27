package tn.esprit.pi.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tn.esprit.pi.entities.enumerations.Status;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import tn.esprit.pi.user.User;

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

        //ajout lien user
        @ManyToOne
        @JoinColumn(name = "user_id")
        private User user;
}

