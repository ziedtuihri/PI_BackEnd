package com.internship.platform.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Candidature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime dateCandidature = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private StatutCandidature statut = StatutCandidature.EN_ATTENTE;

    @ManyToOne
    @JoinColumn(name = "offre_id")
    private Offre offre;

    // Add a Student field later when you have Student entity
    private String studentEmail; // Temporary placeholder
}
