package tn.esprit.pi.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import tn.esprit.pi.entities.enumerations.SprintStatus;
import tn.esprit.pi.user.User;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"projet", "taches", "evaluations"})
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Sprint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long idSprint;

    String nom;

    LocalDate dateDebut;
    LocalDate dateFin;

    @Enumerated(EnumType.STRING)
    private SprintStatus statut;

    // This is the correct and only field you need for "urgent" status

    @Column(name = "is_urgent", nullable = false, columnDefinition = "boolean default false")

    private boolean isUrgent;

    private LocalDate deadlineNotificationDate;
    private boolean completed;
    // Removed the duplicate field: private boolean urgent;

    @ManyToOne
    @JoinColumn(name = "projet_id")
    private Projet projet;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "sprint_etudiants_affectes", joinColumns = @JoinColumn(name = "sprint_id"))
    @Column(name = "email_etudiant")
    List<String> etudiantsAffectes;

    @JsonIgnore
    @OneToMany(mappedBy = "sprint", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Tache> taches;

    @JsonIgnore
    @OneToMany(mappedBy = "sprint")
    private List<Evaluation> evaluations;

    private boolean deadlineNotificationSent = false;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}