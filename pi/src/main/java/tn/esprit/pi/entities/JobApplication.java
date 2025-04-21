package tn.esprit.pi.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
public class JobApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long studentId;

    @ManyToOne
    @JoinColumn(name = "offer_offer_id")
    private Offer offer;

    private String cvPath;
    private String coverLetterPath;
    private String certificatesPath;

    private String linkedinUrl;
    private String portfolioUrl;

    @ElementCollection
    private List<String> certificateUrls; // optional multiple certificate links (URLs)

    private LocalDate appliedAt;
}
