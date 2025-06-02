// Dans tn.esprit.pi.entities/Sprint.java (VERSION FINALE AVEC EVALUATION ET SANS COMMENTAIRE/USER)
package tn.esprit.pi.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import tn.esprit.pi.entities.enumerations.SprintStatus; // Assurez-vous d'avoir cette énumération correcte
import tn.esprit.pi.user.User;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
// IMPORTANT : J'ai ajouté "evaluations" dans l'exclude pour ToString pour éviter les boucles infinies.
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
    private SprintStatus statut; // Utilise SprintStatus

    private boolean isUrgent;
    private LocalDate deadlineNotificationDate;
    private boolean completed; // Assuming you have this already
    private boolean urgent; // <--- ADD THIS LINE


    @ManyToOne
    @JoinColumn(name = "projet_id")
    private Projet projet;

    @ElementCollection(fetch = FetchType.EAGER) // Conservez EAGER si la liste d'étudiants est petite
    @CollectionTable(name = "sprint_etudiants_affectes", joinColumns = @JoinColumn(name = "sprint_id"))
    @Column(name = "email_etudiant") // Renommé en email_etudiant pour plus de clarté
    List<String> etudiantsAffectes;

    @JsonIgnore // Important pour éviter les boucles de sérialisation JSON
    @OneToMany(mappedBy = "sprint", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Tache> taches;

    //association avec evaluation
    @JsonIgnore
    @OneToMany(mappedBy = "sprint")
    private List<Evaluation> evaluations;
    private boolean deadlineNotificationSent = false; // Add this field


    // Les champs "Commentaire" et "User" ont été définitivement retirés d'ici.

    // ajout lien user
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}