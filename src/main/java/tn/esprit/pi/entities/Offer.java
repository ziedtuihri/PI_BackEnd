package tn.esprit.pi.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import tn.esprit.pi.restcontrollers.RegistrationOffer;

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
    @JsonManagedReference
    private Quiz quiz;

    @OneToMany(mappedBy = "offer", cascade = CascadeType.ALL)
    @JsonBackReference
    private List<JobApplication> applications;

    // Default constructor
    public Offer() {
    }

    public Offer(RegistrationOffer request) {
        this.title = request.getTitle();
        this.description = request.getDescription();
        this.location = request.getLocation();
        this.company = request.getCompany();
        this.type = request.getType();
        this.startDate = request.getStartDate();
        this.endDate = request.getEndDate();
        this.companyId = request.getCompanyId();
    }
}
