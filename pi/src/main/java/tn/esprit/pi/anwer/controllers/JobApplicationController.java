package tn.esprit.pi.anwer.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.pi.anwer.entities.JobApplication;
import tn.esprit.pi.anwer.services.FileStorageService;
import tn.esprit.pi.anwer.services.JobApplicationService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class JobApplicationController {
    private final JobApplicationService jobApplicationService;

    @Autowired
    @Qualifier("jobApplicationFileStorageService")
    private FileStorageService fileStorageService;

    @GetMapping
    public ResponseEntity<List<JobApplication>> getAll() {
        return ResponseEntity.ok(jobApplicationService.getAllApplications());
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobApplication> getById(@PathVariable Long id) {
        return jobApplicationService.getApplicationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-offer/{offerId}")
    public ResponseEntity<List<JobApplication>> getByOffer(@PathVariable Long offerId) {
        return ResponseEntity.ok(jobApplicationService.getApplicationsByOfferId(offerId));
    }

    @GetMapping("/by-student-email/{email}")
    public ResponseEntity<List<JobApplication>> getByStudentEmail(@PathVariable String email) {
        return ResponseEntity.ok(jobApplicationService.getApplicationsByStudentEmail(email));
    }

    @PostMapping
    public ResponseEntity<JobApplication> apply(@RequestBody JobApplication application, HttpServletRequest request) {
        return ResponseEntity.ok(jobApplicationService.apply(application, request));
    }

    @PatchMapping("/{applicationId}/score")
    public ResponseEntity<JobApplication> updateScore(@PathVariable Long applicationId, @RequestBody Map<String, Object> body) {
        if (!body.containsKey("score")) {
            return ResponseEntity.badRequest().build();
        }
        Double score = Double.valueOf(body.get("score").toString());
        return ResponseEntity.ok(jobApplicationService.updateQuizScore(applicationId, score));
    }

    @PostMapping("/{applicationId}/upload")
    public ResponseEntity<String> uploadFiles(
            @PathVariable Long applicationId,
            @RequestParam(required = false) MultipartFile cv,
            @RequestParam(required = false) MultipartFile coverLetter,
            @RequestParam(required = false) MultipartFile certificates
    ) {
        try {
            JobApplication app = jobApplicationService.getApplicationById(applicationId)
                    .orElseThrow(() -> new RuntimeException("Application not found"));

            if (cv != null) app.setCvPath(fileStorageService.storeFile(cv, "cvs"));
            if (coverLetter != null) app.setCoverLetterPath(fileStorageService.storeFile(coverLetter, "letters"));
            if (certificates != null) app.setCertificatesPath(fileStorageService.storeFile(certificates, "certs"));

            jobApplicationService.save(app);
            return ResponseEntity.ok("Files uploaded successfully");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/{applicationId}/download/{fileType}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long applicationId, @PathVariable String fileType) {
        try {
            JobApplication app = jobApplicationService.getApplicationById(applicationId)
                    .orElseThrow(() -> new RuntimeException("Application not found"));

            String path = switch (fileType) {
                case "cv" -> app.getCvPath();
                case "coverletter" -> app.getCoverLetterPath();
                case "certificates" -> app.getCertificatesPath();
                default -> null;
            };

            if (path == null || path.isEmpty()) return ResponseEntity.notFound().build();

            Resource resource = fileStorageService.loadFileAsResource(path);
            String filename = path.substring(path.indexOf("_") + 1);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/debug-path/{fileType}/{filename}")
    public ResponseEntity<String> debugPath(@PathVariable String fileType, @PathVariable String filename) {
        Path root = Paths.get("anwer/job/uploads");
        Path fullPath = root.resolve(fileType).resolve(filename).toAbsolutePath();
        boolean exists = Files.exists(fullPath);
        return ResponseEntity.ok("Exists: " + exists + "\nPath: " + fullPath);
    }

    @PatchMapping("/{applicationId}/schedule-interview")
    public ResponseEntity<JobApplication> scheduleInterview(
            @PathVariable Long applicationId,
            @RequestBody Map<String, String> payload
    ) {
        String start = payload.get("start"); // ISO format string
        String end = payload.get("end");
        if (start == null || end == null) {
            return ResponseEntity.badRequest().body(null);
        }
        return ResponseEntity.ok(jobApplicationService.scheduleInterview(applicationId, start, end));
    }

    @PatchMapping("/{applicationId}/status")
    public ResponseEntity<JobApplication> updateStatus(
            @PathVariable Long applicationId,
            @RequestBody Map<String, String> body) {
        String status = body.get("status");
        return ResponseEntity.ok(jobApplicationService.updateApplicationStatus(applicationId, status));
    }
}
