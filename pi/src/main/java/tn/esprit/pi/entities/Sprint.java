package tn.esprit.pi.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tn.esprit.pi.entities.enumerations.Status;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import tn.esprit.pi.user.User;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Sprint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idSprint;

    private String nom;
    private LocalDate dateDebut;
    private LocalDate dateFin;

    @Enumerated(EnumType.STRING)
    private Status statut;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "projet_id")
    private Projet projet;



    @ElementCollection
    @CollectionTable(name = "sprint_etudiants_affectes", joinColumns = @JoinColumn(name = "sprint_id"))
    @Column(name = "nom_etudiant")
    private List<String> etudiantsAffectes;

    @JsonIgnore
    @OneToMany(mappedBy = "sprint", cascade = CascadeType.ALL)
    private List<Tache> taches;

    @JsonIgnore
    @OneToMany(mappedBy = "sprint", cascade = CascadeType.ALL)
    private List<Commentaire> commentaires;  // Association avec les commentaires du sprint


    //association avec evaluation
    @JsonIgnore
    @OneToMany(mappedBy = "sprint")
    private List<Evaluation> evaluations;

    // ajout lien user
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
