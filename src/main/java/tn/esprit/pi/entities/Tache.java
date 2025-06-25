package tn.esprit.pi.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import tn.esprit.pi.entities.enumerations.TaskStatus;
import tn.esprit.pi.user.User;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter @Setter @AllArgsConstructor @NoArgsConstructor @ToString(exclude = {"projet", "sprint", "assignedTo"})
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
        private TaskStatus statut; // <--- This is an ENUM


        private Integer storyPoints; // <--- Optional, not in Angular form
        private Double estimatedHours; // <--- Optional, not in Angular form
        private Double loggedHours = 0.0; // <--- Optional, not in Angular form

        @JsonIgnore
        @ManyToOne
        @JoinColumn(name = "projet_id")
        private Projet projet;

        @JsonIgnore
        @ManyToOne
        @JoinColumn(name = "sprint_id")
        private Sprint sprint; // <--- Expects a Sprint object, not just an ID

        @ElementCollection(fetch = FetchType.EAGER)
        @CollectionTable(name = "tache_etudiants_affectes", joinColumns = @JoinColumn(name = "tache_id"))
        @Column(name = "email_etudiant")
        private List<String> etudiantsAffectes;

        @ManyToOne
        @JoinColumn(name = "assigned_user_id")
        private User assignedTo; // <--- Optional, not in Angular form
}