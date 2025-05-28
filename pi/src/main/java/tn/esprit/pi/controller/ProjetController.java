package tn.esprit.pi.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.pi.dto.EmailDTO;
import tn.esprit.pi.entities.Projet;
import tn.esprit.pi.services.IProjetService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/projets")
public class ProjetController {

    private final IProjetService projetService;

    public ProjetController(IProjetService projetService) {
        this.projetService = projetService;
    }

    // Créer un projet
    @PostMapping
    public ResponseEntity<Projet> createProjet(@RequestBody Projet projet) {
        Projet created = projetService.saveProjet(projet);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // Récupérer un projet par id
    @GetMapping("/{id}")
    public ResponseEntity<Projet> getProjetById(@PathVariable Long id) {
        Projet projet = projetService.getProjetById(id);
        return ResponseEntity.of(Optional.ofNullable(projet));
    }

    // Récupérer tous les projets
    @GetMapping
    public ResponseEntity<List<Projet>> getAllProjets() {
        return ResponseEntity.ok(projetService.getAllProjets());
    }

    // Mettre à jour un projet
    @PutMapping("/{id}")
    public ResponseEntity<Projet> updateProjet(@PathVariable Long id, @RequestBody Projet projet) {
        Projet updated = projetService.updateProjet(id, projet);
        return ResponseEntity.of(Optional.ofNullable(updated));
    }

    // Supprimer un projet
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProjet(@PathVariable Long id) {
        projetService.deleteProjet(id);
        return ResponseEntity.noContent().build();
    }

    // Upload fichier
    @PostMapping(value = "/{id}/upload", consumes = "multipart/form-data")
    public ResponseEntity<Map<String, String>> uploadFile(@PathVariable Long id, @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            projetService.uploadFile(id, file);
            return ResponseEntity.ok(Map.of("message", "Fichier téléchargé avec succès !"));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Erreur d'E/S : " + e.getMessage()));
        } catch (RuntimeException e) {
            HttpStatus status = e.getMessage() != null && e.getMessage().contains("Projet non trouvé")
                    ? HttpStatus.NOT_FOUND : HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(Map.of("error", e.getMessage()));
        }
    }

    // Télécharger fichier
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long id) {
        try {
            byte[] data = projetService.downloadFile(id);
            Projet projet = projetService.getProjetById(id);
            if (projet == null || projet.getFilePath() == null)
                return ResponseEntity.notFound().build();

            Path path = Paths.get(projet.getFilePath());
            String fileName = path.getFileName().toString();
            String mimeType = Files.probeContentType(path);

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                    .contentType(org.springframework.http.MediaType.parseMediaType(Optional.ofNullable(mimeType).orElse("application/octet-stream")))
                    .body(data);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // --- Gestion des emails via EmailDTO ---

    @PostMapping("/{projetId}/add-student-email")
    public ResponseEntity<Projet> addStudentEmailToProjet(
            @PathVariable Long projetId,
            @Valid @RequestBody EmailDTO emailDTO) {
        try {
            Projet projet = projetService.addStudentEmailToProjet(projetId, emailDTO.getEmail());
            return ResponseEntity.ok(projet);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{projetId}/remove-student-email")
    public ResponseEntity<Projet> removeStudentEmailFromProjet(
            @PathVariable Long projetId,
            @Valid @RequestBody EmailDTO emailDTO) {
        try {
            Projet projet = projetService.removeStudentEmailFromProjet(projetId, emailDTO.getEmail());
            return ResponseEntity.ok(projet);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PutMapping("/{projetId}/assign-teacher-email")
    public ResponseEntity<Projet> assignTeacherEmailToProjet(
            @PathVariable Long projetId,
            @Valid @RequestBody EmailDTO emailDTO) {
        try {
            Projet projet = projetService.assignTeacherEmailToProjet(projetId, emailDTO.getEmail());
            return ResponseEntity.ok(projet);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Optionnel : récupérer la liste des emails étudiants
    @GetMapping("/{projetId}/student-emails")
    public ResponseEntity<List<String>> getStudentEmailsForProjet(@PathVariable Long projetId) {
        try {
            List<String> emails = projetService.getStudentEmailsForProjet(projetId);
            return ResponseEntity.ok(emails);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Optionnel : remplacer la liste des emails étudiants
    @PostMapping("/{projetId}/set-student-emails")
    public ResponseEntity<Projet> setStudentEmailsToProjet(
            @PathVariable Long projetId,
            @RequestBody List<String> studentEmails) {
        try {
            Projet projet = projetService.setStudentEmailsToProjet(projetId, studentEmails);
            return ResponseEntity.ok(projet);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

}
