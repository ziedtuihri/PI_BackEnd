package com.internship.platform.entities;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class Convention {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;
    private LocalDate dateDebut;
    private LocalDate dateFin;

    @lombok.Setter
    @lombok.Getter
    @ManyToOne
    @JoinColumn(name = "entreprise_id", referencedColumnName = "id") // Ensure correct column reference
    private Entreprise entreprise;

    // Getter methods
    public Long getId() {
        return id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    // Method to get Entreprise name
    public String getEntrepriseNom() {
        return this.entreprise != null ? this.entreprise.getNom() : null;
    }
}
