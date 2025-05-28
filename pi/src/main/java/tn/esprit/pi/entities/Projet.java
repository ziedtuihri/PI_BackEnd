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
@ToString(exclude = {"sprints", "studentEmailsList", "teacherEmail"})
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "projet")
public class Projet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProjet;

    @Column(nullable = false)
    private String nom;

    @Column(length = 1000)
    private String description;

    private String filePath;

    @Column(nullable = false)
    private LocalDate dateDebut;

    @Column(nullable = false)
    private LocalDate dateFinPrevue;

    private LocalDate dateFinReelle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status statut;

    @Column(name = "teacher_email")
    private String teacherEmail;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "projet_student_emails",
            joinColumns = @JoinColumn(name = "projet_id"))
    @Column(name = "student_email")
    private List<String> studentEmailsList;

    @JsonIgnore
    @OneToMany(mappedBy = "projet",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true)
    private List<Sprint> sprints;



}