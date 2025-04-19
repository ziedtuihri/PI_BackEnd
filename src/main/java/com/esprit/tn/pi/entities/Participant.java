package com.esprit.tn.pi.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nom;
    @Column(unique = true)
    private String email;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;  // Relation avec l'entité User
    @ManyToMany(mappedBy = "participants")
    @JsonIgnore
    private List<Reunion> reunions;
}
