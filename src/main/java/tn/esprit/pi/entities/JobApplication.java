package tn.esprit.pi.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long studentId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "offer_offer_id")
    @JsonIgnoreProperties("applications")
    private Offer offer;

    private String cvPath;
    private String coverLetterPath;
    private String certificatesPath;

    private String linkedinUrl;
    private String portfolioUrl;

    @ElementCollection
    private List<String> certificateUrls;

    private LocalDate appliedAt;
    private Double quizScore;

    @Column(nullable = false)
    private String status = "NEW"; // Possible: NEW, APPROVED, INTERVIEW, REJECTED

    private String meetingLink;
}