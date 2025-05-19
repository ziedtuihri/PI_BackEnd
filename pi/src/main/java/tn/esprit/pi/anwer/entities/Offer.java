package tn.esprit.pi.anwer.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String location;
    private String company;
    private String type; // CDI, CDD, etc.
    private LocalDate startDate;
    private LocalDate endDate;

    private Long companyId;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "quiz_id")
    @JsonManagedReference
    private Quiz quiz;

    @OneToMany(mappedBy = "offer", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<JobApplication> applications;
}