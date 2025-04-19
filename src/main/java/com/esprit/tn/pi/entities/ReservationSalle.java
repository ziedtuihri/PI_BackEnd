package com.esprit.tn.pi.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationSalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "idReunion", nullable = false)
    private Reunion reunion;

    @ManyToOne
    @JoinColumn(name = "idSalle", nullable = false)
    private Salle salle;
}
