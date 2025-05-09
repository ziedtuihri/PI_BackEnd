package com.esprit.tn.pi.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Salle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nom;
    private int capacite;
    private boolean disponible;
    private String etat;

    @OneToMany(mappedBy = "salle") // Assurez-vous que la relation avec ReservationSalle existe
    @JsonBackReference
    private List<ReservationSalle> reservations;

    @ManyToOne // Supposons que chaque salle peut être associée à une réunion
    @JoinColumn(name = "reunion_id") // La colonne de clé étrangère pour la réunion
    private Reunion reunion;
}