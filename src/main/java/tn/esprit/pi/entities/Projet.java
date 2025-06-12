package tn.esprit.pi.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tn.esprit.pi.entities.enumerations.ProjectStatus;
import tn.esprit.pi.entities.enumerations.ProjectType; // Importez votre enum ProjectType

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"sprints", "studentEmailsList", "teacherEmail"})
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "projet")
public class Projet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProjet;

    @NotBlank(message = "Le nom du projet est obligatoire et ne peut pas être vide.")
    @Column(nullable = false)
    private String nom;

    @NotNull(message = "Le type de projet est obligatoire.")
    @Enumerated(EnumType.STRING)
    @Column(name = "project_type", nullable = false)
    private ProjectType projectType;

    // MODIFICATION ICI : AJOUT DE @Lob pour les longs textes/paragraphes
    // et suppression de 'length = 1000' car @Lob gère la taille pour les CLOB/TEXT
    @Lob // Indique que ce champ doit être mappé à un type de données large (TEXT/CLOB)
    private String description;

    private String filePath;

    @NotNull(message = "La date de début du projet est obligatoire.")
    @Column(nullable = false)
    private LocalDate dateDebut;

    @NotNull(message = "La date de fin prévue est obligatoire.")
    @Column(nullable = false)
    private LocalDate dateFinPrevue;

    private LocalDate dateFinReelle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectStatus statut;

    @Column(name = "teacher_email")
    private String teacherEmail;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "projet_student_emails",
            joinColumns = @JoinColumn(name = "projet_id"))
    @Column(name = "student_email")
    private List<String> studentEmailsList;

    @JsonIgnore
    @OneToMany(mappedBy = "projet", cascade = CascadeType.ALL , fetch = FetchType.LAZY)
    private List<Sprint> sprints;
}