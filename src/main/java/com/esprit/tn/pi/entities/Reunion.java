package com.esprit.tn.pi.entities;

import com.esprit.tn.pi.entities.enumeration.TypeReunion;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Reunion {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private String titre;
        private String date;
        private String heure;
        private String duree;
        private String lienZoom;
        private String description;

        @Enumerated(EnumType.STRING)
        private TypeReunion type;

        @ManyToOne
        @JoinColumn(name = "salle_id")
        @JsonBackReference
        private Salle salle;


        @ManyToOne
        @JoinColumn(name = "createur_id")
        private User createur;

        @ManyToMany
        @JoinTable(
                name = "reunion_participants",
                joinColumns = @JoinColumn(name = "reunion_id"),
                inverseJoinColumns = @JoinColumn(name = "participant_id")
        )
        private Set<Participant> participants;
}
