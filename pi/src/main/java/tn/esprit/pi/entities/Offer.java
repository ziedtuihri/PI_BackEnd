package tn.esprit.pi.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Offer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    private String title;

    @Lob
    @Size(max = 10000, message = "Description is too long.")
    private String description;
    private String location;
    private String company;
    private String type; // e.g., "Stage", "CDI", "CDD", etc.

    private LocalDate startDate;
    private LocalDate endDate;

    private Long companyId;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "quiz_id")
    @JsonManagedReference
    private Quiz quiz;

    @OneToMany(mappedBy = "offer", cascade = CascadeType.ALL)
    @JsonBackReference
    private List<JobApplication> applications;
}
