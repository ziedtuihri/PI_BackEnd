package tn.esprit.pi.repositories;

import tn.esprit.pi.entities.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findByOffer_Id(Long offerId);
    List<JobApplication> findByStudentId(Long studentId);
}