package tn.esprit.pi.anwer.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi.anwer.entities.JobApplication;

import java.util.List;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findByOffer_Id(Long offerId);

    List<JobApplication> findByStudentId(Integer studentId);
}
