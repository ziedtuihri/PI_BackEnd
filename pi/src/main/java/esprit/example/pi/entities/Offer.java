package esprit.example.pi.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
public class Offer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    private String title;
    private String description;
    private String location;
    private String company;
    private String type; // e.g., "Stage", "CDI", "CDD", etc.

    private LocalDate startDate;
    private LocalDate endDate;

    private Long companyId;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    @OneToMany(mappedBy = "offer", cascade = CascadeType.ALL)
    private List<JobApplication> applications;
}
