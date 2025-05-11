package com.esprit.tn.pi.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

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
    @Getter
    private boolean disponible;
    private String etat;

    @OneToMany(mappedBy = "salle")
    @JsonBackReference
    private List<ReservationSalle> reservations;

    @ManyToOne
    @JoinColumn(name = "reunion_id")
    @JsonManagedReference

    private Reunion reunion;

}