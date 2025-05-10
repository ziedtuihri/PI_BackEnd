package tn.esprit.pi.restcontrollers;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import tn.esprit.pi.entities.JobApplication;
import tn.esprit.pi.entities.Quiz;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
public class RegistrationOffer {
    private String title;


    private String description;
    private String location;
    private String company;
    private String type; // e.g., "Stage", "CDI", "CDD", etc.

    private LocalDate startDate;
    private LocalDate endDate;

    private Long companyId;
}
