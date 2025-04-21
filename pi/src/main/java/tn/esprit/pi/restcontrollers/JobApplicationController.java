package tn.esprit.pi.restcontrollers;

import tn.esprit.pi.entities.JobApplication;
import tn.esprit.pi.services.FileStorageService;
import tn.esprit.pi.services.JobApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class JobApplicationController {
    private final JobApplicationService jobApplicationService;
    private final FileStorageService fileStorageService;

    @PostMapping
    public ResponseEntity<JobApplication> apply(@RequestBody JobApplication application) {
        return ResponseEntity.ok(jobApplicationService.apply(application));
    }

    @GetMapping("/by-offer/{offerId}")
    public ResponseEntity<List<JobApplication>> getByOffer(@PathVariable Long offerId) {
        return ResponseEntity.ok(jobApplicationService.getApplicationsByOfferId(offerId));
    }

    @GetMapping("/by-student/{studentId}")
    public ResponseEntity<List<JobApplication>> getByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(jobApplicationService.getApplicationsByStudentId(studentId));
    }

    // Upload files: CV, cover letter, certificates
    @PostMapping("/{applicationId}/upload")
    public ResponseEntity<String> uploadFiles(
            @PathVariable Long applicationId,
            @RequestParam(required = false) MultipartFile cv,
            @RequestParam(required = false) MultipartFile coverLetter,
            @RequestParam(required = false) MultipartFile certificates
    ) {
        try {
            JobApplication application = jobApplicationService.getApplicationById(applicationId)
                    .orElseThrow(() -> new RuntimeException("Application not found"));

            if (cv != null) {
                String cvPath = fileStorageService.storeFile(cv, "cvs");
                application.setCvPath(cvPath);
            }

            if (coverLetter != null) {
                String coverLetterPath = fileStorageService.storeFile(coverLetter, "letters");
                application.setCoverLetterPath(coverLetterPath);
            }

            if (certificates != null) {
                String certificatesPath = fileStorageService.storeFile(certificates, "certs");
                application.setCertificatesPath(certificatesPath);
            }

            jobApplicationService.apply(application); // re-save updated paths
            return ResponseEntity.ok("Files uploaded successfully");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    // Optional: Download file
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam String path) throws IOException {
        Path filePath = Paths.get(path).toAbsolutePath();
        Resource resource = new UrlResource(filePath.toUri());

        if (resource.exists()) {
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filePath.getFileName() + "\"")
                    .body(resource);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}
