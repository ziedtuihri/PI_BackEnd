package tn.esprit.pi.anwer.services;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.UUID;

@Slf4j
@Service("jobApplicationFileStorageService")
public class FileStorageService {

    @Value("${jobapp.upload-dir}")
    private String jobAppUploadDir;

    private Path rootPath;

    @PostConstruct
    public void init() {
        this.rootPath = Paths.get(jobAppUploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.rootPath);
            log.info("JobApplication upload root initialized at {}", this.rootPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize job application upload directory", e);
        }
    }

    public String storeFile(MultipartFile file, String subDir) throws IOException {
        Path dirPath = rootPath.resolve(subDir).normalize();
        Files.createDirectories(dirPath);

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = dirPath.resolve(fileName);

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // ✅ Only return the path relative to rootPath — no extra 'uploads/'
        return rootPath.relativize(filePath).toString().replace('\\', '/');
    }

    public Resource loadFileAsResource(String relativePath) throws IOException {
        Path filePath = rootPath.resolve(relativePath).normalize();
        Resource resource = new UrlResource(filePath.toUri());

        if (resource.exists() && resource.isReadable()) {
            return resource;
        } else {
            log.error("File not found or not readable: {}", filePath);
            throw new FileNotFoundException("File not found: " + filePath);
        }
    }

}
