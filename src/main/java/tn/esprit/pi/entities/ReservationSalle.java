package tn.esprit.pi.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
    @JoinColumn(name = "idReunion")
    private Reunion reunion;

    @ManyToOne
    @JoinColumn(name = "idSalle")
    @JsonBackReference
    private Salle salle;

    private String date;
    private String heure;
    private String duree;
}
