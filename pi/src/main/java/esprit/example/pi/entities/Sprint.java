package esprit.example.pi.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import esprit.example.pi.entities.enumerations.Status;
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

    @JsonIgnore
    @OneToMany(mappedBy = "sprint", cascade = CascadeType.ALL)
    private List<Tache> taches;

    @JsonIgnore
    @OneToMany(mappedBy = "sprint", cascade = CascadeType.ALL)
    private List<Commentaire> commentaires;  // Association avec les commentaires du sprint
}
