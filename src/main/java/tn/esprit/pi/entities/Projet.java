package tn.esprit.pi.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tn.esprit.pi.entities.enumerations.Status;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Projet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProjet;

    private String nom;
    private String description;

    private String filePath;
    private LocalDate dateDebut;
    private LocalDate dateFinPrevue;
    private LocalDate dateFinReelle;  // Ajout pour suivre la date réelle de fin du projet

    @Enumerated(EnumType.STRING)
    private Status statut;

    @ElementCollection
    @CollectionTable(name = "projet_etudiants", joinColumns = @JoinColumn(name = "projet_id"))
    @Column(name = "nom_etudiant")
    private List<String> listeEtudiants;

    @JsonIgnore
    @OneToMany(mappedBy = "projet", cascade = CascadeType.ALL , fetch = FetchType.LAZY)
    private List<Sprint> sprints;
}
