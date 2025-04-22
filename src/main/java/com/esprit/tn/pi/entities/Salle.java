package com.esprit.tn.pi.entities;

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
    private boolean disponible;
    @OneToMany(mappedBy = "salle")
    @JsonManagedReference
    private List<ReservationSalle> reservations;

}
