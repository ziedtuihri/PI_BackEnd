package tn.esprit.pi.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {


    @Value("${file.upload-dir}")
    private String uploadDir;

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    public String storeFile(MultipartFile file, String subDir) throws IOException {
        Path dirPath = Paths.get(uploadDir + "/" + subDir).toAbsolutePath().normalize();
        Files.createDirectories(dirPath);

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = dirPath.resolve(fileName);

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Store only the relative path instead of the full path
        return subDir + "/" + fileName;
    }

    public Resource loadFileAsResource(String relativePath) throws IOException {
        try {
            Path filePath = Paths.get(uploadDir).resolve(relativePath).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                logger.error("File not found: {}", relativePath);
                throw new FileNotFoundException("File not found: " + relativePath);
            }
        } catch (MalformedURLException ex) {
            logger.error("File path malformed: {}", relativePath, ex);
            throw new FileNotFoundException("File not found: " + relativePath);
        }
    }
}
