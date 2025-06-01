package com.internship.platform.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Offre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;
    private String description;
    private String competences;
    private String localisation;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private boolean disponible = true;

    @ManyToOne
    @JoinColumn(name = "entreprise_id")
    @JsonIgnoreProperties("offres")
    private Entreprise entreprise;

    @OneToMany(mappedBy = "offre", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("offre")
    private List<Candidature> candidatures;

}
