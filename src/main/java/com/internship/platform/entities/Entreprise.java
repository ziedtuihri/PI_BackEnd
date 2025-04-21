package com.internship.platform.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Entreprise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String secteurActivite;
    private int taille;

    private String adresse;
    private String telephone;
    private String email;
    private String siteWeb;
    private String contactRH;

    @Enumerated(EnumType.STRING)
    private StatutEntreprise statut = StatutEntreprise.EN_ATTENTE;

    @OneToMany(mappedBy = "entreprise", cascade = CascadeType.ALL)
    private List<Offre> offres;

    @OneToMany(mappedBy = "entreprise", cascade = CascadeType.ALL)
    private List<Convention> conventions;
}
