package tn.esprit.pi.anwer.services;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.spring6.SpringTemplateEngine;
import tn.esprit.pi.anwer.email.EmailSenderService;
import tn.esprit.pi.anwer.entities.JobApplication;
import tn.esprit.pi.anwer.entities.Offer;
import tn.esprit.pi.anwer.repositories.JobApplicationRepository;
import tn.esprit.pi.anwer.repositories.OfferRepository;
import tn.esprit.pi.security.JwtService;
import tn.esprit.pi.user.User;
import tn.esprit.pi.user.UserRepository;

import org.thymeleaf.context.Context;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JobApplicationService {
    private final JobApplicationRepository jobApplicationRepository;
    private final OfferRepository offerRepository;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final SpringTemplateEngine templateEngine;

    public List<JobApplication> getAllApplications() {
        return jobApplicationRepository.findAll();
    }

    public Optional<JobApplication> getApplicationById(Long id) {
        return jobApplicationRepository.findById(id);
    }

    public List<JobApplication> getApplicationsByOfferId(Long offerId) {
        return jobApplicationRepository.findByOffer_Id(offerId);
    }

    public List<JobApplication> getApplicationsByStudentEmail(String email) {
        Optional<User> studentOpt = userRepository.findByEmail(email);
        if (studentOpt.isEmpty()) {
            throw new RuntimeException("Student not found with email: " + email);
        }
        Integer studentId = studentOpt.get().getId();
        return jobApplicationRepository.findByStudentId(studentId);
    }

    public JobApplication apply(JobApplication application, HttpServletRequest request) {
        String token = extractToken(request);
        String email = jwtService.extractUsername(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found for email: " + email));

        if (application.getOffer() == null || application.getOffer().getId() == null) {
            throw new IllegalArgumentException("Offer must be provided with a valid ID.");
        }

        Offer offer = offerRepository.findById(application.getOffer().getId())
                .orElseThrow(() -> new RuntimeException("Offer not found"));

        application.setStudentId(user.getId().longValue());
        application.setOffer(offer);
        application.setAppliedAt(LocalDate.now());

        return jobApplicationRepository.save(application);
    }

    public JobApplication updateQuizScore(Long applicationId, Double score) {
        JobApplication app = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        app.setQuizScore(score);
        return jobApplicationRepository.save(app);
    }

    public JobApplication save(JobApplication application) {
        return jobApplicationRepository.save(application);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or malformed Authorization header");
        }
        return header.substring(7);
    }


    private final RestTemplate restTemplate = new RestTemplate();
    private final String WHEREBY_API_URL = "https://api.whereby.dev/v1/meetings";
    private final String WHEREBY_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmFwcGVhci5pbiIsImF1ZCI6Imh0dHBzOi8vYXBpLmFwcGVhci5pbi92MSIsImV4cCI6OTAwNzE5OTI1NDc0MDk5MSwiaWF0IjoxNzQ3NTkwMTk3LCJvcmdhbml6YXRpb25JZCI6MzE2MTg2LCJqdGkiOiIwYmQxNTQwMi1mYmZlLTRiOWYtODc2Yi0zODUxNjIxZGNlOGQifQ.M0rMlcs1Mrky4gEYy1tWbflZR8dE6d-NLHT7OcVFEgM"; // TODO: Replace with your real API key
    private final EmailSenderService emailSenderService;

    public JobApplication scheduleInterview(Long applicationId, String startISO, String endISO) {
        JobApplication app = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        User student = userRepository.findById(app.getStudentId().intValue())
                .orElseThrow(() -> new RuntimeException("Student not found"));

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + WHEREBY_API_KEY);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("endDate", endISO);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    WHEREBY_API_URL,
                    new HttpEntity<>(requestBody, headers),
                    Map.class
            );

            String roomUrl = (String) response.getBody().get("roomUrl");

            app.setMeetingLink(roomUrl);
            app.setStatus("INTERVIEW");
            jobApplicationRepository.save(app);

            // Prepare email content
            Context context = new Context();
            context.setVariable("username", student.getFirstName() + " " + student.getLastName());
            context.setVariable("meetingLink", roomUrl);
            context.setVariable("startTime", startISO);
            context.setVariable("endTime", endISO);

            String htmlContent = templateEngine.process("interview-invite", context);

            // Send email
            emailSenderService.sendSimpleEmail(
                    student.getEmail(),
                    "Interview Invitation - PI Management",
                    htmlContent
            );

            return app;
        } catch (Exception e) {
            throw new RuntimeException("Failed to schedule interview: " + e.getMessage(), e);
        }
    }

    public JobApplication updateApplicationStatus(Long applicationId, String status) {
        JobApplication app = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        app.setStatus(status);
        return jobApplicationRepository.save(app);
    }

}
