package esprit.example.pi.entities;

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
public class Projet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long idProjet;
    private Long id;

    private String nom;

    private String description;

    private LocalDate dateDebut;

    private LocalDate dateFinPrevue;




    @Enumerated(EnumType.STRING)
    private Status statut;

    @OneToMany(mappedBy = "projet", cascade = CascadeType.ALL)
    List<Sprint> sprints;





}

