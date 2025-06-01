package com.internship.platform.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Participation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime dateParticipation = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private StatutParticipation statut = StatutParticipation.VALIDE;

    @ManyToOne
    @JoinColumn(name = "evenement_id")
    private Evenement evenement;

    // Temporary placeholder before having a Student entity
    private String studentEmail;
}
