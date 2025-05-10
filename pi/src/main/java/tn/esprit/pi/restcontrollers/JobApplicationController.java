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
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class JobApplicationController {
    private final JobApplicationService jobApplicationService;
    private final FileStorageService fileStorageService;

    @GetMapping
    public ResponseEntity<List<JobApplication>> getAllJobApplications(){
        return ResponseEntity.ok(jobApplicationService.getAllApplications());
    }

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

    @GetMapping("/{id}")
    public ResponseEntity<Optional<JobApplication>> getApplicationById(@PathVariable Long id){
        return ResponseEntity.ok(jobApplicationService.getApplicationById(id));
    }

    @PatchMapping("/{applicationId}/score")
    public ResponseEntity<JobApplication> updateScore(
            @PathVariable Long applicationId,
            @RequestBody Map<String, Object> scoreUpdate) {

        try {
            JobApplication application = jobApplicationService.getApplicationById(applicationId)
                    .orElseThrow(() -> new RuntimeException("Application not found"));

            if (scoreUpdate.containsKey("score")) {
                Long score = Long.valueOf(scoreUpdate.get("score").toString());
                application.setQuizScore(score);

                JobApplication savedApplication = jobApplicationService.apply(application);
                return ResponseEntity.ok(savedApplication);
            } else {
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Upload files: CV, cover letter, certificates
   /* @PostMapping("/{applicationId}/upload")
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
    }*/

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

            System.out.println("Before update - CV path: " + application.getCvPath());

            if (cv != null) {
                String cvPath = fileStorageService.storeFile(cv, "cvs");
                application.setCvPath(cvPath);
                System.out.println("After setting CV path: " + application.getCvPath());
            }

            if (coverLetter != null) {
                String coverLetterPath = fileStorageService.storeFile(coverLetter, "letters");
                application.setCoverLetterPath(coverLetterPath);
                System.out.println("After setting cover letter path: " + application.getCoverLetterPath());
            }

            if (certificates != null) {
                String certificatesPath = fileStorageService.storeFile(certificates, "certs");
                application.setCertificatesPath(certificatesPath);
                System.out.println("After setting certificates path: " + application.getCertificatesPath());
            }

            JobApplication savedApplication = jobApplicationService.apply(application);
            System.out.println("After saving - CV path: " + savedApplication.getCvPath());

            return ResponseEntity.ok("Files uploaded successfully");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    // Modified download endpoint for better security
    @GetMapping("/{applicationId}/download/{fileType}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Long applicationId,
            @PathVariable String fileType) {
        try {
            JobApplication application = jobApplicationService.getApplicationById(applicationId)
                    .orElseThrow(() -> new RuntimeException("Application not found"));

            String relativePath;
            String filename;

            switch (fileType) {
                case "cv":
                    relativePath = application.getCvPath();
                    filename = "CV.pdf"; // Default name, can be improved
                    break;
                case "coverletter":
                    relativePath = application.getCoverLetterPath();
                    filename = "CoverLetter.pdf"; // Default name
                    break;
                case "certificates":
                    relativePath = application.getCertificatesPath();
                    filename = "Certificates.pdf"; // Default name
                    break;
                default:
                    return ResponseEntity.badRequest().build();
            }

            if (relativePath == null || relativePath.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = fileStorageService.loadFileAsResource(relativePath);

            // Extract original filename if present in the path
            if (relativePath.contains("_")) {
                String originalFilename = relativePath.substring(relativePath.indexOf("_") + 1);
                if (!originalFilename.isEmpty()) {
                    filename = originalFilename;
                }
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // This method can be kept for backward compatibility but should be deprecated
    @GetMapping("/download")
    @Deprecated
    public ResponseEntity<Resource> legacyDownloadFile(@RequestParam String path) throws IOException {
        // Log usage of deprecated endpoint
        return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
                .header(HttpHeaders.LOCATION, "/api/applications/download-by-path")
                .build();
    }

}
