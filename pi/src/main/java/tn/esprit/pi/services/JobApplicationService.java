package tn.esprit.pi.services;

import tn.esprit.pi.entities.JobApplication;
import tn.esprit.pi.repositories.JobApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JobApplicationService {
    private final JobApplicationRepository jobApplicationRepository;

    public List<JobApplication> getAllApplications(){
        return jobApplicationRepository.findAll();
    }

    public Optional<JobApplication> getApplicationById(Long offerId){
        return jobApplicationRepository.findById(offerId);
    }

    public List<JobApplication> getApplicationsByOfferId(Long offerId) {
        return jobApplicationRepository.findByOffer_Id(offerId);
    }

    public List<JobApplication> getApplicationsByStudentId(Long studentId) {
        return jobApplicationRepository.findByStudentId(studentId);
    }

    public JobApplication apply(JobApplication jobApplication) {
        jobApplication.setAppliedAt(LocalDate.now());
        return jobApplicationRepository.save(jobApplication);
    }
}
